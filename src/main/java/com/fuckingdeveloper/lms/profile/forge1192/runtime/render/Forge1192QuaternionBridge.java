package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import java.util.Objects;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

/** Semantic bridge from Minecraft 1.19.2's mutable Quaternion to JOML. */
public final class Forge1192QuaternionBridge {
    private static final float DEGREES_TO_RADIANS = (float) Math.PI / 180.0F;

    private Forge1192QuaternionBridge() {}

    /** Source {@code Quaternion(float, float, float, boolean)}. */
    public static Quaternionf eulerXYZ(float x, float y, float z, boolean degrees) {
        if (degrees) {
            x *= DEGREES_TO_RADIANS;
            y *= DEGREES_TO_RADIANS;
            z *= DEGREES_TO_RADIANS;
        }
        return new Quaternionf().rotationXYZ(x, y, z);
    }

    /** Source mutating multiply retained with its void contract. */
    public static void multiply(Quaternionf left, Quaternionfc right) {
        left.mul(right);
    }

    /** Source dispatcher camera orientation, now owned by the target Camera. */
    public static Quaternionf cameraOrientation(EntityRenderDispatcher dispatcher) {
        var camera = Objects.requireNonNull(dispatcher.camera,
                "Entity renderer camera is not prepared");
        return camera.rotation();
    }
}
