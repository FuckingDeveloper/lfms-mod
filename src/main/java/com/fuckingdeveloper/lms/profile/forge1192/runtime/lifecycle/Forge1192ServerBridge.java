package com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.PermissionCheck;

import java.util.Objects;

/** Exact source-era server values whose target representation changed. */
public final class Forge1192ServerBridge {
    private Forge1192ServerBridge() {}

    public static int operatorUserPermissionLevel(MinecraftServer server) {
        return Objects.requireNonNull(server, "server").operatorUserPermissions().level().id();
    }

    public static boolean hasPermission(CommandSourceStack source, int level) {
        Objects.requireNonNull(source, "source");
        PermissionCheck check = switch (level) {
            case 0 -> Commands.LEVEL_ALL;
            case 1 -> Commands.LEVEL_MODERATORS;
            case 2 -> Commands.LEVEL_GAMEMASTERS;
            case 3 -> Commands.LEVEL_ADMINS;
            default -> Commands.LEVEL_OWNERS;
        };
        return check.check(source.permissions());
    }
}
