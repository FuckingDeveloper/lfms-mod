package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.*;
import com.fuckingdeveloper.lms.target.neoforge263.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.*;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforgespi.language.IModInfo;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

/** Executable source-shaped binaries against real native config objects and file loader. */
public final class SourceConfigRegistrationCheck {
    public enum Choice { FIRST, SECOND }
    public static final class Fixture {
        public static ModConfigSpec build() {
            var b=new ModConfigSpec.Builder();
            b.comment("Settings").push("group");
            b.translation("fixture.flag").worldRestart().define("flag",false);
            b.defineInRange("integer",2,0,10);
            b.defineInRange("long",3L,0L,20L);
            b.defineInRange("double",0.5,0,1);
            b.defineEnum("choice",Choice.FIRST);
            b.define("string","original");
            b.pop();
            return b.build();
        }
        public static Forge1192ModLoadingContext context(){return helper(Forge1192ModLoadingContext.get());}
        private static Forge1192ModLoadingContext helper(Forge1192ModLoadingContext value){return value;}
        public static IEventBus bus(){return Forge1192JavaModLoadingContext.get().getModEventBus();}
        public static Supplier<String> namespace(){return context()::getActiveNamespace;}
        public static void register(Forge1192ModLoadingContext c,Forge1192ModConfigView.Type t,ModConfigSpec s){c.registerConfig(t,s);}
        public static void named(Forge1192ModLoadingContext c,Forge1192ModConfigView.Type t,ModConfigSpec s,String name){c.registerConfig(t,s,name);}
        public static Object read(ModConfigSpec.ConfigValue<?> v){return v.get();}
        public static Object readInt(ModConfigSpec.IntValue v){return v.get();}
        public static void set(ModConfigSpec.ConfigValue<Object> v,Object value){v.set(value);}
        public static void reload(ModConfigSpec s){s.afterReload();}
        public static void save(ModConfigSpec s){s.save();}
        public static Function<ModConfigSpec.ConfigValue<?>,Object> reader(){return ModConfigSpec.ConfigValue::get;}
        public static Supplier<ModConfigSpec.Builder> builder(){return ModConfigSpec.Builder::new;}
    }
    static void run() throws Exception {
        String original=Fixture.class.getName().replace('.','/'),fixture="fixture/SourceConfigs";
        byte[] bytes;try(var in=SourceConfigRegistrationCheck.class.getResourceAsStream("SourceConfigRegistrationCheck$Fixture.class")){bytes=in.readAllBytes();}
        var writer=new ClassWriter(0);
        new ClassReader(bytes).accept(new ClassRemapper(writer,new Remapper(){
            @Override public String map(String owner){
                if(owner.equals(original))return fixture;
                if(owner.startsWith(Target263ConfigLayout.NATIVE))return "net/minecraftforge/common/ForgeConfigSpec"+owner.substring(Target263ConfigLayout.NATIVE.length());
                return switch(owner){
                    case "com/fuckingdeveloper/lms/profile/forge1192/runtime/lifecycle/Forge1192ModLoadingContext"->"net/minecraftforge/fml/ModLoadingContext";
                    case "com/fuckingdeveloper/lms/profile/forge1192/runtime/lifecycle/Forge1192JavaModLoadingContext"->"net/minecraftforge/fml/javafmlmod/FMLJavaModLoadingContext";
                    case "com/fuckingdeveloper/lms/profile/forge1192/runtime/lifecycle/Forge1192ModConfigView$Type"->"net/minecraftforge/fml/config/ModConfig$Type";
                    case "net/neoforged/fml/config/IConfigSpec"->"net/minecraftforge/fml/config/IConfigSpec";
                    case "net/neoforged/bus/api/IEventBus"->"net/minecraftforge/eventbus/api/IEventBus";
                    default->owner;
                };
            }
        }),0);
        var jarPath=Files.createTempFile("lms-source-config", ".jar");
        try(var jar=new java.util.jar.JarOutputStream(Files.newOutputStream(jarPath))){jar.putNextEntry(new java.util.jar.JarEntry(fixture+".class"));jar.write(writer.toByteArray());jar.closeEntry();}
        var bus=BusBuilder.builder().build();bus.start();
        var nativeEvents=new AtomicInteger();bus.addListener((ModConfigEvent event)->nativeEvents.incrementAndGet());
        var info=(IModInfo)Proxy.newProxyInstance(SourceConfigRegistrationCheck.class.getClassLoader(),new Class[]{IModInfo.class},(p,m,a)->{
            if(m.getName().equals("getModId"))return "source_config_host";throw new AssertionError(m);
        });
        var host=new ModContainer(info){public IEventBus getEventBus(){return bus;}};
        var firstEvents=new ArrayList<ModConfigEvent>();var secondEvents=new ArrayList<ModConfigEvent>();
        var firstListener=(Consumer<ModConfigEvent>)event->{check(Forge1192LifecycleBridge.requireActive().modId().equals("config_owner_a"),"callback owner scope");firstEvents.add(event);};
        var dir=Files.createTempDirectory("lms-source-config-files");
        try(var loader=new ManagedLegacyClassLoader(jarPath,SourceConfigRegistrationCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var type=loader.linkOwnedClass(fixture.replace('/','.'));
            var spec=(ModConfigSpec)type.getMethod("build").invoke(null);
            var flag=(ModConfigSpec.ConfigValue<?>)spec.getValues().get(List.of("group","flag"));
            reject(()->invoke(type,"read",new Class[]{ModConfigSpec.ConfigValue.class},flag),IllegalStateException.class);
            Forge1192ModLoadingContext token;Supplier<?> escaped;
            try(var scope=Forge1192LifecycleBridge.enter("config_owner_a",bus,host)) {
                token=(Forge1192ModLoadingContext)type.getMethod("context").invoke(null);
                check(token==type.getMethod("context").invoke(null)&&type.getMethod("bus").invoke(null)==bus,"typed context identity/helper descriptor");
                escaped=(Supplier<?>)type.getMethod("namespace").invoke(null);check(escaped.get().equals("config_owner_a"),"bound context reference");
                Forge1192EventBusBridge.addListener(bus,firstListener,false,ModConfigEvent.class);
                type.getMethod("register",Forge1192ModLoadingContext.class,Forge1192ModConfigView.Type.class,ModConfigSpec.class).invoke(null,token,Forge1192ModConfigView.Type.COMMON,spec);
                var client=(ModConfigSpec)type.getMethod("build").invoke(null);
                type.getMethod("named",Forge1192ModLoadingContext.class,Forge1192ModConfigView.Type.class,ModConfigSpec.class,String.class).invoke(null,token,Forge1192ModConfigView.Type.CLIENT,client,"source-client-explicit.toml");
                var server=(ModConfigSpec)type.getMethod("build").invoke(null);token.registerConfig(Forge1192ModConfigView.Type.SERVER,server);
                var configs=Target263SourceConfigAdapter.sourceConfigs("config_owner_a");
                check(configs.size()==3&&configs.get(Forge1192ModConfigView.Type.COMMON).getFileName().equals("config_owner_a-common.toml")&&configs.get(Forge1192ModConfigView.Type.SERVER).nativeConfig().getType()==ModConfig.Type.SYNCED,"source kinds/filenames/native server policy");
                token.registerConfig(Forge1192ModConfigView.Type.COMMON,new ModConfigSpec.Builder().build());
                reject(()->token.registerConfig(Forge1192ModConfigView.Type.COMMON,spec),IllegalStateException.class);
            }
            reject(escaped::get,IllegalStateException.class);
            try(var scope=Forge1192LifecycleBridge.enter("config_owner_b",bus,host)) {
                Forge1192EventBusBridge.addListener(bus,(Consumer<ModConfigEvent>)secondEvents::add,false,ModConfigEvent.class);
                reject(()->token.registerConfig(Forge1192ModConfigView.Type.COMMON,(ModConfigSpec)invoke(type,"build",new Class[]{})),IllegalStateException.class);
                var other=(ModConfigSpec)type.getMethod("build").invoke(null);
                reject(()->Forge1192ModLoadingContext.get().registerConfig(Forge1192ModConfigView.Type.COMMON,other,"config_owner_a-common.toml"),RuntimeException.class);
                Forge1192ModLoadingContext.get().registerConfig(Forge1192ModConfigView.Type.COMMON,other);
            }
            var config=Target263SourceConfigAdapter.config(spec);var view=Forge1192ModConfigView.of(config);
            check(view.getSpec()==spec&&view.getModId().equals("config_owner_a")&&config.getModId().equals("source_config_host"),"source provenance/native host separated");
            check(Forge1192LoaderReflectionBridge.sourceConfigs(host).isEmpty(),"managed configs leaked into native host source ownership");
            load(config,dir.resolve(config.getFileName()),ModConfigEvent.Loading::new);
            check(firstEvents.size()==1&&secondEvents.isEmpty()&&nativeEvents.get()==1,"config loading cross-owner isolation/native delivery");
            check(invoke(type,"read",new Class[]{ModConfigSpec.ConfigValue.class},flag).equals(false),"source flag default");
            for(var entry:Map.<String,Object>of("long",3L,"double",0.5,"choice",Choice.FIRST,"string","original").entrySet()) {
                var value=(ModConfigSpec.ConfigValue<?>)spec.getValues().get(List.of("group",entry.getKey()));
                check(invoke(type,"read",new Class[]{ModConfigSpec.ConfigValue.class},value).equals(entry.getValue()),"source typed scalar/enum default: "+entry.getKey());
            }
            check(type.getMethod("reader").getGenericReturnType().getTypeName().contains("ModConfigSpec$ConfigValue"),"source generic value signature linkage");
            invoke(type,"set",new Class[]{ModConfigSpec.ConfigValue.class,Object.class},flag,true);
            check(invoke(type,"read",new Class[]{ModConfigSpec.ConfigValue.class},flag).equals(true),"worldRestart source set/cache");
            var integer=(ModConfigSpec.IntValue)spec.getValues().get(List.of("group","integer"));
            check(invoke(type,"readInt",new Class[]{ModConfigSpec.IntValue.class},integer).equals(2),"inherited value source declaration");
            @SuppressWarnings("unchecked") var reader=(Function<ModConfigSpec.ConfigValue<?>,Object>)type.getMethod("reader").invoke(null);
            check(reader.apply(flag).equals(true),"source method reference cache contract");
            check(((Supplier<?>)type.getMethod("builder").invoke(null)).get() instanceof ModConfigSpec.Builder,"constructor method reference projection");
            view.getConfigData().set(List.of("group","flag"),false);
            invoke(type,"reload",new Class[]{ModConfigSpec.class},spec);
            check(reader.apply(flag).equals(false),"direct source afterReload did not clear restart cache");
            invoke(type,"set",new Class[]{ModConfigSpec.ConfigValue.class,Object.class},flag,true);
            invoke(type,"save",new Class[]{ModConfigSpec.class},spec);
            check(Files.readString(view.getFullPath()).contains("flag = true")&&firstEvents.size()==1,"save without manufactured reload");
            var text=Files.readString(view.getFullPath()).replace("flag = true","flag = false").replace("integer = 2","integer = 999");Files.writeString(view.getFullPath(),text);
            load(config,view.getFullPath(),ModConfigEvent.Reloading::new);
            check(reader.apply(flag).equals(false)&&reader.apply(integer).equals(10),"native correction/source restart cache invalidation");
            check(firstEvents.size()==2&&secondEvents.isEmpty()&&nativeEvents.get()==2,"reload routing");
            Forge1192EventBusBridge.unregister(bus,firstListener);
            load(config,view.getFullPath(),ModConfigEvent.Reloading::new);
            check(firstEvents.size()==2&&nativeEvents.get()==3,"source unregister/native listener preserved");
            var set=ModConfig.class.getDeclaredMethod("setConfig",Class.forName("net.neoforged.fml.config.LoadedConfig"),Function.class);set.setAccessible(true);
            set.invoke(config,null,(Function<ModConfig,ModConfigEvent>)ModConfigEvent.Unloading::new);
            reject(()->reader.apply(flag),IllegalStateException.class);
            check(!spec.isLoaded()&&nativeEvents.get()==4,"native unload/cache lifetime");
            var tnt=Path.of("legacy-mods/tntexpansion-forge-1.19.2-3.0.0.jar");
            if(Files.exists(tnt))try(var external=new ManagedLegacyClassLoader(tnt,SourceConfigRegistrationCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var actual=external.linkOwnedClass("net.tonicarter.tntexpansion.Config");
                check(((ModConfigSpec)actual.getField("SPEC").get(null)).getSpec().size()>0,"immutable TNT source builder static initialization");
            }
        }
        System.out.println("PASS: source config builder/typed context/direct and callable values, native load/correction/save/unload, owner/type/file/spec identity and isolated callbacks");
    }
    private static void load(ModConfig c,Path path,Function<ModConfig,ModConfigEvent> event) throws Exception {
        var method=ConfigTracker.class.getDeclaredMethod("loadConfig",ModConfig.class,Path.class,Function.class);method.setAccessible(true);method.invoke(null,c,path,event);
    }
    private static Object invoke(Class<?> type,String name,Class<?>[] parameters,Object...args) {
        try{return type.getMethod(name,parameters).invoke(null,args);}catch(InvocationTargetException e){if(e.getCause() instanceof RuntimeException r)throw r;if(e.getCause() instanceof Error r)throw r;throw new IllegalStateException(e.getCause());}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
    }
    private static void reject(Runnable action,Class<? extends RuntimeException> expected){try{action.run();}catch(RuntimeException e){if(expected.isInstance(e))return;throw e;}throw new AssertionError("Unproven operation succeeded: "+expected);}
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
