package com.fuckingdeveloper.lms.runtime;

import com.fuckingdeveloper.lms.runtime.registry.LegacyRegistryObject;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Compatibility boundary for Forge 1.19.2 built-in registry bootstrap requests.
 *
 * ForgeMod.enableMilkFluid() was an opt-in request made during mod construction.
 * LMS records that request without exposing the removed ForgeMod class. Actual
 * target registration is performed by the native NeoForge opt-in lifecycle.
 * A late unmaterialized request fails rather than mutating a frozen registry.
 */
public final class Forge1192BuiltinRegistryBridge {
    private static final ThreadLocal<Boolean> MILK_FLUID_REQUESTED =
            ThreadLocal.withInitial(() -> Boolean.FALSE);

    private Forge1192BuiltinRegistryBridge() {}

    public static LegacyRegistryObject milk() {
        return LegacyRegistryObject.builtin("minecraft", "milk");
    }

    public static LegacyRegistryObject reachDistance() {
        return LegacyRegistryObject.builtin("minecraft", "block_interaction_range");
    }

    public static LegacyRegistryObject attackRange() {
        return LegacyRegistryObject.builtin("minecraft", "entity_interaction_range");
    }

    public static LegacyRegistryObject entityGravity() {
        return LegacyRegistryObject.builtin("minecraft", "gravity");
    }

    public static LegacyRegistryObject stepHeightAddition() {
        return LegacyRegistryObject.builtin("minecraft", "step_height");
    }

    public static Object get(LegacyRegistryObject builtin) {
        if (builtin == null)
            throw new IllegalArgumentException("Unsupported null legacy builtin registry handle");
        if (builtin.namespace().equals("minecraft")) {
            return switch (builtin.path()) {
                case "milk" -> net.neoforged.neoforge.common.NeoForgeMod.MILK.value();
                case "block_interaction_range" -> Attributes.BLOCK_INTERACTION_RANGE.value();
                case "entity_interaction_range" -> Attributes.ENTITY_INTERACTION_RANGE.value();
                case "gravity" -> Attributes.GRAVITY.value();
                case "step_height" -> Attributes.STEP_HEIGHT.value();
                default -> throw new IllegalStateException("UNRESOLVED_BUILTIN_REGISTRY_VALUE "
                        + builtin.namespace() + ":" + builtin.path());
            };
        }
        throw new IllegalStateException("UNRESOLVED_BUILTIN_REGISTRY_VALUE "
                + builtin.namespace() + ":" + builtin.path());
    }

    public static void enableMilkFluid() {
        var context=Forge1192LifecycleBridge.requireActive();
        if(Forge1192RegistrationContext.registryPhaseCompleted(context.modId(),net.minecraft.core.registries.Registries.FLUID)
                && !net.neoforged.neoforge.common.NeoForgeMod.MILK.isBound())
            throw new IllegalStateException("Source milk opt-in arrived after target fluid registration: "+context.modId());
        net.neoforged.neoforge.common.NeoForgeMod.enableMilkFluid();
        MILK_FLUID_REQUESTED.set(Boolean.TRUE);
    }

    public static boolean milkFluidRequested() {
        return MILK_FLUID_REQUESTED.get();
    }

    public static void clear() {
        MILK_FLUID_REQUESTED.remove();
    }
}
