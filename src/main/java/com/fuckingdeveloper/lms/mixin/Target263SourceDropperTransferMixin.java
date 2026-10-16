package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.itemhandler.Forge1192VanillaItemTransfer;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DropperBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DropperBlock.class)
abstract class Target263SourceDropperTransferMixin {
    @WrapOperation(method="dispenseFrom",at=@At(value="INVOKE",target="Lnet/minecraft/core/dispenser/DispenseItemBehavior;dispense(Lnet/minecraft/core/dispenser/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack lms$sourceInsert(DispenseItemBehavior behavior, BlockSource source, ItemStack stack, Operation<ItemStack> original) {
        var facing=source.state().getValue(DropperBlock.FACING);
        var handler=Forge1192VanillaItemTransfer.find(source.level(),source.pos().relative(facing),facing.getOpposite());
        if(handler==null)return original.call(behavior,source,stack);
        // The native dropper owns slot selection and writes back the returned remainder.
        var temporary=new net.minecraft.world.SimpleContainer(stack.copy());
        Forge1192VanillaItemTransfer.insertOne(temporary,0,handler);
        return temporary.getItem(0);
    }
}
