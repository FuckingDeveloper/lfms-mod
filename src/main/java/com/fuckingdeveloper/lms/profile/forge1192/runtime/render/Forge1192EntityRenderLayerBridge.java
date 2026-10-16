package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

import java.util.Objects;

/** Projects source entity-valued layer callbacks onto the target submit pipeline. */
public final class Forge1192EntityRenderLayerBridge {
    private Forge1192EntityRenderLayerBridge() {}

    public static void submit(LegacyEntityRenderLayer layer, PoseStack poseStack,
            SubmitNodeCollector submitter, int packedLight, EntityRenderState rawState,
            float yRot, float xRot) {
        Objects.requireNonNull(layer, "legacy layer");
        if (!(rawState instanceof LivingEntityRenderState state)) {
            throw new IllegalArgumentException("Forge 1.19.2 living render layer received "
                    + rawState.getClass().getName());
        }
        float partialTick = state.ageInTicks - (float) Math.floor(state.ageInTicks);
        var entity = Forge1192RenderLivingEventBridge.entity(state, partialTick);
        // Derive from the exact entity after matching; tickCount may be large
        // enough that float precision erased the fractional state component.
        partialTick = state.ageInTicks - entity.tickCount;
        if (partialTick < 0.0F || partialTick > 1.0F) {
            throw new IllegalStateException("Invalid target render partial tick " + partialTick);
        }
        var buffers = new LegacyMultiBufferSource(submitter, poseStack);
        layer.lmsLegacyRender(poseStack, buffers, packedLight, entity,
                state.walkAnimationPos, state.walkAnimationSpeed, partialTick,
                state.ageInTicks, yRot, xRot);
        buffers.flush();
    }
}
