package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.block.Forge1192NeighborBridge;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.level.redstone.CollectingNeighborUpdater$MultiNeighborUpdate")
abstract class Target263MultiNeighborOriginMixin {
    @Shadow @Final private BlockPos sourcePos;
    @WrapOperation(method = "runNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/redstone/NeighborUpdater;executeUpdate(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/redstone/Orientation;Z)V"))
    private void lms$origin(Level level, BlockState state, BlockPos pos, Block block, Orientation orientation, boolean moved, Operation<Void> original) {
        Forge1192NeighborBridge.dispatch(new Forge1192NeighborBridge.Origin(level, sourcePos), level,
                () -> original.call(level, state, pos, block, orientation, moved));
    }
}
