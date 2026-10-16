package com.fuckingdeveloper.lms.mixin;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.LegacyArmorItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ItemStack.class)
abstract class Target263ArmorRepairMixin {
    @Inject(method="isValidRepairItem",at=@At("HEAD"),cancellable=true)
    private void lms$sourceRepair(ItemStack candidate,CallbackInfoReturnable<Boolean> callback) {
        var stack = (ItemStack)(Object)this;
        if (stack.getItem() instanceof LegacyArmorItem armor) callback.setReturnValue(armor.m_6832_(stack,candidate));
    }
}
