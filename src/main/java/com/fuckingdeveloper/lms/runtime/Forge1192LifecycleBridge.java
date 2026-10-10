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

    public record Context(String modId, IEventBus modEventBus, LegacyContainer container) {
        public Context {
            Objects.requireNonNull(modId, "modId");
            Objects.requireNonNull(modEventBus, "modEventBus");
            Objects.requireNonNull(container, "container");
        }
    }

    private Forge1192LifecycleBridge() {}

    public static AutoCloseable enter(String modId, IEventBus modEventBus) {
        if (CURRENT.get() != null) throw new IllegalStateException("Nested legacy lifecycle scopes are not supported");
        Context context = new Context(modId, modEventBus, new LegacyContainer(modId));
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
        if (active == null) return null;
        if (!(active instanceof ModContainer container))
            throw new IllegalStateException("Legacy active container is not a target ModContainer: " + active.getClass().getName());
        return container;
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
    /** Per-artifact legacy active-container slot; never exposes LMS's own container. */
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

