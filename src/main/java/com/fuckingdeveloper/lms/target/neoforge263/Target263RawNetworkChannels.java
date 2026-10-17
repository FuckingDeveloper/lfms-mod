package com.fuckingdeveloper.lms.target.neoforge263;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.network.Forge1192NetworkBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.network.Forge1192RawNetworkBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.network.Forge1192RawNetworkBridge.LegacyEventChannel;
import java.util.Objects;
import net.minecraft.network.*;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Configured target's raw play registration, codec, thread and connection contracts. */
public final class Target263RawNetworkChannels {
    private Target263RawNetworkChannels() {}
    private static final java.util.Map<LegacyEventChannel,CustomPacketPayload.Type<RawPayload>> TYPES=new java.util.concurrent.ConcurrentHashMap<>();
    public static boolean isRemotePresent(Connection connection,Identifier channel) {
        return net.neoforged.neoforge.network.registration.NetworkRegistry.hasChannel(Objects.requireNonNull(connection),ConnectionProtocol.PLAY,channel);
    }
    public static boolean register(Forge1192NetworkBridge.LegacyChannel channel,PayloadRegistrar registrar) {
        var raw=Forge1192RawNetworkBridge.find(channel);if(raw==null)return false;
        registrar.playBidirectional(type(raw),codec(raw),Target263RawNetworkChannels::handle,Target263RawNetworkChannels::handle);return true;
    }
    private static CustomPacketPayload.Type<RawPayload> type(LegacyEventChannel raw){return TYPES.computeIfAbsent(raw,key->new CustomPacketPayload.Type<>(key.name()));}
    public static final class RawPayload implements CustomPacketPayload {
        private final LegacyEventChannel channel;
        private final FriendlyByteBuf outgoing;
        private final byte[] incoming;
        private RawPayload(LegacyEventChannel channel,FriendlyByteBuf outgoing,byte[] incoming){this.channel=channel;this.outgoing=outgoing;this.incoming=incoming;}
        @Override public Type<RawPayload> type(){return Target263RawNetworkChannels.type(channel);}
        private byte[] bytes(){if(incoming!=null)return incoming;byte[] bytes=new byte[outgoing.readableBytes()];outgoing.getBytes(outgoing.readerIndex(),bytes);return bytes;}
    }
    /** Original source unsafe buffer remains live until native encoding. */
    public static RawPayload outgoing(LegacyEventChannel channel,FriendlyByteBuf buffer){Forge1192RawNetworkBridge.requireChannel(channel);return new RawPayload(channel,Objects.requireNonNull(buffer),null);}
    public static StreamCodec<RegistryFriendlyByteBuf,RawPayload> codec(LegacyEventChannel channel){
        Forge1192RawNetworkBridge.requireChannel(channel);
        return new StreamCodec<>() {
            public RawPayload decode(RegistryFriendlyByteBuf buffer){byte[] bytes=new byte[buffer.readableBytes()];buffer.readBytes(bytes);return new RawPayload(channel,null,bytes);}
            public void encode(RegistryFriendlyByteBuf buffer,RawPayload payload){
                if(payload.channel!=channel)throw new IllegalArgumentException("Raw payload/channel mismatch");
                if(payload.outgoing!=null)buffer.writeBytes(payload.outgoing,payload.outgoing.readerIndex(),payload.outgoing.readableBytes());else buffer.writeBytes(payload.incoming);
            }
        };
    }
    public static void handle(RawPayload payload,IPayloadContext context){
        var channel=Forge1192RawNetworkBridge.requireChannel(payload.channel);
        ServerPlayer sender=null;
        if(context.flow()==PacketFlow.SERVERBOUND){if(!(context.player() instanceof ServerPlayer actual))throw new IllegalStateException("Serverbound raw payload has no actual ServerPlayer");sender=actual;}
        var source=new Forge1192NetworkBridge.LegacyContext(sender,work->context.enqueueWork(()->Forge1192NetworkBridge.runInChannelScope(channel,work)));
        Forge1192RawNetworkBridge.deliver(payload.channel,payload.bytes(),source,context.flow()==PacketFlow.SERVERBOUND);
    }
}
