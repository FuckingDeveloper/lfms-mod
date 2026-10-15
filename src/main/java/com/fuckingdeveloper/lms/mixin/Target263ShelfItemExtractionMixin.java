package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.runtime.render.Target263GroundItemExtraction;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.blockentity.ShelfRenderer;
import net.minecraft.client.renderer.blockentity.state.ShelfRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.*;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ShelfRenderer.class)
abstract class Target263ShelfItemExtractionMixin {
    @Redirect(method="extractRenderState(Lnet/minecraft/world/level/block/entity/ShelfBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/ShelfRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
            at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/item/ItemModelResolver;updateForTopItem(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/ItemOwner;I)V"))
    private void lms$shelfLight(ItemModelResolver resolver,ItemStackRenderState output,ItemStack stack,
            ItemDisplayContext display,Level level,ItemOwner owner,int seed,
            ShelfBlockEntity shelf,ShelfRenderState state,float partialTick,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay overlay) {
        Target263GroundItemExtraction.extractShelf(output,stack,owner,state.lightCoords,
                ()->resolver.updateForTopItem(output,stack,display,level,owner,seed));
    }
}
