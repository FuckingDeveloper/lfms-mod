package com.fuckingdeveloper.lms.profile;

import java.util.Map;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Exact additional source bootstrap shapes; no integration-fixture selectors. */
final class Forge1192BootstrapRewriter {
    private static final String NETWORK="com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge";
    private static final String DEFERRED="com/fuckingdeveloper/lms/runtime/registry/LegacyDeferredRegister";
    private static final Map<String,String> TYPES=Map.of(
            "net/minecraftforge/network/NetworkRegistry$ChannelBuilder",NETWORK+"$LegacyChannelBuilder",
            "net/minecraftforge/network/NetworkDirection",NETWORK+"$LegacyNetworkDirection",
            "net/minecraftforge/registries/DeferredRegister",DEFERRED,
            "net/minecraftforge/data/event/GatherDataEvent","com/fuckingdeveloper/lms/runtime/event/LegacyDataGenerationEvent");
    static String relocation(String type){return TYPES.get(type);}
    static String descriptor(String descriptor){for(var e:TYPES.entrySet())descriptor=descriptor.replace(e.getKey(),e.getValue());return descriptor;}
    static Handle handle(Handle original){
        if(original.getTag()==Opcodes.H_NEWINVOKESPECIAL&&original.getOwner().equals("net/minecraft/world/item/Item$Properties")&&original.getDesc().equals("()V"))
            return new Handle(Opcodes.H_INVOKESTATIC,DEFERRED,"itemProperties","()Lnet/minecraft/world/item/Item$Properties;",false);
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
