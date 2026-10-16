package com.fuckingdeveloper.lms.profile.forge1192.runtime.data;

import java.util.Objects;
import java.util.function.Supplier;

/** Source-compatible replacement for Minecraft 1.19.2 LazyLoadedValue. */
public final class LegacyLazyLoadedValue<T> implements Supplier<T> {
    private Supplier<T> factory;
    private volatile boolean initialized;
    private T value;

    public LegacyLazyLoadedValue(Supplier<T> factory) {
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    @Override
    public T get() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    value = factory.get();
                    factory = null;
                    initialized = true;
                }
            }
        }
        return value;
    }

    /** Forge 1.19.2 runtime (SRG) identity for {@link #get()}. */
    public T m_13971_() {
        return get();
    }
}
