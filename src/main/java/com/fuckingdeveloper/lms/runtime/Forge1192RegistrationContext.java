package com.fuckingdeveloper.lms.runtime;

import java.util.Objects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import java.util.Iterator;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Scoped bridge between a legacy registration callback and NeoForge's
 * registry-specific registration phase. No global registry mutation is allowed.
 */
public final class Forge1192RegistrationContext {
    private static final ThreadLocal<String> CURRENT_MOD = new ThreadLocal<>();
    private static final ThreadLocal<RegisterEvent> CURRENT_EVENT = new ThreadLocal<>();
    private static final Map<String, Map<ResourceKey<? extends Registry<?>>, List<PendingRegistration>>> PENDING =
            new HashMap<>();
    private static final Map<String, Set<ResourceKey<? extends Registry<?>>>> COMPLETED = new HashMap<>();

    private record PendingRegistration(Identifier name, Object value) {}

    private Forge1192RegistrationContext() {}

    public record Scope(String modId, RegisterEvent event) {
        public Scope {
            Objects.requireNonNull(modId, "modId");
            Objects.requireNonNull(event, "event");
        }
    }

    public static AutoCloseable enterMod(String modId) {
        Objects.requireNonNull(modId, "modId");
        if (CURRENT_MOD.get() != null) throw new IllegalStateException("Nested legacy mod scopes are not supported");
        Thread owner = Thread.currentThread();
        CURRENT_MOD.set(modId);
        return () -> {
            if (Thread.currentThread() != owner || !Objects.equals(CURRENT_MOD.get(), modId))
                throw new IllegalStateException("Legacy mod scope closed out of order or on another thread");
            CURRENT_MOD.remove();
        };
    }

    public static AutoCloseable enterRegistration(RegisterEvent event) {
        Objects.requireNonNull(event, "event");
        if (CURRENT_EVENT.get() != null) throw new IllegalStateException("Nested legacy registration scopes are not supported");
        String modId = CURRENT_MOD.get();
        if (modId == null) throw new IllegalStateException("Legacy registration scope requires an active mod scope");
        Thread owner = Thread.currentThread();
        CURRENT_EVENT.set(event);
        try {
            flushPending(modId, event);
        } catch (RuntimeException | Error e) {
            CURRENT_EVENT.remove();
            throw e;
        }
        return () -> {
            if (Thread.currentThread() != owner || CURRENT_EVENT.get() != event)
                throw new IllegalStateException("Legacy registration scope closed out of order or on another thread");
            synchronized (PENDING) {
                COMPLETED.computeIfAbsent(modId, ignored -> new HashSet<>()).add(event.getRegistryKey());
            }
            CURRENT_EVENT.remove();
        };
    }

    public static AutoCloseable enter(String modId, RegisterEvent event) {
        AutoCloseable mod = enterMod(modId);
        try {
            AutoCloseable registration = enterRegistration(event);
            return () -> { try { registration.close(); } finally { mod.close(); } };
        } catch (RuntimeException e) {
            try { mod.close(); } catch (Exception suppressed) { e.addSuppressed(suppressed); }
            throw e;
        }
    }

    public static Scope requireActive() {
        String modId = CURRENT_MOD.get();
        RegisterEvent event = CURRENT_EVENT.get();
        if (modId == null || event == null)
            throw new IllegalStateException("Legacy registry operation attempted outside NeoForge RegisterEvent");
        return new Scope(modId, event);
    }

    public static String requireModId() {
        String modId = CURRENT_MOD.get();
        if (modId == null) throw new IllegalStateException("Legacy namespace resolution attempted outside LMS mod scope");
        return modId;
    }

    /** Find the authoritative staged identifier for a legacy-owned object by identity. */
    public static java.util.Optional<Identifier> stagedIdentifier(Object value) {
        Objects.requireNonNull(value, "value");
        String modId = requireModId();
        synchronized (PENDING) {
            Map<ResourceKey<? extends Registry<?>>, List<PendingRegistration>> byRegistry = PENDING.get(modId);
            if (byRegistry == null) return java.util.Optional.empty();
            Identifier match = null;
            for (List<PendingRegistration> registrations : byRegistry.values()) {
                for (PendingRegistration registration : registrations) {
                    if (registration.value() != value) continue;
                    if (match != null && !match.equals(registration.name())) {
                        throw new IllegalStateException("Legacy object is staged under multiple registry names");
                    }
                    match = registration.name();
                }
            }
            return java.util.Optional.ofNullable(match);
        }
    }

