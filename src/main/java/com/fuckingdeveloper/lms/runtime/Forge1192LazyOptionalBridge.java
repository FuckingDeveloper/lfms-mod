package com.fuckingdeveloper.lms.runtime;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Minimal Forge 1.19.2 LazyOptional compatibility state.
 * Keeps invalidation semantics explicit instead of degrading to Optional.
 */
public final class Forge1192LazyOptionalBridge {
    private Forge1192LazyOptionalBridge() {}

    public static Object empty() {
        return new Token(null, false);
    }

    public static void invalidate(Object value) {
        if (!(value instanceof Token token))
            throw new IllegalStateException("LazyOptional token has invalid provenance");
        token.invalidate();
    }

    public static final class Token {
        private final Supplier<?> supplier;
        private volatile boolean valid;
        Token(Supplier<?> supplier, boolean valid) {
            this.supplier = supplier;
            this.valid = valid;
        }
        public boolean valid() { return valid; }
        void invalidate() { valid = false; }
        Object value() {
            if (!valid) throw new IllegalStateException("Legacy LazyOptional is invalid");
            return Objects.requireNonNull(supplier, "supplier").get();
        }
    }
}
