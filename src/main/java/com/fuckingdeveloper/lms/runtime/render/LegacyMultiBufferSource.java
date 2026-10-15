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
    private final boolean recordingOnly;
    private final net.minecraft.client.resources.model.sprite.SpriteGetter sprites;
    private final Map<LegacyRenderType, RecordingVertexConsumer> batches = new LinkedHashMap<>();
    private boolean flushed;

    protected LegacyMultiBufferSource() {
        this.submitter = null;
        this.poseStack = null;
        this.recordingOnly = false;
        this.sprites = null;
    }

    public LegacyMultiBufferSource(SubmitNodeCollector submitter, PoseStack poseStack) {
        this.submitter = Objects.requireNonNull(submitter, "submitter");
        this.poseStack = Objects.requireNonNull(poseStack, "poseStack");
        this.recordingOnly = false;
        this.sprites = null;
    }

    private LegacyMultiBufferSource(boolean recordingOnly) { this(recordingOnly,null); }

    private LegacyMultiBufferSource(boolean recordingOnly,net.minecraft.client.resources.model.sprite.SpriteGetter sprites) {
        this.sprites = sprites;
        this.submitter = null;
        this.poseStack = null;
        this.recordingOnly = recordingOnly;
    }

    /** Begin an extraction-phase recording that can be submitted later. */
    public static LegacyMultiBufferSource recording() {
        return new LegacyMultiBufferSource(true);
    }

    public static LegacyMultiBufferSource recording(net.minecraft.client.resources.model.sprite.SpriteGetter sprites) {
        return new LegacyMultiBufferSource(true,Objects.requireNonNull(sprites));
    }
    public net.minecraft.client.renderer.texture.TextureAtlasSprite sprite(net.minecraft.client.resources.model.sprite.SpriteId id) {
        if(sprites==null)throw new UnsupportedOperationException("Source atlas material rendering has no published target SpriteGetter context");
        return Objects.requireNonNull(sprites.get(id),"Target sprite getter returned null");
    }

    /** Exact Forge 1.19.2 SRG ABI used by legacy bytecode. */
    public VertexConsumer m_6299_(LegacyRenderType renderType) {
        return getBuffer(renderType);
    }

    public VertexConsumer getBuffer(LegacyRenderType renderType) {
        if (submitter == null && !recordingOnly) {
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
        snapshot().submit(submitter, poseStack, false);
    }

    /** Freeze extraction output into an immutable target-submittable value. */
    public RecordedGeometry finishRecording() {
        if (!recordingOnly) {
            throw new IllegalStateException("Legacy buffer source is not extraction-phase recording");
        }
        if (flushed) throw new IllegalStateException("Legacy render buffer already finished");
        flushed = true;
        return snapshot();
    }

    public static LegacyBufferSource m_109898_(
            com.mojang.blaze3d.vertex.BufferBuilder ignoredBuilder) {
        return new LegacyBufferSource();
    }

    private RecordedGeometry snapshot() {
        List<RecordedBatch> recorded = new ArrayList<>();
        batches.forEach((renderType, recorder) -> {
            List<Consumer<VertexConsumer>> operations = List.copyOf(recorder.operations);
                if (!operations.isEmpty()) recorded.add(new RecordedBatch(renderType, operations, List.copyOf(recorder.positions)));
        });
        return new RecordedGeometry(List.copyOf(recorded));
    }

    private record RecordedBatch(
            LegacyRenderType renderType,
            List<Consumer<VertexConsumer>> operations, List<Position> positions) {}
    private record Position(float x, float y, float z) {}

    /** Immutable geometry extracted from one source renderer invocation. */
    public static final class RecordedGeometry {
        private final List<RecordedBatch> batches;

        private RecordedGeometry(List<RecordedBatch> batches) {
            this.batches = batches;
        }

        public org.joml.Vector3fc[] extents() {
            return batches.stream().flatMap(batch -> batch.positions().stream())
                    .map(position -> new org.joml.Vector3f(position.x(), position.y(), position.z()))
                    .toArray(org.joml.Vector3fc[]::new);
        }

        public void submit(SubmitNodeCollector submitter, PoseStack poseStack) {
            submit(submitter, poseStack, true);
        }

        private void submit(SubmitNodeCollector submitter, PoseStack poseStack,
                boolean applySubmissionPose) {
            Objects.requireNonNull(submitter, "submitter");
            Objects.requireNonNull(poseStack, "poseStack");
            for (RecordedBatch batch : batches) {
                submitter.submitCustomGeometry(poseStack,
                        batch.renderType().requireTargetRenderType(), (pose, target) -> {
                            VertexConsumer output = applySubmissionPose
                                    ? new PoseTransformingVertexConsumer(pose, target)
                                    : target;
                            batch.operations().forEach(operation -> operation.accept(output));
                        });
            }
        }
    }

    /** Applies the target block-position pose after source-local transforms. */
    private record PoseTransformingVertexConsumer(
            PoseStack.Pose pose, VertexConsumer delegate) implements VertexConsumer {
        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(pose, x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            delegate.setColor(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            delegate.setColor(color);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv3(float u, float v) {
            delegate.setUv3(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            delegate.setNormal(pose, x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            delegate.setLineWidth(width);
            return this;
        }
    }

    private static final class RecordingVertexConsumer implements VertexConsumer {
        private final List<Consumer<VertexConsumer>> operations = new ArrayList<>();
        private final List<Position> positions = new ArrayList<>();

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            positions.add(new Position(x, y, z));
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
