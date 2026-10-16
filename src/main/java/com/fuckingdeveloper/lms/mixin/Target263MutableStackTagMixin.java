package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.itemhandler.LegacyMutableTagCarrier;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.itemhandler.LegacyMutableTagState;


import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
abstract class Target263MutableStackTagMixin implements LegacyMutableTagCarrier {
    @Shadow @Final private net.minecraft.core.component.PatchedDataComponentMap components;
    @Override public net.minecraft.core.component.DataComponentMap lms$sourceComponents() { return components; }
    @Unique private LegacyMutableTagState lms$tags;
    @Override public LegacyMutableTagState lms$mutableTags() {
        if (lms$tags == null) lms$tags = new LegacyMutableTagState();
        return lms$tags;
    }
    @Override public LegacyMutableTagState lms$existingMutableTags() { return lms$tags; }
    @Inject(method = "copy", at = @At("RETURN"))
    private void lms$detachSnapshot(CallbackInfoReturnable<ItemStack> result) {
        if (lms$tags != null) lms$tags.detachCopy((ItemStack)(Object)this, result.getReturnValue());
    }
    @Inject(method="setDamageValue", at=@At("RETURN"))
    private void lms$mirrorSourceDamage(int damage, CallbackInfo callback) {
        var stack = (ItemStack)(Object)this;
        if (com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192DamageDataBridge.source(stack))
            com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192DamageDataBridge.mirror(stack, stack.getItem().getDamage(stack));
    }
}
