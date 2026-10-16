package com.fuckingdeveloper.lms.target.neoforge263.render;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.mixin.Target263GuiLayerAccess;
import java.util.List;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/** Source overlays do not implicitly draw the target's full-screen default background. */
public final class Target263SourceScreenBackground {
    private Target263SourceScreenBackground() {}
    public static boolean sourceOverlay(Screen screen, Gui gui) {
        if (!(screen.getClass().getClassLoader() instanceof ManagedLegacyClassLoader)
                || screen instanceof AbstractContainerScreen<?>) return false;
        return aboveBottom(screen, gui.screen(), ((Target263GuiLayerAccess) gui).lms$screenLayers());
    }
    public static boolean aboveBottom(Object screen, Object active, List<?> layers) {
        if (layers.isEmpty() || screen == layers.getFirst()) return false;
        if (screen == active) return true;
        for (Object layer : layers) if (layer == screen) return true;
        return false;
    }
}
