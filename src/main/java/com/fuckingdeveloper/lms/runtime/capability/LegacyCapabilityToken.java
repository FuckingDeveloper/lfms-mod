package com.fuckingdeveloper.lms.runtime.capability;

/**
 * Structural identity for Forge 1.19.2 CapabilityToken<T>.
 *
 * <p>The legacy token is commonly instantiated as an anonymous subclass so its
 * generic signature can identify T. Current NeoForge no longer exposes this
 * class. LMS preserves only the verifier/linker-visible superclass shape here;
 * capability lookup and resolution remain separate executable migration
 * boundaries and must be adapted explicitly.</p>
 */
public abstract class LegacyCapabilityToken<T> {
    protected LegacyCapabilityToken() {
    }
}
