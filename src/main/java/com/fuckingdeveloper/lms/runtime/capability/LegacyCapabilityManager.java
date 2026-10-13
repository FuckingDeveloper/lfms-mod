package com.fuckingdeveloper.lms.runtime.capability;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Forge 1.19.2 CapabilityManager identity boundary.
 *
 * <p>The original generic payload type's internal name is the source identity,
 * even when different anonymous token classes refer to it. Handles retain no
 * token classes/loaders; modern provider services remain a separate boundary.</p>
 */
public final class LegacyCapabilityManager {
    private static final ConcurrentMap<String, LegacyCapability<?>> HANDLES = new ConcurrentHashMap<>();

    private LegacyCapabilityManager() {}

    /**
     * Exact Forge 1.19.2 executable ABI:
     * CapabilityManager.get(CapabilityToken) -> Capability.
     *
     * Namespace projection rewrites both verifier-visible types to LMS facades
     * but deliberately preserves the method name and arity.
     */
    public static <T> LegacyCapability<T> get(LegacyCapabilityToken<T> token) {
        return get(token, false);
    }

    @SuppressWarnings("unchecked")
    public static <T> LegacyCapability<T> get(LegacyCapabilityToken<T> token, boolean registering) {
        if (token == null) throw new NullPointerException("token");
        return named(token.typeName(), registering);
    }

    @SuppressWarnings("unchecked")
    static <T> LegacyCapability<T> named(String type, boolean registering) {
        LegacyCapability<?> result = HANDLES.computeIfAbsent(type, name -> new LegacyCapability<>(name, registering));
        if (registering)
            throw new IllegalStateException("Source capability registration is not adapted: " + type);
        return (LegacyCapability<T>) result;
    }
}
