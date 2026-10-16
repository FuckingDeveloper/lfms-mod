package com.fuckingdeveloper.lms.target.neoforge263;

import java.lang.invoke.*;
import java.util.Objects;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.*;

/** Source value/cache semantics over exact native value identity; never changes native-only reloads. */
public final class Target263SourceConfigValues {
    private static final VarHandle SPEC=field("spec",ModConfigSpec.class), CACHE=field("cachedValue",Object.class);
    private static final VarHandle LOADED=loadedField();
    private static final MethodHandle CONFIG_OWNER=loadedOwner();
    private Target263SourceConfigValues() {}
    private static VarHandle loadedField() {
        try{return MethodHandles.privateLookupIn(ModConfigSpec.class,MethodHandles.lookup()).findVarHandle(ModConfigSpec.class,"loadedConfig",net.neoforged.fml.config.IConfigSpec.ILoadedConfig.class);}
        catch(ReflectiveOperationException failure){throw new ExceptionInInitializerError(failure);}
    }
    private static MethodHandle loadedOwner() {
        try {var type=Class.forName("net.neoforged.fml.config.LoadedConfig");return MethodHandles.privateLookupIn(type,MethodHandles.lookup()).findVirtual(type,"modConfig",MethodType.methodType(net.neoforged.fml.config.ModConfig.class));}
        catch(ReflectiveOperationException failure){throw new ExceptionInInitializerError(failure);}
    }
    private static VarHandle field(String name,Class<?> type) {
        try{return MethodHandles.privateLookupIn(ConfigValue.class,MethodHandles.lookup()).findVarHandle(ConfigValue.class,name,type);}
        catch(ReflectiveOperationException failure){throw new ExceptionInInitializerError(failure);}
    }
    public static Object get(ConfigValue<?> value) {
        var spec=(ModConfigSpec)Objects.requireNonNull(SPEC.get(value),"Cannot get config value before spec is built");
        if(!spec.isLoaded()) {
            if(net.neoforged.fml.loading.FMLEnvironment.isProduction())return value.getDefault();
            throw new IllegalStateException("Cannot get config value before config is loaded");
        }
        return value.get();
    }
    @SuppressWarnings({"unchecked","rawtypes"}) public static void set(ConfigValue value,Object next) {value.set(next);CACHE.set(value,next);}
    public static boolean getAsBoolean(ModConfigSpec.BooleanValue value){return (Boolean)get(value);}
    public static int getAsInt(ModConfigSpec.IntValue value){return (Integer)get(value);}
    public static long getAsLong(ModConfigSpec.LongValue value){return (Long)get(value);}
    public static double getAsDouble(ModConfigSpec.DoubleValue value){return (Double)get(value);}
    public static void afterReload(ModConfigSpec spec) {
        for(var restart:ModConfigSpec.RestartType.values())spec.resetCaches(restart);
    }
    public static void save(ModConfigSpec spec) {
        var loaded=Objects.requireNonNull(LOADED.get(spec),"Cannot save config value without assigned Config object present");
        final net.neoforged.fml.config.ModConfig config;
        try{config=(net.neoforged.fml.config.ModConfig)CONFIG_OWNER.invoke(loaded);}
        catch(RuntimeException|Error failure){throw failure;}
        catch(Throwable failure){throw new IllegalStateException("Cannot read verified loaded config owner",failure);}
        Forge1192NativeConfigBridge.write(Forge1192ModConfigView.of(config));
    }
    public static void saveValue(ConfigValue<?> value) {save((ModConfigSpec)Objects.requireNonNull(SPEC.get(value),"Config spec not built"));}
}
