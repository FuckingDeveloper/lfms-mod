package com.fuckingdeveloper.lms.runtime.render;

import java.util.Objects;
import net.minecraft.client.gui.screens.Screen;

/** Native keyboard/game-tick routing blocks gameplay input whenever a screen is present. */
public final class Forge1192ScreenInputBridge {
    private Forge1192ScreenInputBridge() {}
    public static boolean getPassEvents(Screen screen) { Objects.requireNonNull(screen); return false; }
    public static void setPassEvents(Screen screen,boolean pass) {
        Objects.requireNonNull(screen);
        if(pass)throw new UnsupportedOperationException("Source Screen.passEvents=true requires gameplay-input routing adaptation");
        // false is already enforced by target KeyboardHandler and Minecraft.tick.
    }
}
