package com.fuckingdeveloper.lms.profile;

import java.util.Map;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Exact additional source bootstrap shapes; no integration-fixture selectors. */
final class Forge1192BootstrapRewriter {
    private static final String NETWORK="com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge";
    private static final String DEFERRED="com/fuckingdeveloper/lms/runtime/registry/LegacyDeferredRegister";
    private static final Map<String,String> TYPES=Map.of(
            "net/minecraft/world/level/block/entity/BlockEntityType$Builder","com/fuckingdeveloper/lms/runtime/LegacyBlockEntityTypeBuilder",
            "net/minecraftforge/fml/ModContainer","com/fuckingdeveloper/lms/runtime/Forge1192ModContainerView",
            "net/minecraftforge/fml/ModList","com/fuckingdeveloper/lms/runtime/Forge1192ModListView",
            "net/minecraftforge/fml/mclanguageprovider/MinecraftModContainer","com/fuckingdeveloper/lms/runtime/Forge1192ModContainerView$MinecraftContainer",
            "net/minecraftforge/network/NetworkRegistry$ChannelBuilder",NETWORK+"$LegacyChannelBuilder",
            "net/minecraftforge/network/NetworkDirection",NETWORK+"$LegacyNetworkDirection",
            "net/minecraftforge/registries/DeferredRegister",DEFERRED,
            "net/minecraftforge/data/event/GatherDataEvent","com/fuckingdeveloper/lms/runtime/event/LegacyDataGenerationEvent");
    static String relocation(String type){return TYPES.get(type);}
    static String descriptor(String descriptor){for(var e:TYPES.entrySet())descriptor=descriptor.replace("L"+e.getKey()+";","L"+e.getValue()+";");return descriptor;}
    static Handle handle(Handle original){
        if(original.getTag()==Opcodes.H_NEWINVOKESPECIAL&&original.getOwner().equals("net/minecraft/world/item/Item$Properties")&&original.getDesc().equals("()V"))
            return new Handle(Opcodes.H_INVOKESTATIC,DEFERRED,"itemProperties","()Lnet/minecraft/world/item/Item$Properties;",false);
        if(original.getTag()==Opcodes.H_NEWINVOKESPECIAL&&original.getOwner().equals("net/minecraft/world/inventory/MenuType")
                &&original.getDesc().equals("(Lnet/minecraft/world/inventory/MenuType$MenuSupplier;)V"))
            return new Handle(Opcodes.H_INVOKESTATIC,"com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge","menuType","(Lnet/minecraft/world/inventory/MenuType$MenuSupplier;)Lnet/minecraft/world/inventory/MenuType;",false);
        int opcode=switch(original.getTag()){case Opcodes.H_INVOKESTATIC->Opcodes.INVOKESTATIC;case Opcodes.H_INVOKEVIRTUAL->Opcodes.INVOKEVIRTUAL;case Opcodes.H_INVOKEINTERFACE->Opcodes.INVOKEINTERFACE;default->-1;};
        if(opcode<0)return original;
        var node=new ClassNode();var method=new MethodNode();node.methods.add(method);
        var call=new MethodInsnNode(opcode,original.getOwner(),original.getName(),original.getDesc(),original.isInterface());method.instructions.add(call);
        rewrite(node);
        if(call.owner.equals(original.getOwner())&&call.name.equals(original.getName())&&call.desc.equals(original.getDesc())&&call.getOpcode()==opcode)return original;
        int kind=call.getOpcode()==Opcodes.INVOKESTATIC?Opcodes.H_INVOKESTATIC:original.getTag();
        return new Handle(kind,call.owner,call.name,call.desc,call.itf);
    }
    private static AbstractInsnNode real(AbstractInsnNode n){while(n!=null&&n.getOpcode()<0)n=n.getNext();return n;}
    /** Partition a straight-line declaration-only listener; other setup work is never moved. */
    static int menuSetupCallbacks(ClassNode node,java.util.function.Predicate<String> isScreen,
            java.util.function.Predicate<String> isBlockRenderer) {
        String source="(Lnet/minecraftforge/fml/event/lifecycle/FMLClientSetupEvent;)V";
        String target="(Lnet/neoforged/neoforge/client/event/RegisterMenuScreensEvent;)V";
        var additions=new java.util.ArrayList<MethodNode>();
        for(var method:java.util.List.copyOf(node.methods)) {
            if(!method.desc.equals(source)||!method.tryCatchBlocks.isEmpty())continue;
            var instructions=new java.util.ArrayList<AbstractInsnNode>();
            for(var instruction:method.instructions)if(instruction.getOpcode()>=0)instructions.add(instruction);
            if(instructions.isEmpty()||instructions.getLast().getOpcode()!=Opcodes.RETURN||(instructions.size()-1)%5!=0)continue;
            var menuSlices=new java.util.ArrayList<java.util.List<AbstractInsnNode>>();boolean safe=true;
            for(int i=0;i<instructions.size()-1;i+=5) {
                var part=instructions.subList(i,i+5);
                if(!(part.get(0) instanceof FieldInsnNode field)||field.getOpcode()!=Opcodes.GETSTATIC||!field.desc.equals("Lnet/minecraftforge/registries/RegistryObject;")
                        ||!(part.get(1) instanceof MethodInsnNode get)||get.getOpcode()!=Opcodes.INVOKEVIRTUAL||!get.owner.equals("net/minecraftforge/registries/RegistryObject")||!get.name.equals("get")||!get.desc.equals("()Ljava/lang/Object;")
                        ||!(part.get(2) instanceof TypeInsnNode cast)||cast.getOpcode()!=Opcodes.CHECKCAST
                        ||!(part.get(3) instanceof InvokeDynamicInsnNode factory)||!factory.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory")
                        ||!(part.get(4) instanceof MethodInsnNode register)) {safe=false;break;}
                boolean menu=isMenuRegistration(register)&&cast.desc.equals("net/minecraft/world/inventory/MenuType")
                        &&factory.desc.equals("()Lnet/minecraft/client/gui/screens/MenuScreens$ScreenConstructor;");
                boolean renderer=register.getOpcode()==Opcodes.INVOKESTATIC&&register.owner.equals("net/minecraft/client/renderer/blockentity/BlockEntityRenderers")
                        &&register.name.equals("m_173590_")&&register.desc.equals("(Lnet/minecraft/world/level/block/entity/BlockEntityType;Lnet/minecraft/client/renderer/blockentity/BlockEntityRendererProvider;)V")
                        &&cast.desc.equals("net/minecraft/world/level/block/entity/BlockEntityType")
                        &&factory.desc.equals("()Lnet/minecraft/client/renderer/blockentity/BlockEntityRendererProvider;");
                boolean constructor=false;
                for(var arg:factory.bsmArgs)if(arg instanceof Handle handle)constructor=handle.getTag()==Opcodes.H_NEWINVOKESPECIAL
                        &&(menu?isScreen.test(handle.getOwner()):renderer&&isBlockRenderer.test(handle.getOwner()));
                if((!menu&&!renderer)||!constructor){safe=false;break;}
                if(menu)menuSlices.add(java.util.List.copyOf(part));
            }
            if(!safe||menuSlices.isEmpty())continue;
            var sites=new java.util.ArrayList<MethodInsnNode>();
            for(var caller:node.methods)for(var instruction:caller.instructions)if(instruction instanceof MethodInsnNode call
                    &&call.owner.equals("net/minecraftforge/eventbus/api/IEventBus")&&call.name.equals("addListener")&&call.desc.equals("(Ljava/util/function/Consumer;)V")
                    &&realBefore(call) instanceof InvokeDynamicInsnNode lambda&&lambda.bsm.getName().equals("metafactory")) {
                for(var arg:lambda.bsmArgs)if(arg instanceof Handle handle&&handle.getOwner().equals(node.name)&&handle.getName().equals(method.name)&&handle.getDesc().equals(source))sites.add(call);
            }
            if(sites.isEmpty())continue;
            String name="lms$menuRegistrations$"+additions.size();
            if(node.methods.stream().anyMatch(m->m.name.equals(name)))throw new IllegalStateException("Source menu callback name collision");
            var hook=new MethodNode(Opcodes.ACC_PRIVATE|Opcodes.ACC_STATIC|Opcodes.ACC_SYNTHETIC,name,target,null,null);
            for(var slice:menuSlices) {
                for(int i=0;i<4;i++)hook.instructions.add(slice.get(i).clone(new java.util.HashMap<>()));
                hook.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
                hook.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"com/fuckingdeveloper/lms/runtime/render/Forge1192MenuScreenBridge","registerDuringPhase",
                        "(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/client/gui/screens/MenuScreens$ScreenConstructor;Lnet/neoforged/neoforge/client/event/RegisterMenuScreensEvent;)V",false));
                for(var instruction:slice)method.instructions.remove(instruction);
            }
            hook.instructions.add(new InsnNode(Opcodes.RETURN));additions.add(hook);
            for(var site:sites)for(var caller:node.methods)if(caller.instructions.indexOf(site)>=0) {
                // Both operands are category-1 references. Retain the exact source bus once.
                caller.instructions.insertBefore(site,new InsnNode(Opcodes.DUP2));
                var code=new InsnList();code.add(new InsnNode(Opcodes.POP));
                code.add(new InvokeDynamicInsnNode("accept","()Ljava/util/function/Consumer;",
                        new Handle(Opcodes.H_INVOKESTATIC,"java/lang/invoke/LambdaMetafactory","metafactory","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",false),
                        Type.getMethodType("(Ljava/lang/Object;)V"),new Handle(Opcodes.H_INVOKESTATIC,node.name,name,target,false),Type.getMethodType(target)));
                code.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE,"net/minecraftforge/eventbus/api/IEventBus","addListener","(Ljava/util/function/Consumer;)V",true));
                caller.instructions.insert(site,code);break;
            }
        }
        node.methods.addAll(additions);return additions.size();
    }
    private static AbstractInsnNode realBefore(AbstractInsnNode node){node=node.getPrevious();while(node!=null&&node.getOpcode()<0)node=node.getPrevious();return node;}
    private static boolean isMenuRegistration(MethodInsnNode call) {
        return call.getOpcode()==Opcodes.INVOKESTATIC&&call.owner.equals("net/minecraft/client/gui/screens/MenuScreens")&&call.name.equals("m_96206_")
                &&call.desc.equals("(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/client/gui/screens/MenuScreens$ScreenConstructor;)V");
    }

    static int rewrite(ClassNode node){
        int changed=0;
        for(var method:node.methods) {
            for(var instruction=method.instructions.getFirst();instruction!=null;){
                var next=instruction.getNext();
                if(instruction instanceof TypeInsnNode allocation&&allocation.getOpcode()==Opcodes.NEW
                        &&allocation.desc.equals("net/minecraft/world/item/Item$Properties")){
                    var duplicate=real(allocation.getNext());var init=duplicate==null?null:real(duplicate.getNext());
                    if(duplicate!=null&&duplicate.getOpcode()==Opcodes.DUP&&init instanceof MethodInsnNode ctor
                            &&ctor.getOpcode()==Opcodes.INVOKESPECIAL&&ctor.owner.equals(allocation.desc)&&ctor.name.equals("<init>")&&ctor.desc.equals("()V")){
                        method.instructions.remove(allocation);method.instructions.remove(duplicate);
                        ctor.setOpcode(Opcodes.INVOKESTATIC);ctor.owner=DEFERRED;ctor.name="itemProperties";ctor.desc="()Lnet/minecraft/world/item/Item$Properties;";ctor.itf=false;
                        changed++;next=ctor.getNext();
                    }
                }
                instruction=next;
            }
        }
        for(var method:node.methods)for(var instruction=method.instructions.getFirst();instruction!=null;){
            var next=instruction.getNext();
            if(instruction instanceof FieldInsnNode field&&field.getOpcode()==Opcodes.GETSTATIC
                    &&field.owner.equals("com/mojang/blaze3d/platform/InputConstants$Type")&&field.name.equals("SCANCODE")){
                method.instructions.set(field,new MethodInsnNode(Opcodes.INVOKESTATIC,"com/fuckingdeveloper/lms/runtime/render/Forge1192KeyboardBridge","rawScanType","()Lcom/mojang/blaze3d/platform/InputConstants$Type;",false));changed++;
            }
            if(!(instruction instanceof MethodInsnNode call)){instruction=next;continue;}
            if(call.owner.equals("net/minecraft/client/gui/screens/MenuScreens")&&call.getOpcode()==Opcodes.INVOKESTATIC
                    &&call.name.equals("m_96206_")&&call.desc.equals("(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/client/gui/screens/MenuScreens$ScreenConstructor;)V")) {
                call.owner="com/fuckingdeveloper/lms/runtime/render/Forge1192MenuScreenBridge";call.name="register";call.itf=false;changed++;
            }
            if(call.owner.equals("net/minecraft/world/inventory/MenuType")&&call.getOpcode()==Opcodes.INVOKESPECIAL
                    &&call.name.equals("<init>")&&call.desc.equals("(Lnet/minecraft/world/inventory/MenuType$MenuSupplier;)V")) {
                method.instructions.insertBefore(call,new FieldInsnNode(Opcodes.GETSTATIC,"net/minecraft/world/flag/FeatureFlags","VANILLA_SET","Lnet/minecraft/world/flag/FeatureFlagSet;"));
                call.desc="(Lnet/minecraft/world/inventory/MenuType$MenuSupplier;Lnet/minecraft/world/flag/FeatureFlagSet;)V";changed++;
            }
            if(call.owner.equals("net/minecraftforge/client/event/InputEvent$Key")
                    &&call.getOpcode()==Opcodes.INVOKEVIRTUAL&&call.desc.equals("()I")
                    &&java.util.Set.of("getKey","getScanCode","getAction","getModifiers").contains(call.name)){
                call.setOpcode(Opcodes.INVOKESTATIC);call.owner="com/fuckingdeveloper/lms/runtime/render/Forge1192KeyboardBridge";call.desc="(Lnet/minecraftforge/client/event/InputEvent$Key;)I";call.itf=false;changed++;
            } else if(call.owner.equals("net/minecraft/client/KeyMapping")&&call.getOpcode()==Opcodes.INVOKEVIRTUAL
                    &&((call.name.equals("m_90832_")&&call.desc.equals("(II)Z"))||(call.name.equals("m_90830_")&&call.desc.equals("(I)Z")))){
                call.name=call.name.equals("m_90832_")?"matches":"matchesMouse";call.setOpcode(Opcodes.INVOKESTATIC);call.owner="com/fuckingdeveloper/lms/runtime/render/Forge1192KeyboardBridge";call.desc="(Lnet/minecraft/client/KeyMapping;"+call.desc.substring(1);call.itf=false;changed++;
            } else if(call.owner.equals("com/mojang/blaze3d/platform/InputConstants$Type")&&call.name.equals("m_84895_")&&call.desc.equals("(I)Lcom/mojang/blaze3d/platform/InputConstants$Key;")){
                call.setOpcode(Opcodes.INVOKESTATIC);call.owner="com/fuckingdeveloper/lms/runtime/render/Forge1192KeyboardBridge";call.name="key";call.desc="(Lcom/mojang/blaze3d/platform/InputConstants$Type;I)Lcom/mojang/blaze3d/platform/InputConstants$Key;";call.itf=false;changed++;
            } else if(call.owner.equals("com/mojang/blaze3d/platform/InputConstants$Key")&&call.name.equals("m_84873_")&&call.desc.equals("()I")){
                call.setOpcode(Opcodes.INVOKESTATIC);call.owner="com/fuckingdeveloper/lms/runtime/render/Forge1192KeyboardBridge";call.name="sourceValue";call.desc="(Lcom/mojang/blaze3d/platform/InputConstants$Key;)I";call.itf=false;changed++;
            } else if(call.owner.equals("net/minecraftforge/fml/util/ObfuscationReflectionHelper")&&call.name.equals("getPrivateValue")&&call.desc.equals("(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;")){
                call.owner="com/fuckingdeveloper/lms/runtime/Forge1192LoaderReflectionBridge";call.itf=false;changed++;
            }
            if(call.owner.equals("net/minecraftforge/registries/DeferredRegister")&&call.name.equals("create")
                    &&call.getOpcode()==Opcodes.INVOKESTATIC
                    &&call.desc.equals("(Lnet/minecraftforge/registries/IForgeRegistry;Ljava/lang/String;)Lnet/minecraftforge/registries/DeferredRegister;")){
                call.owner=DEFERRED;call.desc="(Ljava/lang/Object;Ljava/lang/String;)L"+DEFERRED+";";call.itf=false;changed++;
            } else if(call.owner.equals("net/minecraftforge/network/simple/SimpleChannel")
                    &&call.getOpcode()==Opcodes.INVOKEVIRTUAL
                    &&((call.name.equals("sendToServer")&&call.desc.equals("(Ljava/lang/Object;)V"))
                    ||(call.name.equals("sendTo")&&call.desc.equals("(Ljava/lang/Object;Lnet/minecraft/network/Connection;Lnet/minecraftforge/network/NetworkDirection;)V")))){
                call.setOpcode(Opcodes.INVOKESTATIC);call.owner=NETWORK;call.desc="(Ljava/lang/Object;"+call.desc.substring(1);call.itf=false;changed++;
            } else if(call.owner.equals("net/minecraftforge/network/simple/SimpleChannel")&&call.name.equals("registerMessage")
                    &&call.getOpcode()==Opcodes.INVOKEVIRTUAL&&call.desc.equals("(ILjava/lang/Class;Ljava/util/function/BiConsumer;Ljava/util/function/Function;Ljava/util/function/BiConsumer;Ljava/util/Optional;)Lnet/minecraftforge/network/simple/IndexedMessageCodec$MessageHandler;")){
                call.setOpcode(Opcodes.INVOKESTATIC);call.owner=NETWORK;call.desc="(Ljava/lang/Object;ILjava/lang/Class;Ljava/util/function/BiConsumer;Ljava/util/function/Function;Ljava/util/function/BiConsumer;Ljava/util/Optional;)Ljava/lang/Object;";call.itf=false;changed++;
            }
            instruction=next;
        }
        return changed;
    }
}
