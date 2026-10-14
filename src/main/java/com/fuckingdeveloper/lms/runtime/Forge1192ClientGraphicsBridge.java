package com.fuckingdeveloper.lms.runtime;

import net.minecraft.client.Minecraft;

/** Target 26.3 interpretation of removed source-generation graphics predicates. */
public final class Forge1192ClientGraphicsBridge {
    private Forge1192ClientGraphicsBridge() {}

    /**
     * Forge 1.19.2 used the global fancy-graphics threshold for foliage render choices.
     * Target 26.3 presets encode that choice in the independently customizable
     * cutout-leaves option: FAST sets false and FANCY/FABULOUS set true.
     */
    public static boolean useFancyGraphics() {
        return Boolean.TRUE.equals(Minecraft.getInstance().options.cutoutLeaves().get());
    }
}
