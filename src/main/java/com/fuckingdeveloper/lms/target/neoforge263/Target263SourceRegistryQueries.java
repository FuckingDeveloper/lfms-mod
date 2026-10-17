package com.fuckingdeveloper.lms.target.neoforge263;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import java.util.Optional;
import net.minecraft.core.*;
import net.minecraft.resources.*;
import net.minecraft.util.RandomSource;

/** Native virtual reads preserve defaulted lookup/optional absence and registry-owned identity. */
public final class Target263SourceRegistryQueries {
    private Target263SourceRegistryQueries() {}
    private static void nativeReceiver(Registry<?> registry) {
        if(registry.getClass().getClassLoader() instanceof ManagedLegacyClassLoader)
            throw new UnsupportedOperationException("Source registry subclass read overrides require explicit migration: " + registry.getClass().getName());
    }
    public static <T> T get(Registry<T> registry, Identifier key){nativeReceiver(registry);return registry.getValue(key);}
    public static <T> T get(Registry<T> registry, ResourceKey<T> key){nativeReceiver(registry);return registry.getValue(key);}
    public static <T> Optional<T> getOptional(Registry<T> registry, Identifier key){nativeReceiver(registry);return registry.getOptional(key);}
    public static <T> Optional<T> getOptional(Registry<T> registry, ResourceKey<T> key){nativeReceiver(registry);return registry.getOptional(key);}
    public static <T> Identifier getKey(Registry<T> registry,T value){nativeReceiver(registry);return registry.getKey(value);}
    public static <T> Optional<ResourceKey<T>> getResourceKey(Registry<T> registry,T value){nativeReceiver(registry);return registry.getResourceKey(value);}
    public static <T> int getId(Registry<T> registry,T value){nativeReceiver(registry);return registry.getId(value);}
    public static <T> Optional<Holder.Reference<T>> getHolder(Registry<T> registry,int id){nativeReceiver(registry);return registry.get(id);}
    public static <T> Optional<Holder.Reference<T>> getHolder(Registry<T> registry,ResourceKey<T> key){nativeReceiver(registry);return registry.get(key);}
    public static <T> Holder<T> getHolderOrThrow(Registry<T> registry,ResourceKey<T> key){nativeReceiver(registry);return registry.get(key).orElseThrow(()->new IllegalStateException("Missing key in "+registry.key()+": "+key));}
    public static <T> T byId(Registry<T> registry,int id){nativeReceiver(registry);return registry.byId(id);}
    public static Identifier getDefaultKey(DefaultedRegistry<?> registry){nativeReceiver(registry);return registry.getDefaultKey();}
    public static <T> Optional<Holder.Reference<T>> getRandom(Registry<T> registry,RandomSource random){nativeReceiver(registry);return registry.getRandom(random);}
    public static boolean containsKey(Registry<?> registry,Identifier key){nativeReceiver(registry);return registry.containsKey(key);}
    public static <T> boolean containsKey(Registry<T> registry,ResourceKey<T> key){nativeReceiver(registry);return registry.containsKey(key);}
}
