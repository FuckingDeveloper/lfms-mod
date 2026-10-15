package com.fuckingdeveloper.lms.profile;

import java.util.*;
import java.util.function.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Source player-only container operations retain native opener tracking and source callbacks. */
final class Forge1192ContainerUsersRewriter {
    private static final String CONTAINER="net/minecraft/world/Container", PLAYER="net/minecraft/world/entity/player/Player",
            USER="net/minecraft/world/entity/ContainerUser", COUNTER="net/minecraft/world/level/block/entity/ContainerOpenersCounter";
    private static final String PLAYER_CALL="(L"+PLAYER+";)V", USER_CALL="(L"+USER+";)V";
    interface Names { String canonical(String owner,String name,String descriptor); }
    static int rewrite(ClassNode node, Predicate<String> subtypeContainer, Predicate<String> subtypeCounter, Names names) {
        var additions=new ArrayList<MethodNode>();var wrappers=new LinkedHashMap<Handle,Handle>();int changed=0;
        for(var method:List.copyOf(node.methods)) {
            if(subtypeContainer.test(node.name)&&method.desc.equals(PLAYER_CALL)) {
                String canonical=names.canonical(CONTAINER,method.name,method.desc);
                if(canonical!=null&&(canonical.equals("startOpen")||canonical.equals("stopOpen"))) {
                    if((method.access&Opcodes.ACC_STATIC)!=0)throw new IllegalStateException("Static source container callback: "+node.name);
                    String alias="lms$source"+canonical;method.name=alias;
                    var thunk=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_SYNTHETIC,canonical,USER_CALL,null,null);
                    thunk.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));thunk.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
                    thunk.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"com/fuckingdeveloper/lms/runtime/Forge1192ContainerUsersBridge","requirePlayer","(L"+USER+";)L"+PLAYER+";",false));
                    thunk.instructions.add(new MethodInsnNode((node.access&Opcodes.ACC_INTERFACE)!=0?Opcodes.INVOKEINTERFACE:Opcodes.INVOKEVIRTUAL,node.name,alias,PLAYER_CALL,(node.access&Opcodes.ACC_INTERFACE)!=0));
                    thunk.instructions.add(new InsnNode(Opcodes.RETURN));thunk.maxStack=2;thunk.maxLocals=2;additions.add(thunk);changed++;
                }
            }
            for(var insn:method.instructions) {
                if(insn instanceof MethodInsnNode call) {
                    int kind=switch(call.getOpcode()){case Opcodes.INVOKEVIRTUAL->Opcodes.H_INVOKEVIRTUAL;case Opcodes.INVOKEINTERFACE->Opcodes.H_INVOKEINTERFACE;case Opcodes.INVOKESPECIAL->Opcodes.H_INVOKESPECIAL;default->-1;};
                    if(kind<0)continue;
                    var old=new Handle(kind,call.owner,call.name,call.desc,call.itf);
                    var next=adapt(node,old,subtypeContainer,subtypeCounter,names,wrappers,additions);
                    if(next.equals(old))continue;
                    call.setOpcode(next.getTag()==Opcodes.H_INVOKESTATIC?Opcodes.INVOKESTATIC:Opcodes.INVOKESPECIAL);
                    call.owner=next.getOwner();call.name=next.getName();call.desc=next.getDesc();call.itf=next.isInterface();changed++;
                }else if(insn instanceof LdcInsnNode ldc) {
                    Object next=constant(node,ldc.cst,subtypeContainer,subtypeCounter,names,wrappers,additions);if(next!=ldc.cst){ldc.cst=next;changed++;}
                }else if(insn instanceof InvokeDynamicInsnNode indy)for(int i=0;i<indy.bsmArgs.length;i++) {
                    Object next=constant(node,indy.bsmArgs[i],subtypeContainer,subtypeCounter,names,wrappers,additions);if(next!=indy.bsmArgs[i]){indy.bsmArgs[i]=next;changed++;}
                }
            }
        }
        node.methods.addAll(additions);return changed;
    }
    private static Object constant(ClassNode node,Object value,Predicate<String> container,Predicate<String> counter,Names names,Map<Handle,Handle> wrappers,List<MethodNode> additions) {
        if(value instanceof Handle handle)return adapt(node,handle,container,counter,names,wrappers,additions);
        if(value instanceof ConstantDynamic dynamic) {
            Object[] args=new Object[dynamic.getBootstrapMethodArgumentCount()];boolean changed=false;
            for(int i=0;i<args.length;i++){Object original=dynamic.getBootstrapMethodArgument(i);args[i]=constant(node,original,container,counter,names,wrappers,additions);changed|=args[i]!=original;}
            return changed?new ConstantDynamic(dynamic.getName(),dynamic.getDescriptor(),dynamic.getBootstrapMethod(),args):value;
        }
        return value;
    }
    private static Handle adapt(ClassNode node,Handle handle,Predicate<String> container,Predicate<String> counter,Names names,Map<Handle,Handle> wrappers,List<MethodNode> additions) {
        if(handle.getTag()!=Opcodes.H_INVOKEVIRTUAL&&handle.getTag()!=Opcodes.H_INVOKEINTERFACE&&handle.getTag()!=Opcodes.H_INVOKESPECIAL)return handle;
        boolean isContainer=handle.getDesc().equals(PLAYER_CALL)&&container.test(handle.getOwner());
        String counterDesc="(L"+PLAYER+";Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V";
        boolean isCounter=handle.getDesc().equals(counterDesc)&&counter.test(handle.getOwner());
        if(!isContainer&&!isCounter)return handle;
        String canonical=names.canonical(isContainer?CONTAINER:COUNTER,handle.getName(),handle.getDesc());
        if(canonical==null||!(isContainer?(canonical.equals("startOpen")||canonical.equals("stopOpen")):(canonical.equals("incrementOpeners")||canonical.equals("decrementOpeners"))))return handle;
        if(handle.getTag()==Opcodes.H_INVOKESPECIAL) {
            if(isCounter)throw new UnsupportedOperationException("Source special counter invocation requires explicit adapter: "+handle);
            return new Handle(handle.getTag(),handle.getOwner(),canonical,USER_CALL,handle.isInterface());
        }
        var existing=wrappers.get(handle);if(existing!=null)return existing;
        String desc="(L"+handle.getOwner()+";"+handle.getDesc().substring(1),name="lms$containerUser$"+wrappers.size();
        for(var m:node.methods)if(m.name.equals(name))throw new IllegalStateException("Source container adapter name collision: "+name);
        var wrapper=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC|Opcodes.ACC_SYNTHETIC,name,desc,null,null);
        int slots=isContainer?2:5;
        for(int i=0;i<slots;i++)wrapper.instructions.add(new VarInsnNode(Opcodes.ALOAD,i));
        String targetDesc=USER_CALL;
        if(isCounter) {
            targetDesc=counterDesc.replace("L"+PLAYER+";","Lnet/minecraft/world/entity/LivingEntity;");
            if(canonical.equals("incrementOpeners")) {
                wrapper.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
                wrapper.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,PLAYER,"getContainerInteractionRange","()D",false));
                targetDesc=targetDesc.replace(")V","D)V");
            }
        }
        wrapper.instructions.add(new MethodInsnNode(isContainer?Opcodes.INVOKEINTERFACE:Opcodes.INVOKEVIRTUAL,isContainer?CONTAINER:COUNTER,canonical,targetDesc,isContainer));
        wrapper.instructions.add(new InsnNode(Opcodes.RETURN));wrapper.maxLocals=slots;wrapper.maxStack=7;additions.add(wrapper);
        var result=new Handle(Opcodes.H_INVOKESTATIC,node.name,name,desc,(node.access&Opcodes.ACC_INTERFACE)!=0);wrappers.put(handle,result);return result;
    }
}
