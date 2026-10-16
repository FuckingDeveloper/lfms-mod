package com.fuckingdeveloper.lms.mixin;

import java.util.Stack;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
public interface Target263GuiLayerAccess {
    @Accessor("screenLayers") Stack<Screen> lms$screenLayers();
}
