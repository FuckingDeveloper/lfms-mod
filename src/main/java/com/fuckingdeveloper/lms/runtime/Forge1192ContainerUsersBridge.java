package com.fuckingdeveloper.lms.runtime;

import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;

public final class Forge1192ContainerUsersBridge {
    private Forge1192ContainerUsersBridge() {}
    public static Player requirePlayer(ContainerUser user) {
        if(user instanceof Player player)return player;
        throw new UnsupportedOperationException("Source player-only container callback cannot accept native container user: "+user);
    }
}
