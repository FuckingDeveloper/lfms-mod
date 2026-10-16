package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import net.minecraft.core.Direction;

/** Source-shaped Minecraft 1.19.2 cuboid face. */
public final class LegacyBlockElementFace {
    public final Direction f_111354_;
    public final int f_111355_;
    public final String f_111356_;
    public final LegacyBlockFaceUV f_111357_;

    public LegacyBlockElementFace(
            Direction cullForDirection, int tintIndex, String texture,
            LegacyBlockFaceUV uvs) {
        this.f_111354_ = cullForDirection;
        this.f_111355_ = tintIndex;
        this.f_111356_ = texture;
        this.f_111357_ = uvs;
    }
}
