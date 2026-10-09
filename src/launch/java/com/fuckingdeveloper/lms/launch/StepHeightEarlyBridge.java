package com.fuckingdeveloper.lms.launch;

import net.minecraft.world.entity.player.Player;

/**
 * Link-safe early bridge for IC2 transform-6.
 *
 * Kept in the FML LIBRARY JAR so transformed Minecraft bytecode can resolve it
 * independently of the later ordinary LMS mod source set.
 */
public final class StepHeightEarlyBridge {
    private StepHeightEarlyBridge() {}

    public static float getStepHeight(Player player) {
        return player.maxUpStep();
    }
}
