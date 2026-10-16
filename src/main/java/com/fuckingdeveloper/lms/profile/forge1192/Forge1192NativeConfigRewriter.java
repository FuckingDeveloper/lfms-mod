package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Source/native config ownership, getter and event contracts; no fixture selectors. */
final class Forge1192NativeConfigRewriter {
    private static final String SOURCE="net/minecraftforge/",CONFIG=SOURCE+"fml/config/ModConfig",SPEC=SOURCE+"common/ForgeConfigSpec",
            EVENT=SOURCE+"fml/config/IConfigEvent",MOD_EVENT=SOURCE+"fml/event/config/ModConfigEvent",
            BRIDGE="com/fuckingdeveloper/lms/profile/forge1192/runtime/lifecycle/Forge1192NativeConfigBridge";
    private static final Map<String,String> TYPES=Map.of(
            CONFIG,"com/fuckingdeveloper/lms/profile/forge1192/runtime/lifecycle/Forge1192ModConfigView",CONFIG+"$Type","com/fuckingdeveloper/lms/profile/forge1192/runtime/lifecycle/Forge1192ModConfigView$Type",
            SPEC,"net/neoforged/neoforge/common/ModConfigSpec",SPEC+"$ConfigValue","net/neoforged/neoforge/common/ModConfigSpec$ConfigValue",
            SPEC+"$ValueSpec","net/neoforged/neoforge/common/ModConfigSpec$ValueSpec",SPEC+"$Range","net/neoforged/neoforge/common/ModConfigSpec$Range",
            EVENT,"net/neoforged/fml/event/config/ModConfigEvent");
    static String relocation(String owner){return TYPES.get(owner);}
    static String descriptor(String desc){for(var e:TYPES.entrySet())desc=desc.replace("L"+e.getKey()+";","L"+e.getValue()+";").replace("L"+e.getKey()+"<","L"+e.getValue()+"<");return desc;}
    static int rewrite(ClassNode node){
        if(node.interfaces.contains(EVENT))throw new UnsupportedOperationException("Source custom IConfigEvent implementations are not adapted: "+node.name);
        int changed=0;var helpers=new ArrayList<MethodNode>();
        for(var method:List.copyOf(node.methods))for(var instruction:method.instructions.toArray()) {
            if(instruction instanceof MethodInsnNode call) {
                if(Set.of(MOD_EVENT+"$Loading",MOD_EVENT+"$Reloading",MOD_EVENT+"$Unloading").contains(call.owner)
                        &&call.name.equals("<init>")&&call.desc.equals("(L"+CONFIG+";)V")) {
                    method.instructions.insertBefore(call,new MethodInsnNode(Opcodes.INVOKESTATIC,BRIDGE,"unwrap","(L"+CONFIG+";)Lnet/neoforged/fml/config/ModConfig;",false));
                    call.desc="(Lnet/neoforged/fml/config/ModConfig;)V";changed++;continue;
                }
                int tag=switch(call.getOpcode()){case Opcodes.INVOKESTATIC->Opcodes.H_INVOKESTATIC;case Opcodes.INVOKEVIRTUAL->Opcodes.H_INVOKEVIRTUAL;case Opcodes.INVOKEINTERFACE->Opcodes.H_INVOKEINTERFACE;default->-1;};if(tag<0)continue;
                var original=new Handle(tag,call.owner,call.name,call.desc,call.itf);var next=handle(original);
                if(next.equals(original))continue;call.setOpcode(Opcodes.INVOKESTATIC);call.owner=next.getOwner();call.name=next.getName();call.desc=next.getDesc();call.itf=false;changed++;
            } else if(instruction instanceof LdcInsnNode ldc) {
                if(ldc.cst instanceof String name) {
                    var next=instruction.getNext();while(next!=null&&next.getOpcode()<0)next=next.getNext();
                    String target=relocation(name.replace('.','/'));
                    if(target!=null&&next instanceof MethodInsnNode call&&call.owner.equals("java/lang/Class")&&call.name.equals("forName")&&call.desc.equals("(Ljava/lang/String;)Ljava/lang/Class;")){ldc.cst=target.replace('/','.');changed++;}
                }else {Object next=constant(node,ldc.cst,helpers);if(next!=ldc.cst){ldc.cst=next;changed++;}}
            } else if(instruction instanceof InvokeDynamicInsnNode indy)for(int i=0;i<indy.bsmArgs.length;i++){Object next=constant(node,indy.bsmArgs[i],helpers);if(next!=indy.bsmArgs[i]){indy.bsmArgs[i]=next;changed++;}}
        }
        node.methods.addAll(helpers);return changed;
    }
    private static Handle handle(Handle source){
        if(source.getTag()==Opcodes.H_NEWINVOKESPECIAL&&source.getName().equals("<init>")&&source.getDesc().equals("(L"+CONFIG+";)V")) {
            String kind=source.getOwner().startsWith(MOD_EVENT+"$")?source.getOwner().substring(MOD_EVENT.length()+1):"";
            if(Set.of("Loading","Reloading","Unloading").contains(kind))return bridge("new"+kind,"(L"+CONFIG+";)L"+source.getOwner()+";");
        }
        boolean instance=source.getTag()==Opcodes.H_INVOKEVIRTUAL||source.getTag()==Opcodes.H_INVOKEINTERFACE;
        String name=source.getName(),desc=source.getDesc(),owner=source.getOwner();
        if(instance&&owner.equals(SOURCE+"fml/config/ConfigTracker")&&name.equals("configSets")&&desc.equals("()Ljava/util/Map;"))return bridge(name,"(L"+owner+";)Ljava/util/Map;");
        if(instance&&owner.equals(SPEC)&&name.equals("get")&&desc.equals("(Ljava/util/List;)Ljava/lang/Object;"))return bridge(name,"(L"+SPEC+";Ljava/util/List;)Ljava/lang/Object;");
        if(instance&&owner.equals(SPEC+"$ValueSpec")&&name.equals("needsWorldRestart")&&desc.equals("()Z"))return bridge(name,"(L"+owner+";)Z");
        if(instance&&owner.equals(SOURCE+"fml/config/IConfigSpec")) {
            if(name.equals("afterReload")&&desc.equals("()V"))return bridge(name,"(L"+owner+";)V");
            if(name.equals("correct")&&desc.equals("(Lcom/electronwill/nightconfig/core/CommentedConfig;)I"))return bridge(name,"(L"+owner+";"+desc.substring(1));
        }
        if(instance&&(owner.equals(EVENT)||owner.equals(MOD_EVENT)||owner.startsWith(MOD_EVENT+"$"))) {
            if(name.equals("getConfig")&&desc.equals("()L"+CONFIG+";"))return bridge(name,"(Lnet/neoforged/fml/event/config/ModConfigEvent;)L"+CONFIG+";");
            if(name.equals("self")&&desc.equals("()L"+SOURCE+"eventbus/api/Event;"))return bridge(name,"(Lnet/neoforged/fml/event/config/ModConfigEvent;)Lnet/neoforged/bus/api/Event;");
        }
        if(source.getTag()==Opcodes.H_INVOKESTATIC&&owner.equals(EVENT)&&Set.of("reloading","loading").contains(name)&&desc.equals("(L"+CONFIG+";)L"+EVENT+";"))return bridge(name,desc);
        return source;
    }
    private static Handle bridge(String name,String desc){return new Handle(Opcodes.H_INVOKESTATIC,BRIDGE,name,desc,false);}
    private static Object constant(ClassNode node,Object value,List<MethodNode> helpers){
        if(value instanceof Handle source){
            var target=handle(source);
            if(target.equals(source)||source.getTag()!=Opcodes.H_INVOKEVIRTUAL&&source.getTag()!=Opcodes.H_INVOKEINTERFACE)return target;
            String desc="(L"+source.getOwner()+";"+source.getDesc().substring(1),name="lms$configHandle$"+helpers.size();
            while(true){String candidate=name;if(node.methods.stream().noneMatch(m->m.name.equals(candidate))&&helpers.stream().noneMatch(m->m.name.equals(candidate)))break;name+="$";}
            var method=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC|Opcodes.ACC_SYNTHETIC,name,desc,null,null);
            int local=0;for(Type type:Type.getArgumentTypes(desc)){method.instructions.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD),local));local+=type.getSize();}
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,target.getOwner(),target.getName(),target.getDesc(),false));method.instructions.add(new InsnNode(Type.getReturnType(desc).getOpcode(Opcodes.IRETURN)));method.maxLocals=local;method.maxStack=local;helpers.add(method);
            return new Handle(Opcodes.H_INVOKESTATIC,node.name,name,desc,(node.access&Opcodes.ACC_INTERFACE)!=0);
        }
        if(value instanceof ConstantDynamic dynamic){Object[] args=new Object[dynamic.getBootstrapMethodArgumentCount()];boolean changed=false;for(int i=0;i<args.length;i++){Object old=dynamic.getBootstrapMethodArgument(i);args[i]=constant(node,old,helpers);changed|=args[i]!=old;}return changed?new ConstantDynamic(dynamic.getName(),dynamic.getDescriptor(),dynamic.getBootstrapMethod(),args):value;}
        return value;
    }
}
