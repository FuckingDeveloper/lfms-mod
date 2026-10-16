package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;

/** Exact 1.19.2 SRG default-format fields projected onto target layouts. */
public final class LegacyDefaultVertexFormat {
    public static final LegacyVertexFormat f_166850_ = wrap(DefaultVertexFormat.POSITION_TEX);
    public static final LegacyVertexFormat f_85811_ = wrap(DefaultVertexFormat.BLOCK);
    public static final LegacyVertexFormat f_85812_ = wrap(DefaultVertexFormat.ENTITY);
    public static final LegacyVertexFormat f_85813_ = wrap(DefaultVertexFormat.PARTICLE);
    public static final LegacyVertexFormat f_85814_ = wrap(DefaultVertexFormat.POSITION);
    public static final LegacyVertexFormat f_85815_ = wrap(DefaultVertexFormat.POSITION_COLOR);
    public static final LegacyVertexFormat f_166851_ = wrap(DefaultVertexFormat.POSITION_COLOR_NORMAL);
    public static final LegacyVertexFormat f_85816_ = wrap(DefaultVertexFormat.POSITION_COLOR_LIGHTMAP);
    public static final LegacyVertexFormat f_85817_ = wrap(DefaultVertexFormat.POSITION_TEX);
    public static final LegacyVertexFormat f_85818_ = wrap(DefaultVertexFormat.POSITION_TEX_COLOR);
    public static final LegacyVertexFormat f_85819_ = wrap(DefaultVertexFormat.POSITION_TEX_COLOR);
    public static final LegacyVertexFormat f_85820_ = wrap(DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
    public static final LegacyVertexFormat f_85821_ = wrap(DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR);
    public static final LegacyVertexFormat f_85822_ = wrap(DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

    private LegacyDefaultVertexFormat() {}

    private static LegacyVertexFormat wrap(com.mojang.renderpearl.api.vertex.VertexFormat target) {
        return LegacyVertexFormat.wrap(target);
    }
}
