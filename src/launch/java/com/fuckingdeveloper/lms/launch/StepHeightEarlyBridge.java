package com.fuckingdeveloper.lms.launch;

import net.minecraft.world.entity.player.Player;

/**
 * Early-library implementation of IC2 Classic 1.19.2 ASMHacks#getStepHeight.
 *
 * The legacy hook returned 0.6F while the player was sneaking
 * (Player#m_6144_ / isShiftKeyDown), otherwise the Forge step height.
 * NeoForge 26.3 uses Entity#maxUpStep() for the latter.
 *
 * This class lives in the FML LIBRARY jar so transformed Player bytecode
 * can link it before the ordinary LMS mod is initialized.
 */
public final class StepHeightEarlyBridge {
    private StepHeightEarlyBridge() {}

    public static float getStepHeight(Player player) {
        return player.isShiftKeyDown() ? 0.6F : player.maxUpStep();
    }
}
