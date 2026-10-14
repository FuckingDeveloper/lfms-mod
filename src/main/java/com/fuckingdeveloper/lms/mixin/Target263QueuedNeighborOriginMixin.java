package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.runtime.Forge1192NeighborBridge;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = {"net.minecraft.world.level.redstone.CollectingNeighborUpdater$SimpleNeighborUpdate", "net.minecraft.world.level.redstone.CollectingNeighborUpdater$FullNeighborUpdate"})
abstract class Target263QueuedNeighborOriginMixin {
    @Unique private Forge1192NeighborBridge.Origin lms$sourceOrigin;
    @Inject(method = "<init>", at = @At("RETURN"))
    private void lms$capture(CallbackInfo callback) { lms$sourceOrigin = Forge1192NeighborBridge.captureNotification(); }
    @WrapOperation(method = "runNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/redstone/NeighborUpdater;executeUpdate(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/redstone/Orientation;Z)V"))
    private void lms$origin(Level level, BlockState state, BlockPos pos, Block block, Orientation orientation, boolean moved, Operation<Void> original) {
        Forge1192NeighborBridge.dispatch(lms$sourceOrigin, level, () -> original.call(level, state, pos, block, orientation, moved));
    }
}
