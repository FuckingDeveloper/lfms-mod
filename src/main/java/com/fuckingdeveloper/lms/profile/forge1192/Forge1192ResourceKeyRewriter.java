package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

/** Exact source key operations across direct and callable forms, before type migration. */
final class Forge1192ResourceKeyRewriter {
    private static final String ROOT="net/minecraft/resources/ResourceKey", K="L"+ROOT+";", ID="Lnet/minecraft/resources/ResourceLocation;";
    private record Operation(String srg,String named,String desc,boolean isStatic) {}
    private static final List<Operation> OPERATIONS=List.of(
            new Operation("m_135782_","location","()"+ID,false),
            new Operation("m_211136_","registry","()"+ID,false),
            new Operation("m_135783_","isFor","("+K+")Z",false),
            new Operation("m_195975_","cast","("+K+")Ljava/util/Optional;",false),
            new Operation("m_135785_","create","("+K+ID+")"+K,true),
            new Operation("m_135788_","createRegistryKey","("+ID+")"+K,true),
            new Operation("m_195966_","codec","("+K+")Lcom/mojang/serialization/Codec;",true));
    static int rewrite(ClassNode node) {
        int count=0;
        for(var method:node.methods)for(var insn:method.instructions) {
            if(insn instanceof MethodInsnNode call) {
                String mapped=method(call.owner,call.name,call.desc,call.getOpcode()==INVOKESTATIC,call.getOpcode()==INVOKEVIRTUAL);
                if(mapped!=null&&!mapped.equals(call.name)){call.name=mapped;count++;}
            } else if(insn instanceof LdcInsnNode ldc) {
                Object mapped=constant(ldc.cst);if(!Objects.equals(mapped,ldc.cst)){ldc.cst=mapped;count++;}
            } else if(insn instanceof InvokeDynamicInsnNode indy) {
                var bootstrap=(Handle)constant(indy.bsm);if(!bootstrap.equals(indy.bsm)){indy.bsm=bootstrap;count++;}
                for(int i=0;i<indy.bsmArgs.length;i++){Object mapped=constant(indy.bsmArgs[i]);if(!Objects.equals(mapped,indy.bsmArgs[i])){indy.bsmArgs[i]=mapped;count++;}}
            }
        }
        return count;
    }
    private static String method(String owner,String name,String desc,boolean isStatic,boolean virtual) {
        if(!owner.equals(ROOT))return null;
        for(var op:OPERATIONS)if((op.srg.equals(name)||op.named.equals(name))&&op.desc.equals(desc)
                &&op.isStatic==isStatic&&(isStatic||virtual))
            return op.named.equals("location")?com.fuckingdeveloper.lms.target.InstalledTargetAdapters.resourceKeyLocationName():op.named;
        return null;
    }
    private static Object constant(Object value) {
        if(value instanceof Handle h) {
            String mapped=method(h.getOwner(),h.getName(),h.getDesc(),h.getTag()==H_INVOKESTATIC,h.getTag()==H_INVOKEVIRTUAL);
            if(mapped!=null&&!mapped.equals(h.getName()))return new Handle(h.getTag(),h.getOwner(),mapped,h.getDesc(),h.isInterface());
        } else if(value instanceof ConstantDynamic d) {
            Object[] args=new Object[d.getBootstrapMethodArgumentCount()];for(int i=0;i<args.length;i++)args[i]=constant(d.getBootstrapMethodArgument(i));
            return new ConstantDynamic(d.getName(),d.getDescriptor(),(Handle)constant(d.getBootstrapMethod()),args);
        }
        return value;
    }
}
