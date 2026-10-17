package com.fuckingdeveloper.lms.profile.forge1192;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Notification identity is projected centrally; removed source payloads are not equivalent. */
final class Forge1192RecipeNotificationRewriter {
    static final String SOURCE="net/minecraftforge/client/event/RecipesUpdatedEvent";
    private Forge1192RecipeNotificationRewriter() {}
    static int validate(ClassNode node) {
        if(SOURCE.equals(node.superName)) throw unsupported(node,"source event subclass");
        for(var method:node.methods) for(var insn:method.instructions) {
            if(insn instanceof MethodInsnNode call) member(node,call.owner,call.name,call.desc,call.getOpcode());
            if(insn instanceof FieldInsnNode field && field.owner.equals(SOURCE)) throw unsupported(node,"source event field "+field.name);
            if(insn instanceof TypeInsnNode type && type.getOpcode()==Opcodes.NEW && type.desc.equals(SOURCE)) throw unsupported(node,"source event construction");
            if(insn instanceof LdcInsnNode ldc) constant(node,ldc.cst);
            if(insn instanceof InvokeDynamicInsnNode indy) { constant(node,indy.bsm);for(Object value:indy.bsmArgs)constant(node,value); }
        }
        return 0;
    }
    private static void member(ClassNode node,String owner,String name,String descriptor,int opcode) {
        if(!owner.equals(SOURCE))return;
        boolean identity=opcode==Opcodes.INVOKEVIRTUAL && switch(name+descriptor) {
            case "getClass()Ljava/lang/Class;", "hashCode()I", "equals(Ljava/lang/Object;)Z", "toString()Ljava/lang/String;" -> true;
            default -> false;
        };
        if(!identity)throw unsupported(node,"source event member "+name+descriptor);
    }
    private static void constant(ClassNode node,Object value) {
        if(value instanceof Handle handle) {
            int opcode=switch(handle.getTag()) {
                case Opcodes.H_INVOKEVIRTUAL -> Opcodes.INVOKEVIRTUAL;
                case Opcodes.H_INVOKESTATIC -> Opcodes.INVOKESTATIC;
                case Opcodes.H_INVOKESPECIAL,Opcodes.H_NEWINVOKESPECIAL -> Opcodes.INVOKESPECIAL;
                case Opcodes.H_INVOKEINTERFACE -> Opcodes.INVOKEINTERFACE;
                default -> -1;
            };
            member(node,handle.getOwner(),handle.getName(),handle.getDesc(),opcode);
        } else if(value instanceof ConstantDynamic dynamic) {
            constant(node,dynamic.getBootstrapMethod());
            for(int i=0;i<dynamic.getBootstrapMethodArgumentCount();i++)constant(node,dynamic.getBootstrapMethodArgument(i));
        }
    }
    private static UnsupportedOperationException unsupported(ClassNode node,String operation) {
        return new UnsupportedOperationException("Recipe notification supports timing/class identity only; payload/construction requires a recipe-data adapter: "+node.name+": "+operation);
    }
}
