package com.fuckingdeveloper.lms.target.neoforge263;

import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import java.lang.invoke.*;
import java.util.List;

/** Exact native per-holder initializer order; avoids executing unrelated holders' initializers. */
public final class Target263PendingComponentDefaults {
    private static final VarHandle ENTRIES;
    private static final MethodHandle KEY, RUN;
    static {
        try {
            var lookup=MethodHandles.privateLookupIn(DataComponentInitializers.class,MethodHandles.lookup());
            ENTRIES=lookup.findVarHandle(DataComponentInitializers.class,"initializers",List.class);
            var entry=Class.forName("net.minecraft.core.component.DataComponentInitializers$InitializerEntry",false,DataComponentInitializers.class.getClassLoader());
            var entryLookup=MethodHandles.privateLookupIn(entry,MethodHandles.lookup());
            KEY=entryLookup.findVirtual(entry,"key",MethodType.methodType(ResourceKey.class));
            RUN=entryLookup.findVirtual(entry,"run",MethodType.methodType(void.class,DataComponentMap.Builder.class,HolderLookup.Provider.class));
        }catch(ReflectiveOperationException failure){throw new ExceptionInInitializerError(failure);}
    }
    private Target263PendingComponentDefaults() {}
    public static <T> DataComponentMap materialize(Holder.Reference<T> holder,HolderLookup.Provider provider) {
        return materialize(BuiltInRegistries.DATA_COMPONENT_INITIALIZERS,holder,provider);
    }
    public static <T> DataComponentMap materialize(DataComponentInitializers initializers,Holder.Reference<T> holder,HolderLookup.Provider provider) {
        var key=holder.key();
        if(provider.lookupOrThrow(key.registryKey()).getOrThrow(key).value()!=holder.value())
            throw new IllegalStateException("Pending component lookup has another value for "+key);
        var builder=DataComponentMap.builder();
        // The native add API appends initializer entries. An operation sees a fresh snapshot, so
        // no initializer/loader is retained in a global cache and later additions remain visible.
        var entries=List.copyOf((List<?>)ENTRIES.get(initializers));
        boolean initialized=false;
        try {
            for(var entry:entries)if(key.equals((ResourceKey<?>)KEY.invoke(entry))){RUN.invoke(entry,builder,provider);initialized=true;}
        }catch(RuntimeException|Error failure){throw failure;}
        catch(Throwable failure){throw new IllegalStateException("Native pending initializer failed for "+key,failure);}
        // This is the same registry-specific modifier operation as native createInitializerForRegistry.
        if(initialized && key.isFor(Registries.ITEM))net.neoforged.neoforge.internal.DataComponentModifiers.apply(provider,(Item)holder.value(),builder);
        return builder.build();
    }
}
