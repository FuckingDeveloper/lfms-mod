package com.fuckingdeveloper.lms.runtime.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * Structural and behavioral compatibility contract for Forge 1.19.2 IForgeBlock.
 *
 * <p>The modern IBlockExtension destruction hook gained the player's tool and
 * moved the destroy callback into its caller. Legacy overrides can invoke the
 * old interface default directly, so merely renaming the interface would both
 * break linkage and omit the legacy destroy callback.</p>
 */
public interface LegacyIForgeBlock extends net.neoforged.neoforge.common.extensions.IBlockExtension {
    private Block self() {
        return (Block) this;
    }

    default boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos,
                                        Player player, boolean willHarvest, FluidState fluid) {
        self().playerWillDestroy(level, pos, state, player);
        return level.setBlock(pos, fluid.createLegacyBlock(), level.isClientSide() ? 11 : 3);
    }
}
