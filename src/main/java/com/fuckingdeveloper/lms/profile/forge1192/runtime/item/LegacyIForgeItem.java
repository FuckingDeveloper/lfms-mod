package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

/**
 * Structural compatibility contract for Forge 1.19.2 IForgeItem.
 *
 * <p>The legacy type was an interface and can appear in user-defined interface
 * inheritance. Mapping it directly to modern Item corrupts JVM type shape.
 * Operations are adapted independently; this marker preserves only the legacy
 * interface identity until each executable boundary has semantic evidence.</p>
 */
public interface LegacyIForgeItem {
    default int getBurnTime(net.minecraft.world.item.ItemStack stack, net.minecraft.world.item.crafting.RecipeType<?> type) { return -1; }
    default boolean hasCraftingRemainingItem(net.minecraft.world.item.ItemStack stack) {
        return com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192CraftingRemainderBridge.defaultHas((net.minecraft.world.item.Item)this,stack);
    }
    default net.minecraft.world.item.ItemStack getCraftingRemainingItem(net.minecraft.world.item.ItemStack stack) {
        return com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192CraftingRemainderBridge.defaultGet((net.minecraft.world.item.Item)this,stack);
    }
}
