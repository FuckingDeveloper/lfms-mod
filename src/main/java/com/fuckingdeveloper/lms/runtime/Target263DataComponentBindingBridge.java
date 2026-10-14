package com.fuckingdeveloper.lms.runtime;

import java.util.Objects;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.item.Item;

/** Target 26.3 boundary for ordered work immediately before component binding. */
public final class Target263DataComponentBindingBridge {
    private static final class Context {
        private final ReloadableServerResources resources;
        private final HolderLookup.Provider registries;
        private Map<ResourceKey<Item>, DataComponentMap> itemDefaults;

        private Context(ReloadableServerResources resources,
                HolderLookup.Provider registries) {
            this.resources = Objects.requireNonNull(resources, "resources");
            this.registries = Objects.requireNonNull(registries, "registries");
        }

        private Map<ResourceKey<Item>, DataComponentMap> itemDefaults() {
            if (itemDefaults != null) return itemDefaults;
            Map<ResourceKey<Item>, DataComponentMap> built = new LinkedHashMap<>();
            for (var pending : BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(registries)) {
                if (!pending.key().equals(Registries.ITEM)) continue;
                @SuppressWarnings("unchecked")
                var items = (DataComponentInitializers.PendingComponents<Item>) pending;
                items.forEach((holder, components) -> {
                    ResourceKey<Item> key = holder.unwrapKey().orElseThrow(() ->
                            new IllegalStateException("Pending item components have no registry key"));
                    if (built.putIfAbsent(key, components) != null) {
                        throw new IllegalStateException("Duplicate pending item components for "
                                + key.identifier());
                    }
                });
            }
            itemDefaults = Map.copyOf(built);
            return itemDefaults;
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
                resources.newComponents =
                        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(registries);
            }
        } finally {
            DIRTY.remove();
            CURRENT.remove();
        }
    }

    public static void initializersChanged() {
        Context context = CURRENT.get();
        if (context == null) {
            throw new IllegalStateException(
                    "Data component initializer changed outside pre-binding lifecycle");
        }
        context.itemDefaults = null;
        DIRTY.set(true);
    }

    public static Optional<HolderLookup.Provider> currentRegistries() {
        if (Forge1192BlockDataBridge.dataContextActive()) return Forge1192BlockDataBridge.currentLookup();
        Context context = CURRENT.get();
        return context == null ? Optional.empty() : Optional.of(context.registries);
    }

    public static Optional<DataComponentMap> currentItemDefaults(ResourceKey<Item> key) {
        Context context = CURRENT.get();
        if (context == null) return Optional.empty();
        return Optional.ofNullable(context.itemDefaults().get(key));
    }
}
