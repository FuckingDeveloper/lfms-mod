package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.runtime.Forge1192VanillaConstructionBridge;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
abstract class Target263CreativeItemIntentMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void lms$captureCreativeIntent(Item.Properties properties, CallbackInfo callback) {
        Forge1192VanillaConstructionBridge.captureCreativeItem(properties);
    }
}
