package com.fuckingdeveloper.lms.runtime;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;

/** Target adapter for item semantics that moved into default components. */
public final class Forge1192ItemBridge {
    private Forge1192ItemBridge() {}

    /** Forge 1.19.2 Item#isEdible was equivalent to having food properties. */
    public static boolean isEdible(Item item) {
        if (!item.builtInRegistryHolder().areComponentsBound()) {
            throw new IllegalStateException("Legacy Item#isEdible queried before default components were bound: "
                    + item);
        }
        return item.components().has(DataComponents.FOOD);
    }
}