    /**
     * Resolve an unqualified legacy registry name within the active mod's
     * namespace. Qualified names are kept qualified.
     */
    public static Identifier checkPrefix(String name, boolean warn) {
        String modId = requireModId();
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) throw new IllegalArgumentException("Empty legacy registry name");
        int colon = name.indexOf(':');
        if (colon >= 0) {
            return Identifier.parse(name);
        }
        return Identifier.fromNamespaceAndPath(modId, name);
    }

    /**
     * Preserve Forge 1.19.2's direct registry registration contract while
     * executing strictly inside NeoForge's matching RegisterEvent phase.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register(Object registryToken, Identifier name, Object value) {
        Registry<Object> target = requireRegistryToken(registryToken);
        String modId = requireModId();
        RegisterEvent event = CURRENT_EVENT.get();
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(value, "value");

        ResourceKey<? extends Registry<?>> targetKey = target.key();
        if (event == null || !targetKey.equals(event.getRegistryKey())) {
            // Forge 1.19.2 permitted direct registration during mod
            // construction. Preserve that intent as queued work only; the
            // target registry is still mutated exclusively by its RegisterEvent.
            stage(modId, targetKey, name, value);
            return;
        }

        registerOn(event, name, value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerOn(RegisterEvent event, Identifier name, Object value) {

        // RegisterEvent#getRegistryKey intentionally exposes a wildcard because
        // the event is runtime-typed. The value type is known only to the
        // legacy call site, so contain the unavoidable erasure at this bridge.
        ResourceKey<? extends Registry<Object>> key =
                (ResourceKey<? extends Registry<Object>>) (ResourceKey) event.getRegistryKey();
        Object adapted = adaptRegistrationValue(event, name, value);
        event.register(key, name, () -> adapted);
    }

    private static Object adaptRegistrationValue(RegisterEvent event, Identifier name, Object value) {
        if (event.getRegistryKey().equals(Registries.RECIPE_SERIALIZER)
                && value instanceof com.fuckingdeveloper.lms.runtime.recipe.LegacyRecipeSerializer serializer) {
            return com.fuckingdeveloper.lms.runtime.recipe.Forge1192RecipeSerializerBridge.adapt(
                    name, serializer);
        }
        return value;
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

    /** Return the active event registry only when it represents {@code key}. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    static Registry<?> activeRegistry(ResourceKey<? extends Registry<?>> key) {
        RegisterEvent event = CURRENT_EVENT.get();
        if (event == null || !event.getRegistryKey().equals(key)) return null;
        return (Registry) event.getRegistry();
    }

    /**
     * Stack-compatible replacement for legacy RegisterEvent#getForgeRegistry.
     * Consumer calls are rewritten to static LMS operations, so the concrete
     * object never escapes as a legacy IForgeRegistry instance.
     */
    public static Object activeRegistryObject(RegisterEvent event) {
        Scope scope = requireActive();
        if (scope.event() != event) {
            throw new IllegalStateException("Legacy registry facade requested for a non-active RegisterEvent");
        }
        return activeRegistry();
    }

    private static void stage(String modId, ResourceKey<? extends Registry<?>> targetKey,
                              Identifier name, Object value) {
        synchronized (PENDING) {
            if (COMPLETED.getOrDefault(modId, Set.of()).contains(targetKey)) {
                throw new IllegalStateException("Legacy registration for " + targetKey.identifier()
                        + " occurred after its NeoForge RegisterEvent phase: " + name);
            }
            PENDING.computeIfAbsent(modId, ignored -> new HashMap<>())
                    .computeIfAbsent(targetKey, ignored -> new ArrayList<>())
                    .add(new PendingRegistration(name, value));
        }
    }

    private static void flushPending(String modId, RegisterEvent event) {
        while (true) {
            PendingRegistration registration;
            synchronized (PENDING) {
                Map<ResourceKey<? extends Registry<?>>, List<PendingRegistration>> byRegistry = PENDING.get(modId);
                List<PendingRegistration> registrations =
                        byRegistry == null ? null : byRegistry.get(event.getRegistryKey());
                if (registrations == null || registrations.isEmpty()) return;
                registration = registrations.getFirst();
            }

            // Remove only after RegisterEvent accepted this entry. If it
            // throws, the failing entry and every unattempted successor remain
            // staged. Startup is currently fatal, but retaining the queue also
            // makes diagnostics/retry semantics truthful rather than silently
            // discarding work that never ran.
            registerOn(event, registration.name(), registration.value());
            synchronized (PENDING) {
                Map<ResourceKey<? extends Registry<?>>, List<PendingRegistration>> byRegistry = PENDING.get(modId);
                List<PendingRegistration> registrations =
                        byRegistry == null ? null : byRegistry.get(event.getRegistryKey());
                if (registrations == null || registrations.isEmpty()
                        || registrations.getFirst() != registration) {
                    throw new IllegalStateException("Legacy pending registration queue changed during flush for "
                            + modId + " / " + event.getRegistryKey().identifier());
                }
                registrations.removeFirst();
                if (registrations.isEmpty()) byRegistry.remove(event.getRegistryKey());
                if (byRegistry.isEmpty()) PENDING.remove(modId);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Registry<Object> requireRegistryToken(Object token) {
        Objects.requireNonNull(token, "legacy registry receiver");
        if (!(token instanceof Registry registry)) {
            throw new IllegalStateException("Legacy registry receiver is not a target Registry: "
                    + token.getClass().getName());
        }
        return (Registry<Object>) registry;
    }

    public static boolean containsKey(Object registryToken, Identifier name) {
        Objects.requireNonNull(name, "name");
        return requireRegistryToken(registryToken).containsKey(name);
    }

    public static Object getValue(Object registryToken, Identifier name) {
        Objects.requireNonNull(name, "name");
        return requireRegistryToken(registryToken).getValue(name);
    }

    public static Identifier getKey(Object registryToken, Object value) {
        Objects.requireNonNull(value, "value");
        return requireRegistryToken(registryToken).getKey(value);
    }

    public static Iterator<Object> iterator(Object registryToken) {
        return requireRegistryToken(registryToken).iterator();
    }
}
