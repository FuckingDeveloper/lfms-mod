package com.fuckingdeveloper.lms.runtime.capability;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Verifier-visible Forge 1.19.2 LazyOptional<T> facade.
 *
 * <p>This preserves lazy value identity and invalidation without pretending that
 * java.util.Optional or a modern NeoForge capability lookup has the same lifecycle.</p>
 */
public final class LegacyLazyOptional<T> {
    private final Supplier<? extends T> supplier;
    private volatile boolean valid;

    private LegacyLazyOptional(Supplier<? extends T> supplier, boolean valid) {
        this.supplier = supplier;
        this.valid = valid;
    }

    public static <T> LegacyLazyOptional<T> empty() {
        return new LegacyLazyOptional<>(null, false);
    }

    public static <T> LegacyLazyOptional<T> of(Supplier<? extends T> supplier) {
        return new LegacyLazyOptional<>(Objects.requireNonNull(supplier, "supplier"), true);
    }

    public void invalidate() { valid = false; }
    public boolean isPresent() { return valid && supplier != null; }

    public T orElse(T fallback) {
        return isPresent() ? Objects.requireNonNull(supplier.get(), "supplier value") : fallback;
    }
}
