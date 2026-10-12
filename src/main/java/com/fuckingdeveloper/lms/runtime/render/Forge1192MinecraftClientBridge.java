package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Client state views whose ownership moved after Forge 1.19.2. */
public final class Forge1192MinecraftClientBridge {
    private Forge1192MinecraftClientBridge() {}

    public static Screen screen(Minecraft minecraft) {
        return minecraft.gui == null ? null : minecraft.gui.screen();
    }
}
