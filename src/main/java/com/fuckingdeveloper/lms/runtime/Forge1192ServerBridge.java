package com.fuckingdeveloper.lms.runtime;

import net.minecraft.server.MinecraftServer;

/** Exact source-era server values whose target representation changed. */
public final class Forge1192ServerBridge {
    private Forge1192ServerBridge() {}

    public static int operatorUserPermissionLevel(MinecraftServer server) {
        return server.operatorUserPermissions().level().id();
    }
}
