package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.client.Minecraft;

/** Target input-state accessors replacing removed static Screen helpers. */
public final class Forge1192ClientInputBridge {
    private Forge1192ClientInputBridge() {}

    public static boolean hasShiftDown() {
        return Minecraft.getInstance().hasShiftDown();
    }

    public static boolean hasControlDown() {
        return Minecraft.getInstance().hasControlDown();
    }

    public static boolean hasAltDown() {
        return Minecraft.getInstance().hasAltDown();
    }
}
