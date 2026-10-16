package com.fuckingdeveloper.lms.target.neoforge263;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.HolderLookup;

/** Owns the updated-tag lookup during synchronous target reload-event delivery. */
public final class Target263ReloadLookupBridge {
    private static final ThreadLocal<HolderLookup.Provider> CURRENT = new ThreadLocal<>();

    private Target263ReloadLookupBridge() {}

    public static Optional<HolderLookup.Provider> currentRegistries() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static void run(HolderLookup.Provider registries, Runnable callback) {
        Objects.requireNonNull(registries, "registries");
        Objects.requireNonNull(callback, "callback");
        HolderLookup.Provider previous = CURRENT.get();
        CURRENT.set(registries);
        try {
            callback.run();
        } finally {
            if (previous == null) CURRENT.remove();
            else CURRENT.set(previous);
        }
    }
}
