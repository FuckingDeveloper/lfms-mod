package com.fuckingdeveloper.lms.runtime;

import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.util.thread.EffectiveSide;

/**
 * Forge 1.19.2 logical-side compatibility. This deliberately delegates to
 * NeoForge's effective logical side instead of deriving it from physical Dist.
 */
public final class Forge1192SideBridge {
    private Forge1192SideBridge() {}

    public static LogicalSide getEffectiveSide() {
        return EffectiveSide.get();
    }
    public static boolean isClient(net.neoforged.api.distmarker.Dist dist) {
        return dist == net.neoforged.api.distmarker.Dist.CLIENT;
    }

    public static boolean isDedicatedServer(net.neoforged.api.distmarker.Dist dist) {
        return dist == net.neoforged.api.distmarker.Dist.DEDICATED_SERVER;
    }
}

