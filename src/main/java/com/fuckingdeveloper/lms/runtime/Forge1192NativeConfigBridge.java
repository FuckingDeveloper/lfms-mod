package com.fuckingdeveloper.lms.runtime;

import com.electronwill.nightconfig.core.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.locks.Lock;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.*;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Forge1192NativeConfigBridge {
    private Forge1192NativeConfigBridge(){}
    private static final Field OWNER=field("container",ModContainer.class),LOCK=field("lock",Lock.class);
    private static Field field(String name,Class<?> expected){
        try{var field=ModConfig.class.getDeclaredField(name);if(field.getType()!=expected)throw new IllegalStateException("Native config ABI changed: "+name);field.setAccessible(true);return field;}
        catch(ReflectiveOperationException failure){throw new ExceptionInInitializerError(failure);}
    }
    private static final java.lang.invoke.MethodHandle WRITE=writer();
    private static java.lang.invoke.MethodHandle writer(){
        try{return java.lang.invoke.MethodHandles.privateLookupIn(ConfigTracker.class,java.lang.invoke.MethodHandles.lookup()).findStatic(ConfigTracker.class,"writeConfig",java.lang.invoke.MethodType.methodType(void.class,java.nio.file.Path.class,UnmodifiableCommentedConfig.class));}
        catch(ReflectiveOperationException failure){throw new ExceptionInInitializerError(failure);}
    }
    public static void write(Forge1192ModConfigView config){
        Lock lock;
        try{lock=(Lock)LOCK.get(config.nativeConfig());}catch(IllegalAccessException failure){throw new IllegalStateException(failure);}
        lock.lock();try {
            var data=config.getConfigData();var path=config.getFullPath();
            if(data==null||path==null)throw new UnsupportedOperationException("Source save requires a loaded native file config: "+config.getModId()+"/"+config.getFileName());
            try{WRITE.invokeExact(path,(UnmodifiableCommentedConfig)data);}
            catch(RuntimeException|Error failure){throw failure;}catch(Throwable failure){throw new IllegalStateException("Native config file write failed",failure);}
        }finally{lock.unlock();}
    }
    private static final Field CONFIG_SETS=trackerSets();
    private static Field trackerSets(){try{var f=ConfigTracker.class.getDeclaredField("configSets");if(f.getType()!=java.util.EnumMap.class)throw new IllegalStateException("Native config set ABI changed");f.setAccessible(true);return f;}catch(ReflectiveOperationException failure){throw new ExceptionInInitializerError(failure);}}
    @SuppressWarnings("unchecked") public static java.util.Map<Forge1192ModConfigView.Type,java.util.Set<Forge1192ModConfigView>> configSets(ConfigTracker tracker){
        final java.util.Map<ModConfig.Type,java.util.Set<ModConfig>> sets;
        try{sets=(java.util.Map<ModConfig.Type,java.util.Set<ModConfig>>)CONFIG_SETS.get(tracker);}catch(IllegalAccessException failure){throw new IllegalStateException(failure);}
        return new java.util.AbstractMap<>() {
            @Override public java.util.Set<Forge1192ModConfigView> get(Object type){
                if(!(type instanceof Forge1192ModConfigView.Type source))return null;
                var nativeSet=sets.get(source.nativeType());
                return new java.util.AbstractSet<>() {
                    public int size(){synchronized(nativeSet){return nativeSet.size();}}
                    public java.util.Iterator<Forge1192ModConfigView> iterator(){synchronized(nativeSet){return nativeSet.stream().map(Forge1192ModConfigView::of).toList().iterator();}}
                };
            }
            @Override public java.util.Set<Entry<Forge1192ModConfigView.Type,java.util.Set<Forge1192ModConfigView>>> entrySet(){
                if(!sets.get(ModConfig.Type.STARTUP).isEmpty())throw new UnsupportedOperationException("Native STARTUP config set cannot project into source enum");
                var entries=new java.util.LinkedHashSet<Entry<Forge1192ModConfigView.Type,java.util.Set<Forge1192ModConfigView>>>();
                for(var type:Forge1192ModConfigView.Type.values())entries.add(new SimpleImmutableEntry<>(type,get(type)));return java.util.Collections.unmodifiableSet(entries);
            }
        };
    }
    public static ModConfig unwrap(Forge1192ModConfigView config){return config.nativeConfig();}
    public static Forge1192ModConfigView getConfig(ModConfigEvent event){return Forge1192ModConfigView.of(event.getConfig());}
    public static ModConfigEvent.Reloading newReloading(Forge1192ModConfigView config){return new ModConfigEvent.Reloading(config.nativeConfig());}
    public static ModConfigEvent.Loading newLoading(Forge1192ModConfigView config){return new ModConfigEvent.Loading(config.nativeConfig());}
    public static ModConfigEvent.Unloading newUnloading(Forge1192ModConfigView config){return new ModConfigEvent.Unloading(config.nativeConfig());}
    public static ModConfigEvent reloading(Forge1192ModConfigView config){return new ModConfigEvent.Reloading(config.nativeConfig());}
    public static ModConfigEvent loading(Forge1192ModConfigView config){return new ModConfigEvent.Loading(config.nativeConfig());}
    public static net.neoforged.bus.api.Event self(ModConfigEvent event){return event;}
    public static void dispatch(Forge1192ModContainerView container,ModConfigEvent event){
        var nativeOwner=container.nativeOwner().orElseThrow(()->new UnsupportedOperationException("Managed config event registration is not adapted"));
        try {
            if(OWNER.get(event.getConfig())!=nativeOwner)throw new IllegalArgumentException("Source config event owner differs from actual native owner");
            var lock=(Lock)LOCK.get(event.getConfig());lock.lock();try{nativeOwner.acceptEvent(event);}finally{lock.unlock();}
        }catch(IllegalAccessException failure){throw new IllegalStateException("Cannot access verified native config ownership/lock",failure);}
    }
    public static Object get(ModConfigSpec spec,List<String> path){return spec.getSpec().get(path);}
    public static boolean needsWorldRestart(ModConfigSpec.ValueSpec spec){
        return switch(spec.restartType()){case NONE->false;case WORLD->true;case GAME->throw new UnsupportedOperationException("Native GAME restart has no source world-restart-only flag");};
    }
    public static void afterReload(IConfigSpec spec){requireSpec(spec).afterReload();}
    public static int correct(IConfigSpec spec,CommentedConfig config){return requireSpec(spec).correct(config,(action,path,oldValue,newValue)->{});}
    private static ModConfigSpec requireSpec(IConfigSpec spec){if(spec instanceof ModConfigSpec nativeSpec)return nativeSpec;throw new UnsupportedOperationException("Unadapted native/source config spec: "+spec.getClass().getName());}
}
