package com.fuckingdeveloper.lms.runtime;

import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import java.util.Iterator;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Scoped bridge between a legacy registration callback and NeoForge's
 * registry-specific registration phase. No global registry mutation is allowed.
 */
public final class Forge1192RegistrationContext {
    private static final ThreadLocal<Scope> CURRENT = new ThreadLocal<>();

    private Forge1192RegistrationContext() {}

    public record Scope(String modId, RegisterEvent event) {
        public Scope {
            Objects.requireNonNull(modId, "modId");
            Objects.requireNonNull(event, "event");
        }
    }

    public static AutoCloseable enter(String modId, RegisterEvent event) {
        if (CURRENT.get() != null) {
            throw new IllegalStateException("Nested legacy registration scopes are not supported");
        }
        Scope scope = new Scope(modId, event);
        CURRENT.set(scope);
        return () -> {
            if (CURRENT.get() != scope) {
                throw new IllegalStateException("Legacy registration scope closed on another thread or out of order");
            }
            CURRENT.remove();
        };
    }

    public static Scope requireActive() {
        Scope scope = CURRENT.get();
        if (scope == null) {
            throw new IllegalStateException("Legacy registration attempted outside NeoForge RegisterEvent");
        }
        return scope;
    }

    /**
     * Resolve an unqualified legacy registry name within the active mod's
     * namespace. Qualified names are kept qualified.
     */
    public static Identifier checkPrefix(String name, boolean warn) {
        Scope scope = requireActive();
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) throw new IllegalArgumentException("Empty legacy registry name");
        int colon = name.indexOf(':');
        if (colon >= 0) {
            return Identifier.parse(name);
        }
        return Identifier.fromNamespaceAndPath(scope.modId(), name);
    }

    /**
     * Preserve Forge 1.19.2's direct registry registration contract while
     * executing strictly inside NeoForge's matching RegisterEvent phase.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register(Object ignoredRegistry, Identifier name, Object value) {
        Scope scope = requireActive();
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(value, "value");

        // RegisterEvent#getRegistryKey intentionally exposes a wildcard because
        // the event is runtime-typed. The value type is known only to the
        // legacy call site, so contain the unavoidable erasure at this bridge.
        ResourceKey<? extends Registry<Object>> key =
                (ResourceKey<? extends Registry<Object>>) (ResourceKey) scope.event().getRegistryKey();
        scope.event().register(key, name, () -> value);
    }

    public static void register(Object ignoredRegistry, String name, Object value) {
        register(ignoredRegistry, checkPrefix(name, false), value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Registry<Object> activeRegistry() {
        Scope scope = requireActive();
        // RegisterEvent exposes the actual registry for the current phase.
        // Keep the unavoidable runtime type erasure contained in this bridge.
        return (Registry<Object>) (Registry) scope.event().getRegistry();
    }

    /**
     * Stack-compatible replacement for legacy RegisterEvent#getForgeRegistry.
     * Consumer calls are rewritten to static LMS operations, so the concrete
     * object never escapes as a legacy IForgeRegistry instance.
     */
    public static Object activeRegistryObject() {
        return activeRegistry();
    }

    public static boolean containsKey(Object ignoredRegistry, Identifier name) {
        Objects.requireNonNull(name, "name");
        return activeRegistry().containsKey(name);
    }

    public static Object getValue(Object ignoredRegistry, Identifier name) {
        Objects.requireNonNull(name, "name");
        return activeRegistry().getValue(name);
    }

    public static Identifier getKey(Object ignoredRegistry, Object value) {
        Objects.requireNonNull(value, "value");
        return activeRegistry().getKey(value);
    }

    public static Iterator<Object> iterator(Object ignoredRegistry) {
        return activeRegistry().iterator();
    }
}
