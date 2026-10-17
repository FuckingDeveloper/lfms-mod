package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.jar.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforgespi.language.IModInfo;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

/** Independent source-shaped constructor/accessor/listener consumers, no fixture mod names. */
public final class SourceExtensionEventCheck {
    public static final class Fixture {
        public static LegacyExtensionPoint.DisplayTest value(Supplier<String> version,BiPredicate<String,Boolean> test){return new LegacyExtensionPoint.DisplayTest(version,test);}
        public static void register(Supplier<LegacyExtensionPoint.DisplayTest> value){Forge1192ModLoadingContext.get().registerExtensionPoint(LegacyExtensionPoint.DisplayTest.class,value);}
        public static Object read(){return Forge1192ModLoadingContext.get().getActiveContainer().getCustomExtension(LegacyExtensionPoint.DisplayTest.class).orElseThrow();}
        public static void listen(IEventBus bus,EventPriority priority,boolean canceled,Class<SubscriberIndexCheck.ProbeEvent> type,Consumer<SubscriberIndexCheck.ProbeEvent> consumer){bus.addListener(priority,canceled,type,consumer);}
        public static Object unknown(){return com.mojang.blaze3d.platform.InputConstants.UNKNOWN;}
        public static Object screen(net.neoforged.neoforge.client.event.ScreenEvent.Init.Pre event){return event.getScreen();}
        public static Object player(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event){return event.getPlayer();}
        public static void safe(Supplier<Forge1192DistExecutorBridge.SafeRunnable> supplier){Forge1192DistExecutorBridge.safeRunWhenOn(net.neoforged.api.distmarker.Dist.CLIENT,supplier);}
    }
    static void run() throws Exception {
        String old=Fixture.class.getName().replace('.','/'),name="probe/SourceExtensions";
        byte[] bytes;try(var in=SourceExtensionEventCheck.class.getResourceAsStream("SourceExtensionEventCheck$Fixture.class")){bytes=in.readAllBytes();}
        var writer=new ClassWriter(0);new ClassReader(bytes).accept(new ClassRemapper(writer,new Remapper(){
            @Override public String mapFieldName(String owner,String field,String descriptor){
                return owner.equals("com/mojang/blaze3d/platform/InputConstants") && field.equals("UNKNOWN") ? "f_84822_" : field;
            }
            @Override public String map(String owner){
                if(owner.equals(old))return name;
                String root="com/fuckingdeveloper/lms/profile/forge1192/runtime/lifecycle/";
                if(owner.startsWith(root+"LegacyExtensionPoint"))return owner.replace(root+"LegacyExtensionPoint","net/minecraftforge/fml/IExtensionPoint");
                if(owner.startsWith(root+"Forge1192DistExecutorBridge"))return owner.replace(root+"Forge1192DistExecutorBridge","net/minecraftforge/fml/DistExecutor");
                if(owner.equals(root+"Forge1192ModLoadingContext"))return "net/minecraftforge/fml/ModLoadingContext";
                if(owner.equals(root+"Forge1192ModContainerView"))return "net/minecraftforge/fml/ModContainer";
                if(owner.startsWith("net/neoforged/neoforge/"))return owner.replace("net/neoforged/neoforge/","net/minecraftforge/");
                if(owner.startsWith("net/neoforged/bus/api/"))return owner.replace("net/neoforged/bus/api/","net/minecraftforge/eventbus/api/");
                if(owner.startsWith("net/neoforged/api/"))return owner.replace("net/neoforged/api/","net/minecraftforge/api/");
                return owner;
            }
        }),0);
        var path=Files.createTempFile("lms-source-extensions", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry(name+".class"));jar.write(writer.toByteArray());jar.closeEntry();jar.putNextEntry(new JarEntry("META-INF/mods.toml"));jar.write("modLoader=\"javafml\"\nloaderVersion=\"[43,)\"\nlicense=\"test\"\n[[mods]]\nmodId=\"extension_owner\"\nversion=\"1\"\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));jar.closeEntry();}
            byte[] original=Files.readAllBytes(path);
            var bus=BusBuilder.builder().build();bus.start();
            var info=(IModInfo)Proxy.newProxyInstance(SourceExtensionEventCheck.class.getClassLoader(),new Class[]{IModInfo.class},(p,m,a)->{if(m.getName().equals("getModId"))return "extension_host";throw new AssertionError(m);});
            var host=new ModContainer(info){public IEventBus getEventBus(){return bus;}};
            try(var session=new Forge1192RuntimeSession("extension_owner",path,SourceExtensionEventCheck.class.getClassLoader(),List.of(name.replace('/','.')));
                var loader=new ManagedLegacyClassLoader(path,SourceExtensionEventCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                session.bindModEventBus(bus,host);
                var type=loader.linkOwnedClass(name.replace('/','.'));
                check(type.getMethod("unknown").invoke(null)==com.mojang.blaze3d.platform.InputConstants.UNKNOWN,"source unbound key is not native SDL identity");int[] supplied={0},predicate={0},registered={0},calls={0};
                Supplier<String> version=()->{supplied[0]++;return "v";};BiPredicate<String,Boolean> test=(remote,server)->{predicate[0]++;return remote.equals("ok")&&server;};
                var value=(LegacyExtensionPoint.DisplayTest)type.getMethod("value",Supplier.class,BiPredicate.class).invoke(null,version,test);
                check(supplied[0]==0&&predicate[0]==0&&value.suppliedVersion()==version&&value.remoteVersionTest()==test,"record evaluated/replaced original callbacks");
                check(value.suppliedVersion().get().equals("v")&&value.remoteVersionTest().test("ok",true)&&!value.remoteVersionTest().test("other",true),"source callbacks changed");
                Supplier<LegacyExtensionPoint.DisplayTest> extension=()->{registered[0]++;return value;};
                Consumer<SubscriberIndexCheck.ProbeEvent> consumer=e->{check(Forge1192LifecycleBridge.requireActive().modId().equals("extension_owner"),"explicit listener lost source owner");calls[0]++;};
                try(var scope=Forge1192LifecycleBridge.enter("extension_owner",bus,host)) {
                    type.getMethod("register",Supplier.class).invoke(null,extension);check(registered[0]==0,"extension supplier not lazy");
                    check(type.getMethod("read").invoke(null)==value&&registered[0]==1,"extension identity/lookup");
                    type.getMethod("listen",IEventBus.class,EventPriority.class,boolean.class,Class.class,Consumer.class).invoke(null,bus,EventPriority.HIGH,true,SubscriberIndexCheck.ProbeEvent.class,consumer);
                    Supplier<Forge1192DistExecutorBridge.SafeRunnable> runnable=()->()->calls[0]++;
                    type.getMethod("safe",Supplier.class).invoke(null,runnable);check(calls[0]==1,"safe source callable not executed");
                }
                var event=new SubscriberIndexCheck.ProbeEvent();event.setCanceled(true);bus.post(event);check(calls[0]==2&&Forge1192LifecycleBridge.currentContext().isEmpty(),"explicit canceled listener dispatch/scope restoration");
                Forge1192EventBusBridge.unregister(bus,consumer);bus.post(new SubscriberIndexCheck.ProbeEvent());check(calls[0]==2,"explicit listener unregister identity");
                var logout=new net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut(null,null,null);
                check(type.getMethod("player",logout.getClass()).invoke(null,logout)==null,"nullable native player semantics changed");
                check(Arrays.equals(original,Files.readAllBytes(path)),"legacy binary changed");
            }
        }finally{Files.deleteIfExists(path);}
        System.out.println("PASS: source display record/callback/supplier identity and lazy extension lookup, explicit listener cancellation/owner/unregister, serializable safe callable and native nullable inherited event getter; old server-list display intentionally unsupported");
    }
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
