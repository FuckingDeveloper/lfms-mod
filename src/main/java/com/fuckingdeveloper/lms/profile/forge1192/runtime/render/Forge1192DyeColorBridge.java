package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeColor;

/** Exact value-shape adapter for the Forge 1.19.2 dye diffuse color API. */
public final class Forge1192DyeColorBridge {
    private Forge1192DyeColorBridge() {}

    public static float[] textureDiffuseColors(DyeColor color) {
        int packed = color.getTextureDiffuseColor();
        return new float[] {
                ARGB.redFloat(packed),
                ARGB.greenFloat(packed),
                ARGB.blueFloat(packed)
        };
    }
}
