package com.fuckingdeveloper.lms.runtime.capability;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Forge 1.19.2 CapabilityManager identity boundary.
 *
 * <p>Anonymous CapabilityToken subclasses are the stable legacy identity.
 * Repeated lookups for the same token class therefore return the same typed
 * handle. This preserves handle identity without inventing modern NeoForge
 * provider semantics.</p>
 */
public final class LegacyCapabilityManager {
    private static final ConcurrentMap<Class<?>, LegacyCapability<?>> HANDLES = new ConcurrentHashMap<>();

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
        return (LegacyCapability<T>) HANDLES.computeIfAbsent(
                token.getClass(), type -> new LegacyCapability<>(type, registering));
    }
}
