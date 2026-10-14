package com.fuckingdeveloper.lms.runtime;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.Objects;

/** Forge 1.19.2 capability-registration compatibility boundary. */
public final class Forge1192CapabilityBridge {
    private Forge1192CapabilityBridge() {}

    /**
     * Forge 1.19.2 register(Class) registered the capability interface type.
     * The modern event no longer has an equivalent type-only mutation. Preserve
     * the declaration as explicit evidence; provider attachment remains owned
     * by the concrete modern capability registrations.
     */
    public static void register(RegisterCapabilitiesEvent event, Class<?> capabilityType) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(capabilityType, "capabilityType");
        if (!capabilityType.isInterface())
            throw new IllegalArgumentException("Legacy capability declaration must be an interface: "
                    + capabilityType.getName());
    }
}
