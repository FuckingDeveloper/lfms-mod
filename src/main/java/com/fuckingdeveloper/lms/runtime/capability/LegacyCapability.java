package com.fuckingdeveloper.lms.runtime.capability;

/**
 * Verifier-visible identity for a Forge 1.19.2 Capability<T> handle.
 *
 * <p>The handle deliberately carries only the legacy token identity. Provider
 * lookup/attachment is a separate migration boundary and must not be guessed
 * here.</p>
 */
public final class LegacyCapability<T> {
    private final Class<?> tokenClass;
    private final boolean registering;

    LegacyCapability(Class<?> tokenClass, boolean registering) {
        this.tokenClass = tokenClass;
        this.registering = registering;
    }

    public Class<?> tokenClass() { return tokenClass; }
    public boolean registering() { return registering; }
}
