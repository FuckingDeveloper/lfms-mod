package com.fuckingdeveloper.lms.runtime.registry;

/**
 * Structural Forge 1.19.2 RegistryObject facade.
 *
 * The concrete Forge holder no longer exists in the target runtime. LMS keeps
 * the verifier-visible type shape while executable operations are rewritten to
 * explicit runtime bridges.
 */
public final class LegacyRegistryObject {
    private LegacyRegistryObject() {
        throw new UnsupportedOperationException("LMS structural facade");
    }
}
