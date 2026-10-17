package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

/** Only local, non-observing source model-view transactions can use a relative GUI transform. */
final class Forge1192GuiModelViewRewriter {
    private static final String SYSTEM="com/mojang/blaze3d/systems/RenderSystem",POSE="com/mojang/blaze3d/vertex/PoseStack";
    private static final String BRIDGE="com/fuckingdeveloper/lms/profile/forge1192/runtime/render/Forge1192GuiModelViewBridge";
    private static final Set<String> STACK_OPS=Set.of("pushPose()V","m_85836_()V","popPose()V","m_85849_()V",
            "translate(DDD)V","m_85837_(DDD)V","scale(FFF)V","m_85841_(FFF)V");
    private static String operation(String owner,String name,String desc) {
        if(!owner.equals(SYSTEM))return null;
        if((name.equals("getModelViewStack")||name.equals("m_157191_"))&&desc.equals("()L"+POSE+";"))return "stack";
        if((name.equals("applyModelViewMatrix")||name.equals("m_157182_"))&&desc.equals("()V"))return "apply";
        return null;
    }
    static int rewrite(ClassNode node) {
        int count=0;
        for(var method:node.methods) {
            var getters=new HashSet<AbstractInsnNode>();var operations=new ArrayList<MethodInsnNode>();boolean unprovenConstant=false;boolean invalidShape=false;
            for(var insn:method.instructions) {
                if(insn instanceof LdcInsnNode ldc)unprovenConstant|=globalHandle(ldc.cst);
                if(insn instanceof InvokeDynamicInsnNode indy)for(Object arg:indy.bsmArgs)unprovenConstant|=globalHandle(arg);
                if(insn instanceof MethodInsnNode call&&operation(call.owner,call.name,call.desc)!=null) {
                    if(call.getOpcode()!=Opcodes.INVOKESTATIC)invalidShape=true;
                    operations.add(call);if(operation(call.owner,call.name,call.desc).equals("stack"))getters.add(call);
                }
            }
            // Leave unproven operations untouched. Existing lazy consumers retain their
            // original explicit linkage gap; an unrelated class method is not disabled.
            if(operations.isEmpty()||getters.isEmpty()||unprovenConstant||invalidShape)continue;
            boolean[] closed={true};
            try {
                new Analyzer<>(new SourceInterpreter(Opcodes.ASM9) {
                    private boolean selected(SourceValue value){return value!=null&&value.insns.stream().anyMatch(getters::contains);}
                    private void reject(SourceValue value){if(selected(value))closed[0]=false;}
                    @Override public SourceValue copyOperation(AbstractInsnNode insn,SourceValue value){return value;}
                    @Override public SourceValue unaryOperation(AbstractInsnNode insn,SourceValue value){reject(value);return super.unaryOperation(insn,value);}
                    @Override public SourceValue binaryOperation(AbstractInsnNode insn,SourceValue left,SourceValue right){reject(left);reject(right);return super.binaryOperation(insn,left,right);}
                    @Override public SourceValue ternaryOperation(AbstractInsnNode insn,SourceValue a,SourceValue b,SourceValue c){reject(a);reject(b);reject(c);return super.ternaryOperation(insn,a,b,c);}
                    @Override public SourceValue naryOperation(AbstractInsnNode insn,List<? extends SourceValue> values){
                        if(values.stream().anyMatch(this::selected)) {
                            if(!(insn instanceof MethodInsnNode call)||call.getOpcode()!=Opcodes.INVOKEVIRTUAL||!call.owner.equals(POSE)
                                    ||!STACK_OPS.contains(call.name+call.desc)||!selected(values.getFirst())
                                    ||!getters.containsAll(values.getFirst().insns)||values.stream().skip(1).anyMatch(this::selected))
                                closed[0]=false;
                        }
                        return super.naryOperation(insn,values);
                    }
                    @Override public void returnOperation(AbstractInsnNode insn,SourceValue value,SourceValue expected){reject(value);}
                }).analyze(node.name,method);
            }catch(AnalyzerException failure){throw new IllegalStateException("Cannot prove source model-view flow: "+node.name+"#"+method.name,failure);}
            if(!closed[0])continue;
            for(var call:operations){call.name=operation(call.owner,call.name,call.desc);call.owner=BRIDGE;call.itf=false;count++;}
        }
        return count;
    }
    private static boolean globalHandle(Object value) {
        if(value instanceof Handle handle)return operation(handle.getOwner(),handle.getName(),handle.getDesc())!=null;
        if(value instanceof ConstantDynamic dynamic)for(int i=0;i<dynamic.getBootstrapMethodArgumentCount();i++)if(globalHandle(dynamic.getBootstrapMethodArgument(i)))return true;
        return false;
    }
}
