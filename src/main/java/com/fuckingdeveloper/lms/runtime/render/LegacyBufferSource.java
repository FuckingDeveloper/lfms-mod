package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * Source-shaped immediate buffer facade. Minecraft 26.3 removed this drawing
 * mode; operations fail explicitly until a concrete submit-node adaptation is
 * selected by a surrounding renderer bridge.
 */
public final class LegacyBufferSource extends LegacyMultiBufferSource {
    public LegacyBufferSource() {
        super();
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return super.getBuffer(renderType);
    }

    public void endLastBatch() {
        unsupported();
    }

    public void endBatch() {
        unsupported();
    }

    public void endBatch(RenderType renderType) {
        unsupported();
    }

    private static void unsupported() {
        throw new UnsupportedOperationException(
                "Forge 1.19.2 immediate BufferSource flush requires a Minecraft 26.3 renderer adapter");
    }
}
