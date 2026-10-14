package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;

/** Source GUI item-render operations; never represents the removed world renderer. */
public final class LegacyTooltipItemRenderer {
    LegacyTooltipItemRenderer() {}

    public void m_174253_(ItemStack stack, int x, int y, int seed) {
        Forge1192TooltipGuiBridge.graphics(this).item(stack, x, y, seed);
    }

    public void m_115169_(Font font, ItemStack stack, int x, int y) {
        Forge1192TooltipGuiBridge.graphics(this).itemDecorations(font, stack, x, y);
    }
}
