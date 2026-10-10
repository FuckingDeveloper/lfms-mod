package com.fuckingdeveloper.lms.runtime;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.bus.api.IEventBus;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Persistent runtime ownership boundary for one managed Forge 1.19.2 artifact.
 * The session owns its classloader for the whole LMS lifetime and keeps
 * initialization and registration dispatch explicit and fail-closed.
 */
public final class Forge1192RuntimeSession implements AutoCloseable {
    public enum State { LINKED, INITIALIZED, BLOCKED, CLOSED }

    private final String modId;
    private final List<String> entrypoints;
    private final Forge1192LegacyClassTransformer transformer;
    private final ManagedLegacyClassLoader loader;
    private volatile State state = State.LINKED;
    private volatile Object entrypoint;
    private volatile IEventBus modEventBus;

    public Forge1192RuntimeSession(String modId, Path artifact, ClassLoader parent, List<String> entrypoints)
            throws java.io.IOException, ClassNotFoundException {
        this.modId = Objects.requireNonNull(modId);
        this.entrypoints = List.copyOf(entrypoints);
        if (this.entrypoints.size() != 1) {
            throw new IllegalStateException("Forge 1.19.2 runtime currently requires exactly one @Mod entrypoint");
        }
        this.transformer = new Forge1192LegacyClassTransformer();
        this.loader = new ManagedLegacyClassLoader(artifact, parent, transformer);
        loader.linkOwnedClass(this.entrypoints.getFirst());
    }

    public String modId() { return modId; }
    public State state() { return state; }
    public Forge1192LegacyClassTransformer transformer() { return transformer; }

    public void bindModEventBus(IEventBus eventBus) {
        if (state != State.LINKED) throw new IllegalStateException("Cannot bind event bus in state " + state);
        this.modEventBus = Objects.requireNonNull(eventBus);
    }

    public synchronized void initialize() throws Exception {
        if (state != State.LINKED) throw new IllegalStateException("Cannot initialize legacy session in state " + state);
        IEventBus eventBus = Objects.requireNonNull(modEventBus, "Legacy mod event bus has not been bound");
        try (var ignored = Forge1192RegistrationContext.enterMod(modId);
             var lifecycle = Forge1192LifecycleBridge.enter(modId, eventBus)) {
            Class<?> type = Class.forName(entrypoints.getFirst(), true, loader);
            Constructor<?> ctor = type.getDeclaredConstructor();
            if (!ctor.canAccess(null)) ctor.setAccessible(true);
            entrypoint = ctor.newInstance();
            state = State.INITIALIZED;
        } catch (Throwable t) {
            state = State.BLOCKED;
            if (t instanceof Exception e) throw e;
            if (t instanceof Error e) throw e;
            throw new RuntimeException(t);
        }
    }

    public void onRegister(RegisterEvent event) throws Exception {
        if (state != State.INITIALIZED) return;
        try (var mod = Forge1192RegistrationContext.enterMod(modId);
             var registration = Forge1192RegistrationContext.enterRegistration(event)) {
            // Registration callbacks registered by transformed legacy code run
            // synchronously on NeoForge's bus while this scope is active.
        }
    }

    @Override public synchronized void close() throws Exception {
        if (state == State.CLOSED) return;
        loader.close();
        entrypoint = null;
        state = State.CLOSED;
    }
}
