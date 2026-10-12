package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Records Forge 1.19.2 immediate buffer writes and submits them through the
 * target's deferred custom-geometry pipeline.
 */
public class LegacyMultiBufferSource {
    private final SubmitNodeCollector submitter;
    private final PoseStack poseStack;
    private final Map<LegacyRenderType, RecordingVertexConsumer> batches = new LinkedHashMap<>();
    private boolean flushed;

    protected LegacyMultiBufferSource() {
        this.submitter = null;
        this.poseStack = null;
    }

    public LegacyMultiBufferSource(SubmitNodeCollector submitter, PoseStack poseStack) {
        this.submitter = Objects.requireNonNull(submitter, "submitter");
        this.poseStack = Objects.requireNonNull(poseStack, "poseStack");
    }

    /** Exact Forge 1.19.2 SRG ABI used by legacy bytecode. */
    public VertexConsumer m_6299_(LegacyRenderType renderType) {
        return getBuffer(renderType);
    }

    public VertexConsumer getBuffer(LegacyRenderType renderType) {
        if (submitter == null) {
            throw new UnsupportedOperationException(
                    "Forge 1.19.2 immediate MultiBufferSource rendering has no direct Minecraft 26.3 target");
        }
        if (flushed) throw new IllegalStateException("Legacy render buffer already flushed");
        return batches.computeIfAbsent(Objects.requireNonNull(renderType, "renderType"),
                ignored -> new RecordingVertexConsumer());
    }

    public void flush() {
        if (submitter == null) {
            throw new UnsupportedOperationException(
                    "Forge 1.19.2 immediate MultiBufferSource rendering has no direct Minecraft 26.3 target");
        }
        if (flushed) throw new IllegalStateException("Legacy render buffer already flushed");
        flushed = true;
        batches.forEach((renderType, recorder) -> {
            List<Consumer<VertexConsumer>> operations = List.copyOf(recorder.operations);
            if (!operations.isEmpty()) {
                submitter.submitCustomGeometry(poseStack, renderType.requireTargetRenderType(),
                        (pose, target) -> operations.forEach(operation -> operation.accept(target)));
            }
        });
    }

    public static LegacyBufferSource m_109898_(
            com.mojang.blaze3d.vertex.BufferBuilder ignoredBuilder) {
        return new LegacyBufferSource();
    }

    private static final class RecordingVertexConsumer implements VertexConsumer {
        private final List<Consumer<VertexConsumer>> operations = new ArrayList<>();

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            operations.add(target -> target.addVertex(x, y, z));
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            operations.add(target -> target.setColor(red, green, blue, alpha));
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            operations.add(target -> target.setColor(color));
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            operations.add(target -> target.setUv(u, v));
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            operations.add(target -> target.setUv1(u, v));
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            operations.add(target -> target.setUv2(u, v));
            return this;
        }

        @Override
        public VertexConsumer setUv3(float u, float v) {
            operations.add(target -> target.setUv3(u, v));
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            operations.add(target -> target.setNormal(x, y, z));
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            operations.add(target -> target.setLineWidth(width));
            return this;
        }
    }
}
