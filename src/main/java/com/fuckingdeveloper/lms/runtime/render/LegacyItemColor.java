package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.world.item.ItemStack;

/** Source-shaped Minecraft 1.19.2 procedural item-color callback. */
@FunctionalInterface
public interface LegacyItemColor {
    int getColor(ItemStack stack, int tintIndex);
}
