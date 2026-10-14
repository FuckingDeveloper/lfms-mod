package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;

/** Source GUI item-render operations; never represents the removed world renderer. */
public final class LegacyTooltipItemRenderer {
    LegacyTooltipItemRenderer() {}

    public void m_174253_(ItemStack stack, int x, int y, int seed) {
        Forge1192GuiPlaneBridge.submit(0);
        Forge1192TooltipGuiBridge.graphics(this).item(stack, x, y, seed);
    }

    public void m_115169_(Font font, ItemStack stack, int x, int y) {
        Forge1192GuiPlaneBridge.submit(0);
        Forge1192TooltipGuiBridge.graphics(this).itemDecorations(font, stack, x, y);
    }
    public void renderGuiItem(ItemStack stack,int x,int y){Forge1192GuiItemExtractionBridge.itemWithContext(this,null,stack,x,y,0);}
    public void renderAndDecorateItem(ItemStack stack,int x,int y){renderAndDecorateItem(stack,x,y,0);}
    public void renderAndDecorateItem(ItemStack stack,int x,int y,int seed){if(!stack.isEmpty())Forge1192GuiItemExtractionBridge.itemWithContext(this,net.minecraft.client.Minecraft.getInstance().player,stack,x,y,seed);}
    public void renderAndDecorateItem(net.minecraft.world.entity.LivingEntity owner,ItemStack stack,int x,int y,int seed){if(!stack.isEmpty())Forge1192GuiItemExtractionBridge.itemWithContext(this,owner,stack,x,y,seed);}
    public void renderAndDecorateFakeItem(ItemStack stack,int x,int y){renderAndDecorateItem((net.minecraft.world.entity.LivingEntity)null,stack,x,y,0);}
    public void renderGuiItemDecorations(Font font,ItemStack stack,int x,int y){renderGuiItemDecorations(font,stack,x,y,null);}
    public void renderGuiItemDecorations(Font font,ItemStack stack,int x,int y,String count){Forge1192GuiPlaneBridge.submit(0);Forge1192TooltipGuiBridge.graphics(this).itemDecorations(font,stack,x,y,count);}
}
