package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.client.gui.components.events.GuiEventListener;

/** Vertical source intent dispatched through the authoritative native listener. */
public final class Forge1192GuiScrollBridge {
    private Forge1192GuiScrollBridge() {}
    public static boolean mouseScrolled(GuiEventListener listener,double x,double y,double amount) {
        return listener.mouseScrolled(x,y,0,amount);
    }
    public static double verticalAmount(double horizontal,double vertical) {
        return sourceAmount(net.minecraft.util.Util.getPlatform()==net.minecraft.util.Util.OS.OSX,horizontal,vertical);
    }
    /** Pinned source MouseHandler chooses the axis before sensitivity; native deltas are already scaled. */
    public static double sourceAmount(boolean macOS,double horizontal,double vertical) {
        return macOS&&vertical==0?horizontal:vertical;
    }
}
