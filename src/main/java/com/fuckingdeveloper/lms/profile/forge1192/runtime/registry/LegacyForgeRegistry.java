package com.fuckingdeveloper.lms.profile.forge1192.runtime.registry;

/**
 * Verifier-visible identity for Forge 1.19.2 IForgeRegistry.
 *
 * <p>Current NeoForge no longer exposes the mutable IForgeRegistry facade.
 * LMS therefore uses this marker only in transformed descriptors/signatures.
 * Executable legacy registry operations are rewritten to
 * Forge1192RegistrationContext and must never dispatch through this type.</p>
 */
public interface LegacyForgeRegistry {
}
