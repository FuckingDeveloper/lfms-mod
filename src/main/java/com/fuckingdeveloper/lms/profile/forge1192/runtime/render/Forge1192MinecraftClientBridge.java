package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Client state views whose ownership moved after Forge 1.19.2. */
public final class Forge1192MinecraftClientBridge {
    private static final boolean TRACE = Boolean.getBoolean("lms.traceScreenLayers");
    private static void trace(String operation, net.minecraft.client.gui.Gui gui) {
        if (!TRACE) return;
        var active=gui.screen();
        var layers=((com.fuckingdeveloper.lms.mixin.Target263GuiLayerAccess)gui).lms$screenLayers();
        var caller=StackWalker.getInstance().walk(frames->frames
                .filter(frame->!frame.getClassName().equals(Forge1192MinecraftClientBridge.class.getName()))
                .limit(4).map(Object::toString).toList());
        org.slf4j.LoggerFactory.getLogger(Forge1192MinecraftClientBridge.class).info(
                "LMS screen-layer trace operation={} active={} depth={} caller={}",operation,
                active==null?"null":active.getClass().getName(),layers.size(),caller);
    }
    private Forge1192MinecraftClientBridge() {}

    public static Screen screen(Minecraft minecraft) {
        return minecraft.gui == null ? null : minecraft.gui.screen();
    }

    /** Screen transitions moved to Gui; retain its native lifecycle and events. */
    public static void setScreen(Minecraft minecraft, Screen screen) {
        java.util.Objects.requireNonNull(minecraft, "minecraft");
        if (minecraft.gui == null) throw new IllegalStateException("Client GUI is not initialized for a legacy screen transition");
        trace("before-set",minecraft.gui);
        minecraft.gui.setScreen(screen);
        trace("after-set",minecraft.gui);
    }
    /** Source Forge layered screens retain the native stack, initialization and return lifecycle. */
    public static void pushGuiLayer(Minecraft minecraft, Screen screen) {
        var gui=requireGui(minecraft);trace("before-push",gui);
        gui.pushScreenLayer(screen);trace("after-push",gui);
    }
    public static void popGuiLayer(Minecraft minecraft) {
        var gui=requireGui(minecraft);trace("before-pop",gui);
        gui.popScreenLayer();trace("after-pop",gui);
    }
    public static void clearGuiLayers(Minecraft minecraft) {
        var gui=requireGui(minecraft);trace("before-clear",gui);
        gui.clearScreenLayers();trace("after-clear",gui);
    }
    private static net.minecraft.client.gui.Gui requireGui(Minecraft minecraft) {
        java.util.Objects.requireNonNull(minecraft, "minecraft");
        if (minecraft.gui == null) throw new IllegalStateException("Client GUI is not initialized for a legacy layered screen transition");
        return minecraft.gui;
    }
}
