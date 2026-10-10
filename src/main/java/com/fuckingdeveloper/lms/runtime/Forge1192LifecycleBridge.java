package com.fuckingdeveloper.lms.runtime;

import net.neoforged.bus.api.IEventBus;

import java.util.Objects;

/**
 * Thread-scoped Forge 1.19.2 lifecycle context. Removed static FML context
 * accessors are redirected here instead of relying on NeoForge implementation
 * globals whose semantics changed after 1.19.2.
 */
public final class Forge1192LifecycleBridge {
    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

    public record Context(String modId, IEventBus modEventBus) {
        public Context {
            Objects.requireNonNull(modId, "modId");
            Objects.requireNonNull(modEventBus, "modEventBus");
        }
    }

    private Forge1192LifecycleBridge() {}

    public static AutoCloseable enter(String modId, IEventBus modEventBus) {
        if (CURRENT.get() != null) throw new IllegalStateException("Nested legacy lifecycle scopes are not supported");
        Context context = new Context(modId, modEventBus);
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

    public static Object getActiveContainer(Object token) {
        Context context = requireActive();
        if (token != context) throw new IllegalStateException("Legacy ModLoadingContext token escaped its LMS scope");
        // LMS does not fabricate a NeoForge ModContainer for a legacy artifact.
        // The legacy active-container contract therefore remains explicit and
        // fail-closed until a per-artifact container facade exists.
        throw new UnsupportedOperationException(
                "Legacy ModLoadingContext#getActiveContainer requires a per-artifact ModContainer facade for " + context.modId());
    }

    public static void setActiveContainer(Object token, Object container) {
        Context context = requireActive();
        if (token != context) throw new IllegalStateException("Legacy ModLoadingContext token escaped its LMS scope");
        throw new UnsupportedOperationException(
                "Legacy ModLoadingContext#setActiveContainer requires a per-artifact ModContainer facade for " + context.modId());
    }

    public static String getActiveNamespace(Object token) {
        Context context = requireActive();
        if (token != context) throw new IllegalStateException("Legacy ModLoadingContext token escaped its LMS scope");
        return context.modId();
    }
}
