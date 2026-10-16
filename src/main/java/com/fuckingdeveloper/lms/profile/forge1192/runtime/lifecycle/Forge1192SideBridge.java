package com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle;

import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.util.thread.EffectiveSide;

/**
 * Forge 1.19.2 logical-side compatibility. This deliberately delegates to
 * NeoForge's effective logical side outside proven client-operation scopes.
 */
public final class Forge1192SideBridge {
    private static final ThreadLocal<LogicalSide> CLIENT_OPERATION = new ThreadLocal<>();
    private Forge1192SideBridge() {}

    /** Client-owned tooltip evaluation can run on a worker inheriting a server group. */
    public static <T> T clientTooltip(java.util.function.Supplier<T> callback) {
        var previous=CLIENT_OPERATION.get();CLIENT_OPERATION.set(LogicalSide.CLIENT);
        try{return callback.get();}
        finally{if(previous==null)CLIENT_OPERATION.remove();else CLIENT_OPERATION.set(previous);}
    }

    public static LogicalSide getEffectiveSide() {
        var context=CLIENT_OPERATION.get();
        return context==null?EffectiveSide.get():context;
    }
    public static boolean isClient(net.neoforged.api.distmarker.Dist dist) {
        return dist == net.neoforged.api.distmarker.Dist.CLIENT;
    }

    public static boolean isDedicatedServer(net.neoforged.api.distmarker.Dist dist) {
        return dist == net.neoforged.api.distmarker.Dist.DEDICATED_SERVER;
    }
}

