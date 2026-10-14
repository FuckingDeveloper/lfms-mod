package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Verified 1.19.2 dirty operations, delegated to the bound 26.3 extractor. */
public final class Forge1192RenderInvalidationBridge {
    private Forge1192RenderInvalidationBridge() {}

    private static LevelExtractor extractor(LevelRenderer renderer) {
        java.util.Objects.requireNonNull(renderer, "renderer");
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.levelRenderer != renderer || client.levelExtractor == null)
            throw new IllegalStateException("Legacy render invalidation requires the active renderer's native extractor");
        return client.levelExtractor;
    }

    public static void setBlocksDirty(LevelRenderer renderer, int x0, int y0, int z0, int x1, int y1, int z1) {
        extractor(renderer).setBlocksDirty(x0, y0, z0, x1, y1, z1);
    }

    public static void setBlockDirty(LevelRenderer renderer, BlockPos pos, BlockState oldState, BlockState newState) {
        extractor(renderer).setBlockDirty(pos, oldState, newState);
    }

    public static void setSectionDirty(LevelRenderer renderer, int x, int y, int z) {
        extractor(renderer).setSectionDirty(x, y, z);
    }

    public static void setSectionDirtyWithNeighbors(LevelRenderer renderer, int x, int y, int z) {
        extractor(renderer).setSectionDirtyWithNeighbors(x, y, z);
    }
}
