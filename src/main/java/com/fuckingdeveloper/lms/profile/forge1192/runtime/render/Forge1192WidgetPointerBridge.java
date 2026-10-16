package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

/** Source coordinate-only widget operations mean a primary-button activation. */
public final class Forge1192WidgetPointerBridge {
    private Forge1192WidgetPointerBridge() {}
    public static MouseButtonEvent left(double x,double y) {
        return new MouseButtonEvent(x,y,new MouseButtonInfo(Forge1192KeyboardBridge.mouseCode(0),0));
    }
}
