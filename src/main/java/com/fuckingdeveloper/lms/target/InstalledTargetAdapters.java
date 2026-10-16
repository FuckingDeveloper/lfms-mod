package com.fuckingdeveloper.lms.target;

import com.fuckingdeveloper.lms.runtime.blockdata.BlockDataTargetLayout;
import com.fuckingdeveloper.lms.target.neoforge263.Target263BlockDataLayout;

/** Explicit build composition. This does not discover or guess future runtimes. */
public final class InstalledTargetAdapters {
    private InstalledTargetAdapters() {}
    public static BlockDataTargetLayout blockDataCallbacks() { return Target263BlockDataLayout.layout(); }
}
