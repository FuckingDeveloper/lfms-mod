package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192DamageDataBridge;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** Only the base default changes; actual source item overrides remain virtual. */
@Mixin(IItemExtension.class)
interface Target263ItemDamageDefaultsMixin {
    @Inject(method="getDamage", at=@At("HEAD"), cancellable=true)
    private void lms$sourceDamage(ItemStack stack, CallbackInfoReturnable<Integer> result) {
        if (Forge1192DamageDataBridge.source(stack)) result.setReturnValue(Forge1192DamageDataBridge.getDefault(stack));
    }
    @Inject(method="setDamage", at=@At("HEAD"), cancellable=true)
    private void lms$sourceSetDamage(ItemStack stack, int damage, CallbackInfo callback) {
        if (Forge1192DamageDataBridge.source(stack)) {
            Forge1192DamageDataBridge.setDefault(stack, damage);
            callback.cancel();
        }
    }
}
