package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Client state views whose ownership moved after Forge 1.19.2. */
public final class Forge1192MinecraftClientBridge {
    private Forge1192MinecraftClientBridge() {}

    public static Screen screen(Minecraft minecraft) {
        return minecraft.gui == null ? null : minecraft.gui.screen();
    }

    /** Screen transitions moved to Gui; retain its native lifecycle and events. */
    public static void setScreen(Minecraft minecraft, Screen screen) {
        java.util.Objects.requireNonNull(minecraft, "minecraft");
        if (minecraft.gui == null) throw new IllegalStateException("Client GUI is not initialized for a legacy screen transition");
        minecraft.gui.setScreen(screen);
    }
}
