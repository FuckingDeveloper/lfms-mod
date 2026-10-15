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
    private final List<RecordedItem> items = new ArrayList<>();
    private boolean flushed;

    protected LegacyMultiBufferSource() {
        this.submitter = null;
        this.poseStack = null;
        this.recordingOnly = false;
        this.sprites = null;
    }

    public LegacyMultiBufferSource(SubmitNodeCollector submitter, PoseStack poseStack) {
        this(submitter,poseStack,null);
    }

    public LegacyMultiBufferSource(SubmitNodeCollector submitter, PoseStack poseStack,
            net.minecraft.client.resources.model.sprite.SpriteGetter sprites) {
        this.submitter = Objects.requireNonNull(submitter, "submitter");
        this.poseStack = Objects.requireNonNull(poseStack, "poseStack");
        this.recordingOnly = false;
        this.sprites = sprites;
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
                type -> new RecordingVertexConsumer(type == LegacyRenderType.m_110504_()));
    }

    public void flush() {
        flush(0);
    }

    public void flush(int outlineColor) {
        if (submitter == null) {
            throw new UnsupportedOperationException(
                    "Forge 1.19.2 immediate MultiBufferSource rendering has no direct Minecraft 26.3 target");
        }
        if (flushed) throw new IllegalStateException("Legacy render buffer already flushed");
        flushed = true;
        snapshot().submit(submitter, poseStack, false, outlineColor);
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

    public void recordItem(net.minecraft.client.renderer.item.ItemModelResolver resolver,
            net.minecraft.world.item.ItemStack stack, net.minecraft.world.item.ItemDisplayContext display,
            PoseStack sourcePose, net.minecraft.world.level.Level level, net.minecraft.world.entity.ItemOwner owner,
            int light, int overlay, int seed) {
        if (flushed || (!recordingOnly && submitter == null)) throw new IllegalStateException("No active source geometry recording");
        var state = new net.minecraft.client.renderer.item.ItemStackRenderState();
        resolver.updateForTopItem(state, stack.copy(), display, level, owner, seed);
        items.add(new RecordedItem(state, new org.joml.Matrix4f(sourcePose.last().pose()), light, overlay));
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
        return new RecordedGeometry(List.copyOf(recorded), List.copyOf(items));
    }

    private record RecordedItem(net.minecraft.client.renderer.item.ItemStackRenderState state,
            org.joml.Matrix4fc pose, int light, int overlay) {}

    private record RecordedBatch(
            LegacyRenderType renderType,
            List<Consumer<VertexConsumer>> operations, List<Position> positions) {}
    private record Position(float x, float y, float z) {}

    /** Immutable geometry extracted from one source renderer invocation. */
    public static final class RecordedGeometry {
        private final List<RecordedBatch> batches;
        private final List<RecordedItem> items;

        private RecordedGeometry(List<RecordedBatch> batches, List<RecordedItem> items) {
            this.batches = batches;
            this.items = items;
        }

        public org.joml.Vector3fc[] extents() {
            var result = new java.util.ArrayList<org.joml.Vector3fc>();
            batches.stream().flatMap(batch -> batch.positions().stream())
                    .map(position -> new org.joml.Vector3f(position.x(), position.y(), position.z()))
                    .forEach(result::add);
            for(var item : items) item.state().visitExtents(vertex -> result.add(new org.joml.Vector3f(vertex).mulPosition(item.pose())));
            return result.toArray(org.joml.Vector3fc[]::new);
        }

        public void submit(SubmitNodeCollector submitter, PoseStack poseStack) {
            submit(submitter, poseStack, 0);
        }

        public void submit(SubmitNodeCollector submitter, PoseStack poseStack, int outlineColor) {
            submit(submitter, poseStack, true, outlineColor);
        }

        private void submit(SubmitNodeCollector submitter, PoseStack poseStack,
                boolean applySubmissionPose, int outlineColor) {
            Objects.requireNonNull(submitter, "submitter");
            Objects.requireNonNull(poseStack, "poseStack");
            for (RecordedBatch batch : batches) {
                var type = batch.renderType().requireTargetRenderType();
                submitBatch(submitter, poseStack, batch, type, applySubmissionPose, 0);
                if (outlineColor != 0) type.outline().ifPresent(outline ->
                        submitBatch(submitter, poseStack, batch, outline, applySubmissionPose, outlineColor));
            }
            for (var item : items) {
                PoseStack nestedPose = applySubmissionPose ? poseStack : new PoseStack();
                nestedPose.pushPose();
                try {
                    nestedPose.mulPose(item.pose());
                    item.state().submit(nestedPose, submitter, item.light(), item.overlay(), outlineColor);
                } finally { nestedPose.popPose(); }
            }
        }
        private static void submitBatch(SubmitNodeCollector submitter, PoseStack poseStack, RecordedBatch batch,
                net.minecraft.client.renderer.rendertype.RenderType type, boolean applySubmissionPose, int outlineColor) {
            submitter.submitCustomGeometry(poseStack, type, (pose, target) -> {
                VertexConsumer output = applySubmissionPose || outlineColor != 0
                        ? new PoseTransformingVertexConsumer(pose, target, applySubmissionPose, outlineColor) : target;
                batch.operations().forEach(operation -> operation.accept(output));
            });
        }
    }

    /** Applies the target block-position pose after source-local transforms. */
    private record PoseTransformingVertexConsumer(
            PoseStack.Pose pose, VertexConsumer delegate, boolean applyPose, int outlineColor) implements VertexConsumer {
        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            if (applyPose) delegate.addVertex(pose, x, y, z);
            else delegate.addVertex(x, y, z);
            if (outlineColor != 0) delegate.setColor(outlineColor);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            if (outlineColor != 0) delegate.setColor(outlineColor);
            else delegate.setColor(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            delegate.setColor(outlineColor != 0 ? outlineColor : color);
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
            if (applyPose) delegate.setNormal(pose, x, y, z);
            else delegate.setNormal(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            delegate.setLineWidth(width);
            return this;
        }
    }

    private static final class RecordingVertexConsumer implements VertexConsumer {
        private final boolean sourceLines;
        private RecordingVertexConsumer(boolean sourceLines) { this.sourceLines=sourceLines; }
        private final List<Consumer<VertexConsumer>> operations = new ArrayList<>();
        private final List<Position> positions = new ArrayList<>();

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            positions.add(new Position(x, y, z));
            operations.add(target -> {
                if(sourceLines)Forge1192VertexConsumerBridge.sourceLineVertex(target,x,y,z);
                else target.addVertex(x,y,z);
            });
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
