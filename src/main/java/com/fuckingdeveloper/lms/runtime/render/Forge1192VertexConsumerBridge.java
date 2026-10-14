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

    /**
     * Target 26.3 finalizes the current vertex when the next vertex begins or
     * when its mesh is built. All source channels were already applied before
     * this call, so no explicit finalization operation remains to execute.
     */
    public static void endVertex(VertexConsumer consumer) {
    }
}
