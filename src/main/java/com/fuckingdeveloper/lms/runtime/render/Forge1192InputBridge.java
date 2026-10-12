package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import org.lwjgl.sdl.SDLMouse;

/** Adapts the GLFW-window-scoped 1.19.2 keyboard query to target SDL input. */
public final class Forge1192InputBridge {
    private Forge1192InputBridge() {}

    public static boolean isKeyDown(Window ignoredWindow, int key) {
        return InputConstants.isKeyDown(key);
    }

    public static int mouseButton(Window ignoredWindow, int button) {
        int mask = button > 0 && button <= Integer.SIZE ? 1 << button - 1 : 0;
        return (SDLMouse.SDL_GetMouseState(null, null) & mask) != 0
                ? InputConstants.PRESS : InputConstants.RELEASE;
    }
}
