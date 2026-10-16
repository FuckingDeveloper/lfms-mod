package com.fuckingdeveloper.lms.profile.forge1192.runtime.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Exact Forge 1.19.2 BlockBehaviour base interaction, before subclass dispatch. */
public final class Forge1192BlockInteractionBridge {
    private Forge1192BlockInteractionBridge() {}

    public static InteractionResult baseUse(Block block, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.PASS;
    }
}
