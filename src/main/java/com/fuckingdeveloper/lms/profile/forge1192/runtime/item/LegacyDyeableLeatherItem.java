package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

/**
 * Component-backed contract for Forge 1.19.2's removed DyeableLeatherItem.
 * SRG-shaped names keep verifier-visible legacy overrides and special calls
 * linked while storing color through the target runtime's DYED_COLOR component.
 */
public interface LegacyDyeableLeatherItem {
    int DEFAULT_LEATHER_COLOR = 0xA06540;

    default boolean m_41113_(ItemStack stack) {
        return stack.has(DataComponents.DYED_COLOR);
    }

    default int m_41121_(ItemStack stack) {
        return DyedItemColor.getOrDefault(stack, ARGB.opaque(DEFAULT_LEATHER_COLOR));
    }

    default void m_41123_(ItemStack stack) {
        stack.remove(DataComponents.DYED_COLOR);
    }

    default void m_41115_(ItemStack stack, int color) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color & 0xFFFFFF));
    }

    static ItemStack m_41118_(ItemStack stack, List<DyeItem> dyes) {
        var colors = new java.util.ArrayList<net.minecraft.world.item.DyeColor>(dyes.size());
        for (DyeItem dye : dyes) {
            var color = dye.components().get(DataComponents.DYE);
            if (color == null) {
                throw new IllegalStateException("Target DyeItem is missing its DYE component: " + dye);
            }
            colors.add(color);
        }
        return DyedItemColor.applyDyes(stack, colors);
    }
}
