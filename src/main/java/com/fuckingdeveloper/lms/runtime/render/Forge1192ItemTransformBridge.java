package com.fuckingdeveloper.lms.runtime.render;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;
import org.joml.Quaternionf;

/** Target adapter for Forge 1.19.2 item-display transform values. */
public final class Forge1192ItemTransformBridge {
    private Forge1192ItemTransformBridge() {}

    public static boolean hasTransform(ItemTransforms transforms, ItemDisplayContext context) {
        return transforms.getTransform(context) != ItemTransform.NO_TRANSFORM;
    }

    public static void apply(ItemTransform transform, boolean leftHand, PoseStack poses) {
        transform.apply(leftHand, poses.last());
    }

    public static ItemTransforms copy(ItemTransforms transforms) {
        return transforms;
    }

    public static ItemTransforms create(
            ItemTransform thirdPersonLeftHand, ItemTransform thirdPersonRightHand,
            ItemTransform firstPersonLeftHand, ItemTransform firstPersonRightHand,
            ItemTransform head, ItemTransform gui, ItemTransform ground, ItemTransform fixed) {
        return new ItemTransforms(thirdPersonLeftHand, thirdPersonRightHand,
                firstPersonLeftHand, firstPersonRightHand, head, gui, ground, fixed,
                ItemTransform.NO_TRANSFORM);
    }

    public static ItemTransforms create(
            ItemTransform thirdPersonLeftHand, ItemTransform thirdPersonRightHand,
            ItemTransform firstPersonLeftHand, ItemTransform firstPersonRightHand,
            ItemTransform head, ItemTransform gui, ItemTransform ground, ItemTransform fixed,
            ImmutableMap<ItemDisplayContext, ItemTransform> moddedTransforms) {
        return new ItemTransforms(thirdPersonLeftHand, thirdPersonRightHand,
                firstPersonLeftHand, firstPersonRightHand, head, gui, ground, fixed,
                ItemTransform.NO_TRANSFORM, moddedTransforms);
    }

    public static Vector3f rotation(ItemTransform transform) {
        return new Vector3f(transform.rotation());
    }

    public static Vector3f translation(ItemTransform transform) {
        return new Vector3f(transform.translation());
    }

    public static Vector3f scale(ItemTransform transform) {
        return new Vector3f(transform.scale());
    }

    public static Vector3f rightRotation(ItemTransform transform) {
        return new Vector3f(transform.rightRotation());
    }

    /** Source Quaternion#fromXYZDegrees, preserving X then Y then Z order. */
    public static Quaternionf quaternionFromXYZDegrees(Vector3f degrees) {
        return new Quaternionf().rotationXYZ(
                (float) Math.toRadians(degrees.x),
                (float) Math.toRadians(degrees.y),
                (float) Math.toRadians(degrees.z));
    }
}
