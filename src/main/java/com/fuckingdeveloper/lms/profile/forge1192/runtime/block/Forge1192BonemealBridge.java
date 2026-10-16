package com.fuckingdeveloper.lms.profile.forge1192.runtime.block;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealSource;

/** Source-generation intent at the target fertilizer callback boundary. */
public final class Forge1192BonemealBridge {
    private Forge1192BonemealBridge() {}

    public static void requireInteraction(BonemealSource source) {
        if (source != BonemealSource.INTERACTION) {
            throw new UnsupportedOperationException("Unproven source bonemeal origin: " + source);
        }
    }

    public static boolean clientSide(LevelReader reader, BonemealSource source) {
        requireInteraction(source);
        if (reader instanceof Level level) return level.isClientSide();
        throw new UnsupportedOperationException("Source bonemeal validity requires authoritative Level side: " + reader.getClass().getName());
    }
}
