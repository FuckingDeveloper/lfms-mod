package com.fuckingdeveloper.lms.runtime;

import net.neoforged.neoforge.fluids.FluidStack;

/** Profile-owned compatibility for Forge 1.19.2 fluid identity operations. */
public final class Forge1192FluidBridge {
    private Forge1192FluidBridge() {}

    /**
     * Legacy isFluidEqual ignores amount and fluid stack metadata.
     * NeoForge's same-fluid predicate preserves that identity comparison.
     */
    public static boolean isFluidEqual(FluidStack left, FluidStack right) {
        return FluidStack.isSameFluid(left, right);
    }
}
