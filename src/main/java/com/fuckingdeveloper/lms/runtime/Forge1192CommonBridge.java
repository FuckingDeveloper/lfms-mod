package com.fuckingdeveloper.lms.runtime;

/**
 * Forge 1.19.2 common compatibility operations whose old API was imperative
 * but whose modern equivalent is either unconditional or lifecycle-owned.
 */
public final class Forge1192CommonBridge {
    private Forge1192CommonBridge() {}

    /**
     * Forge 1.19.2 used this switch to opt into Forge's milk fluid.
     * LMS records the request explicitly; it must not mutate registries here.
     */
    private static volatile boolean milkFluidRequested;

    public static void enableMilkFluid() {
        milkFluidRequested = true;
    }

    public static boolean milkFluidRequested() {
        return milkFluidRequested;
    }
}
