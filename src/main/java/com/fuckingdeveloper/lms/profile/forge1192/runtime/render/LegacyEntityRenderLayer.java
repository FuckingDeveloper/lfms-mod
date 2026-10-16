package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;

/** Exact erased Forge 1.19.2 RenderLayer override implemented by managed subclasses. */
public interface LegacyEntityRenderLayer {
    void lmsLegacyRender(PoseStack poseStack, LegacyMultiBufferSource buffers,
            int packedLight, Entity entity, float walkPosition, float walkSpeed,
            float partialTick, float ageInTicks, float yRot, float xRot);
}
