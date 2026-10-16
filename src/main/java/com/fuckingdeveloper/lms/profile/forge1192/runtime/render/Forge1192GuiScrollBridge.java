package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import net.minecraft.client.gui.components.events.GuiEventListener;

/** Vertical source intent dispatched through the authoritative native listener. */
public final class Forge1192GuiScrollBridge {
    private Forge1192GuiScrollBridge() {}
    public static boolean mouseScrolled(GuiEventListener listener,double x,double y,double amount) {
        return listener.mouseScrolled(x,y,0,amount);
    }
    public static double verticalAmount(double horizontal,double vertical) {
        double amount=sourceAmount(net.minecraft.util.Util.getPlatform()==net.minecraft.util.Util.OS.OSX,horizontal,vertical);
        if(Boolean.getBoolean("lms.traceGuiInput"))org.slf4j.LoggerFactory.getLogger(Forge1192GuiScrollBridge.class).info(
                "LMS GUI wheel horizontal={} vertical={} source={}",horizontal,vertical,amount);
        return amount;
    }
    /** Pinned source MouseHandler chooses the axis before sensitivity; native deltas are already scaled. */
    public static double sourceAmount(boolean macOS,double horizontal,double vertical) {
        return macOS&&vertical==0?horizontal:vertical;
    }
}
