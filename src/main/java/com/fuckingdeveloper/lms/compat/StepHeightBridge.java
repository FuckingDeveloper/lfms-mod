package com.fuckingdeveloper.lms.compat;

import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.function.ToDoubleFunction;

/**
 * Runtime compatibility boundary for the legacy Forge Player#getStepHeight hook.
 *
 * <p>The NeoForge 26.3 fallback is Player#maxUpStep(). A legacy-mod adapter can
 * install a delegate later, after controlled legacy class loading is available.
 * Until then the transformed Player keeps vanilla/NeoForge behaviour instead of
 * linking directly against an IC2 class that is not yet loadable.</p>
 */
public final class StepHeightBridge {
    private static volatile ToDoubleFunction<Player> delegate;

    private StepHeightBridge() {
    }

    public static float getStepHeight(Player player) {
        ToDoubleFunction<Player> current = delegate;
        return current == null ? player.maxUpStep() : (float) current.applyAsDouble(player);
    }

    public static void install(ToDoubleFunction<Player> newDelegate) {
        delegate = Objects.requireNonNull(newDelegate, "newDelegate");
    }

    public static void clear() {
        delegate = null;
    }
}
