package com.fuckingdeveloper.lms.runtime.color;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Binary compatibility contract for the Forge 1.19.2 BlockColor callback.
 * The method name is the legacy production bytecode name (not a 26.3 API).
 * Registration and tint-index dispatch are separate capabilities.
 */
@FunctionalInterface
public interface LegacyBlockColor {
    int m_92566_(BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex);
}
