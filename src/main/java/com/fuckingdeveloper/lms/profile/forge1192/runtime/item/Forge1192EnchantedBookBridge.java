package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.itemhandler.LegacyMutableTagCarrier;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.Forge1192DynamicRegistryBridge;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/** Source read semantics, independent of the consuming mod. */
public final class Forge1192EnchantedBookBridge {
    private Forge1192EnchantedBookBridge() {}
    public static ListTag getEnchantments(ItemStack stack) {
        var state = ((LegacyMutableTagCarrier)(Object)stack).lms$mutableTags();
        var tag = state.acquireStoredEnchantments(stack);
        return tag == null ? new ListTag() : tag.getListOrEmpty("StoredEnchantments");
    }
    public static String descriptionId(Object value) {
        java.util.Objects.requireNonNull(value, "enchantment");
        var registry = Forge1192DynamicRegistryBridge.enchantments();
        net.minecraft.resources.Identifier id;
        if (value instanceof ResourceKey<?> key) {
            if (!key.isFor(net.minecraft.core.registries.Registries.ENCHANTMENT))
                throw new IllegalArgumentException("Not an enchantment key: " + key);
            registry.getOptional(key.identifier()).orElseThrow();
            id = key.identifier();
        } else if (value instanceof Enchantment enchantment) {
            @SuppressWarnings("unchecked") var typed = (net.minecraft.core.Registry<Enchantment>)registry;
            id = java.util.Objects.requireNonNull(typed.getKey(enchantment), "Unregistered enchantment");
        } else throw new IllegalArgumentException("Not an enchantment: " + value);
        return "enchantment." + id.getNamespace() + "." + id.getPath().replace('/', '.');
    }
}
