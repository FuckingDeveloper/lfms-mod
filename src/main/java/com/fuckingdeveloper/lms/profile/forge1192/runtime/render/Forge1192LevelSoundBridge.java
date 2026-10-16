package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Preserve source exclusion, position/entity, category, volume, pitch and seed. */
public final class Forge1192LevelSoundBridge {
    private Forge1192LevelSoundBridge() {}

    public static void playSound(Level level, Player except, BlockPos pos,
            SoundEvent sound, SoundSource source, float volume, float pitch) {
        level.playSound(except, pos, sound, source, volume, pitch);
    }

    public static void playSound(Level level, Player except, double x, double y, double z,
            SoundEvent sound, SoundSource source, float volume, float pitch) {
        level.playSound(except, x, y, z, sound, source, volume, pitch);
    }

    public static void playSound(Level level, Player except, Entity entity,
            SoundEvent sound, SoundSource source, float volume, float pitch) {
        level.playSound(except, entity, sound, source, volume, pitch);
    }

    public static void playSeededSound(Level level, Player except, double x, double y, double z,
            SoundEvent sound, SoundSource source, float volume, float pitch, long seed) {
        level.playSeededSound(except, x, y, z, sound, source, volume, pitch, seed);
    }

    public static void playSeededSound(Level level, Player except, Entity entity,
            SoundEvent sound, SoundSource source, float volume, float pitch, long seed) {
        level.playSeededSound(except, entity, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
                source, volume, pitch, seed);
    }
}
