package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/** Source-owned per-stack callback, independent of target cooking components. */
public interface LegacyFuelItem {
    int lms$burnTime(ItemStack stack, RecipeType<?> type);
}
