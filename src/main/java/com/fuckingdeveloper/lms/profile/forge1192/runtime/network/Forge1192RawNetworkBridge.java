package com.fuckingdeveloper.lms.profile.forge1192.runtime.network;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192EventBusBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192LifecycleBridge;
import com.fuckingdeveloper.lms.target.InstalledTargetAdapters;
import io.netty.buffer.Unpooled;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.*;

/** Source raw-channel provenance, event delivery and buffer lifetime. Native payloads belong to the target. */
public final class Forge1192RawNetworkBridge {
    private Forge1192RawNetworkBridge() {}
    private static final Map<Forge1192NetworkBridge.LegacyChannel,LegacyEventChannel> CHANNELS=new ConcurrentHashMap<>();
    public static final class LegacyEventChannel {
        private final Forge1192NetworkBridge.LegacyChannel channel;
        private final IEventBus bus=BusBuilder.builder().build();
        private LegacyEventChannel(Forge1192NetworkBridge.LegacyChannel channel){this.channel=channel;bus.start();}
        public Identifier name(){return channel.name();}
        private void requireOwner(){
            if(!Forge1192LifecycleBridge.requireActive().modId().equals(channel.modId()))
                throw new UnsupportedOperationException("Cross-owner raw-channel listener registration needs separate enqueue ownership semantics");
        }
        public <T extends LegacyNetworkEvent> void addListener(Class<T> event,Consumer<T> listener){requireOwner();Forge1192EventBusBridge.addListenerExplicit(bus,EventPriority.NORMAL,false,event,listener);}
        public void registerObject(Object object){requireOwner();Forge1192EventBusBridge.register(bus,object);}
        public void unregisterObject(Object object){requireOwner();Forge1192EventBusBridge.unregister(bus,object);}
        public boolean isRemotePresent(Connection connection){return InstalledTargetAdapters.rawChannelPresent(connection,channel.name());}
    }
    public static LegacyEventChannel create(Forge1192NetworkBridge.LegacyChannel channel){
        Forge1192NetworkBridge.requireChannel(channel);var result=new LegacyEventChannel(channel);
        if(CHANNELS.putIfAbsent(channel,result)!=null)throw new IllegalStateException("Raw channel already initialized: "+channel.name());
        return result;
    }
    public static LegacyEventChannel find(Forge1192NetworkBridge.LegacyChannel channel){Forge1192NetworkBridge.requireChannel(channel);return CHANNELS.get(channel);}
    public static Forge1192NetworkBridge.LegacyChannel requireChannel(LegacyEventChannel raw){
        if(raw==null||CHANNELS.get(raw.channel)!=raw)throw new IllegalArgumentException("Raw source channel has no provenance");
        return Forge1192NetworkBridge.requireChannel(raw.channel);
    }
    public static void deliver(LegacyEventChannel raw,byte[] bytes,Forge1192NetworkBridge.LegacyContext context,boolean clientOrigin){
        var channel=requireChannel(raw);
        Supplier<Forge1192NetworkBridge.LegacyContext> supplier=()->context;
        var data=new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
        LegacyNetworkEvent event=clientOrigin?new LegacyNetworkEvent.ClientCustomPayloadEvent(data,supplier):new LegacyNetworkEvent.ServerCustomPayloadEvent(data,supplier);
        try(var ignored=Forge1192LifecycleBridge.enterRecorded(channel.modId())) {
            raw.bus.post(event);
            if(!context.packetHandled())throw new UnsupportedOperationException("Native fallback for an unhandled source raw payload is not adapted: "+channel.name());
        }catch(RuntimeException|Error failure){throw failure;}
        catch(Exception failure){throw new IllegalStateException("Cannot close raw source delivery scope",failure);}
        finally{data.release();}
    }
}
