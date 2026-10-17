package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Source/native config ownership, getter and event contracts; no fixture selectors. */
final class Forge1192NativeConfigRewriter {
    private static final String SOURCE="net/minecraftforge/",CONFIG=SOURCE+"fml/config/ModConfig",SPEC=SOURCE+"common/ForgeConfigSpec",
            EVENT=SOURCE+"fml/config/IConfigEvent",MOD_EVENT=SOURCE+"fml/event/config/ModConfigEvent",
            BRIDGE="com/fuckingdeveloper/lms/profile/forge1192/runtime/lifecycle/Forge1192NativeConfigBridge";
    static final Set<String> SPEC_SUFFIXES=Set.of("","$Builder","$ConfigValue","$BooleanValue","$IntValue","$LongValue","$DoubleValue","$EnumValue","$ValueSpec","$Range");
    private static final Map<String,String> TYPES=Map.of(
            CONFIG,"com/fuckingdeveloper/lms/profile/forge1192/runtime/lifecycle/Forge1192ModConfigView",CONFIG+"$Type","com/fuckingdeveloper/lms/profile/forge1192/runtime/lifecycle/Forge1192ModConfigView$Type",
            SPEC,"net/neoforged/neoforge/common/ModConfigSpec",SPEC+"$ConfigValue","net/neoforged/neoforge/common/ModConfigSpec$ConfigValue",
            SPEC+"$ValueSpec","net/neoforged/neoforge/common/ModConfigSpec$ValueSpec",SPEC+"$Range","net/neoforged/neoforge/common/ModConfigSpec$Range",
            EVENT,"net/neoforged/fml/event/config/ModConfigEvent");
    static String relocation(String owner){
        if(owner.equals("net/minecraftforge/fml/ModLoadingContext"))return "com/fuckingdeveloper/lms/profile/forge1192/runtime/lifecycle/Forge1192ModLoadingContext";
        if(owner.equals("net/minecraftforge/fml/javafmlmod/FMLJavaModLoadingContext"))return "com/fuckingdeveloper/lms/profile/forge1192/runtime/lifecycle/Forge1192JavaModLoadingContext";
        if(owner.startsWith(SPEC) && SPEC_SUFFIXES.contains(owner.substring(SPEC.length())))
            return com.fuckingdeveloper.lms.target.InstalledTargetAdapters.configSpecOwner()+owner.substring(SPEC.length());
        return TYPES.get(owner);
    }
    static String descriptor(String desc){
        for(String owner:java.util.List.of(CONFIG,CONFIG+"$Type",EVENT,"net/minecraftforge/fml/ModLoadingContext","net/minecraftforge/fml/javafmlmod/FMLJavaModLoadingContext"))
            desc=desc.replace("L"+owner+";","L"+relocation(owner)+";").replace("L"+owner+"<","L"+relocation(owner)+"<");
        desc=eraseSpecArguments(desc);
        desc=desc.replace("net/minecraftforge/fml/IExtensionPoint", "com/fuckingdeveloper/lms/profile/forge1192/runtime/lifecycle/LegacyExtensionPoint");
        desc=desc.replace("Lnet/minecraftforge/fml/config/IConfigSpec;","Lnet/neoforged/fml/config/IConfigSpec;")
                .replace("Lnet/minecraftforge/fml/ModContainer;","Lcom/fuckingdeveloper/lms/profile/forge1192/runtime/lifecycle/Forge1192ModContainerView;")
                .replace("Lnet/minecraftforge/eventbus/api/IEventBus;","Lnet/neoforged/bus/api/IEventBus;");
        for(String suffix:SPEC_SUFFIXES)desc=desc.replace("L"+SPEC+suffix+";","L"+relocation(SPEC+suffix)+";")
                .replace("L"+SPEC+suffix+"<","L"+relocation(SPEC+suffix)+"<");
        return desc;
    }
    private static String eraseSpecArguments(String signature) {
        String prefix="Lnet/minecraftforge/fml/config/IConfigSpec<";
        int start;
        while((start=signature.indexOf(prefix))>=0) {
            int depth=1,end=start+prefix.length();
            while(end<signature.length() && depth>0){char c=signature.charAt(end++);if(c=='<')depth++;else if(c=='>')depth--;}
            if(depth!=0)throw new IllegalArgumentException("Malformed source config signature: "+signature);
            signature=signature.substring(0,start)+prefix.substring(0,prefix.length()-1)+signature.substring(end);
        }
        return signature;
    }
    static int rewrite(ClassNode node, Forge1192MethodDeclarations declarations){
        if(node.interfaces.contains(EVENT))throw new UnsupportedOperationException("Source custom IConfigEvent implementations are not adapted: "+node.name);
        if(node.superName!=null && (node.superName.equals(SPEC) || node.superName.startsWith(SPEC+"$"))
                || node.interfaces.contains(SOURCE+"fml/config/IConfigSpec"))
            throw new UnsupportedOperationException("Source custom configuration implementations are not adapted: "+node.name);
        int changed=0;var helpers=new ArrayList<MethodNode>();
        for(var method:List.copyOf(node.methods))for(var instruction:method.instructions.toArray()) {
            if(instruction instanceof MethodInsnNode call) {
                if(Set.of(MOD_EVENT+"$Loading",MOD_EVENT+"$Reloading",MOD_EVENT+"$Unloading").contains(call.owner)
                        &&call.name.equals("<init>")&&call.desc.equals("(L"+CONFIG+";)V")) {
                    method.instructions.insertBefore(call,new MethodInsnNode(Opcodes.INVOKESTATIC,BRIDGE,"unwrap","(L"+CONFIG+";)Lnet/neoforged/fml/config/ModConfig;",false));
                    call.desc="(Lnet/neoforged/fml/config/ModConfig;)V";changed++;continue;
                }
                int tag=switch(call.getOpcode()){case Opcodes.INVOKESTATIC->Opcodes.H_INVOKESTATIC;case Opcodes.INVOKEVIRTUAL->Opcodes.H_INVOKEVIRTUAL;case Opcodes.INVOKEINTERFACE->Opcodes.H_INVOKEINTERFACE;case Opcodes.INVOKESPECIAL->call.name.equals("<init>")?Opcodes.H_NEWINVOKESPECIAL:Opcodes.H_INVOKESPECIAL;default->-1;};if(tag<0)continue;
                var original=new Handle(tag,call.owner,call.name,call.desc,call.itf);var next=handle(original,declarations);
                if(next.equals(original))continue;call.setOpcode(opcode(next));call.owner=next.getOwner();call.name=next.getName();call.desc=next.getDesc();call.itf=false;changed++;
            } else if(instruction instanceof LdcInsnNode ldc) {
                if(ldc.cst instanceof String name) {
                    var next=instruction.getNext();while(next!=null&&next.getOpcode()<0)next=next.getNext();
                    String target=relocation(name.replace('.','/'));
                    if(target!=null&&next instanceof MethodInsnNode call&&call.owner.equals("java/lang/Class")&&call.name.equals("forName")&&call.desc.equals("(Ljava/lang/String;)Ljava/lang/Class;")){ldc.cst=target.replace('/','.');changed++;}
                }else {Object next=constant(node,ldc.cst,helpers,declarations);if(next!=ldc.cst){ldc.cst=next;changed++;}}
            } else if(instruction instanceof InvokeDynamicInsnNode indy)for(int i=0;i<indy.bsmArgs.length;i++){Object next=constant(node,indy.bsmArgs[i],helpers,declarations);if(next!=indy.bsmArgs[i]){indy.bsmArgs[i]=next;changed++;}}
        }
        node.methods.addAll(helpers);return changed;
    }
    private static int opcode(Handle handle){return switch(handle.getTag()){case Opcodes.H_INVOKESTATIC->Opcodes.INVOKESTATIC;case Opcodes.H_NEWINVOKESPECIAL,Opcodes.H_INVOKESPECIAL->Opcodes.INVOKESPECIAL;case Opcodes.H_INVOKEINTERFACE->Opcodes.INVOKEINTERFACE;default->Opcodes.INVOKEVIRTUAL;};}
    private static Handle handle(Handle source, Forge1192MethodDeclarations declarations){
        var rule=Forge1192MethodRules.catalog().resolve(opcode(source),source.getOwner(),source.getName(),source.getDesc(),declarations::owner);
        if(rule.isPresent()&&rule.get().family().equals("SOURCE_CONFIG"))return rule.get().bridge();
        if(Forge1192MethodRules.family(source.getOwner(),source.getName(),source.getDesc(),declarations).filter("SOURCE_CONFIG"::equals).isPresent())
            throw new IllegalStateException("Unadapted source config invocation shape: "+source);
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
        if(owner.equals(SPEC+"$Builder") || Set.of(SPEC+"$ConfigValue",SPEC+"$BooleanValue",SPEC+"$IntValue",SPEC+"$LongValue",SPEC+"$DoubleValue",SPEC+"$EnumValue").contains(owner))
            throw new UnsupportedOperationException("Unproven source configuration operation: "+owner+"#"+name+desc);
        return source;
    }
    private static Handle bridge(String name,String desc){return new Handle(Opcodes.H_INVOKESTATIC,BRIDGE,name,desc,false);}
    private static Object constant(ClassNode node,Object value,List<MethodNode> helpers, Forge1192MethodDeclarations declarations){
        if(value instanceof Handle source){
            var target=handle(source,declarations);
            if(target.equals(source)||target.getTag()!=Opcodes.H_INVOKESTATIC||source.getTag()!=Opcodes.H_INVOKEVIRTUAL&&source.getTag()!=Opcodes.H_INVOKEINTERFACE)return target;
            String desc="(L"+source.getOwner()+";"+source.getDesc().substring(1),name="lms$configHandle$"+helpers.size();
            while(true){String candidate=name;if(node.methods.stream().noneMatch(m->m.name.equals(candidate))&&helpers.stream().noneMatch(m->m.name.equals(candidate)))break;name+="$";}
            var method=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC|Opcodes.ACC_SYNTHETIC,name,desc,null,null);
            int local=0;for(Type type:Type.getArgumentTypes(desc)){method.instructions.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD),local));local+=type.getSize();}
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,target.getOwner(),target.getName(),target.getDesc(),false));method.instructions.add(new InsnNode(Type.getReturnType(desc).getOpcode(Opcodes.IRETURN)));method.maxLocals=local;method.maxStack=local;helpers.add(method);
            return new Handle(Opcodes.H_INVOKESTATIC,node.name,name,desc,(node.access&Opcodes.ACC_INTERFACE)!=0);
        }
        if(value instanceof ConstantDynamic dynamic){Object[] args=new Object[dynamic.getBootstrapMethodArgumentCount()];boolean changed=false;for(int i=0;i<args.length;i++){Object old=dynamic.getBootstrapMethodArgument(i);args[i]=constant(node,old,helpers,declarations);changed|=args[i]!=old;}return changed?new ConstantDynamic(dynamic.getName(),dynamic.getDescriptor(),dynamic.getBootstrapMethod(),args):value;}
        return value;
    }
}
