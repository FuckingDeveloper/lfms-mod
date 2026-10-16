package com.fuckingdeveloper.lms.profile.forge1192;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

/** Native extraction replaces only verified singleton-getter/renderStatic pairs. */
final class Forge1192NestedItemRewriter {
    private static final String RENDERER="net/minecraft/client/renderer/entity/ItemRenderer";
    private static final String BRIDGE="com/fuckingdeveloper/lms/profile/forge1192/runtime/render/Forge1192NestedItemBridge";
    private static final String SIMPLE="(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;IILcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";
    private static final String CONTEXT="(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V";
    static int rewrite(ClassNode node) {
        int count=0;
        for(var method:node.methods) {
            boolean selected=false;
            for(var i:method.instructions) if(i instanceof MethodInsnNode c && operation(c)) selected=true;
            if(!selected)continue;
            Frame<SourceValue>[] frames;
            try { frames=new Analyzer<>(new SourceInterpreter(Opcodes.ASM9) {
                @Override public SourceValue copyOperation(AbstractInsnNode instruction,SourceValue value){return value;}
            }).analyze(node.name,method); }
            catch(AnalyzerException failure){throw new IllegalStateException("Cannot establish source renderStatic receiver dataflow",failure);}
            var instructions=method.instructions.toArray();
            var planned=new java.util.LinkedHashMap<MethodInsnNode,java.util.List<MethodInsnNode>>();
            for(int i=0;i<instructions.length;i++) {
                if(!(instructions[i] instanceof MethodInsnNode call) || !operation(call))continue;
                var frame=frames[i]; if(frame==null)continue;
                var receiver=frame.getStack(frame.getStackSize()-Type.getArgumentTypes(call.desc).length-1);
                if(receiver.insns.size()!=1 || !(receiver.insns.iterator().next() instanceof MethodInsnNode getter)
                        || getter.getOpcode()!=Opcodes.INVOKEVIRTUAL || !getter.owner.equals("net/minecraft/client/Minecraft")
                        || !getter.name.equals("m_91291_") || !getter.desc.equals("()L"+RENDERER+";"))continue;
                planned.computeIfAbsent(getter,k->new java.util.ArrayList<>()).add(call);
            }
            for(var plan:planned.entrySet()) {
                var getter=plan.getKey();boolean exclusive=true;
                for(int i=0;i<instructions.length;i++) {
                    var instruction=instructions[i];var frame=frames[i];if(frame==null)continue;
                    int consumed=0;
                    if(instruction instanceof MethodInsnNode c) consumed=Type.getArgumentTypes(c.desc).length+(c.getOpcode()==Opcodes.INVOKESTATIC?0:1);
                    else if(instruction instanceof InvokeDynamicInsnNode d) consumed=Type.getArgumentTypes(d.desc).length;
                    else if(instruction instanceof FieldInsnNode f) consumed=switch(f.getOpcode()){case Opcodes.GETFIELD,Opcodes.PUTSTATIC->1;case Opcodes.PUTFIELD->2;default->0;};
                    else if(instruction.getOpcode()==Opcodes.ARETURN || instruction.getOpcode()==Opcodes.CHECKCAST
                            || instruction.getOpcode()==Opcodes.INSTANCEOF || instruction.getOpcode()==Opcodes.MONITORENTER
                            || instruction.getOpcode()==Opcodes.MONITOREXIT || instruction.getOpcode()==Opcodes.IFNULL
                            || instruction.getOpcode()==Opcodes.IFNONNULL)consumed=1;
                    else if(instruction.getOpcode()==Opcodes.IF_ACMPEQ || instruction.getOpcode()==Opcodes.IF_ACMPNE)consumed=2;
                    else if(instruction.getOpcode()==Opcodes.AASTORE)consumed=3;
                    for(int j=0;j<consumed;j++)if(frame.getStack(frame.getStackSize()-1-j).insns.contains(getter)
                            && !(instruction instanceof MethodInsnNode c && plan.getValue().contains(c) && j==consumed-1))exclusive=false;
                }
                if(!exclusive)continue; // Keep the original unsupported operation; never change a foreign use's type.
                getter.setOpcode(Opcodes.INVOKESTATIC);getter.owner=BRIDGE;getter.name="resolver";
                getter.desc="(Lnet/minecraft/client/Minecraft;)Lnet/minecraft/client/renderer/item/ItemModelResolver;";getter.itf=false;
                for(var call:plan.getValue()) {
                    call.setOpcode(Opcodes.INVOKESTATIC);call.owner=BRIDGE;call.name="renderStatic";
                    call.desc="(Lnet/minecraft/client/renderer/item/ItemModelResolver;"+call.desc.substring(1);call.itf=false;count++;
                }
            }
        }
        return count;
    }
    private static boolean operation(MethodInsnNode c) {
        return c.getOpcode()==Opcodes.INVOKEVIRTUAL && c.owner.equals(RENDERER)
                && (c.name.equals("m_174269_") && c.desc.equals(SIMPLE) || c.name.equals("m_174242_") && c.desc.equals(CONTEXT));
    }
}
