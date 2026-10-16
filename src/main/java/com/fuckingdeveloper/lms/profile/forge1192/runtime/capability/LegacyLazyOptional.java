package com.fuckingdeveloper.lms.profile.forge1192.runtime.capability;

import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Verifier-visible Forge 1.19.2 LazyOptional<T> facade.
 *
 * <p>This preserves lazy value identity and invalidation without pretending that
 * java.util.Optional or a modern NeoForge capability lookup has the same lifecycle.</p>
 */
public final class LegacyLazyOptional<T> {
    private static final LegacyLazyOptional<?> EMPTY = new LegacyLazyOptional<>(null, true);

    private final Supplier<? extends T> supplier;
    private final Object lock = new Object();
    private final Set<Consumer<LegacyLazyOptional<T>>> listeners = new HashSet<>();
    private volatile boolean valid;
    private boolean resolved;
    private T value;

    private LegacyLazyOptional(Supplier<? extends T> supplier, boolean valid) {
        this.supplier = supplier;
        this.valid = valid;
    }

    public static <T> LegacyLazyOptional<T> empty() {
        return LegacyLazyOptional.<T>castEmpty();
    }

    public static <T> LegacyLazyOptional<T> of(Supplier<? extends T> supplier) {
        return supplier == null ? empty() : new LegacyLazyOptional<>(supplier, true);
    }

    @SuppressWarnings("unchecked")
    private static <T> LegacyLazyOptional<T> castEmpty() {
        return (LegacyLazyOptional<T>) EMPTY;
    }

    /** Forge's cast changes only the inferred generic type and retains identity. */
    @SuppressWarnings("unchecked")
    public <X> LegacyLazyOptional<X> cast() {
        return (LegacyLazyOptional<X>) this;
    }

    public boolean isPresent() { return valid && supplier != null; }

    public T orElse(T fallback) {
        T resolvedValue = getValue();
        return resolvedValue == null ? fallback : resolvedValue;
    }

    public T orElseGet(Supplier<? extends T> fallback) {
        T resolvedValue = getValue();
        return resolvedValue == null ? Objects.requireNonNull(fallback, "fallback").get() : resolvedValue;
    }

    public void ifPresent(Consumer<? super T> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        T resolvedValue = getValue();
        if (valid && resolvedValue != null) consumer.accept(resolvedValue);
    }

    public java.util.Optional<T> resolve() {
        return java.util.Optional.ofNullable(getValue());
    }

    public void addListener(Consumer<LegacyLazyOptional<T>> listener) {
        Objects.requireNonNull(listener, "listener");
        synchronized (lock) {
            if (isPresent()) {
                listeners.add(listener);
                return;
            }
        }
        listener.accept(this);
    }

    public void invalidate() {
        java.util.List<Consumer<LegacyLazyOptional<T>>> notify;
        synchronized (lock) {
            if (!valid) return;
            valid = false;
            notify = java.util.List.copyOf(listeners);
            listeners.clear();
        }
        notify.forEach(listener -> listener.accept(this));
    }

    private T getValue() {
        if (!isPresent()) return null;
        synchronized (lock) {
            if (!valid || supplier == null) return null;
            if (!resolved) {
                value = Objects.requireNonNull(supplier.get(), "Legacy LazyOptional supplier value");
                resolved = true;
            }
            return value;
        }
    }
}
