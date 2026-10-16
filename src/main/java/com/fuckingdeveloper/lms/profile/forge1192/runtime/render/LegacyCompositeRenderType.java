package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

/** Distinct source CompositeRenderType identity backed by captured legacy state. */
public final class LegacyCompositeRenderType extends LegacyRenderType {
    LegacyCompositeRenderType(String name, LegacyVertexFormat format,
            LegacyVertexFormatMode mode, int bufferSize, boolean sortOnUpload,
            LegacyCompositeState state) {
        super(name, format, mode, bufferSize, state);
    }
}
