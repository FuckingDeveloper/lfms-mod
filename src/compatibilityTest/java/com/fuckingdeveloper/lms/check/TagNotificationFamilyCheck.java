package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.event.LegacyTagsUpdatedEvent;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.*;
import com.fuckingdeveloper.lms.target.neoforge263.Target263SourceTagNotifications;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforgespi.language.IModInfo;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

public final class TagNotificationFamilyCheck {
    private static final String SOURCE="net/minecraftforge/event/TagsUpdatedEvent",HOST="fixture/TagsObserver",E="L"+SOURCE+";",BUS="net/minecraftforge/eventbus/api/IEventBus";
    private static final List<String> CALLS=new ArrayList<>();private static final List<Object> EVENTS=new ArrayList<>();
    public static void record(Object event,String kind){
        check(Forge1192LifecycleBridge.requireActive().modId().equals("tag_fixture"),"tag callback source owner lost");
        check(com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.Forge1192RegistrationContext.requireModId().equals("tag_fixture"),"tag callback namespace lost");
        CALLS.add(kind);EVENTS.add(event);
    }
    public static void run() throws Exception {
        var file=Files.createTempFile("source-tags-",".jar");
        var bus=BusBuilder.builder().build();
        var info=(IModInfo)java.lang.reflect.Proxy.newProxyInstance(TagNotificationFamilyCheck.class.getClassLoader(),new Class[]{IModInfo.class},(p,m,a)->{if(m.getName().equals("getModId"))return "tag_fixture";throw new AssertionError(m);});
        var host=new ModContainer(info){public IEventBus getEventBus(){return bus;}};
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(file))){jar.putNextEntry(new JarEntry(HOST+".class"));jar.write(fixture());jar.closeEntry();}
            try(var loader=new ManagedLegacyClassLoader(file,TagNotificationFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass(HOST.replace('/','.'));
                check(type.getMethod("eventType").invoke(null)==LegacyTagsUpdatedEvent.class,"source tag identity not projected");
                for(boolean client:List.of(false,true))for(boolean integrated:List.of(false,true)) {
                    var view=(LegacyTagsUpdatedEvent)type.getMethod("construct",RegistryAccess.class,boolean.class,boolean.class).invoke(null,RegistryAccess.EMPTY,client,integrated);
                    check(view.getClass()==type.getMethod("eventType").invoke(null)&&view.getRegistryAccess()==RegistryAccess.EMPTY,"source ctor/registry identity");
                    check(view.getUpdateCause()==(client?LegacyTagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED:LegacyTagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD)&&view.shouldUpdateStaticData()==(!client||!integrated),"source ctor cause/static rule");
                }
                var nativeServer=new TagsUpdatedEvent.ServerDataLoad(null,RegistryAccess.EMPTY);
                check(Target263SourceTagNotifications.view(nativeServer)==Target263SourceTagNotifications.view(nativeServer)&&Target263SourceTagNotifications.view(nativeServer).getUpdateCause()==LegacyTagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD,"one native server publication has inconsistent source identity/cause");
                var nativeClient=new TagsUpdatedEvent.ClientPacketReceived(RegistryAccess.EMPTY,true);
                check(!Target263SourceTagNotifications.view(nativeClient).shouldUpdateStaticData()&&Target263SourceTagNotifications.view(nativeClient).getRegistryAccess()==nativeClient.getRegistries(),"native client payload/static-data projection");
                Consumer<?> consumer;
                try(var scope=Forge1192LifecycleBridge.enter("tag_fixture",bus,host,loader)) {
                    consumer=(Consumer<?>)type.getMethod("subscribe",IEventBus.class).invoke(null,NeoForge.EVENT_BUS);
                    Forge1192EventBusBridge.register(NeoForge.EVENT_BUS,type);
                }
                try {
                    var connection=new Connection(PacketFlow.CLIENTBOUND);
                    var channel=new EmbeddedChannel(connection);
                    try {
                        check(connection.isConnected(),"real embedded native connection not active");
                        CALLS.clear();EVENTS.clear();
                        Target263SourceTagNotifications.configuration(connection,nativeClient);check(CALLS.isEmpty(),"source tags delivered during configuration");
                        Consumer<net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn> login=e->CALLS.add("native-login");
                        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,false,net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn.class,login);
                        try { NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn(null,null,connection));Target263SourceTagNotifications.loginCompleted(connection); }
                        finally { NeoForge.EVENT_BUS.unregister(login); }
                        check(CALLS.equals(List.of("native-login","explicit","annotated")),"source tags did not follow native login notification with source priorities: "+CALLS);
                        check(EVENTS.size()==2&&EVENTS.get(0)==EVENTS.get(1)&&EVENTS.get(0).getClass()==LegacyTagsUpdatedEvent.class,"callback event identity differs");
                        CALLS.clear();Target263SourceTagNotifications.loginCompleted(connection);check(CALLS.isEmpty(),"duplicate login delivery");
                        Target263SourceTagNotifications.publication(connection,new TagsUpdatedEvent.ClientPacketReceived(RegistryAccess.EMPTY,false));check(CALLS.equals(List.of("explicit","annotated")),"ready client update not delivered");
                        CALLS.clear();Target263SourceTagNotifications.configuration(connection,nativeClient);connection.disconnect(net.minecraft.network.chat.Component.literal("fixture done"));Target263SourceTagNotifications.loginCompleted(connection);check(CALLS.isEmpty(),"closed connection delivered stale data");
                    } finally {channel.finishAndReleaseAll();}
                    CALLS.clear();var posted=new LegacyTagsUpdatedEvent(RegistryAccess.EMPTY,false,false);
                    check(!Forge1192EventBusBridge.post(NeoForge.EVENT_BUS,posted)&&CALLS.equals(List.of("explicit","annotated")),"explicit source post/cancellation contract");
                } finally {
                    Forge1192EventBusBridge.unregister(NeoForge.EVENT_BUS,consumer);Forge1192EventBusBridge.unregister(NeoForge.EVENT_BUS,type);
                    CALLS.clear();Forge1192EventBusBridge.post(NeoForge.EVENT_BUS,new LegacyTagsUpdatedEvent(RegistryAccess.EMPTY,true,true));check(CALLS.isEmpty(),"source tag unregister leaked callbacks");
                    Forge1192LifecycleBridge.release("tag_fixture",loader);
                }
                Class.forName("net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl",false,loader.getParent());
                var packetListener=Class.forName("net.minecraft.client.multiplayer.ClientPacketListener",false,loader.getParent());
                check(Arrays.stream(packetListener.getDeclaredMethods()).anyMatch(m->m.getName().contains("lms$afterNativeLogin")&&Arrays.equals(m.getParameterTypes(),new Class[]{net.minecraft.network.protocol.game.ClientboundLoginPacket.class,org.spongepowered.asm.mixin.injection.callback.CallbackInfo.class})),"native completed-login hook was not installed");
            }
        } finally {CALLS.clear();EVENTS.clear();Files.deleteIfExists(file);}
        System.out.println("PASS: source tag class/constructor/enum/registry/static-data identity, native publication view, real embedded connection/readiness delivery and installed completed-login hook, source priority/ownership/explicit+annotated listeners, disconnect and unregister");
    }
    private static byte[] fixture() {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,HOST,null,"java/lang/Object",null);
        var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"eventType","()Ljava/lang/Class;",null,null);m.visitCode();m.visitLdcInsn(Type.getType(E));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"construct","(Lnet/minecraft/core/RegistryAccess;ZZ)"+E,null,null);m.visitCode();m.visitTypeInsn(NEW,SOURCE);m.visitInsn(DUP);m.visitVarInsn(ALOAD,0);m.visitVarInsn(ILOAD,1);m.visitVarInsn(ILOAD,2);m.visitMethodInsn(INVOKESPECIAL,SOURCE,"<init>","(Lnet/minecraft/core/RegistryAccess;ZZ)V",false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        for(var name:List.of("explicit","annotated")) {
            m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,name,"("+E+")V",null,null);
            if(name.equals("annotated")){var a=m.visitAnnotation("Lnet/minecraftforge/eventbus/api/SubscribeEvent;",true);a.visitEnum("priority","Lnet/minecraftforge/eventbus/api/EventPriority;","LOWEST");a.visitEnd();}
            m.visitCode();m.visitVarInsn(ALOAD,0);m.visitLdcInsn(name);m.visitMethodInsn(INVOKESTATIC,"com/fuckingdeveloper/lms/check/TagNotificationFamilyCheck","record","(Ljava/lang/Object;Ljava/lang/String;)V",false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
        }
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"subscribe","(L"+BUS+";)Ljava/util/function/Consumer;",null,null);m.visitCode();
        m.visitInvokeDynamicInsn("accept","()Ljava/util/function/Consumer;",new Handle(H_INVOKESTATIC,"java/lang/invoke/LambdaMetafactory","metafactory","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",false),Type.getMethodType("(Ljava/lang/Object;)V"),new Handle(H_INVOKESTATIC,HOST,"explicit","("+E+")V",false),Type.getMethodType("("+E+")V"));m.visitVarInsn(ASTORE,1);
        m.visitVarInsn(ALOAD,0);m.visitFieldInsn(GETSTATIC,"net/minecraftforge/eventbus/api/EventPriority","HIGHEST","Lnet/minecraftforge/eventbus/api/EventPriority;");m.visitInsn(ICONST_0);m.visitLdcInsn(Type.getType(E));m.visitVarInsn(ALOAD,1);m.visitMethodInsn(INVOKEINTERFACE,BUS,"addListener","(Lnet/minecraftforge/eventbus/api/EventPriority;ZLjava/lang/Class;Ljava/util/function/Consumer;)V",true);m.visitVarInsn(ALOAD,1);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();return w.toByteArray();
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
