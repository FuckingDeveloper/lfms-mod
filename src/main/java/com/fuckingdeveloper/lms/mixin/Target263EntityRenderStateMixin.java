package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.Forge1192RenderLivingEventBridge;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Captures target-authoritative entity identity before render events erase it. */
@Mixin(EntityRenderer.class)
abstract class Target263EntityRenderStateMixin {
    @Inject(method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;", at = @At("RETURN"))
    private void lms$captureSourceEntity(
            Entity entity,
            float partialTick,
            CallbackInfoReturnable<EntityRenderState> callback) {
        EntityRenderState state = callback.getReturnValue();
        if (entity instanceof LivingEntity living && state instanceof LivingEntityRenderState livingState) {
            Forge1192RenderLivingEventBridge.capture(living, livingState);
        }
    }
}
