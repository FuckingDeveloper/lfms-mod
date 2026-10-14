package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2f;
import java.lang.invoke.*;
import java.util.function.Supplier;

/** Bounded source GUI phases and primitive state; no direct GPU/global state mutation. */
public final class Forge1192ContainerGraphicsBridge {
    private Forge1192ContainerGraphicsBridge() {}
    public static void background(LegacyContainerGraphics screen,GuiGraphicsExtractor graphics,int x,int y,float partial) {
        Forge1192TooltipGuiBridge.extractGui(graphics,Forge1192TooltipGuiBridge.Phase.BACKGROUND,()->screen.lms$background(new PoseStack(),partial,x,y));
    }
    public static void labels(LegacyContainerGraphics screen,GuiGraphicsExtractor graphics,int x,int y) {
        Forge1192TooltipGuiBridge.extractGui(graphics,Forge1192TooltipGuiBridge.Phase.LABELS,()->screen.lms$labels(new PoseStack(),x,y));
    }
    private static final MethodHandle BASE_LABELS=baseLabelsHandle();
    private static MethodHandle baseLabelsHandle() {
        try {
            var lookup=MethodHandles.privateLookupIn(AbstractContainerScreen.class,MethodHandles.lookup());
            return lookup.findSpecial(AbstractContainerScreen.class,"extractLabels",MethodType.methodType(void.class,GuiGraphicsExtractor.class,int.class,int.class),AbstractContainerScreen.class);
        }catch(ReflectiveOperationException error){throw new ExceptionInInitializerError(error);}
    }
    public static void defaultLabels(AbstractContainerScreen<?> screen,PoseStack pose,int x,int y) {
        try {BASE_LABELS.invoke(screen,Forge1192TooltipGuiBridge.scope().graphics,x,y);}
        catch(RuntimeException|Error failure){throw failure;}catch(Throwable failure){throw new IllegalStateException("Native default labels failed",failure);}
    }
    /** Native default background already ran before the source background callback. */
    public static void defaultBackground(PoseStack pose) {
        var scope=Forge1192TooltipGuiBridge.scope();
        if(scope.phase!=Forge1192TooltipGuiBridge.Phase.BACKGROUND)throw new IllegalStateException("Default source background outside native background extraction");
        // Source Screen background/GuiComponent gradient ends with blend disabled and texture enabled.
        scope.blend=false;scope.textureEnabled=true;
    }
    public static LegacyGuiShader positionColorShader(){return LegacyGuiShader.POSITION_COLOR;}
    public static LegacyGuiShader positionTexShader(){return LegacyGuiShader.POSITION_TEX;}
    public static LegacyGuiShader positionTexColorShader(){return LegacyGuiShader.POSITION_TEX_COLOR;}
    public static void setShader(Supplier<LegacyGuiShader> supplier) {Forge1192TooltipGuiBridge.scope().shader=java.util.Objects.requireNonNull(supplier.get());}
    public static void enableBlend(){Forge1192TooltipGuiBridge.scope().blend=true;}
    public static void disableBlend(){Forge1192TooltipGuiBridge.scope().blend=false;}
    public static void enableTexture(){Forge1192TooltipGuiBridge.scope().textureEnabled=true;}
    public static void disableTexture(){Forge1192TooltipGuiBridge.scope().textureEnabled=false;}
    public static void defaultBlendFunc(){Forge1192TooltipGuiBridge.scope();} // Exact source factors are the target TRANSLUCENT pipeline.

    static Matrix3x2f planar(PoseStack poses) {
        var m=poses.last().pose();
        if(m.m02()!=0 || m.m12()!=0 || m.m03()!=0 || m.m13()!=0 || m.m23()!=0 || m.m32()!=0 || m.m33()!=1)
            throw new IllegalStateException("Non-planar source GUI pose is not adapted");
        return new Matrix3x2f(m.m00(),m.m01(),m.m10(),m.m11(),m.m30(),m.m31());
    }
    private static int text(Font font,PoseStack pose,Object text,float x,float y,int color,boolean shadow) {
        if(font.getClass().getClassLoader() instanceof com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader)
            throw new UnsupportedOperationException("Source custom font draw callbacks are not adapted");
        color=sourceTextColor(color);
        var graphics=Forge1192TooltipGuiBridge.scope().graphics;
        graphics.pose().pushMatrix();
        try {
            graphics.pose().mul(planar(pose));graphics.pose().translate(x,y);
            int width;
            if(text instanceof Component component){graphics.text(font,component,0,0,color,shadow);width=font.width(component);}
            else if(text instanceof FormattedCharSequence sequence){graphics.text(font,sequence,0,0,color,shadow);width=font.width(sequence);}
            else {var string=(String)text;graphics.text(font,string,0,0,color,shadow);width=font.width(string);}
            return (int)(x+width)+(shadow?1:0);
        }finally{graphics.pose().popMatrix();}
    }
    public static int sourceTextColor(int color){return (color & 0xFC000000)==0?color|0xFF000000:color;}
    public static int draw(Font font,PoseStack pose,Component text,float x,float y,int color){return text(font,pose,text,x,y,color,false);}
    public static int draw(Font font,PoseStack pose,String text,float x,float y,int color){return text==null?0:text(font,pose,text,x,y,color,false);}
    public static int draw(Font font,PoseStack pose,FormattedCharSequence text,float x,float y,int color){return text(font,pose,text,x,y,color,false);}
    public static int drawShadow(Font font,PoseStack pose,Component text,float x,float y,int color){return text(font,pose,text,x,y,color,true);}
    public static int drawShadow(Font font,PoseStack pose,String text,float x,float y,int color){return text==null?0:text(font,pose,text,x,y,color,true);}
    public static int drawShadow(Font font,PoseStack pose,FormattedCharSequence text,float x,float y,int color){return text(font,pose,text,x,y,color,true);}
}
