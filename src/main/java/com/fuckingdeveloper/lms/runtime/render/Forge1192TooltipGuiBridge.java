package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
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
        Identifier texture;
        net.minecraft.client.renderer.texture.AbstractTexture textureObject;
        int color = -1;
        boolean blend = true;
        boolean depthTest = true;
        final Forge1192GuiPlaneBridge.Order planes=new Forge1192GuiPlaneBridge.Order();
        boolean flatItemLighting;
        int colorWriteMask = com.mojang.renderpearl.api.pipeline.ColorTargetState.WRITE_ALL;
        boolean textureEnabled = true;
        Phase phase=Phase.TOOLTIP;
        LegacyGuiShader shader;
        LegacyGuiBuffer buffer;
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
        try {callback.run();} finally {if(previous==null)ACTIVE.remove();else ACTIVE.set(previous);}
    }
    static GuiGraphicsExtractor graphics(LegacyTooltipItemRenderer renderer) {
        Scope scope = scope();
        if (renderer != scope.renderer) throw new IllegalStateException("Foreign legacy tooltip item renderer");
        if(scope.colorWriteMask!=com.mojang.renderpearl.api.pipeline.ColorTargetState.WRITE_ALL)
            throw new UnsupportedOperationException("Masked source GUI item/decorations are not adapted");
        return scope.graphics;
    }
    public static void shaderColor(float red, float green, float blue, float alpha) {
        scope().color = ARGB.colorFromFloat(alpha, red, green, blue);
    }
    public static void shaderTexture(int unit, Identifier texture) {
        if (unit != 0) throw new IllegalStateException("Unsupported legacy tooltip texture unit " + unit);
        scope().texture = java.util.Objects.requireNonNull(texture, "tooltip texture");
        scope().textureObject = null;
    }
    public static void blit(PoseStack pose, int x, int y, int z, float u, float v,
                            int width, int height, int textureWidth, int textureHeight) {
        Scope scope = scope();
        if(scope.colorWriteMask!=com.mojang.renderpearl.api.pipeline.ColorTargetState.WRITE_ALL)
            throw new UnsupportedOperationException("Masked source direct GUI blit is not adapted");
        var transform=Forge1192GuiPlaneBridge.planar(pose.last().pose());
        if (scope.texture == null) throw new IllegalStateException("Legacy tooltip blit has no bound texture");
        Forge1192GuiPlaneBridge.submit(Forge1192GuiPlaneBridge.depth(pose.last().pose(),z));
        scope.graphics.pose().pushMatrix();
        try {
            scope.graphics.pose().mul(transform);
            scope.graphics.blit(RenderPipelines.GUI_TEXTURED, scope.texture, x, y, u, v,
                    width, height, textureWidth, textureHeight, scope.color);
        }finally{scope.graphics.pose().popMatrix();}
    }
}
