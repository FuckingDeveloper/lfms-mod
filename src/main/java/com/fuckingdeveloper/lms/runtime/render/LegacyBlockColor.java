package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Source-shaped Minecraft 1.19.2 procedural block-color callback. */
@FunctionalInterface
public interface LegacyBlockColor {
    int getColor(BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex);
}
