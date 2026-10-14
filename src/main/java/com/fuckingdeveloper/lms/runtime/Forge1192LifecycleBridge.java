package com.fuckingdeveloper.lms.runtime;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

import java.util.Objects;

/**
 * Thread-scoped Forge 1.19.2 lifecycle context. Removed static FML context
 * accessors are redirected here instead of relying on NeoForge implementation
 * globals whose semantics changed after 1.19.2.
 */
public final class Forge1192LifecycleBridge {
    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();
    private static final java.util.concurrent.ConcurrentHashMap<String, Binding> BINDINGS =
            new java.util.concurrent.ConcurrentHashMap<>();

    private record Binding(IEventBus modEventBus, ModContainer hostContainer) {}

    public record Context(String modId, IEventBus modEventBus, ModContainer hostContainer, LegacyContainer container) {
        public Context {
            Objects.requireNonNull(modId, "modId");
            Objects.requireNonNull(modEventBus, "modEventBus");
            Objects.requireNonNull(hostContainer, "hostContainer");
            Objects.requireNonNull(container, "container");
        }
    }

    private Forge1192LifecycleBridge() {}

    public static AutoCloseable enter(String modId, IEventBus modEventBus, ModContainer hostContainer) {
        Objects.requireNonNull(modId, "modId");
        Binding binding = new Binding(
                Objects.requireNonNull(modEventBus, "modEventBus"),
                Objects.requireNonNull(hostContainer, "hostContainer"));
        Binding previous = BINDINGS.putIfAbsent(modId, binding);
        if (previous != null
                && (previous.modEventBus() != modEventBus || previous.hostContainer() != hostContainer)) {
            throw new IllegalStateException("Conflicting lifecycle binding for legacy mod " + modId);
        }
        return activate(new Context(modId, modEventBus, hostContainer, new LegacyContainer(modId)));
    }

    /** Reconstruct the source-era FML thread context for a deferred callback. */
    public static AutoCloseable enterRecorded(String modId) {
        Context active = CURRENT.get();
        if (active != null) {
            if (!active.modId().equals(Objects.requireNonNull(modId, "modId"))) {
                throw new IllegalStateException("Cannot enter legacy lifecycle for " + modId
                        + " while " + active.modId() + " is active");
            }
            // Target services may synchronously call back into another legacy
            // event owned by the same mod. Preserve the existing source-era
            // context (including active-container state) for that re-entry.
            return () -> {};
        }
        Binding binding = BINDINGS.get(Objects.requireNonNull(modId, "modId"));
        if (binding == null) {
            throw new IllegalStateException("No lifecycle binding recorded for legacy mod " + modId);
        }
        return activate(new Context(modId, binding.modEventBus(), binding.hostContainer(),
                new LegacyContainer(modId)));
    }

    /** Registered event callbacks may cross source owners; preserve and restore the caller's context. */
    public static AutoCloseable enterCallback(String modId) {
        Objects.requireNonNull(modId);
        Context previous = CURRENT.get();
        if (previous != null && previous.modId().equals(modId)) return () -> {};
        Binding binding = BINDINGS.get(modId);
        if (binding == null) throw new IllegalStateException("No callback owner binding recorded for " + modId);
        Context callback = new Context(modId, binding.modEventBus(), binding.hostContainer(), new LegacyContainer(modId));
        Thread owner = Thread.currentThread(); CURRENT.set(callback);
        return () -> {
            if (Thread.currentThread() != owner || CURRENT.get() != callback)
                throw new IllegalStateException("Source callback scope closed out of order or on another thread");
            if (previous == null) CURRENT.remove(); else CURRENT.set(previous);
        };
    }

    private static AutoCloseable activate(Context context) {
        if (CURRENT.get() != null) throw new IllegalStateException("Nested legacy lifecycle scopes are not supported");
        Thread owner = Thread.currentThread();
        CURRENT.set(context);
        return () -> {
            if (Thread.currentThread() != owner || CURRENT.get() != context)
                throw new IllegalStateException("Legacy lifecycle scope closed out of order or on another thread");
            CURRENT.remove();
        };
    }

    public static Context requireActive() {
        Context context = CURRENT.get();
        if (context == null) throw new IllegalStateException("Legacy FML lifecycle access outside LMS lifecycle scope");
        return context;
    }

    public static IEventBus modEventBus(String modId) {
        Binding binding = BINDINGS.get(Objects.requireNonNull(modId, "modId"));
        if (binding == null) throw new IllegalStateException("No lifecycle event bus recorded for legacy mod " + modId);
        return binding.modEventBus();
    }

    public static Object getJavaModLoadingContext() {
        return requireActive();
    }

    public static IEventBus getModEventBus(Object token) {
        Context context = requireActive();
        if (token != context) throw new IllegalStateException("Legacy FMLJavaModLoadingContext token escaped its LMS scope");
        return context.modEventBus();
    }

    public static Object getModLoadingContext() {
        return requireActive();
    }

    public static ModContainer getActiveContainer(Object token) {
        Context context = requireActive();
        if (token != context) throw new IllegalStateException("Legacy ModLoadingContext token escaped its LMS scope");
        Object active = context.container().active();
        if (active == null || active == context.container()) return context.hostContainer();
        if (active instanceof ModContainer container) return container;
        throw new IllegalStateException("Unsupported legacy active container override: "
                + active.getClass().getName());
    }

    public static void setActiveContainer(Object token, Object container) {
        Context context = requireActive();
        if (token != context) throw new IllegalStateException("Legacy ModLoadingContext token escaped its LMS scope");
        context.container().setActive(container);
    }

    public static String getActiveNamespace(Object token) {
        Context context = requireActive();
        if (token != context) throw new IllegalStateException("Legacy ModLoadingContext token escaped its LMS scope");
        return context.modId();
    }
    /** Per-artifact legacy active-container slot; host container is returned for target API compatibility. */
    public static final class LegacyContainer {
        private final String modId;
        private Object active;
        private LegacyContainer(String modId) { this.modId = modId; this.active = this; }
        public String modId() { return modId; }
        private Object active() { return active; }
        private void setActive(Object value) {
            if (value == null) throw new NullPointerException("legacy active container");
            active = value;
        }
    }
}

