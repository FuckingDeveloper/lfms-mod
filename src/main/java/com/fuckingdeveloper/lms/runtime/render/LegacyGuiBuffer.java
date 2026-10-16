package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import java.util.*;

/** Immutable extracted GUI quads with source UV/color/planar pose and batch lifetime. */
public final class LegacyGuiBuffer implements VertexConsumer {
    private static final RenderPipeline COLOR_OPAQUE=RenderPipeline.builder(RenderPipelines.GUI_SNIPPET).withLocation(net.minecraft.resources.Identifier.fromNamespaceAndPath("lms","legacy_gui_color_opaque")).withColorTargetState(ColorTargetState.DEFAULT).build();
    private static final RenderPipeline TEXTURE_OPAQUE=RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET).withLocation(net.minecraft.resources.Identifier.fromNamespaceAndPath("lms","legacy_gui_texture_opaque")).withColorTargetState(ColorTargetState.DEFAULT).build();
    private record PipelineKey(boolean texture,Optional<BlendFunction> blend,int writeMask) {}
    private static final java.util.concurrent.ConcurrentMap<PipelineKey,RenderPipeline> STATE_PIPELINES=new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.atomic.AtomicLong PIPELINE_IDS=new java.util.concurrent.atomic.AtomicLong();
    public static RenderPipeline pipeline(boolean texture,boolean blend,int writeMask) {
        return pipeline(texture,blend?Optional.of(LegacyGuiBlend.DEFAULT):Optional.empty(),writeMask);
    }
    public static RenderPipeline pipeline(boolean texture,Optional<BlendFunction> blend,int writeMask) {
        if((writeMask & ~ColorTargetState.WRITE_ALL)!=0)throw new IllegalArgumentException("Invalid GUI color-write mask");
        var base=texture?TEXTURE_OPAQUE:COLOR_OPAQUE;
        if(writeMask==ColorTargetState.WRITE_ALL&&blend.isEmpty())return base;
        var key=new PipelineKey(texture,blend,writeMask);
        return STATE_PIPELINES.computeIfAbsent(key,unused->{var state=base.getColorTargetStates().getFirst();return base.toBuilder()
                .withLocation(net.minecraft.resources.Identifier.fromNamespaceAndPath("lms","legacy_gui_state/"+PIPELINE_IDS.getAndIncrement()))
                .withColorTargetState(new ColorTargetState(blend,state.format(),writeMask)).build();});
    }
    public record Point(float x,float y,float z,float u,float v,int color) {}
    // Bounded opt-in diagnostics retain strings only, never a managed receiver/classloader.
    private static final Set<String> TRACED=java.util.concurrent.ConcurrentHashMap.newKeySet();
    private boolean building,texture,color,current;
    private float x,y,z,u,v;private int argb=-1;
    private final List<Point> points=new ArrayList<>();
    public void begin(LegacyVertexFormatMode mode,LegacyVertexFormat format) {
        Forge1192TooltipGuiBridge.scope();
        if(building)throw new IllegalStateException("Nested source GUI batch");
        if(mode!=LegacyVertexFormatMode.QUADS)throw new IllegalStateException("Only source GUI QUADS are adapted");
        var actual=format.target();
        texture=actual==DefaultVertexFormat.POSITION_TEX || actual==DefaultVertexFormat.POSITION_TEX_COLOR;
        color=actual==DefaultVertexFormat.POSITION_COLOR || actual==DefaultVertexFormat.POSITION_TEX_COLOR;
        if(!texture && !color)throw new IllegalStateException("Unknown source GUI vertex format");
        var scope=Forge1192TooltipGuiBridge.scope();
        var expected=texture?(color?LegacyGuiShader.POSITION_TEX_COLOR:LegacyGuiShader.POSITION_TEX):LegacyGuiShader.POSITION_COLOR;
        if(scope.shader!=expected)throw new IllegalStateException("Source GUI shader/format mismatch");
        if(texture && !scope.textureEnabled)throw new IllegalStateException("Textured source batch has textures disabled");
        building=true;points.clear();current=false;
    }
    private void requireBuilding(){if(!building)throw new IllegalStateException("Source GUI vertex outside batch");}
    private void flushPoint(){if(current){points.add(new Point(x,y,z,u,v,argb));current=false;}}
    @Override public VertexConsumer addVertex(float x,float y,float z){requireBuilding();flushPoint();this.x=x;this.y=y;this.z=z;u=v=0;argb=-1;current=true;return this;}
    public VertexConsumer vertex(Matrix4f pose,float x,float y,float z){Forge1192GuiPlaneBridge.planar(pose);var point=pose.transformPosition(new Vector3f(x,y,z));return addVertex(point.x,point.y,point.z);}
    @Override public VertexConsumer setColor(int r,int g,int b,int a){return setColor(ARGB.color(a,r,g,b));}
    @Override public VertexConsumer setColor(int argb){requireBuilding();if(!current)throw new IllegalStateException("Color before source vertex");this.argb=argb;return this;}
    @Override public VertexConsumer setUv(float u,float v){requireBuilding();if(!current)throw new IllegalStateException("UV before source vertex");this.u=u;this.v=v;return this;}
    @Override public VertexConsumer setUv1(int u,int v){throw new UnsupportedOperationException("GUI UV1");}
    @Override public VertexConsumer setUv2(int u,int v){throw new UnsupportedOperationException("GUI UV2");}
    @Override public VertexConsumer setUv3(float u,float v){throw new UnsupportedOperationException("GUI UV3");}
    @Override public VertexConsumer setNormal(float x,float y,float z){throw new UnsupportedOperationException("GUI normal");}
    @Override public VertexConsumer setLineWidth(float width){throw new UnsupportedOperationException("GUI line width");}
    public void finish() {
        requireBuilding();flushPoint();building=false;
        if(points.size()%4!=0)throw new IllegalStateException("Incomplete source GUI quad batch");
        if(points.isEmpty())return;
        float depth=uniformPlane(points);
        var scope=Forge1192TooltipGuiBridge.scope();var graphics=scope.graphics;
        var transform=new Matrix3x2f(graphics.pose());var snapshot=new ArrayList<Point>();
        for(var point:points){var xy=transform.transformPosition(new Vector2f(point.x,point.y));snapshot.add(new Point(xy.x,xy.y,0,point.u,point.v,ARGB.multiply(point.color,scope.color)));}
        TextureSetup textures=TextureSetup.noTexture();
        if(texture){if(scope.texture==null && scope.textureObject==null)throw new IllegalStateException("No source GUI texture bound");var nativeTexture=scope.textureObject!=null?scope.textureObject:Minecraft.getInstance().getTextureManager().getTexture(scope.texture);textures=TextureSetup.singleTexture(nativeTexture.getTextureView(),nativeTexture.getSampler());}
        if(scope.diagnosticWidget!=null && TRACED.size()<128){
            String sample="widget="+scope.diagnosticWidget+" texture="+scope.texture+" object="+(scope.textureObject==null?"null":scope.textureObject.getClass().getName())
                    +" textured="+texture+" blend="+scope.blend+" color="+Integer.toHexString(scope.color)
                    +" vertices="+points.stream().map(p->p.u+","+p.v+","+Integer.toHexString(p.color)).toList();
            if(TRACED.add(sample))org.slf4j.LoggerFactory.getLogger(LegacyGuiBuffer.class).info("LMS widget-quad trace {}",sample);
        }
        RenderPipeline pipeline=pipeline(texture,scope.blend?Optional.of(scope.blendFunction):Optional.empty(),scope.colorWriteMask);
        var clip=graphics.peekScissorStack();
        Forge1192GuiPlaneBridge.submit(depth);
        graphics.submitGuiElementRenderState(new QuadState(List.copyOf(snapshot),pipeline,textures,clip,bounds(snapshot)));
        points.clear();
    }
    public static float uniformPlane(List<Point> points) {
        if(points.isEmpty())throw new IllegalArgumentException("Empty GUI primitive has no plane");
        float depth=points.getFirst().z;
        for(var point:points)if(!Float.isFinite(point.x)||!Float.isFinite(point.y)||!Float.isFinite(point.z)||point.z!=depth)
            throw new UnsupportedOperationException("Source GUI batch contains mixed/nonfinite planes");
        return depth;
    }
    public static ScreenRectangle bounds(List<Point> points) {
        if(points.isEmpty())return null;
        float minX=Float.POSITIVE_INFINITY,minY=minX,maxX=Float.NEGATIVE_INFINITY,maxY=maxX;
        for(var p:points){minX=Math.min(minX,p.x);minY=Math.min(minY,p.y);maxX=Math.max(maxX,p.x);maxY=Math.max(maxY,p.y);}
        int x=(int)Math.floor(minX),y=(int)Math.floor(minY);return new ScreenRectangle(x,y,(int)Math.ceil(maxX)-x,(int)Math.ceil(maxY)-y);
    }
    public record QuadState(List<Point> points,RenderPipeline pipeline,TextureSetup textureSetup,ScreenRectangle scissorArea,ScreenRectangle bounds) implements GuiElementRenderState {
        public QuadState {points=List.copyOf(points);}
        @Override public void buildVertices(VertexConsumer consumer){for(var p:points){consumer.addVertex(p.x,p.y,p.z).setColor(p.color);if(textureSetup.texure0()!=null)consumer.setUv(p.u,p.v);}}
    }
}
