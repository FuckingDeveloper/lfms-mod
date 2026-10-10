package com.fuckingdeveloper.lms.runtime;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Forge 1.19.2 physical-distribution execution compatibility.
 *
 * <p>Preserves the essential DistExecutor contract: the supplied branch is
 * obtained and executed only when the requested physical distribution matches
 * the current NeoForge distribution. The indirection through Supplier is kept
 * deliberately so client-only implementation classes are not eagerly linked
 * on a dedicated server.</p>
 */
public final class Forge1192DistExecutorBridge {
    private Forge1192DistExecutorBridge() {}

    private static boolean matches(Dist requested) {
        return net.neoforged.fml.loading.FMLEnvironment.getDist() == requested;
    }

    /**
     * Forge 1.19.2 branch selector. Only the supplier for the active physical
     * distribution is evaluated; the opposite-side branch remains unlinked.
     *
     * The erased descriptor intentionally remains
     * (Supplier,Supplier)Object to match legacy bytecode.
     */
    public static <T> T unsafeRunForDist(
            Supplier<? extends T> clientTarget,
            Supplier<? extends T> dedicatedServerTarget) {
        Objects.requireNonNull(clientTarget, "clientTarget");
        Objects.requireNonNull(dedicatedServerTarget, "dedicatedServerTarget");
        return FMLEnvironment.getDist() == Dist.CLIENT
                ? clientTarget.get()
                : dedicatedServerTarget.get();
    }

    public static void unsafeRunWhenOn(Dist dist, Supplier<? extends Runnable> toRun) {
        Objects.requireNonNull(dist, "dist");
        Objects.requireNonNull(toRun, "toRun");
        if (matches(dist)) {
            Objects.requireNonNull(toRun.get(), "DistExecutor runnable").run();
        }
    }

    public static <T> T unsafeCallWhenOn(Dist dist, Supplier<? extends Supplier<T>> toCall) {
        Objects.requireNonNull(dist, "dist");
        Objects.requireNonNull(toCall, "toCall");
        if (!matches(dist)) return null;
        return Objects.requireNonNull(toCall.get(), "DistExecutor callable").get();
    }

    public static void safeRunWhenOn(Dist dist, Supplier<? extends Runnable> toRun) {
        unsafeRunWhenOn(dist, toRun);
    }

    public static <T> T safeCallWhenOn(Dist dist, Supplier<? extends Supplier<T>> toCall) {
        return unsafeCallWhenOn(dist, toCall);
    }
}
