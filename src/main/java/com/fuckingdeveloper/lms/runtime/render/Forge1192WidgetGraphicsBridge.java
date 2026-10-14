package com.fuckingdeveloper.lms.runtime.render;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
public final class Forge1192WidgetGraphicsBridge {
    private Forge1192WidgetGraphicsBridge() {}
    public static void extract(LegacyWidgetGraphics widget,GuiGraphicsExtractor graphics,int x,int y,float partial) {
        Forge1192TooltipGuiBridge.extractGui(graphics,Forge1192TooltipGuiBridge.Phase.WIDGET,()->widget.lms$widget(new PoseStack(),x,y,partial));
    }
    public static int yImage(AbstractWidget widget,boolean hovered){return !widget.active?0:hovered?2:1;}
    /** Pinned source AbstractWidget.renderBg is an empty extension callback. */
    public static void defaultBackground(PoseStack pose,net.minecraft.client.Minecraft client,int x,int y) {}
    public static void drawCenteredString(PoseStack pose, net.minecraft.client.gui.Font font, net.minecraft.network.chat.Component text, int x, int y, int color) {
        Forge1192ContainerGraphicsBridge.drawShadow(font, pose, text, x - font.width(text)/2, y, color);
    }
    public static void drawCenteredString(PoseStack pose, net.minecraft.client.gui.Font font, String text, int x, int y, int color) {
        Forge1192ContainerGraphicsBridge.drawShadow(font, pose, text, x - font.width(text)/2, y, color);
    }
    public static void drawCenteredString(PoseStack pose, net.minecraft.client.gui.Font font, net.minecraft.util.FormattedCharSequence text, int x, int y, int color) {
        Forge1192ContainerGraphicsBridge.drawShadow(font, pose, text, x - font.width(text)/2, y, color);
    }
}
