package com.fuckingdeveloper.lms.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Target adapters for Forge 1.19.2 block-state extension operations. */
public final class Forge1192BlockStateBridge {
    private static final ClassValue<Boolean> LEGACY_OCCLUSION_OVERRIDE = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            try {
                var method = type.getMethod("m_7952_", BlockState.class, BlockGetter.class, BlockPos.class);
                return method.getReturnType() == VoxelShape.class;
            } catch (NoSuchMethodException absent) {
                return false;
            }
        }
    };

    private Forge1192BlockStateBridge() {}

    public static boolean is(BlockState state, Block block) {
        return state.is(block);
    }

    /** Both runtimes cache full-cube occlusion for non-dynamic states. */
    public static boolean isSolidRender(BlockState state, BlockGetter level, BlockPos pos) {
        if (!state.canOcclude()) return false;
        requirePositionIndependentOcclusion(state);
        return state.isSolidRender();
    }

    private static void requirePositionIndependentOcclusion(BlockState state) {
        Block block = state.getBlock();
        if (block.hasDynamicShape() || LEGACY_OCCLUSION_OVERRIDE.get(block.getClass())) {
            throw new IllegalStateException("Legacy world/position-dependent occlusion query requires "
                    + "a shape adapter for " + block.getClass().getName());
        }
    }

    public static ItemStack getCloneItemStack(
            BlockState state, HitResult removedHitContext, BlockGetter level,
            BlockPos pos, Player player) {
        if (!(level instanceof LevelReader reader)) {
            throw new IllegalStateException(
                    "Legacy clone-item world does not satisfy the target LevelReader contract: "
                            + level.getClass().getName());
        }
        return state.getCloneItemStack(pos, reader, false, player);
    }
}
