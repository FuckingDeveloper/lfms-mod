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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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
    private static final Map<String, List<PendingItemStackAction>> PENDING_ITEM_STACK_CONSUMERS =
            new HashMap<>();
    private static final Set<String> ITEM_STACK_FLUSH_REGISTERED = new HashSet<>();

    private record PendingRegistration(Identifier name, Object value) {}
    private interface PendingItemStackAction {}
    private record PendingItemStackConsumer(
            Object receiver, ItemLike item, Identifier id, String method) implements PendingItemStackAction {}
    private record PendingStaticItemStackConsumer(
            Item item, Class<?> owner, String method) implements PendingItemStackAction {}
    private record PendingItemComponentInitializer(
            Object receiver, Class<?> owner, String method) implements PendingItemStackAction {}
    private record PendingStaticItemComponentInitializer(
            Class<?> owner, String method) implements PendingItemStackAction {}

    private Forge1192RegistrationContext() {}

    public record Scope(String modId, RegisterEvent event) {
        public Scope {
            Objects.requireNonNull(modId, "modId");
            Objects.requireNonNull(event, "event");
        }
    }

    public static AutoCloseable enterMod(String modId) {
        Objects.requireNonNull(modId, "modId");
        String active = CURRENT_MOD.get();
        if (active != null) {
            if (!active.equals(modId)) {
                throw new IllegalStateException("Cannot enter legacy mod scope for " + modId
                        + " while " + active + " is active");
            }
            // Synchronous target-to-legacy callbacks retain the owning mod's
            // namespace. Registry-event nesting remains independently forbidden.
            return () -> {};
        }
        Thread owner = Thread.currentThread();
        CURRENT_MOD.set(modId);
        return () -> {
            if (Thread.currentThread() != owner || !Objects.equals(CURRENT_MOD.get(), modId))
                throw new IllegalStateException("Legacy mod scope closed out of order or on another thread");
            CURRENT_MOD.remove();
        };
    }

    public static boolean registryPhaseCompleted(String modId,ResourceKey<? extends Registry<?>> key) {
        synchronized(PENDING){return COMPLETED.getOrDefault(modId,Set.of()).contains(key);}
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
        return stagedIdentifier(value, modId);
    }

    /** Read-only staged lookup for lifecycle callbacks that run after the mod scope closed. */
    public static java.util.Optional<Identifier> stagedIdentifierIfActive(Object value) {
        Objects.requireNonNull(value, "value");
        String modId = CURRENT_MOD.get();
        return modId == null ? java.util.Optional.empty() : stagedIdentifier(value, modId);
    }

    private static java.util.Optional<Identifier> stagedIdentifier(Object value, String modId) {
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

    /**
     * Delay an ItemStack-consuming side effect until target item components
     * are bound. The item itself remains registered in the proper item phase.
     */
    public static void deferItemStackConsumer(
            Object receiver, ItemLike item, Identifier id, String method) {
        Objects.requireNonNull(receiver, "receiver");
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(method, "method");
        String modId = requireModId();
        RegisterEvent event = CURRENT_EVENT.get();
        if (event == null || !event.getRegistryKey().equals(Registries.ITEM)) {
            invokeItemStackConsumer(new PendingItemStackConsumer(receiver, item, id, method));
            return;
        }
        enqueueItemStackAction(modId, new PendingItemStackConsumer(receiver, item, id, method));
    }

    /** Delay a proven static ItemStack consumer until item components are bound. */
    public static void deferStaticItemStackConsumer(Item item, Class<?> owner, String method) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(method, "method");
        String modId = requireModId();
        var action = new PendingStaticItemStackConsumer(item, owner, method);
        RegisterEvent event = CURRENT_EVENT.get();
        if (event == null || !event.getRegistryKey().equals(Registries.ITEM)) {
            invokeItemStackAction(action);
            return;
        }
        enqueueItemStackAction(modId, action);
    }

    /**
     * Delay a proven no-arg initializer whose owned call graph consumes
     * ItemStack defaults. Minecraft 26.3 binds those defaults during data
     * loading, not during CommonSetup as legacy Forge code assumed.
     */
    public static void deferItemComponentInitializer(
            Object receiver, Class<?> owner, String method) {
        Objects.requireNonNull(receiver, "receiver");
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(method, "method");
        String modId = requireModId();
        var action = new PendingItemComponentInitializer(receiver, owner, method);
        if (Target263DataComponentBindingBridge.currentRegistries().isPresent()
                || net.minecraft.world.item.Items.AIR.builtInRegistryHolder().areComponentsBound()) {
            invokeItemStackAction(action);
            return;
        }
        enqueueItemStackAction(modId, action);
    }

    /** Delay a proven static no-arg component consumer until defaults are bound. */
    public static void deferStaticItemComponentInitializer(Class<?> owner, String method) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(method, "method");
        String modId = requireModId();
        var action = new PendingStaticItemComponentInitializer(owner, method);
        if (Target263DataComponentBindingBridge.currentRegistries().isPresent()
                || net.minecraft.world.item.Items.AIR.builtInRegistryHolder().areComponentsBound()) {
            invokeItemStackAction(action);
            return;
        }
        enqueueItemStackAction(modId, action);
    }

    private static void enqueueItemStackAction(String modId, PendingItemStackAction action) {
        synchronized (PENDING) {
            PENDING_ITEM_STACK_CONSUMERS.computeIfAbsent(modId, ignored -> new ArrayList<>())
                    .add(action);
            if (ITEM_STACK_FLUSH_REGISTERED.add(modId)) {
                NeoForge.EVENT_BUS.addListener((TagsUpdatedEvent.ServerDataLoad event) -> {
                    if (event.shouldUpdateStaticData()) {
                        Target263DataComponentBindingBridge.runBeforeBinding(
                                event.getServerResources(), event.getRegistries(),
                                () -> flushItemStackConsumersScoped(modId));
                    }
                });
            }
        }
    }

    private static void flushItemStackConsumersScoped(String modId) {
        try (var lifecycle = Forge1192LifecycleBridge.enterRecorded(modId);
             var ignored = enterMod(modId)) {
            flushItemStackConsumers(modId);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close deferred ItemStack consumer scope", e);
        }
    }

    private static void flushItemStackConsumers(String modId) {
        while (true) {
            PendingItemStackAction pending;
            synchronized (PENDING) {
                List<PendingItemStackAction> consumers = PENDING_ITEM_STACK_CONSUMERS.get(modId);
                if (consumers == null || consumers.isEmpty()) return;
                pending = consumers.getFirst();
            }
            invokeItemStackAction(pending);
            synchronized (PENDING) {
                List<PendingItemStackAction> consumers = PENDING_ITEM_STACK_CONSUMERS.get(modId);
                if (consumers == null || consumers.isEmpty() || consumers.getFirst() != pending) {
                    throw new IllegalStateException("Legacy deferred ItemStack queue changed during flush for "
                            + modId);
                }
                consumers.removeFirst();
                if (consumers.isEmpty()) PENDING_ITEM_STACK_CONSUMERS.remove(modId);
            }
        }
    }

    private static void invokeItemStackAction(PendingItemStackAction pending) {
        if (pending instanceof PendingItemStackConsumer consumer) {
            invokeItemStackConsumer(consumer);
        } else if (pending instanceof PendingStaticItemStackConsumer consumer) {
            invokeStaticItemStackConsumer(consumer);
        } else if (pending instanceof PendingItemComponentInitializer initializer) {
            invokeItemComponentInitializer(initializer);
        } else if (pending instanceof PendingStaticItemComponentInitializer initializer) {
            invokeStaticItemComponentInitializer(initializer);
        } else {
            throw new IllegalStateException("Unknown deferred ItemStack action: " + pending.getClass().getName());
        }
    }

    private static void invokeItemStackConsumer(PendingItemStackConsumer pending) {
        Class<?> type = pending.receiver().getClass();
        java.lang.reflect.Method target = null;
        for (Class<?> current = type; current != null && target == null; current = current.getSuperclass()) {
            try {
                target = current.getDeclaredMethod(
                        pending.method(), ItemStack.class, Identifier.class);
            } catch (NoSuchMethodException ignored) {
                // Continue through the proven receiver hierarchy.
            }
        }
        if (target == null) {
            throw new IllegalStateException("Deferred ItemStack consumer not found: "
                    + type.getName() + "#" + pending.method());
        }
        try {
            target.setAccessible(true);
            target.invoke(pending.receiver(),
                    Forge1192EarlyItemStackBridge.create(pending.item()), pending.id());
        } catch (ReflectiveOperationException e) {
            Throwable cause = e instanceof java.lang.reflect.InvocationTargetException invocation
                    && invocation.getCause() != null ? invocation.getCause() : e;
            throw new IllegalStateException("Deferred ItemStack consumer failed: "
                    + type.getName() + "#" + pending.method(), cause);
        }
    }

    private static void invokeStaticItemStackConsumer(PendingStaticItemStackConsumer pending) {
        try {
            java.lang.reflect.Method target = pending.owner().getDeclaredMethod(pending.method(), Item.class);
            if (!java.lang.reflect.Modifier.isStatic(target.getModifiers()) || target.getReturnType() != void.class) {
                throw new IllegalStateException("Deferred ItemStack consumer has changed shape: " + target);
            }
            target.setAccessible(true);
            target.invoke(null, pending.item());
        } catch (ReflectiveOperationException e) {
            Throwable cause = e instanceof java.lang.reflect.InvocationTargetException invocation
                    && invocation.getCause() != null ? invocation.getCause() : e;
            throw new IllegalStateException("Deferred static ItemStack consumer failed: "
                    + pending.owner().getName() + "#" + pending.method(), cause);
        }
    }

    private static void invokeItemComponentInitializer(PendingItemComponentInitializer pending) {
        try {
            java.lang.reflect.Method target = pending.owner().getDeclaredMethod(pending.method());
            if (java.lang.reflect.Modifier.isStatic(target.getModifiers())
                    || target.getReturnType() != void.class) {
                throw new IllegalStateException("Deferred item-component initializer has changed shape: "
                        + target);
            }
            target.setAccessible(true);
            target.invoke(pending.receiver());
        } catch (ReflectiveOperationException e) {
            Throwable cause = e instanceof java.lang.reflect.InvocationTargetException invocation
                    && invocation.getCause() != null ? invocation.getCause() : e;
            throw new IllegalStateException("Deferred item-component initializer failed: "
                    + pending.owner().getName() + "#" + pending.method(), cause);
        }
    }

    private static void invokeStaticItemComponentInitializer(PendingStaticItemComponentInitializer pending) {
        try {
            java.lang.reflect.Method target = pending.owner().getDeclaredMethod(pending.method());
            if (!java.lang.reflect.Modifier.isStatic(target.getModifiers())
                    || target.getReturnType() != void.class) {
                throw new IllegalStateException("Deferred static item-component initializer has changed shape: "
                        + target);
            }
            target.setAccessible(true);
            target.invoke(null);
        } catch (ReflectiveOperationException e) {
            Throwable cause = e instanceof java.lang.reflect.InvocationTargetException invocation
                    && invocation.getCause() != null ? invocation.getCause() : e;
            throw new IllegalStateException("Deferred static item-component initializer failed: "
                    + pending.owner().getName() + "#" + pending.method(), cause);
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
