package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.blaze3d.vertex.VertexConsumer;

/** Target-26.3 execution boundary for the Forge 1.19.2 fluent vertex ABI. */
public final class Forge1192VertexConsumerBridge {
    private Forge1192VertexConsumerBridge() {
    }

    /** Target 26.3 stores vertex positions as floats rather than source doubles. */
    public static VertexConsumer addVertex(VertexConsumer consumer, double x, double y, double z) {
        return consumer.addVertex((float) x, (float) y, (float) z);
    }

    /** Source normal(Matrix3f,...) multiplies without renormalizing the result. */
    public static VertexConsumer normal(VertexConsumer consumer, org.joml.Matrix3f matrix, float x, float y, float z) {
        var normal=matrix.transform(x,y,z,new org.joml.Vector3f());
        return consumer.setNormal(normal.x(),normal.y(),normal.z());
    }

    /** Source RenderType.lines uses LineStateShard's window-scaled default. */
    public static float sourceLineWidth(int framebufferWidth) {
        return Math.max(2.5F, framebufferWidth / 1920.0F * 2.5F);
    }

    public static void sourceLineVertex(VertexConsumer consumer, float x, float y, float z) {
        consumer.addVertex(x,y,z).setLineWidth(sourceLineWidth(net.minecraft.client.Minecraft.getInstance().getWindow().getWidth()));
    }

    /**
     * Target 26.3 finalizes the current vertex when the next vertex begins or
     * when its mesh is built. All source channels were already applied before
     * this call, so no explicit finalization operation remains to execute.
     */
    public static void endVertex(VertexConsumer consumer) {
    }
}
