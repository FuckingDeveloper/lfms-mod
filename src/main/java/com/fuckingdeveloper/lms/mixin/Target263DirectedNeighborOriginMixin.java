package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.block.Forge1192NeighborBridge;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.redstone.Orientation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Native directed producers retain a source position absent from the new call ABI. */
@Mixin({DiodeBlock.class, ObserverBlock.class})
abstract class Target263DirectedNeighborOriginMixin {
    @WrapOperation(method = "updateNeighborsInFront", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;neighborChanged(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/redstone/Orientation;)V"))
    private void lms$directedOrigin(Level level, BlockPos destination, Block block, Orientation orientation,
                                    Operation<Void> original, @Local(argsOnly = true) BlockPos sourcePos) {
        Forge1192NeighborBridge.notify(new Forge1192NeighborBridge.Origin(level, sourcePos.immutable()), level,
                () -> original.call(level, destination, block, orientation));
    }
}
