package com.fuckingdeveloper.lms.runtime.event;

import com.fuckingdeveloper.lms.runtime.render.LegacyMultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.Objects;

/**
 * Forge 1.19.2 block-highlight event projected onto the target's deferred
 * outline-rendering phase.
 *
 * <p>The target separates state extraction from geometry submission. LMS
 * therefore invokes the legacy listener from a {@code CustomBlockOutlineRenderer}
 * and records its vertex output as target render nodes. Cancellation retains
 * the source meaning of suppressing the vanilla outline.</p>
 */
public final class LegacyRenderHighlightBlockEvent extends Event implements ICancellableEvent {
    private final BlockHitResult target;
    private final Camera camera;
    private final PoseStack poseStack;
    private final LegacyMultiBufferSource buffers;

    public LegacyRenderHighlightBlockEvent(
            BlockHitResult target,
            Camera camera,
            PoseStack poseStack,
            SubmitNodeCollector submitter) {
        this.target = Objects.requireNonNull(target, "target");
        this.camera = Objects.requireNonNull(camera, "camera");
        this.poseStack = Objects.requireNonNull(poseStack, "poseStack");
        this.buffers = new LegacyMultiBufferSource(
                Objects.requireNonNull(submitter, "submitter"), poseStack);
    }

    public BlockHitResult getTarget() {
        return target;
    }

    public Camera getCamera() {
        return camera;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public LegacyMultiBufferSource getMultiBufferSource() {
        return buffers;
    }

    public void flushGeometry() {
        buffers.flush();
    }
}
