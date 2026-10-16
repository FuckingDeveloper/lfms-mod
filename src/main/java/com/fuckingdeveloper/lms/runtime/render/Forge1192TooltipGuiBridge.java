package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import java.util.function.Consumer;

/** Scoped legacy GUI primitives recorded through the authoritative target extractor. */
public final class Forge1192TooltipGuiBridge {
    private static final ThreadLocal<Scope> ACTIVE = new ThreadLocal<>();
    enum Phase { TOOLTIP, BACKGROUND, LABELS, WIDGET }
    static final class Scope {
        final GuiGraphicsExtractor graphics;
        final LegacyTooltipItemRenderer renderer = new LegacyTooltipItemRenderer();
        String diagnosticWidget;
        Identifier texture;
        net.minecraft.client.renderer.texture.AbstractTexture textureObject;
        int color = -1;
        boolean blend = true;
        com.mojang.renderpearl.api.pipeline.BlendFunction blendFunction=LegacyGuiBlend.DEFAULT;
        boolean depthTest = true;
        final Forge1192GuiPlaneBridge.Order planes=new Forge1192GuiPlaneBridge.Order();
        boolean flatItemLighting;
        int colorWriteMask = com.mojang.renderpearl.api.pipeline.ColorTargetState.WRITE_ALL;
        boolean textureEnabled = true;
        Phase phase=Phase.TOOLTIP;
        LegacyGuiShader shader;
        LegacyGuiBuffer buffer;
        boolean sourceScissor;
        Scope(GuiGraphicsExtractor graphics) { this.graphics = graphics; }
    }
    private Forge1192TooltipGuiBridge() {}

    static void extract(GuiGraphicsExtractor graphics, Consumer<LegacyTooltipItemRenderer> callback) {
        if (ACTIVE.get() != null) throw new IllegalStateException("Nested legacy tooltip GUI extraction");
        var scope = new Scope(graphics);
        ACTIVE.set(scope);
        try { callback.accept(scope.renderer); } finally { ACTIVE.remove(); }
    }

