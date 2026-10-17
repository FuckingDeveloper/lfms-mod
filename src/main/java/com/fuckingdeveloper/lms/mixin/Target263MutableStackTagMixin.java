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
    @Unique private void lms$syncEnchantments() {
        if (lms$tags != null) lms$tags.reconcileStoredEnchantments((ItemStack)(Object)this);
    }
    @Inject(method = {"getComponents", "getComponentsPatch", "immutableComponents", "getPrototype", "hasNonDefault", "isComponentsPatchEmpty", "copy", "set", "remove"}, at = @At("HEAD"))
    private void lms$reconcileEnchantments(CallbackInfoReturnable<?> callback) { lms$syncEnchantments(); }
    @Inject(method = {"applyComponentsAndValidate", "applyComponents"}, at = @At("HEAD"))
    private void lms$reconcileEnchantmentsBeforeApply(CallbackInfo callback) { lms$syncEnchantments(); }
    @Inject(method = {"isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"}, at = @At("HEAD"))
    private static void lms$reconcileComparedStacks(ItemStack a, ItemStack b, CallbackInfoReturnable<Boolean> callback) {
        lms$syncStack(a); lms$syncStack(b);
    }
    @Inject(method = "isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStackTemplate;)Z", at = @At("HEAD"))
    private static void lms$reconcileComparedTemplate(ItemStack a, net.minecraft.world.item.ItemStackTemplate b, CallbackInfoReturnable<Boolean> callback) {
        lms$syncStack(a);
    }
    @Inject(method = "matchesIgnoringComponents", at = @At("HEAD"))
    private static void lms$reconcileIgnoringComponents(ItemStack a, ItemStack b, java.util.function.Predicate<net.minecraft.core.component.DataComponentType<?>> ignored, CallbackInfoReturnable<Boolean> callback) {
        lms$syncStack(a); lms$syncStack(b);
    }
    @Unique private static void lms$syncStack(ItemStack stack) {
        var carrier = (LegacyMutableTagCarrier)(Object)stack;
        if (carrier.lms$existingMutableTags() != null) carrier.lms$existingMutableTags().reconcileStoredEnchantments(stack);
    }
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
