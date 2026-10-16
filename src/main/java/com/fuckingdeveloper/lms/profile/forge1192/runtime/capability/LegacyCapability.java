package com.fuckingdeveloper.lms.profile.forge1192.runtime.capability;

/**
 * Verifier-visible identity for a Forge 1.19.2 Capability<T> handle.
 *
 * <p>The handle deliberately carries only the legacy token identity. Provider
 * lookup/attachment is a separate migration boundary and must not be guessed
 * here.</p>
 */
public final class LegacyCapability<T> {
    private final String typeName;
    private final boolean registering;

    LegacyCapability(String typeName, boolean registering) {
        this.typeName = typeName;
        this.registering = registering;
    }

    public String getName() { return typeName; }
    public boolean isRegistered() {
        throw new IllegalStateException("Source capability registration state is not adapted: " + typeName);
    }
    public <R> LegacyLazyOptional<R> cast(LegacyLazyOptional<T> value) { return value.cast(); }
    public <R> LegacyLazyOptional<R> orEmpty(LegacyCapability<R> requested, LegacyLazyOptional<T> value) {
        return this == requested ? value.cast() : LegacyLazyOptional.empty();
    }
    public boolean registering() { return registering; }
}
