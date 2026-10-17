package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import java.util.concurrent.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

/** Source declaration proof stays independent from the supplied target field catalog. */
final class Forge1192BlockEntityConstantsRewriter {
    static final String SOURCE="net/minecraft/world/level/block/entity/BlockEntityType", DESC="L"+SOURCE+";";
    private static final Set<String> MEMBERS=Forge1192BuiltinBlockEntityCatalog.identifiers().keySet();
    static boolean declares(String owner,String name,String descriptor) {
        return SOURCE.equals(owner) && DESC.equals(descriptor) && MEMBERS.contains(name);
    }
    interface Evidence {
        String declaration(ClassNode active,String owner,String name,String descriptor);
        String canonical(String name);
        Map<String,Handle> targetCatalog();
    }
    private final Evidence evidence;
    private final ConcurrentMap<String,Optional<String>> names=new ConcurrentHashMap<>();
    private volatile Map<String,Handle> target;
    Forge1192BlockEntityConstantsRewriter(Evidence evidence){this.evidence=evidence;}
    private Map<String,Handle> target() {
        var value=target;if(value!=null)return value;
        synchronized(this){if(target==null)target=Map.copyOf(evidence.targetCatalog());return target;}
    }
    private Handle resolve(ClassNode node,int tag,String owner,String name,String desc) {
        if(!desc.equals(DESC) || tag!=H_GETSTATIC && tag!=H_PUTSTATIC)return null;
        var cached=names.get(name);
        if(cached==null) {
            var resolved=Optional.ofNullable(name.startsWith("f_")?evidence.canonical(name):MEMBERS.contains(name)?name:null);
            var previous=names.putIfAbsent(name,resolved);cached=previous==null?resolved:previous;
        }
        String canonical=cached.orElse(null);
        if(canonical==null||!MEMBERS.contains(canonical))return null;
        if(!owner.equals(SOURCE) && !SOURCE.equals(evidence.declaration(node,owner,name,desc)))return null;
        if(tag==H_PUTSTATIC)throw new UnsupportedOperationException("Source built-in block-entity constants are final: "+name);
        var field=target().get(canonical);
        if(field==null)throw new UnsupportedOperationException("Native built-in block-entity constant is unavailable: "+canonical);
        return field;
    }
    int rewrite(ClassNode node) {
        int count=0;
        for(var method:node.methods)for(var insn:method.instructions) {
            if(insn instanceof FieldInsnNode field) {
                int tag=field.getOpcode()==GETSTATIC?H_GETSTATIC:field.getOpcode()==PUTSTATIC?H_PUTSTATIC:-1;
                if(tag<0)continue;
                var mapped=resolve(node,tag,field.owner,field.name,field.desc);
                if(mapped!=null){field.owner=mapped.getOwner();field.name=mapped.getName();count++;}
            } else if(insn instanceof LdcInsnNode ldc){Object mapped=constant(node,ldc.cst);if(!Objects.equals(mapped,ldc.cst)){ldc.cst=mapped;count++;}}
            else if(insn instanceof InvokeDynamicInsnNode indy) {
                var bootstrap=(Handle)constant(node,indy.bsm);if(!bootstrap.equals(indy.bsm)){indy.bsm=bootstrap;count++;}
                for(int i=0;i<indy.bsmArgs.length;i++){Object mapped=constant(node,indy.bsmArgs[i]);if(!Objects.equals(mapped,indy.bsmArgs[i])){indy.bsmArgs[i]=mapped;count++;}}
            }
        }
        return count;
    }
    private Object constant(ClassNode node,Object value) {
        if(value instanceof Handle handle){var mapped=resolve(node,handle.getTag(),handle.getOwner(),handle.getName(),handle.getDesc());return mapped==null?value:mapped;}
        if(value instanceof ConstantDynamic dynamic) {
            Object[] args=new Object[dynamic.getBootstrapMethodArgumentCount()];boolean changed=false;
            var bootstrap=(Handle)constant(node,dynamic.getBootstrapMethod());changed|=!bootstrap.equals(dynamic.getBootstrapMethod());
            for(int i=0;i<args.length;i++){Object old=dynamic.getBootstrapMethodArgument(i);args[i]=constant(node,old);changed|=!Objects.equals(args[i],old);}
            return changed?new ConstantDynamic(dynamic.getName(),dynamic.getDescriptor(),bootstrap,args):value;
        }
        return value;
    }
}
