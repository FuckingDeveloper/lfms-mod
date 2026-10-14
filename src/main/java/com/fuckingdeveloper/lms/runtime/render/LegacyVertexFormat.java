package com.fuckingdeveloper.lms.runtime.render;

import com.google.common.collect.ImmutableList;
import com.mojang.renderpearl.api.vertex.VertexFormat;

/** Source-shaped Forge 1.19.2 vertex format backed by target layout metadata. */
public final class LegacyVertexFormat {
    private final VertexFormat target;
    private final ImmutableList<LegacyVertexFormatElement> elements;

    private LegacyVertexFormat(VertexFormat target) {
        this.target = target;
        this.elements = target.getElements().stream()
                .map(LegacyVertexFormatElement::wrap)
                .collect(ImmutableList.toImmutableList());
    }

    public static LegacyVertexFormat wrap(VertexFormat target) {
        return new LegacyVertexFormat(target);
    }

    public int getVertexSize() { return target.getVertexSize(); }

    public int m_86020_() { return getVertexSize(); }

    public ImmutableList<LegacyVertexFormatElement> getElements() { return elements; }

    public ImmutableList<LegacyVertexFormatElement> m_86023_() { return getElements(); }

    public VertexFormat target() { return target; }
}
