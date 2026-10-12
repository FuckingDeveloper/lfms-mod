package com.fuckingdeveloper.lms.runtime;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.ReloadableServerResources;

/** Target 26.3 boundary for ordered work immediately before component binding. */
public final class Target263DataComponentBindingBridge {
    private record Context(ReloadableServerResources resources,
                           HolderLookup.Provider registries) {
        private Context {
            Objects.requireNonNull(resources, "resources");
            Objects.requireNonNull(registries, "registries");
        }
    }

    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> DIRTY = ThreadLocal.withInitial(() -> false);

    private Target263DataComponentBindingBridge() {}

    public static void runBeforeBinding(ReloadableServerResources resources,
            RegistryAccess registries, Runnable action) {
        Objects.requireNonNull(action, "action");
        if (CURRENT.get() != null) throw new IllegalStateException("Nested component binding scope");
        Context context = new Context(resources, registries);
        CURRENT.set(context);
        DIRTY.set(false);
        try {
            action.run();
            if (DIRTY.get()) {
                resources.newComponents.clear();
                resources.newComponents.addAll(
                        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(registries));
            }
        } finally {
            DIRTY.remove();
            CURRENT.remove();
        }
    }

    public static void initializersChanged() {
        if (CURRENT.get() == null) {
            throw new IllegalStateException(
                    "Data component initializer changed outside pre-binding lifecycle");
        }
        DIRTY.set(true);
    }

    public static Optional<HolderLookup.Provider> currentRegistries() {
        Context context = CURRENT.get();
        return context == null ? Optional.empty() : Optional.of(context.registries());
    }
}
