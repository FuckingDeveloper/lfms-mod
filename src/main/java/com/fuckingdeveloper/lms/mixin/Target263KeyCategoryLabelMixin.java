package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.Forge1192KeyMappingBridge;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Registered source category intent retains its actual source language key. */
@Mixin(KeyMapping.Category.class)
abstract class Target263KeyCategoryLabelMixin {
    @Inject(method="label",at=@At("HEAD"),cancellable=true)
    private void lms$sourceLabel(CallbackInfoReturnable<Component> callback) {
        Forge1192KeyMappingBridge.sourceCategoryLabel((KeyMapping.Category)(Object)this)
                .ifPresent(key -> callback.setReturnValue(Component.translatable(key)));
    }
}
