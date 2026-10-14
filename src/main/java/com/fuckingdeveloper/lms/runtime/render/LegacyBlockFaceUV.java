package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.math.Quadrant;
import net.minecraft.client.resources.model.cuboid.CuboidFace;

import java.util.Arrays;

/** Source-shaped Minecraft 1.19.2 block-face UV data. */
public final class LegacyBlockFaceUV {
    public float[] f_111387_;
    public final int f_111388_;

    public LegacyBlockFaceUV(float[] uvs, int rotation) {
        if (uvs != null && uvs.length != 4) {
            throw new IllegalArgumentException("BlockFaceUV requires four coordinates");
        }
        this.f_111387_ = uvs == null ? null : uvs.clone();
        this.f_111388_ = rotation;
    }

    public float m_111392_(int vertex) { return target().getVertexU(vertex); }
    public float m_111396_(int vertex) { return target().getVertexV(vertex); }
    public int m_111398_(int vertex) { return Quadrant.parseJson(f_111388_).rotateVertexIndex(vertex); }
    public void m_111394_(float[] uvs) { this.f_111387_ = uvs.clone(); }

    public CuboidFace.UVs target() {
        if (f_111387_ == null) {
            throw new IllegalStateException("Unresolved implicit Forge 1.19.2 block-face UVs");
        }
        return new CuboidFace.UVs(f_111387_[0], f_111387_[1], f_111387_[2], f_111387_[3]);
    }

    public Quadrant rotation() { return Quadrant.parseJson(f_111388_); }

    @Override public String toString() {
        return "LegacyBlockFaceUV" + Arrays.toString(f_111387_) + "@" + f_111388_;
    }
}
