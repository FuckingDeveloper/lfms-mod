package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.runtime.Forge1192ToolModificationBridge;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.spongepowered.asm.mixin.Mixin;

/** Block has no concrete native override in the pinned target. */
@Mixin(Block.class)
abstract class Target263SourceToolDefaultMixin implements IBlockExtension {
    @Override public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility action, boolean simulate) {
        if (Forge1192ToolModificationBridge.active(state, context, action, simulate))
            return Forge1192ToolModificationBridge.sourceDefault(state, context, action, simulate);
        return IBlockExtension.super.getToolModifiedState(state, context, action, simulate);
    }
}
