package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;

/** Short-lived native item-entity extraction inputs; never retained by submitted state. */
public final class Target263GroundItemExtraction {
    private record Inputs(ItemStackRenderState output, ItemStack stack, ItemOwner owner, int light, int outline) {}
    private static final ThreadLocal<Inputs> CURRENT = new ThreadLocal<>();
    private Target263GroundItemExtraction() {}

    public static void extract(ItemStackRenderState output, ItemStack stack, ItemOwner owner,
            int light, int outline, Runnable extraction) {
        Inputs previous = CURRENT.get();
        CURRENT.set(new Inputs(output, stack, owner, light, outline));
        try { extraction.run(); }
        finally {
            if (previous == null) CURRENT.remove(); else CURRENT.set(previous);
        }
    }

    public static int requireLight(ItemStackRenderState output, ItemStack stack, ItemOwner owner) {
        Inputs inputs = CURRENT.get();
        if (inputs == null || inputs.output() != output || inputs.stack() != stack || inputs.owner() != owner)
            throw new UnsupportedOperationException("Source GROUND geometry requires matching native item-entity extraction inputs");
        if (inputs.outline() != 0)
            throw new UnsupportedOperationException("Source custom item outline extraction is not adapted");
        return inputs.light();
    }
}
