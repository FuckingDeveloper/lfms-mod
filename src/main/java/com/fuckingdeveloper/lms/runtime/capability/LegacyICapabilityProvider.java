package com.fuckingdeveloper.lms.runtime.capability;

/**
 * Structural identity for Forge 1.19.2 capability providers.
 * Capability lookup itself is an executable migration boundary and is adapted
 * independently; this contract exists only to preserve legacy type shape.
 */
public interface LegacyICapabilityProvider {
    <T> LegacyLazyOptional<T> getCapability(LegacyCapability<T> capability, net.minecraft.core.Direction side);

    default <T> LegacyLazyOptional<T> getCapability(LegacyCapability<T> capability) {
        return getCapability(capability, null);
    }
}
