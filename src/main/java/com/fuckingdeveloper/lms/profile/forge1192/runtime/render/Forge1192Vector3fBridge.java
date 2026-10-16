package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Semantic bridge from Minecraft 1.19.2's mutable Vector3f to JOML. */
public final class Forge1192Vector3fBridge {
    public static final Vector3f XP = new Vector3f(1.0F, 0.0F, 0.0F);
    public static final Vector3f YP = new Vector3f(0.0F, 1.0F, 0.0F);
    public static final Vector3f ZP = new Vector3f(0.0F, 0.0F, 1.0F);

    private Forge1192Vector3fBridge() {}

    public static void mul(Vector3f vector, float scalar) {
        vector.mul(scalar);
    }

    public static void set(Vector3f vector, float x, float y, float z) {
        vector.set(x, y, z);
    }

    public static boolean normalize(Vector3f vector) {
        float lengthSquared = vector.lengthSquared();
        if (lengthSquared < Float.MIN_NORMAL) return false;
        vector.mul(Mth.invSqrt(lengthSquared));
        return true;
    }

    public static Quaternionf rotation(Vector3f axis, float radians) {
        return axisAngle(axis, radians);
    }

    public static Quaternionf rotationDegrees(Vector3f axis, float degrees) {
        return axisAngle(axis, degrees * ((float) Math.PI / 180.0F));
    }

    private static Quaternionf axisAngle(Vector3f axis, float angle) {
        float halfAngle = angle / 2.0F;
        float sin = Mth.sin(halfAngle);
        return new Quaternionf(
                axis.x() * sin,
                axis.y() * sin,
                axis.z() * sin,
                Mth.cos(halfAngle));
    }
}
