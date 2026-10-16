package com.fuckingdeveloper.lms.runtime.blockdata;

import java.util.Optional;

/** Target-owned storage context; source key projection and callback dispatch stay source-owned. */
public interface BlockDataContext<T, L> {
    Optional<L> lookup();
    boolean hasOutput();

    /** Bind the exact mutable root, retaining native ops/diagnostics and lookup identity. */
    BlockDataContext<T, L> withOutput(T root);
}
