package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

/** Source tooltip size/image ABI executed during target GUI extraction. */
public interface Forge1192TooltipComponentBridge extends ClientTooltipComponent {
    int legacyHeight();
    int legacyWidth(Font font);

    default void legacyImage(Font font, int x, int y, PoseStack pose,
                             LegacyTooltipItemRenderer renderer, int z) {}

    @Override default int getHeight(Font font) { return legacyHeight(); }
    @Override default int getWidth(Font font) { return legacyWidth(font); }
    @Override default void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        Forge1192TooltipGuiBridge.extract(graphics,
                renderer -> legacyImage(font, x, y, new PoseStack(), renderer, 0));
    }
}
