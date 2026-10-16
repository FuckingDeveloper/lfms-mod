package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.itemhandler.LegacyMutableTagCarrier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ItemResource.class)
abstract class Target263ItemResourceSnapshotMixin {
    @ModifyVariable(method = "of(Lnet/minecraft/world/item/ItemStack;)Lnet/neoforged/neoforge/transfer/item/ItemResource;", at = @At("HEAD"), argsOnly = true)
    private static ItemStack lms$stableResource(ItemStack stack) {
        if ((Object)stack instanceof LegacyMutableTagCarrier carrier && carrier.lms$existingMutableTags() != null)
            return carrier.lms$existingMutableTags().resourceSnapshot(stack);
        return stack;
    }
}
