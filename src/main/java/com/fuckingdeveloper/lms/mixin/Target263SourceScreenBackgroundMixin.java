package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.runtime.render.Target263SourceScreenBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class Target263SourceScreenBackgroundMixin {
    @Shadow @Final protected Minecraft minecraft;
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void lms$sourceOverlayBackground(GuiGraphicsExtractor graphics, int x, int y,
            float partialTick, CallbackInfo callback) {
        if (Target263SourceScreenBackground.sourceOverlay((Screen)(Object)this, minecraft.gui)) {
            // Retain the non-background native tail; explicit source/native custom painting is untouched.
            minecraft.gui.hud.extractDeferredSubtitles();
            callback.cancel();
        }
    }
}
