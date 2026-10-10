package com.fuckingdeveloper.lms.runtime.registry;

/**
 * Verifier-visible replacement for Forge 1.19.2 RegistryObject.
 *
 * LMS preserves the holder type across transformed producer/consumer boundaries.
 * Executable holder operations are adapted separately by the profile transformer.
 */
public final class LegacyRegistryObject {
    private final String namespace;
    private final String path;

    private LegacyRegistryObject(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    public static LegacyRegistryObject builtin(String namespace, String path) {
        return new LegacyRegistryObject(namespace, path);
    }

    public String namespace() { return namespace; }
    public String path() { return path; }
}
