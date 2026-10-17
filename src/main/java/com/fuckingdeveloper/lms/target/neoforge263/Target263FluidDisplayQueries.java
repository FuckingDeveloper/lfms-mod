package com.fuckingdeveloper.lms.target.neoforge263;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;

/** Verified target queries retain stack-sensitive virtual FluidType callbacks. */
public final class Target263FluidDisplayQueries {
    private Target263FluidDisplayQueries() {}
    public static Component getDisplayName(FluidStack stack) { return stack.getHoverName(); }
    public static String getTranslationKey(FluidStack stack) { return stack.getDescriptionId(); }
}
