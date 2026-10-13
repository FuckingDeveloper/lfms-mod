package com.fuckingdeveloper.lms.runtime;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

import java.lang.reflect.Constructor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fuckingdeveloper.lms.transform.TransformationSpec;

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
    private volatile ModContainer hostContainer;
    private final Set<String> coremodHookBindings = new LinkedHashSet<>();

    public Forge1192RuntimeSession(String modId, Path artifact, ClassLoader parent, List<String> entrypoints)
            throws java.io.IOException, ClassNotFoundException {
        this(modId, artifact, List.of(), parent, entrypoints);
    }

    public Forge1192RuntimeSession(String modId, Path artifact, List<Path> dependencies,
                                   ClassLoader parent, List<String> entrypoints)
            throws java.io.IOException, ClassNotFoundException {
        this.modId = Objects.requireNonNull(modId);
        this.entrypoints = List.copyOf(entrypoints);
        if (this.entrypoints.size() != 1) {
            throw new IllegalStateException("Forge 1.19.2 runtime currently requires exactly one @Mod entrypoint");
        }
        this.transformer = new Forge1192LegacyClassTransformer();
        this.loader = new ManagedLegacyClassLoader(artifact, dependencies, parent, transformer);
        loader.linkOwnedClass(this.entrypoints.getFirst());
    }

    public String modId() { return modId; }
    public State state() { return state; }
    public Forge1192LegacyClassTransformer transformer() { return transformer; }

    public void bindModEventBus(IEventBus eventBus, ModContainer container) {
        if (state != State.LINKED) throw new IllegalStateException("Cannot bind event bus in state " + state);
        this.modEventBus = Objects.requireNonNull(eventBus);
        this.hostContainer = Objects.requireNonNull(container);
    }

    public synchronized void initialize() throws Exception {
        if (state != State.LINKED) throw new IllegalStateException("Cannot initialize legacy session in state " + state);
        IEventBus eventBus = Objects.requireNonNull(modEventBus, "Legacy mod event bus has not been bound");
        try (var ignored = Forge1192RegistrationContext.enterMod(modId);
             var lifecycle = Forge1192LifecycleBridge.enter(modId, eventBus,
                     Objects.requireNonNull(hostContainer, "Host ModContainer has not been bound"))) {
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

    /** Bind exact managed coremod hooks to the target-visible early-launch bootstrap. */
    public synchronized void bindCoremodHooks(List<TransformationSpec> specs) {
        var bindings = specs.stream()
                .filter(spec -> spec.readiness() == TransformationSpec.Readiness.READY)
                .map(spec -> java.util.Map.entry(spec, managedHooks(spec)))
                .filter(entry -> !entry.getValue().isEmpty())
                .toList();
        if (bindings.isEmpty()) return;
        if (state != State.INITIALIZED) {
            throw new IllegalStateException("Cannot bind coremod hooks in state " + state);
        }
        try {
            Class<?> bootstrap = Class.forName(
                    "com.fuckingdeveloper.lms.launch.ManagedLegacyHookBootstrap", true, loader.getParent());
            var register = bootstrap.getMethod("register", String.class, String.class,
                    String.class, String.class, MethodHandle.class);
            for (var binding : bindings) {
                TransformationSpec spec = binding.getKey();
                Set<TransformationSpec.MethodRef> hooks = binding.getValue();
                for (var hook : hooks) {
                    if (hook.invocation() != TransformationSpec.Invocation.STATIC) {
                        throw new IllegalStateException("Managed coremod hook is not static: " + hook);
                    }
                    String binaryOwner = hook.owner().replace('/', '.');
                    if (!loader.owns(binaryOwner)) {
                        throw new IllegalStateException("Coremod hook owner is not owned by runtime session "
                                + modId + ": " + binaryOwner);
                    }
                    Class<?> owner = Class.forName(binaryOwner, false, loader);
                    MethodType type = MethodType.fromMethodDescriptorString(hook.descriptor(), loader);
                    MethodHandle handle = MethodHandles.privateLookupIn(owner, MethodHandles.lookup())
                            .findStatic(owner, hook.name(), type);
                    register.invoke(null, spec.id(), hook.owner(), hook.name(), hook.descriptor(), handle);
                    coremodHookBindings.add(spec.id());
                }
            }
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Cannot bind managed coremod hooks for " + modId, failure);
        }
    }

    private static Set<TransformationSpec.MethodRef> managedHooks(TransformationSpec spec) {
        Set<TransformationSpec.MethodRef> hooks = new LinkedHashSet<>();
        if (spec.kind() == TransformationSpec.Kind.METHOD_CALL_REDIRECT
                && spec.replacement() != null) {
            hooks.add(spec.replacement());
        }
        for (var edit : spec.edits()) {
            var values = edit.values().isEmpty()
                    ? (edit.value() == null ? List.<TransformationSpec.InstructionSpec>of()
                    : List.of(edit.value()))
                    : edit.values();
            for (var value : values) {
                if (value.kind() == TransformationSpec.InstructionKind.MANAGED_METHOD_CALL
                        && value.method() != null) hooks.add(value.method());
            }
        }
        return hooks;
    }


    @Override public synchronized void close() throws Exception {
        if (state == State.CLOSED) return;
        if (!coremodHookBindings.isEmpty()) {
            Class<?> bootstrap = Class.forName(
                    "com.fuckingdeveloper.lms.launch.ManagedLegacyHookBootstrap", true, loader.getParent());
            var unregister = bootstrap.getMethod("unregister", String.class);
            for (String binding : coremodHookBindings) unregister.invoke(null, binding);
            coremodHookBindings.clear();
        }
        loader.close();
        entrypoint = null;
        state = State.CLOSED;
    }
}
