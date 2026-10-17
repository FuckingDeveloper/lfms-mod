package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

/** Source inheritance/ABI proof precedes projection to a source-shaped ingredient view. */
final class Forge1192IngredientRewriter {
    static final String SOURCE="net/minecraft/world/item/crafting/Ingredient";
    static final String VIEW="com/fuckingdeveloper/lms/profile/forge1192/runtime/recipe/LegacyIngredient";
    interface Evidence {boolean subtype(String owner,String root);String method(String owner,String name,String descriptor);String field(String name,String descriptor);}
    private static String root(String owner,Evidence evidence){
        if(evidence.subtype(owner,SOURCE))return SOURCE;
        for(String type:List.of(SOURCE+"$Value",SOURCE+"$ItemValue",SOURCE+"$TagValue"))if(evidence.subtype(owner,type))return type;
        return null;
    }
    private static String name(String owner,String name,String desc,Evidence evidence){String root=root(owner,evidence);return root==null||!name.startsWith("m_")?name:Optional.ofNullable(evidence.method(root,name,desc)).orElse(name);}
    static int rewrite(ClassNode node,Evidence evidence){
        int changed=0;
        if(evidence.subtype(node.name,SOURCE))for(var method:node.methods)
            if(Set.of("lms$sourceValues","lms$callback","lms$nativeView","lms$epoch","lms$nativeReload","lms$guardedInvalidate").contains(method.name))
                throw new IllegalStateException("Source ingredient runtime-member collision: "+node.name+"#"+method.name+method.desc);
        for(var method:node.methods){String next=name(node.name,method.name,method.desc,evidence);if(!next.equals(method.name)){method.name=next;changed++;}}
        for(var method:node.methods)for(var instruction:method.instructions){
            if(instruction instanceof MethodInsnNode call){
                String next=name(call.owner,call.name,call.desc,evidence);
                if(!next.equals(call.name)){call.name=next;changed++;}
                if(call.getOpcode()==INVOKEVIRTUAL&&call.name.equals("invalidate")&&call.desc.equals("()V")&&evidence.subtype(call.owner,SOURCE)){
                    call.setOpcode(INVOKESTATIC);call.owner=VIEW;call.name="lms$guardedInvalidate";call.desc="(L"+VIEW+";)V";call.itf=false;changed++;
                }
            }
            if(instruction instanceof FieldInsnNode field&&evidence.subtype(field.owner,SOURCE)&&field.desc.equals("L"+SOURCE+";")){
                String canonical=evidence.field(field.name,field.desc);if("EMPTY".equals(canonical)&&!field.name.equals(canonical)){field.name=canonical;changed++;}
            }
            if(instruction instanceof LdcInsnNode ldc){Object next=constant(ldc.cst,evidence);if(next!=ldc.cst){ldc.cst=next;changed++;}}
            if(instruction instanceof InvokeDynamicInsnNode indy)for(int i=0;i<indy.bsmArgs.length;i++){Object next=constant(indy.bsmArgs[i],evidence);if(next!=indy.bsmArgs[i]){indy.bsmArgs[i]=next;changed++;}}
        }
        return changed;
    }
    private static Object constant(Object value,Evidence evidence){
        if(value instanceof Handle handle){
            if(handle.getTag()==H_GETSTATIC&&evidence.subtype(handle.getOwner(),SOURCE)&&handle.getDesc().equals("L"+SOURCE+";")&&"EMPTY".equals(evidence.field(handle.getName(),handle.getDesc())))return new Handle(handle.getTag(),handle.getOwner(),"EMPTY",handle.getDesc(),false);
            if(Set.of(H_INVOKEVIRTUAL,H_INVOKESTATIC,H_INVOKESPECIAL,H_INVOKEINTERFACE).contains(handle.getTag())){
                String next=name(handle.getOwner(),handle.getName(),handle.getDesc(),evidence);
                if(next.equals("invalidate")&&handle.getDesc().equals("()V")&&handle.getTag()==H_INVOKEVIRTUAL&&evidence.subtype(handle.getOwner(),SOURCE))
                    throw new UnsupportedOperationException("Source protected invalidation handles outside native reload are not adapted");
                if(!next.equals(handle.getName()))return new Handle(handle.getTag(),handle.getOwner(),next,handle.getDesc(),handle.isInterface());
            }
        }
        if(value instanceof ConstantDynamic c){Object[] args=new Object[c.getBootstrapMethodArgumentCount()];boolean changed=false;for(int i=0;i<args.length;i++){Object old=c.getBootstrapMethodArgument(i);args[i]=constant(old,evidence);changed|=old!=args[i];}if(changed)return new ConstantDynamic(c.getName(),c.getDescriptor(),c.getBootstrapMethod(),args);}
        return value;
    }
}
