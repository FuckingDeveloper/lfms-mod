package com.fuckingdeveloper.lms.profile.forge1192.runtime.network;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.*;
import io.netty.buffer.Unpooled;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import net.minecraft.network.*;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Distinct raw play protocol over the existing channel ownership/negotiation boundary. */
public final class Forge1192RawNetworkBridge {
    private Forge1192RawNetworkBridge() {}
    private static final Map<Forge1192NetworkBridge.LegacyChannel,LegacyEventChannel> CHANNELS=new ConcurrentHashMap<>();
    public static final class LegacyEventChannel {
        private final Forge1192NetworkBridge.LegacyChannel channel;
        private final IEventBus bus=BusBuilder.builder().build();
        private final CustomPacketPayload.Type<RawPayload> type;
        private LegacyEventChannel(Forge1192NetworkBridge.LegacyChannel channel){this.channel=channel;type=new CustomPacketPayload.Type<>(channel.name());bus.start();}
        private void requireOwner(){
            if(!Forge1192LifecycleBridge.requireActive().modId().equals(channel.modId()))
                throw new UnsupportedOperationException("Cross-owner raw-channel listener registration needs separate enqueue ownership semantics");
        }
        public <T extends LegacyNetworkEvent> void addListener(Class<T> event,Consumer<T> listener){requireOwner();Forge1192EventBusBridge.addListenerExplicit(bus,EventPriority.NORMAL,false,event,listener);}
        public void registerObject(Object object){requireOwner();Forge1192EventBusBridge.register(bus,object);}
        public void unregisterObject(Object object){requireOwner();Forge1192EventBusBridge.unregister(bus,object);}
        public boolean isRemotePresent(Connection connection){return net.neoforged.neoforge.network.registration.NetworkRegistry.hasChannel(Objects.requireNonNull(connection),ConnectionProtocol.PLAY,channel.name());}
    }
    public static LegacyEventChannel create(Forge1192NetworkBridge.LegacyChannel channel){
        Forge1192NetworkBridge.requireChannel(channel);var result=new LegacyEventChannel(channel);
        if(CHANNELS.putIfAbsent(channel,result)!=null)throw new IllegalStateException("Raw channel already initialized: "+channel.name());
        return result;
    }
    public static boolean register(Forge1192NetworkBridge.LegacyChannel channel,PayloadRegistrar registrar){
        var raw=CHANNELS.get(channel);if(raw==null)return false;
        registrar.playBidirectional(raw.type,codec(raw),Forge1192RawNetworkBridge::handle,Forge1192RawNetworkBridge::handle);return true;
    }
    public static final class RawPayload implements CustomPacketPayload {
        private final LegacyEventChannel channel;
        private final FriendlyByteBuf outgoing;
        private final byte[] incoming;
        private RawPayload(LegacyEventChannel channel,FriendlyByteBuf outgoing,byte[] incoming){this.channel=channel;this.outgoing=outgoing;this.incoming=incoming;}
        @Override public Type<RawPayload> type(){return channel.type;}
        private byte[] bytes(){
            if(incoming!=null)return incoming;
            byte[] result=new byte[outgoing.readableBytes()];outgoing.getBytes(outgoing.readerIndex(),result);return result;
        }
    }
    /** Retains the original unsafe source buffer until encoding; never prefixes a discriminator. */
    public static RawPayload outgoing(LegacyEventChannel channel,FriendlyByteBuf buffer){
        requireChannel(channel);return new RawPayload(channel,Objects.requireNonNull(buffer),null);
    }
    private static void requireChannel(LegacyEventChannel raw){
        if(raw==null||CHANNELS.get(raw.channel)!=raw)throw new IllegalArgumentException("Raw source channel has no provenance");
        Forge1192NetworkBridge.requireChannel(raw.channel);
    }
    public static StreamCodec<RegistryFriendlyByteBuf,RawPayload> codec(LegacyEventChannel channel){
        requireChannel(channel);
        return new StreamCodec<>() {
            public RawPayload decode(RegistryFriendlyByteBuf buffer){byte[] bytes=new byte[buffer.readableBytes()];buffer.readBytes(bytes);return new RawPayload(channel,null,bytes);}
            public void encode(RegistryFriendlyByteBuf buffer,RawPayload payload){
                if(payload.channel!=channel)throw new IllegalArgumentException("Raw payload/channel mismatch");
                if(payload.outgoing!=null)buffer.writeBytes(payload.outgoing,payload.outgoing.readerIndex(),payload.outgoing.readableBytes());
                else buffer.writeBytes(payload.incoming);
            }
        };
    }
    public static void handle(RawPayload payload,IPayloadContext nativeContext){
        requireChannel(payload.channel);
        ServerPlayer sender=null;
        if(nativeContext.flow()==PacketFlow.SERVERBOUND){
            if(!(nativeContext.player() instanceof ServerPlayer player))throw new IllegalStateException("Serverbound raw payload has no actual ServerPlayer");sender=player;
        }
        var channel=payload.channel.channel;
        var context=new Forge1192NetworkBridge.LegacyContext(sender,work->nativeContext.enqueueWork(()->Forge1192NetworkBridge.runInChannelScope(channel,work)));
        Supplier<Forge1192NetworkBridge.LegacyContext> supplier=()->context;
        var data=new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.bytes()));
        LegacyNetworkEvent event=nativeContext.flow()==PacketFlow.SERVERBOUND
                ?new LegacyNetworkEvent.ClientCustomPayloadEvent(data,supplier):new LegacyNetworkEvent.ServerCustomPayloadEvent(data,supplier);
        try(var ignored=Forge1192LifecycleBridge.enterRecorded(channel.modId())) {
            payload.channel.bus.post(event);
            if(!context.packetHandled())throw new UnsupportedOperationException("Native fallback for an unhandled source raw payload is not adapted: "+channel.name());
        }catch(RuntimeException|Error failure){throw failure;}
        catch(Exception failure){throw new IllegalStateException("Cannot close raw source delivery scope",failure);}
        finally{data.release();}
    }
}
