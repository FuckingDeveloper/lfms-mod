package com.fuckingdeveloper.lms.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

/** Target adapters for Forge 1.19.2 block-state extension operations. */
public final class Forge1192BlockStateBridge {
    private Forge1192BlockStateBridge() {}

    public static boolean is(BlockState state, Block block) {
        return state.is(block);
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