    static Scope scope() {
        Scope scope = ACTIVE.get();
        if (scope == null) throw new IllegalStateException("Legacy tooltip GUI primitive outside extraction");
        return scope;
    }
    static void extractGui(GuiGraphicsExtractor graphics, Phase phase, Runnable callback) {
        var previous=ACTIVE.get();var scope=new Scope(java.util.Objects.requireNonNull(graphics));scope.phase=phase; ACTIVE.set(scope);
        try {callback.run();} finally {if(scope.sourceScissor)graphics.disableScissor();if(previous==null)ACTIVE.remove();else ACTIVE.set(previous);}
    }
    static GuiGraphicsExtractor graphics(LegacyTooltipItemRenderer renderer) {
        Scope scope = scope();
        if (renderer != scope.renderer) throw new IllegalStateException("Foreign legacy tooltip item renderer");
        if(scope.colorWriteMask!=com.mojang.renderpearl.api.pipeline.ColorTargetState.WRITE_ALL)
            throw new UnsupportedOperationException("Masked source GUI item/decorations are not adapted");
        return scope.graphics;
    }
    /** Source OpenGL scissors use framebuffer pixels and a bottom-left origin.
     * Recover the GUI rectangle on the target integer GUI grid; replace only
     * this scope's source clip, retaining the enclosing native viewport. */
    public static void enableScissor(int x,int y,int width,int height) {
        var scope=scope();var window=net.minecraft.client.Minecraft.getInstance().getWindow();
        int[] rect=guiScissor(x,y,width,height,window.getWidth(),window.getHeight(),scope.graphics.guiWidth(),scope.graphics.guiHeight());
        if(scope.sourceScissor)scope.graphics.disableScissor();
        scope.graphics.enableScissor(rect[0],rect[1],rect[2],rect[3]);scope.sourceScissor=true;
    }
    public static int[] guiScissor(int x,int y,int width,int height,int pixelsWide,int pixelsHigh,int guiWide,int guiHigh){
        if(width<0||height<0||pixelsWide<=0||pixelsHigh<=0||guiWide<=0||guiHigh<=0)throw new IllegalArgumentException("Invalid source framebuffer scissor geometry");
        return new int[]{(int)Math.round((double)x*guiWide/pixelsWide),(int)Math.round(((double)pixelsHigh-y-height)*guiHigh/pixelsHigh),(int)Math.round(((double)x+width)*guiWide/pixelsWide),(int)Math.round(((double)pixelsHigh-y)*guiHigh/pixelsHigh)};
    }
    public static void disableScissor(){var scope=scope();if(scope.sourceScissor){scope.graphics.disableScissor();scope.sourceScissor=false;}}
    public static void shaderColor(float red, float green, float blue, float alpha) {
        scope().color = ARGB.colorFromFloat(alpha, red, green, blue);
    }
    public static void shaderTexture(int unit, Identifier texture) {
        if (unit != 0) throw new IllegalStateException("Unsupported legacy tooltip texture unit " + unit);
        scope().texture = java.util.Objects.requireNonNull(texture, "tooltip texture");
        scope().textureObject = null;
    }
    public static void blit(PoseStack pose, int x, int y, float u, float v,
                            int width, int height, int textureWidth, int textureHeight) {
        blit(pose,x,y,width,height,u,v,width,height,textureWidth,textureHeight);
    }
    public static void blit(PoseStack pose, int x, int y, int width, int height, float u, float v,
                            int sourceWidth, int sourceHeight, int textureWidth, int textureHeight) {
        drawBlit(pose,x,y,0,u,v,width,height,sourceWidth,sourceHeight,textureWidth,textureHeight);
    }
    public static void blit(PoseStack pose, int x, int y, int z, float u, float v,
                            int width, int height, int textureWidth, int textureHeight) {
        drawBlit(pose,x,y,z,u,v,width,height,width,height,textureWidth,textureHeight);
    }
    private static void drawBlit(PoseStack pose,int x,int y,int z,float u,float v,int width,int height,
                                 int sourceWidth,int sourceHeight,int textureWidth,int textureHeight) {
        drawUv(pose,x,y,z,width,height,u/textureWidth,(u+sourceWidth)/textureWidth,v/textureHeight,(v+sourceHeight)/textureHeight);
    }
    public static void blit(PoseStack pose,int x,int y,int z,int width,int height,net.minecraft.client.renderer.texture.TextureAtlasSprite sprite) {
        drawUv(pose,x,y,z,width,height,sprite.getU0(),sprite.getU1(),sprite.getV0(),sprite.getV1());
    }
    private static void drawUv(PoseStack pose,int x,int y,int z,int width,int height,float u0,float u1,float v0,float v1) {
        Scope scope = scope();
        var buffer=new LegacyGuiBuffer();
        // Source GuiComponent.innerBlit selects POSITION_TEX but preserves blend/color/mask and binding.
        scope.shader=LegacyGuiShader.POSITION_TEX;
        buffer.begin(LegacyVertexFormatMode.QUADS,LegacyDefaultVertexFormat.f_85817_);
        var matrix=pose.last().pose();
        buffer.vertex(matrix,x,y+height,z).setUv(u0,v1);
        buffer.vertex(matrix,x+width,y+height,z).setUv(u1,v1);
        buffer.vertex(matrix,x+width,y,z).setUv(u1,v0);
        buffer.vertex(matrix,x,y,z).setUv(u0,v0);
        buffer.finish();
    }
    public static void fill(PoseStack pose,int x0,int y0,int x1,int y1,int color) {
        if(x0<x1){int swap=x0;x0=x1;x1=swap;}
        if(y0<y1){int swap=y0;y0=y1;y1=swap;}
        var scope=scope();scope.blend=true;scope.blendFunction=LegacyGuiBlend.DEFAULT;scope.textureEnabled=false;scope.shader=LegacyGuiShader.POSITION_COLOR;
        var buffer=new LegacyGuiBuffer();buffer.begin(LegacyVertexFormatMode.QUADS,LegacyDefaultVertexFormat.f_85815_);
        var matrix=pose.last().pose();
        buffer.vertex(matrix,x0,y1,0).setColor(color);buffer.vertex(matrix,x1,y1,0).setColor(color);
        buffer.vertex(matrix,x1,y0,0).setColor(color);buffer.vertex(matrix,x0,y0,0).setColor(color);buffer.finish();
        scope.textureEnabled=true;scope.blend=false;
    }

}
