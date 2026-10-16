package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import com.mojang.math.OctahedralGroup;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Matrix4fc;

/** Source-shaped Minecraft 1.19.2 block-model rotations. */
public enum LegacyBlockModelRotation implements ModelState {
    X0_Y0(0, 0),
    X0_Y90(0, 90),
    X0_Y180(0, 180),
    X0_Y270(0, 270),
    X90_Y0(90, 0),
    X90_Y90(90, 90),
    X90_Y180(90, 180),
    X90_Y270(90, 270),
    X180_Y0(180, 0),
    X180_Y90(180, 90),
    X180_Y180(180, 180),
    X180_Y270(180, 270),
    X270_Y0(270, 0),
    X270_Y90(270, 90),
    X270_Y180(270, 180),
    X270_Y270(270, 270);

    private final int x;
    private final int y;
    private final OctahedralGroup actualRotation;
    private final ModelState delegate;

    LegacyBlockModelRotation(int x, int y) {
        this.x = x;
        this.y = y;
        OctahedralGroup rotation = OctahedralGroup.IDENTITY;
        for (int angle = 0; angle < y; angle += 90)
            rotation = rotation.compose(OctahedralGroup.ROT_90_Y_NEG);
        for (int angle = 0; angle < x; angle += 90)
            rotation = rotation.compose(OctahedralGroup.ROT_90_X_NEG);
        this.actualRotation = rotation;
        this.delegate = BlockModelRotation.get(rotation);
    }

    public Transformation getRotation() {
        return delegate.transformation();
    }

    @Override
    public Transformation transformation() {
        return delegate.transformation();
    }

    @Override
    public Matrix4fc faceTransformation(Direction direction) {
        return delegate.faceTransformation(direction);
    }

    @Override
    public Matrix4fc inverseFaceTransformation(Direction direction) {
        return delegate.inverseFaceTransformation(direction);
    }

    public OctahedralGroup actualRotation() {
        return actualRotation;
    }

    public static LegacyBlockModelRotation by(int x, int y) {
        int normalizedX = Mth.positiveModulo(x, 360);
        int normalizedY = Mth.positiveModulo(y, 360);
        for (LegacyBlockModelRotation rotation : values()) {
            if (rotation.x == normalizedX && rotation.y == normalizedY) return rotation;
        }
        return null;
    }
}
