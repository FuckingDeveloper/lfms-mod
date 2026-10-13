package com.fuckingdeveloper.lms.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

/** Target adapter for verified Forge 1.19.2 LevelChunk Mixin access members. */
public final class Forge1192LevelChunkBridge {
    private Forge1192LevelChunkBridge() {}

    public static boolean isLoaded(LevelChunk chunk) {
        return chunk.loaded;
    }

    public static void removeBlockEntityTicker(LevelChunk chunk, BlockPos pos) {
        chunk.removeBlockEntityTicker(pos);
    }

    public static void updateBlockEntityTicker(LevelChunk chunk, BlockEntity blockEntity) {
        chunk.updateBlockEntityTicker(blockEntity);
    }
}
