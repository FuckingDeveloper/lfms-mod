package com.fuckingdeveloper.lms.runtime;

import com.fuckingdeveloper.lms.runtime.registry.LegacyRegistryObject;

/**
 * Compatibility boundary for Forge 1.19.2 built-in registry bootstrap requests.
 *
 * ForgeMod.enableMilkFluid() was an opt-in request made during mod construction.
 * LMS records that request without exposing the removed ForgeMod class. Actual
 * target registration is deliberately kept separate: consumers can proceed
 * through linkage while unsupported registry materialization remains explicit.
 */
public final class Forge1192BuiltinRegistryBridge {
    private static final ThreadLocal<Boolean> MILK_FLUID_REQUESTED =
            ThreadLocal.withInitial(() -> Boolean.FALSE);

    private Forge1192BuiltinRegistryBridge() {}

    public static LegacyRegistryObject milk() {
        return LegacyRegistryObject.builtin("minecraft", "milk");
    }

    public static Object get(LegacyRegistryObject builtin) {
        if (builtin == null)
            throw new IllegalArgumentException("Unsupported null legacy builtin registry handle");
        // Resolution is intentionally deferred until the target registry identity
        // is proven for this Minecraft/NeoForge version.
        throw new IllegalStateException("UNRESOLVED_BUILTIN_REGISTRY_VALUE "
                + builtin.namespace() + ":" + builtin.path());
    }

    public static void enableMilkFluid() {
        Forge1192LifecycleBridge.requireActive();
        MILK_FLUID_REQUESTED.set(Boolean.TRUE);
    }

    public static boolean milkFluidRequested() {
        return MILK_FLUID_REQUESTED.get();
    }

    public static void clear() {
        MILK_FLUID_REQUESTED.remove();
    }
}
