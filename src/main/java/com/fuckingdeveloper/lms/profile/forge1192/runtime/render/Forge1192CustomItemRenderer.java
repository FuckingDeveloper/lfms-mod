package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.*;

/** Source custom-renderer shape; vanilla source renderer fallbacks are not implied. */
public abstract class Forge1192CustomItemRenderer {
    protected Forge1192CustomItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {}
    public void m_108829_(ItemStack stack, ItemDisplayContext context, PoseStack pose,
            LegacyMultiBufferSource buffers, int light, int overlay) {
        throw new UnsupportedOperationException("Source custom item renderer has no renderByItem override");
    }
}
