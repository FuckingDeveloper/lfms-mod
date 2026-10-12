package com.fuckingdeveloper.lms.runtime;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.lang.reflect.Constructor;

/** Materializes exact item defaults without prematurely binding global target holders. */
public final class Forge1192EarlyItemStackBridge {
    private static final Constructor<ItemStack> ISOLATED_CONSTRUCTOR = isolatedConstructor();

    private Forge1192EarlyItemStackBridge() {}

    public static ItemStack create(ItemLike item) {
        return create(item, 1);
    }

    public static ItemStack create(ItemLike itemLike, int count) {
        Item item = itemLike.asItem();
        Holder.Reference<Item> holder = item.builtInRegistryHolder();
        try {
            holder.components();
            return new ItemStack(item, count);
        } catch (NullPointerException notBound) {
            if (!"Components not bound yet".equals(notBound.getMessage())) throw notBound;
        }

        ResourceKey<Item> key = holder.unwrapKey().orElseThrow(() ->
                new IllegalStateException("Legacy early ItemStack item has no registry key"));
        var properties = Forge1192VanillaConstructionBridge.itemProperties(item)
                .orElseThrow(() -> new IllegalStateException(
                        "No preserved Item.Properties for early ItemStack " + key.identifier()));
        DataComponentMap.Builder components = DataComponentMap.builder();
        RegistryAccess.Frozen context = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        DataComponentInitializers.Initializer<Item> initializer = properties.componentInitializer;
        initializer.run(components, context, key);
        components.set(DataComponents.ITEM_NAME, Component.translatable(item.getDescriptionId()));
        components.set(DataComponents.ITEM_MODEL, key.identifier());
        net.neoforged.neoforge.internal.DataComponentModifiers.apply(context, item, components);
        try {
            return ISOLATED_CONSTRUCTOR.newInstance(
                    holder, count, new PatchedDataComponentMap(components.build()));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot materialize isolated early ItemStack", e);
        }
    }

    static ItemStack create(Item item, int count, DataComponentMap defaults) {
        try {
            return ISOLATED_CONSTRUCTOR.newInstance(
                    item.builtInRegistryHolder(), count, new PatchedDataComponentMap(defaults));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot materialize isolated component-modifier ItemStack", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Constructor<ItemStack> isolatedConstructor() {
        try {
            Constructor<?> constructor = ItemStack.class.getDeclaredConstructor(
                    Holder.class, int.class, PatchedDataComponentMap.class);
            if (!constructor.trySetAccessible()) {
                throw new IllegalStateException("ItemStack isolated constructor is not access-transformed");
            }
            return (Constructor<ItemStack>) constructor;
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
