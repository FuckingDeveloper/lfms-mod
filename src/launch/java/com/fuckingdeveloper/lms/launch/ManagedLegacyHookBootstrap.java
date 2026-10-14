package com.fuckingdeveloper.lms.launch;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** Target-visible, exact-identity linkage boundary for managed legacy coremod hooks. */
public final class ManagedLegacyHookBootstrap {
    public static final Handle BOOTSTRAP_HANDLE = new Handle(
            Opcodes.H_INVOKESTATIC,
            "com/fuckingdeveloper/lms/launch/ManagedLegacyHookBootstrap",
            "bootstrap",
            MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class,
                    MethodType.class, String.class, String.class, String.class, String.class)
                    .toMethodDescriptorString(),
            false);

    private record Key(String binding, String owner, String name, String descriptor) {}

    private static final ConcurrentHashMap<Key, MethodHandle> HOOKS = new ConcurrentHashMap<>();

    private ManagedLegacyHookBootstrap() {}

    public static void register(String binding, String owner, String name, String descriptor,
            MethodHandle handle) {
        Key key = new Key(require(binding, "binding"), require(owner, "owner"),
                require(name, "name"), require(descriptor, "descriptor"));
        MethodHandle exact = Objects.requireNonNull(handle, "handle");
        if (!exact.type().toMethodDescriptorString().equals(descriptor)) {
            throw new IllegalArgumentException("Managed hook type mismatch for " + key
                    + ": declared=" + descriptor + " handle=" + exact.type());
        }
        MethodHandle previous = HOOKS.putIfAbsent(key, exact);
        if (previous != null && previous != exact) {
            throw new IllegalStateException("Ambiguous managed hook binding: " + key);
        }
    }

    public static void unregister(String binding) {
        HOOKS.keySet().removeIf(key -> key.binding().equals(binding));
    }

    public static CallSite bootstrap(MethodHandles.Lookup caller, String invokedName,
            MethodType invokedType, String binding, String owner, String name, String descriptor) {
        Key key = new Key(binding, owner, name, descriptor);
        MethodHandle handle = HOOKS.get(key);
        if (handle == null) {
            throw new BootstrapMethodError("No managed legacy coremod hook registered for " + key
                    + " caller=" + caller.lookupClass().getName());
        }
        if (!name.equals(invokedName) || !handle.type().equals(invokedType)) {
            throw new BootstrapMethodError("Managed legacy coremod hook type mismatch for " + key
                    + ": invokedName=" + invokedName + " invokedType=" + invokedType
                    + " handleType=" + handle.type());
        }
        return new ConstantCallSite(handle);
    }

    private static String require(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " is blank");
        return value;
    }
}
