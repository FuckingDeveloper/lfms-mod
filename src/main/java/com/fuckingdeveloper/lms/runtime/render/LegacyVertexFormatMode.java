package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;

/** Source-shaped Forge 1.19.2 vertex primitive mode. */
public enum LegacyVertexFormatMode {
    LINES(4, 2, 2, false, PrimitiveTopology.LINES),
    LINE_STRIP(5, 2, 1, true, PrimitiveTopology.DEBUG_LINE_STRIP),
    DEBUG_LINES(1, 2, 2, false, PrimitiveTopology.DEBUG_LINES),
    DEBUG_LINE_STRIP(3, 2, 1, true, PrimitiveTopology.DEBUG_LINE_STRIP),
    TRIANGLES(4, 3, 3, false, PrimitiveTopology.TRIANGLES),
    TRIANGLE_STRIP(5, 3, 1, true, PrimitiveTopology.TRIANGLE_STRIP),
    TRIANGLE_FAN(6, 3, 1, true, PrimitiveTopology.TRIANGLE_FAN),
    QUADS(7, 4, 4, false, PrimitiveTopology.QUADS);

    public final int asGLMode;
    public final int primitiveLength;
    public final int primitiveStride;
    public final boolean connectedPrimitives;
    private final PrimitiveTopology target;

    LegacyVertexFormatMode(int asGLMode, int primitiveLength, int primitiveStride,
                           boolean connectedPrimitives, PrimitiveTopology target) {
        this.asGLMode = asGLMode;
        this.primitiveLength = primitiveLength;
        this.primitiveStride = primitiveStride;
        this.connectedPrimitives = connectedPrimitives;
        this.target = target;
    }

    public int indexCount(int vertices) {
        return this == QUADS ? vertices / 4 * 6 : vertices;
    }

    public PrimitiveTopology target() {
        return target;
    }
}
