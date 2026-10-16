package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.itemhandler.Forge1192VanillaItemTransfer;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.neoforged.neoforge.transfer.item.ContainerOrHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
abstract class Target263SourceHopperTransferMixin {
    @Inject(method="ejectItems",at=@At(value="INVOKE",target="Lnet/neoforged/neoforge/transfer/item/ContainerOrHandler;isEmpty()Z"),cancellable=true)
    private static void lms$insert(Level level, BlockPos pos, HopperBlockEntity hopper, CallbackInfoReturnable<Boolean> result,
                                  @Local ContainerOrHandler discovered) {
        if(!discovered.isEmpty())return;
        var facing=hopper.getBlockState().getValue(HopperBlock.FACING);
        var handler=Forge1192VanillaItemTransfer.find(level,pos.relative(facing),facing.getOpposite());
        if(handler==null)return;
        for(int slot=0;slot<hopper.getContainerSize();slot++)
            if(Forge1192VanillaItemTransfer.insertOne(hopper,slot,handler)){result.setReturnValue(true);return;}
        result.setReturnValue(false);
    }
    @Inject(method="suckInItems",at=@At(value="INVOKE",target="Lnet/neoforged/neoforge/transfer/item/ContainerOrHandler;container()Lnet/minecraft/world/Container;",ordinal=0),cancellable=true)
    private static void lms$extract(Level level, Hopper hopper, CallbackInfoReturnable<Boolean> result,
                                   @Local ContainerOrHandler discovered) {
        if(!discovered.isEmpty())return;
        var pos=BlockPos.containing(hopper.getLevelX(),hopper.getLevelY()+1,hopper.getLevelZ());
        var handler=Forge1192VanillaItemTransfer.find(level,pos,Direction.DOWN);
        if(handler!=null)result.setReturnValue(Forge1192VanillaItemTransfer.extractOne(handler,hopper));
    }
}
