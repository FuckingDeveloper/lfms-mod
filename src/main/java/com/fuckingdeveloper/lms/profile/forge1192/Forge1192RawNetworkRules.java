package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.transform.MethodRuleCatalog;
import java.util.*;
import org.objectweb.asm.*;

/** Shared source identities for raw-channel admission, projection and listener adaptation. */
public final class Forge1192RawNetworkRules {
    public static final String CHANNEL="net/minecraftforge/network/event/EventNetworkChannel";
    public static final String EVENT="net/minecraftforge/network/NetworkEvent";
    public static final String TARGET_CHANNEL="com/fuckingdeveloper/lms/profile/forge1192/runtime/network/Forge1192RawNetworkBridge$LegacyEventChannel";
    public static final String TARGET_EVENT="com/fuckingdeveloper/lms/profile/forge1192/runtime/network/LegacyNetworkEvent";
    private Forge1192RawNetworkRules() {}
    public static String type(String name) {
        if(name.equals(CHANNEL))return TARGET_CHANNEL;
        if(name.equals(EVENT)||name.equals(EVENT+"$ClientCustomPayloadEvent")||name.equals(EVENT+"$ServerCustomPayloadEvent"))return name.replace(EVENT,TARGET_EVENT);
        return null;
    }
    public static boolean listener(int opcode,String owner,String name,String descriptor){return opcode==Opcodes.INVOKEVIRTUAL&&owner.equals(CHANNEL)&&name.equals("addListener")&&descriptor.equals("(Ljava/util/function/Consumer;)V");}
    public static List<MethodRuleCatalog.Rule> entries() {
        var result=new ArrayList<MethodRuleCatalog.Rule>();
        String factory="(Lnet/minecraft/resources/ResourceLocation;Ljava/util/function/Supplier;Ljava/util/function/Predicate;Ljava/util/function/Predicate;)L"+CHANNEL+";";
        result.add(rule("new", "net/minecraftforge/network/NetworkRegistry","newEventChannel",factory,Opcodes.INVOKESTATIC,
                new Handle(Opcodes.H_INVOKESTATIC,"com/fuckingdeveloper/lms/profile/forge1192/runtime/network/Forge1192NetworkBridge","newEventChannel",factory.replace("net/minecraft/resources/ResourceLocation","net/minecraft/resources/Identifier").replace(CHANNEL,TARGET_CHANNEL),false)));
        result.add(rule("listener",CHANNEL,"addListener","(Ljava/util/function/Consumer;)V",Opcodes.INVOKEVIRTUAL,new Handle(Opcodes.H_INVOKEVIRTUAL,TARGET_CHANNEL,"addListener","(Ljava/lang/Class;Ljava/util/function/Consumer;)V",false)));
        for(String member:List.of("registerObject","unregisterObject"))result.add(rule(member,CHANNEL,member,"(Ljava/lang/Object;)V",Opcodes.INVOKEVIRTUAL,new Handle(Opcodes.H_INVOKEVIRTUAL,TARGET_CHANNEL,member,"(Ljava/lang/Object;)V",false)));
        result.add(rule("presence",CHANNEL,"isRemotePresent","(Lnet/minecraft/network/Connection;)Z",Opcodes.INVOKEVIRTUAL,new Handle(Opcodes.H_INVOKEVIRTUAL,TARGET_CHANNEL,"isRemotePresent","(Lnet/minecraft/network/Connection;)Z",false)));
        for(var entry:Map.of("getPayload","()Lnet/minecraft/network/FriendlyByteBuf;","getSource","()Ljava/util/function/Supplier;","getLoginIndex","()I").entrySet())
            result.add(rule(entry.getKey(),EVENT,entry.getKey(),entry.getValue(),Opcodes.INVOKEVIRTUAL,new Handle(Opcodes.H_INVOKEVIRTUAL,TARGET_EVENT,entry.getKey(),entry.getValue(),false)));
        return List.copyOf(result);
    }
    private static MethodRuleCatalog.Rule rule(String id,String owner,String name,String desc,int opcode,Handle target) {
        return new MethodRuleCatalog.Rule("raw-channel-"+id,"RAW_EVENT_CHANNEL",new MethodRuleCatalog.Member(owner,name,desc),opcode,target,"Pinned Forge43.4.0 NetworkEvent/EventNetworkChannel; native26.3 raw play payload, network-thread delivery and scoped source event bus");
    }
}
