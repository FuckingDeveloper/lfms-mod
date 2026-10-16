package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import net.minecraft.core.Direction;
import org.joml.Vector3f;

import java.util.Map;

/** Source-shaped Minecraft 1.19.2 block-model element. */
public final class LegacyBlockElement {
    public final Vector3f f_111308_;
    public final Vector3f f_111309_;
    public final Map<Direction, LegacyBlockElementFace> f_111310_;
    public final LegacyBlockElementRotation f_111311_;
    public final boolean f_111312_;

    public LegacyBlockElement(
            Vector3f from, Vector3f to,
            Map<Direction, LegacyBlockElementFace> faces,
            LegacyBlockElementRotation rotation, boolean shade) {
        this.f_111308_ = new Vector3f(from);
        this.f_111309_ = new Vector3f(to);
        this.f_111310_ = Map.copyOf(faces);
        this.f_111311_ = rotation;
        this.f_111312_ = shade;
    }
}
