package com.fuckingdeveloper.lms.profile.forge1192;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;

/** Conservative closed-flow admission, independent of a fixture's identity. */
final class Forge1192PoseMatrixDrawRewriter {
    private static final String M4="com/mojang/math/Matrix4f", M3="com/mojang/math/Matrix3f";
    private static final String V="com/mojang/blaze3d/vertex/VertexConsumer", R="L"+V+";";
    private Forge1192PoseMatrixDrawRewriter() {}
    private static boolean matrix(String value) { return value != null && (value.contains(M4) || value.contains(M3)); }
    private static Handle member(int tag,String owner,String name,String desc) {
        if(tag==Opcodes.H_INVOKEVIRTUAL && owner.equals("com/mojang/blaze3d/vertex/PoseStack$Pose")) {
            if(name.equals("m_85861_") && desc.equals("()L"+M4+";"))return new Handle(tag,owner,"pose","()Lorg/joml/Matrix4f;",false);
            if(name.equals("m_85864_") && desc.equals("()L"+M3+";"))return new Handle(tag,owner,"normal","()Lorg/joml/Matrix3f;",false);
        }
        if(tag==Opcodes.H_INVOKEINTERFACE && owner.equals(V)) {
            if(name.equals("m_85982_") && desc.equals("(L"+M4+";FFF)"+R))
                return new Handle(tag,owner,"addVertex","(Lorg/joml/Matrix4fc;FFF)"+R,true);
            if(name.equals("m_85977_") && desc.equals("(L"+M3+";FFF)"+R))
                return new Handle(Opcodes.H_INVOKESTATIC,"com/fuckingdeveloper/lms/profile/forge1192/runtime/render/Forge1192VertexConsumerBridge","normal",
                        "("+R+"Lorg/joml/Matrix3f;FFF)"+R,false);
        }
        return null;
    }
    private static int tag(MethodInsnNode call) {
        return switch(call.getOpcode()) {case Opcodes.INVOKEVIRTUAL->Opcodes.H_INVOKEVIRTUAL;case Opcodes.INVOKEINTERFACE->Opcodes.H_INVOKEINTERFACE;default->Opcodes.H_INVOKESTATIC;};
    }
    private static boolean privateHelper(ClassNode node,String owner,String name,String desc) {
        return owner.equals(node.name) && node.methods.stream().anyMatch(m->m.name.equals(name) && m.desc.equals(desc) && (m.access&Opcodes.ACC_PRIVATE)!=0);
    }
    static int rewrite(ClassNode node) {
        boolean getter=false;
        if(matrix(node.signature) || matrix(node.superName) || node.fields.stream().anyMatch(f->matrix(f.desc)||matrix(f.signature)))return 0;
        for(var method:node.methods) {
            String result=Type.getReturnType(method.desc).getDescriptor();
            if(result.equals("Ljava/lang/Object;") || result.equals("[Ljava/lang/Object;"))return 0;
            if(matrix(method.desc) && (method.access&Opcodes.ACC_PRIVATE)==0)return 0;
            if((method.access&Opcodes.ACC_PRIVATE)==0 && Type.getReturnType(method.desc).getSort()==Type.OBJECT)return 0;
            for(var instruction:method.instructions) {
                if(instruction.getOpcode()==Opcodes.AASTORE || instruction.getOpcode()==Opcodes.PUTFIELD || instruction.getOpcode()==Opcodes.PUTSTATIC)return 0;
                if(instruction instanceof TypeInsnNode type && matrix(type.desc))return 0;
                if(instruction instanceof FieldInsnNode field && (matrix(field.owner)||matrix(field.desc)))return 0;
                if(instruction instanceof MethodInsnNode call) {
                    if(matrix(call.owner))return 0;
                    if(call.owner.equals("java/lang/Object") && !call.name.equals("<init>"))return 0;
                    for(var arg:Type.getArgumentTypes(call.desc))if(arg.getDescriptor().equals("Ljava/lang/Object;")||arg.getDescriptor().equals("[Ljava/lang/Object;"))return 0;
                    var adapted=member(tag(call),call.owner,call.name,call.desc);
                    getter |= adapted!=null && call.owner.endsWith("PoseStack$Pose");
                    if(matrix(call.desc) && adapted==null && !privateHelper(node,call.owner,call.name,call.desc))return 0;
                }
                if(instruction instanceof InvokeDynamicInsnNode dynamic && matrix(dynamic.desc)) {
                    if(!dynamic.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory") || !dynamic.bsm.getName().equals("metafactory"))return 0;
                    if(dynamic.bsmArgs.length!=3 || !(dynamic.bsmArgs[1] instanceof Handle impl)
                            || !privateHelper(node,impl.getOwner(),impl.getName(),impl.getDesc()))return 0;
                }
                if(instruction instanceof LdcInsnNode ldc && (ldc.cst instanceof Handle || ldc.cst instanceof ConstantDynamic
                        || ldc.cst instanceof Type type && matrix(type.getDescriptor())))return 0;
            }
        }
        if(!getter)return 0;
        for(var method:node.methods)for(var instruction:method.instructions)if(instruction instanceof MethodInsnNode call) {
            var adapted=member(tag(call),call.owner,call.name,call.desc);
            if(adapted!=null) {
                call.setOpcode(adapted.getTag()==Opcodes.H_INVOKESTATIC?Opcodes.INVOKESTATIC:adapted.getTag()==Opcodes.H_INVOKEINTERFACE?Opcodes.INVOKEINTERFACE:Opcodes.INVOKEVIRTUAL);
                call.owner=adapted.getOwner();call.name=adapted.getName();call.desc=adapted.getDesc();call.itf=adapted.isInterface();
            }
        }
        var mapped=new ClassNode();
        node.accept(new ClassRemapper(mapped,new Remapper(Opcodes.ASM9) {
            @Override public String map(String type) {return type.equals(M4)?"org/joml/Matrix4f":type.equals(M3)?"org/joml/Matrix3f":type;}
        }));
        node.methods=mapped.methods;
        return 1;
    }
}
