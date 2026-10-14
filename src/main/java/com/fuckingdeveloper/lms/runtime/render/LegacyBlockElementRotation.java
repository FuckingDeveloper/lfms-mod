package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.client.resources.model.cuboid.CuboidRotation;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

/** Source-shaped Minecraft 1.19.2 cuboid-element rotation. */
public final class LegacyBlockElementRotation {
    public final Vector3f f_111378_;
    public final Direction.Axis f_111379_;
    public final float f_111380_;
    public final boolean f_111381_;

    public LegacyBlockElementRotation(
            Vector3f origin, Direction.Axis axis, float angle, boolean rescale) {
        this.f_111378_ = new Vector3f(origin);
        this.f_111379_ = axis;
        this.f_111380_ = angle;
        this.f_111381_ = rescale;
    }

    public CuboidRotation target() {
        return new CuboidRotation(
                f_111378_, new CuboidRotation.SingleAxisRotation(f_111379_, f_111380_),
                f_111381_);
    }
}
