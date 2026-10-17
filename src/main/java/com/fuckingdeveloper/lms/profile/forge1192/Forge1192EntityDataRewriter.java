package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import static org.objectweb.asm.Opcodes.*;

/** Proven source construction definitions become native builder definitions, never live data mutations. */
final class Forge1192EntityDataRewriter {
    static final String ENTITY="net/minecraft/world/entity/Entity", DATA="net/minecraft/network/syncher/SynchedEntityData";
    private static final String DEFINE="(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;)V";
    interface Evidence {
        boolean entity(String owner);
        String method(String owner,String name,String desc);
        boolean dataField(FieldInsnNode field);
    }
    private static boolean hook(String owner,String name,String desc,Evidence e) {
        return desc.equals("()V") && e.entity(owner) && "defineSynchedData".equals(e.method(ENTITY,name,desc));
    }
    private static UnsupportedOperationException gap(ClassNode node,String reason) {
        return new UnsupportedOperationException("Source entity-data builder boundary: "+node.name+": "+reason);
    }
    static int rewrite(ClassNode node,Evidence e,String builder) {
        int count=0;
        for(var method:node.methods) {
            boolean callback=(method.access&ACC_STATIC)==0&&hook(node.name,method.name,method.desc,e);
            // Reject external/callable hook entry instead of losing its construction-only builder.
            for(var insn:method.instructions) {
                if(insn instanceof MethodInsnNode call&&hook(call.owner,call.name,call.desc,e)
                        &&(!callback||call.getOpcode()!=INVOKESPECIAL))throw gap(node,"callback invoked outside a superclass definition hook");
                if(insn instanceof LdcInsnNode ldc)rejectConstant(node,ldc.cst,e);
                if(insn instanceof InvokeDynamicInsnNode indy) {
                    rejectConstant(node,indy.bsm,e);
                    for(Object arg:indy.bsmArgs)rejectConstant(node,arg,e);
                }
            }
            if(!callback)continue;
            if(node.methods.stream().anyMatch(m -> m != method && m.name.equals("defineSynchedData") && m.desc.equals("(L"+builder+";)V")))
                throw gap(node,"native builder hook already declared");
            if((method.access&(ACC_ABSTRACT|ACC_NATIVE))!=0)throw gap(node,"abstract/native callback requires separate proof");
            Frame<SourceValue>[] frames;
            try {frames=new Analyzer<>(new SourceInterpreter()).analyze(node.name,method);}
            catch(AnalyzerException failure){throw new IllegalStateException("Cannot prove source data callback: "+node.name,failure);}
            var reads=new HashSet<FieldInsnNode>();var definitions=new IdentityHashMap<MethodInsnNode,FieldInsnNode>();
            var instructions=method.instructions.toArray();
            for(int i=0;i<instructions.length;i++) {
                var insn=instructions[i];var frame=frames[i];
                if(insn instanceof VarInsnNode variable&&variable.var==0&&variable.getOpcode()!=ALOAD)
                    throw gap(node,"original this local is rebound");
                if(insn instanceof MethodInsnNode call&&hook(call.owner,call.name,call.desc,e)
                        &&(frame==null||!originalThis(frame.getStack(frame.getStackSize()-1))))
                    throw gap(node,"superclass definition receiver is not original this");
                if(insn instanceof FieldInsnNode field&&field.desc.equals("L"+DATA+";")&&e.dataField(field)) {
                    if(field.getOpcode()!=GETFIELD||frame==null||!originalThis(frame.getStack(frame.getStackSize()-1)))
                        throw gap(node,"data definition receiver is not original this");
                    reads.add(field);
                }
                if(insn instanceof MethodInsnNode call&&call.owner.equals(DATA)) {
                    if(!call.desc.equals(DEFINE)||!"define".equals(e.method(DATA,call.name,call.desc))||frame==null)
                        throw gap(node,"live data reads/mutations or unsupported data operation inside initialization");
                    var source=frame.getStack(frame.getStackSize()-3);
                    if(source.insns.size()!=1||!(source.insns.iterator().next() instanceof FieldInsnNode field)
                            ||!e.dataField(field)||definitions.containsValue(field))
                        throw gap(node,"definition data escaped, merged, duplicated or cached");
                    definitions.put(call,field);
                }
            }
            if(reads.size()!=definitions.size()||!reads.equals(new HashSet<>(definitions.values())))
                throw gap(node,"data read is not consumed by exactly one definition");
            // The new builder occupies slot one. Shift all old locals and their debug/type metadata.
            for(var insn:instructions) {
                if(insn instanceof VarInsnNode variable&&variable.var>=1)variable.var++;
                if(insn instanceof IincInsnNode increment&&increment.var>=1)increment.var++;
                if(insn instanceof FrameNode)method.instructions.remove(insn);
            }
            if(method.localVariables!=null)for(var local:method.localVariables)if(local.index>=1)local.index++;
            shiftAnnotations(method.visibleLocalVariableAnnotations);shiftAnnotations(method.invisibleLocalVariableAnnotations);
            method.maxLocals++;method.name="defineSynchedData";method.desc="(L"+builder+";)V";
            // Source callbacks have no parameters; preserve their type variables while adding the real target argument.
            if(method.signature!=null)method.signature=method.signature.replace("()V","(L"+builder+";)V");
            for(var field:reads) {
                var replacement=new InsnList();replacement.add(new InsnNode(POP));replacement.add(new VarInsnNode(ALOAD,1));
                method.instructions.insertBefore(field,replacement);method.instructions.remove(field);
            }
            for(var call:definitions.keySet()) {
                call.owner=builder;call.name="define";call.desc=DEFINE.substring(0,DEFINE.length()-1)+"L"+builder+";";
                method.instructions.insert(call,new InsnNode(POP));
            }
            for(var insn:instructions)if(insn instanceof MethodInsnNode call&&hook(call.owner,call.name,call.desc,e)) {
                method.instructions.insertBefore(call,new VarInsnNode(ALOAD,1));call.name="defineSynchedData";call.desc=method.desc;
            }
            count++;
        }
        return count;
    }
    private static boolean originalThis(SourceValue value) {
        return value.insns.size()==1&&value.insns.iterator().next() instanceof VarInsnNode v&&v.getOpcode()==ALOAD&&v.var==0;
    }
    private static void shiftAnnotations(List<LocalVariableAnnotationNode> annotations) {
        if(annotations!=null)for(var annotation:annotations)for(int i=0;i<annotation.index.size();i++)
            if(annotation.index.get(i)>=1)annotation.index.set(i,annotation.index.get(i)+1);
    }
    private static void rejectConstant(ClassNode node,Object value,Evidence e) {
        if(value instanceof Handle handle&&hook(handle.getOwner(),handle.getName(),handle.getDesc(),e))throw gap(node,"callable initialization hook");
        if(value instanceof ConstantDynamic dynamic) {
            rejectConstant(node,dynamic.getBootstrapMethod(),e);
            for(int i=0;i<dynamic.getBootstrapMethodArgumentCount();i++)rejectConstant(node,dynamic.getBootstrapMethodArgument(i),e);
        }
    }
}
