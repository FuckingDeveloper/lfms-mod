package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192LifecycleBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.network.*;
import io.netty.buffer.Unpooled;
import com.fuckingdeveloper.lms.target.neoforge263.Target263RawNetworkChannels;
import java.lang.reflect.Proxy;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import java.util.jar.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.*;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

/** Raw bytes, original buffer ownership and typed immutable source listener projection. */
public final class RawNetworkFamilyCheck {
    private static LegacyNetworkEvent observed;
    private static int deliveries;
    public static void receive(LegacyNetworkEvent event) {
        check(event instanceof LegacyNetworkEvent.ServerCustomPayloadEvent,"client receive event origin");
        check(Forge1192LifecycleBridge.requireActive().modId().equals("raw_fixture"),"source network owner");
        check(event.getLoginIndex()==Integer.MAX_VALUE&&event.getPayload().readUnsignedByte()==42,"raw event bytes/index");
        observed=event;deliveries++;event.getSource().get().setPacketHandled(true);
        event.getSource().get().enqueueWork(()->check(Forge1192LifecycleBridge.requireActive().modId().equals("raw_fixture"),"queued owner"));
    }
    static void run() throws Exception {
        var bus=BusBuilder.builder().build();bus.start();
        var info=(IModInfo)Proxy.newProxyInstance(RawNetworkFamilyCheck.class.getClassLoader(),new Class[]{IModInfo.class},(p,m,a)->{if(m.getName().equals("getModId"))return "raw_fixture";throw new AssertionError(m);});
        var host=new ModContainer(info){public IEventBus getEventBus(){return bus;}};
        var file=Files.createTempFile("lms-raw-channel", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(file))){jar.putNextEntry(new JarEntry("probe/RawListener.class"));jar.write(fixture());jar.closeEntry();}
            byte[] original=Files.readAllBytes(file);
            try(var loader=new ManagedLegacyClassLoader(file,RawNetworkFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var source=loader.linkOwnedClass("probe.RawListener");
                Forge1192RawNetworkBridge.LegacyEventChannel channel;
                try(var scope=Forge1192LifecycleBridge.enter("raw_fixture",bus,host)) {
                    channel=(Forge1192RawNetworkBridge.LegacyEventChannel)source.getMethod("create",Identifier.class,Supplier.class,Predicate.class,Predicate.class).invoke(null,Identifier.fromNamespaceAndPath("raw_fixture","events"),(Supplier<String>)()->"1",(Predicate<String>)s->true,(Predicate<String>)s->true);
                    source.getMethod("listen",Forge1192RawNetworkBridge.LegacyEventChannel.class).invoke(null,channel);
                    channel.addListener(LegacyNetworkEvent.class,event->check(event==observed&&event.getPayload().readUnsignedByte()==43&&event.getSource()==observed.getSource(),"listener buffer/context identity and shared reader index"));
                }
                var input=new FriendlyByteBuf(Unpooled.buffer());input.writeByte(99);input.writeByte(42);input.readerIndex(1);
                var outgoing=Target263RawNetworkChannels.outgoing(channel,input);input.writeByte(43);
                var wire=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
                var codec=Target263RawNetworkChannels.codec(channel);codec.encode(wire,outgoing);
                check(input.readerIndex()==1&&wire.readableBytes()==2&&wire.getUnsignedByte(0)==42&&wire.getUnsignedByte(1)==43,"raw codec prefixed/snapshotted/mutated source buffer");
                var incoming=codec.decode(wire);wire.release();input.release();
                var queued=new ArrayList<Runnable>();
                var context=(IPayloadContext)Proxy.newProxyInstance(RawNetworkFamilyCheck.class.getClassLoader(),new Class[]{IPayloadContext.class},(p,m,a)->{
                    if(m.getName().equals("flow"))return PacketFlow.CLIENTBOUND;
                    if(m.getName().equals("enqueueWork")){queued.add((Runnable)a[0]);return CompletableFuture.completedFuture(null);}
                    throw new AssertionError(m);
                });
                var registrar=new net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent().registrar("1")
                        .executesOn(net.neoforged.neoforge.network.registration.HandlerThread.NETWORK);
                check(Target263RawNetworkChannels.register(Forge1192RawNetworkBridge.requireChannel(channel),registrar),"raw native registrar dispatch");
                NativeRegistration.client(channel.name()).handle(incoming,context);
                check(deliveries==1&&observed.getPayload().refCnt()==0&&queued.size()==1,"delivery/lifetime/enqueue");
                queued.getFirst().run();check(Forge1192LifecycleBridge.currentContext().isEmpty(),"network scope leaked");
                check(Arrays.equals(original,Files.readAllBytes(file)),"source JAR changed");
            }
        }finally{Files.deleteIfExists(file);observed=null;}
        System.out.println("PASS: immutable typed source raw channel/listener, unprefixed native codec, late writes, unchanged reader index, shared event/payload/context, release and deferred source scope; remote wire/login remain unverified");
    }
    private static final class NativeRegistration extends net.neoforged.neoforge.network.registration.NetworkRegistry {
        @SuppressWarnings("unchecked")
        static net.neoforged.neoforge.network.handling.IPayloadHandler<Target263RawNetworkChannels.RawPayload> client(Identifier id) {
            var play=net.minecraft.network.ConnectionProtocol.PLAY;
            check(CLIENTBOUND_HANDLERS.get(play).containsKey(id)&&SERVERBOUND_HANDLERS.get(play).containsKey(id),"both actual native raw play directions");
            return (net.neoforged.neoforge.network.handling.IPayloadHandler<Target263RawNetworkChannels.RawPayload>)CLIENTBOUND_HANDLERS.get(play).get(id);
        }
    }
    private static byte[] fixture(){
        String channel="net/minecraftforge/network/event/EventNetworkChannel",event="net/minecraftforge/network/NetworkEvent",name="probe/RawListener";
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,name,null,"java/lang/Object",null);
        String factory="(Lnet/minecraft/resources/ResourceLocation;Ljava/util/function/Supplier;Ljava/util/function/Predicate;Ljava/util/function/Predicate;)L"+channel+";";
        var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"create",factory,null,null);m.visitCode();for(int i=0;i<4;i++)m.visitVarInsn(ALOAD,i);m.visitMethodInsn(INVOKESTATIC,"net/minecraftforge/network/NetworkRegistry","newEventChannel",factory,false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"consume","(L"+event+";)V",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitMethodInsn(INVOKESTATIC,"com/fuckingdeveloper/lms/check/RawNetworkFamilyCheck","receive","(Lcom/fuckingdeveloper/lms/profile/forge1192/runtime/network/LegacyNetworkEvent;)V",false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"listen","(L"+channel+";)V",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);
        m.visitInvokeDynamicInsn("accept","()Ljava/util/function/Consumer;",new Handle(H_INVOKESTATIC,"java/lang/invoke/LambdaMetafactory","metafactory","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",false),Type.getMethodType("(Ljava/lang/Object;)V"),new Handle(H_INVOKESTATIC,name,"consume","(L"+event+";)V",false),Type.getMethodType("(L"+event+";)V"));
        m.visitMethodInsn(INVOKEVIRTUAL,channel,"addListener","(Ljava/util/function/Consumer;)V",false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();return w.toByteArray();
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
