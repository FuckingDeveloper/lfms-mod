package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.runtime.render.Target263GroundItemExtraction;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** EntityRenderer has already populated lightCoords at this exact native call. */
@Mixin(ItemEntityRenderer.class)
abstract class Target263GroundItemExtractionMixin {
    @Redirect(method = "extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;extractItemGroupRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/item/ItemModelResolver;)V"))
    private void lms$extractWithNativeLight(ItemEntityRenderState state, Entity entity, ItemStack stack, ItemModelResolver resolver) {
        Target263GroundItemExtraction.extract(state.item, stack, entity, state.lightCoords, state.outlineColor,
                () -> state.extractItemGroupRenderState(entity, stack, resolver));
    }
}
