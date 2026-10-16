package com.fuckingdeveloper.lms.target.neoforge263.render;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;

/** Short-lived native item-entity extraction inputs; never retained by submitted state. */
public final class Target263GroundItemExtraction {
    private record Inputs(ItemStackRenderState output, ItemStack stack, ItemOwner owner, int light, int outline, net.minecraft.world.item.ItemDisplayContext display) {}
    private static final ThreadLocal<Inputs> CURRENT = new ThreadLocal<>();
    private Target263GroundItemExtraction() {}

    public static void extract(ItemStackRenderState output, ItemStack stack, ItemOwner owner,
            int light, int outline, Runnable extraction) {
        Inputs previous = CURRENT.get();
        CURRENT.set(new Inputs(output, stack, owner, light, outline, net.minecraft.world.item.ItemDisplayContext.GROUND));
        try { extraction.run(); }
        finally {
            if (previous == null) CURRENT.remove(); else CURRENT.set(previous);
        }
    }

    public static void extractShelf(ItemStackRenderState output, ItemStack stack, ItemOwner owner,
            int light, Runnable extraction) {
        Inputs previous = CURRENT.get();
        CURRENT.set(new Inputs(output, stack, owner, light, 0, net.minecraft.world.item.ItemDisplayContext.ON_SHELF));
        try { extraction.run(); }
        finally { if (previous == null) CURRENT.remove(); else CURRENT.set(previous); }
    }
    public static int requireShelfLight(ItemStackRenderState output, ItemStack stack, ItemOwner owner) {
        return requireLight(output, stack, owner, net.minecraft.world.item.ItemDisplayContext.ON_SHELF);
    }
    public static int requireLight(ItemStackRenderState output, ItemStack stack, ItemOwner owner) {
        return requireLight(output, stack, owner, net.minecraft.world.item.ItemDisplayContext.GROUND);
    }
    private static int requireLight(ItemStackRenderState output, ItemStack stack, ItemOwner owner,
            net.minecraft.world.item.ItemDisplayContext display) {
        Inputs inputs = CURRENT.get();
        if (inputs == null || inputs.display() != display || inputs.output() != output || inputs.stack() != stack || inputs.owner() != owner)
            throw new UnsupportedOperationException("Source geometry requires matching native item-entity/shelf extraction inputs");
        return inputs.light();
    }
}
