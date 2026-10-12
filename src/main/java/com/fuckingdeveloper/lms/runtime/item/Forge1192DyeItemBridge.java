package com.fuckingdeveloper.lms.runtime.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;

/** Target-backed lookup for the removed Forge 1.19.2 DyeItem color index. */
public final class Forge1192DyeItemBridge {
    private Forge1192DyeItemBridge() {}

    public static DyeItem byColor(DyeColor color) {
        DyeItem match = null;
        for (var item : BuiltInRegistries.ITEM) {
            if (!(item instanceof DyeItem dye)
                    || item.components().get(DataComponents.DYE) != color) {
                continue;
            }
            if (match != null && match != dye) {
                throw new IllegalStateException("Multiple target DyeItems for " + color);
            }
            match = dye;
        }
        if (match == null) {
            throw new IllegalStateException("No target DyeItem for " + color);
        }
        return match;
    }
}
