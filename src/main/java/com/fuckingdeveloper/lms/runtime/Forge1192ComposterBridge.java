package com.fuckingdeveloper.lms.runtime;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Compostable;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

/** Target adapter for Forge 1.19.2's mutable composter chance table. */
public final class Forge1192ComposterBridge {
    private Forge1192ComposterBridge() {}

    /**
     * Preserve the standard legacy composter probabilities using the target's
     * source-verified providers.  Arbitrary probabilities remain explicit:
     * 26.3 requires a data-registry entry and cannot represent them by mutating
     * a global runtime map.
     */
    public static float registerCompostable(Object value, float chance) {
        Forge1192RegistrationContext.requireModId();
        if (!(value instanceof ItemLike itemLike)) {
            throw new IllegalArgumentException("Legacy compostable value is not an ItemLike: " + value);
        }
        Item item = itemLike.asItem();
        if (item.builtInRegistryHolder().areComponentsBound()) {
            throw new IllegalStateException("Legacy compostable registration occurred after item components were bound: "
                    + item);
        }
        ResourceKey<Item> itemKey = BuiltInRegistries.ITEM.getResourceKey(item)
                .orElseThrow(() -> new IllegalStateException(
                        "Legacy compostable item is not registered: " + item));
        ResourceKey<ContextIntProvider> provider = providerFor(chance);
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.add(itemKey,
                (components, context, key) ->
                        components.set(DataComponents.COMPOSTABLE, new Compostable(provider)));
        return 0.0F;
    }

    private static ResourceKey<ContextIntProvider> providerFor(float chance) {
        if (Float.compare(chance, 0.3F) == 0) return ContextIntProviders.COMPOSTABLE_LOW;
        if (Float.compare(chance, 0.5F) == 0) return ContextIntProviders.COMPOSTABLE_LOW_MEDIUM;
        if (Float.compare(chance, 0.65F) == 0) return ContextIntProviders.COMPOSTABLE_MEDIUM;
        if (Float.compare(chance, 0.85F) == 0) return ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH;
        if (Float.compare(chance, 1.0F) == 0) return ContextIntProviders.COMPOSTABLE_ALWAYS_ADD_ONE;
        throw new UnsupportedOperationException(
                "Minecraft 26.3 has no inline representation for legacy composting chance " + chance);
    }
}
