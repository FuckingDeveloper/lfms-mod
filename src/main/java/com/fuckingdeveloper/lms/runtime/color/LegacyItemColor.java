package com.fuckingdeveloper.lms.runtime.color;

import net.minecraft.world.item.ItemStack;

/**
 * Binary compatibility contract for the Forge 1.19.2 ItemColor callback.
 * Kept separate from modern item tint-source registration.
 */
@FunctionalInterface
public interface LegacyItemColor {
    int m_92671_(ItemStack stack, int tintIndex);
}
