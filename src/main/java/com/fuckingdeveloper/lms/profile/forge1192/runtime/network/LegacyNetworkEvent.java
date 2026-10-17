package com.fuckingdeveloper.lms.profile.forge1192.runtime.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.bus.api.Event;

/** Actual source event shape; play class names identify the packet's origin. */
public class LegacyNetworkEvent extends Event {
    private final FriendlyByteBuf payload;
    private final Supplier<Forge1192NetworkBridge.LegacyContext> source;
    private final int loginIndex;
    public LegacyNetworkEvent(Supplier<Forge1192NetworkBridge.LegacyContext> source){this(null,source,-1);}
    private LegacyNetworkEvent(FriendlyByteBuf payload,Supplier<Forge1192NetworkBridge.LegacyContext> source,int index){this.payload=payload;this.source=source;loginIndex=index;}
    public FriendlyByteBuf getPayload(){return payload;}
    public Supplier<Forge1192NetworkBridge.LegacyContext> getSource(){return source;}
    public int getLoginIndex(){return loginIndex;}
    public static class ServerCustomPayloadEvent extends LegacyNetworkEvent {
        ServerCustomPayloadEvent(FriendlyByteBuf data,Supplier<Forge1192NetworkBridge.LegacyContext> source){super(data,source,Integer.MAX_VALUE);}
    }
    public static class ClientCustomPayloadEvent extends LegacyNetworkEvent {
        ClientCustomPayloadEvent(FriendlyByteBuf data,Supplier<Forge1192NetworkBridge.LegacyContext> source){super(data,source,Integer.MAX_VALUE);}
    }
}
