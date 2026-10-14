package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.runtime.Target263FluidModelLookupBridge;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Map;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Models are already baked here, but ModelManager applies them only after the barrier. */
@Mixin(ModelManager.class)
abstract class Target263FluidModelLookupMixin {
    @WrapOperation(method="lambda$loadModels$1", at=@At(value="INVOKE",target="Lnet/neoforged/neoforge/client/ClientHooks;onModifyBakingResult(Lnet/minecraft/client/resources/model/ModelBakery$BakingResult;Lnet/minecraft/client/resources/model/sprite/MaterialBaker;Lnet/minecraft/client/renderer/texture/SpriteLoader$Preparations;Lnet/minecraft/client/renderer/texture/SpriteLoader$Preparations;Lnet/minecraft/client/resources/model/ModelBakery;)V"))
    private static void lms$pendingFluidModels(ModelBakery.BakingResult result,MaterialBaker materials,SpriteLoader.Preparations blocks,SpriteLoader.Preparations items,ModelBakery bakery,Operation<Void> original,@Local(ordinal=1) Map<Fluid,FluidModel> fluidModels) {
        try(var scope=Target263FluidModelLookupBridge.enter(fluidModels)) {
            original.call(result,materials,blocks,items,bakery);
        }
    }
}
