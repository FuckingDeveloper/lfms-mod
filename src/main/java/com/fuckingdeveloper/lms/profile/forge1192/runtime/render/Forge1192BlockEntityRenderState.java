package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

/** Target state containing geometry fully extracted from a source renderer. */
public final class Forge1192BlockEntityRenderState extends BlockEntityRenderState {
    private LegacyMultiBufferSource.RecordedGeometry geometry;

    void geometry(LegacyMultiBufferSource.RecordedGeometry geometry) {
        if (this.geometry != null) {
            throw new IllegalStateException("Legacy block-entity geometry already extracted");
        }
        this.geometry = java.util.Objects.requireNonNull(geometry, "geometry");
    }

    LegacyMultiBufferSource.RecordedGeometry geometry() {
        if (geometry == null) {
            throw new IllegalStateException("Legacy block-entity geometry was not extracted");
        }
        return geometry;
    }
}
