package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Adapts the Forge 1.19.2 immediate block-entity renderer contract to the
 * target extract/submit pipeline without retaining a live block entity in the
 * submitted state.
 */
public interface Forge1192BlockEntityRendererBridge
        extends BlockEntityRenderer<BlockEntity, Forge1192BlockEntityRenderState> {

    void lmsLegacyRender(BlockEntity blockEntity, float partialTick,
            PoseStack poseStack, LegacyMultiBufferSource buffers,
            int packedLight, int packedOverlay);

    @Override
    default Forge1192BlockEntityRenderState createRenderState() {
        return new Forge1192BlockEntityRenderState();
    }

    @Override
    default void extractRenderState(BlockEntity blockEntity,
            Forge1192BlockEntityRenderState state, float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress);
        LegacyMultiBufferSource buffers = LegacyMultiBufferSource.recording(net.minecraft.client.Minecraft.getInstance().getAtlasManager());
        lmsLegacyRender(blockEntity, partialTick, new PoseStack(), buffers,
                state.lightCoords, OverlayTexture.NO_OVERLAY);
        state.geometry(buffers.finishRecording());
    }

    @Override
    default void submit(Forge1192BlockEntityRenderState state,
            PoseStack poseStack, SubmitNodeCollector submitter,
            CameraRenderState camera) {
        state.geometry().submit(submitter, poseStack);
    }
}
