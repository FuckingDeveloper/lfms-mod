package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.util.ARGB;
import org.joml.*;
import java.util.*;

/** Concrete source ABI. Unsupported native builder operations remain absent/fail-fast. */
public final class LegacyImmediateBuffer implements VertexConsumer {
    private LegacyWorldRenderScope owner;
    private LegacyGuiBuffer gui;
    private Forge1192TooltipGuiBridge.Scope guiOwner;
    private boolean building,current;
    private float x,y,z;private int color=-1;
    private LegacyVertexFormatMode mode;
    private final List<LegacyWorldGeometry.Vertex> vertices=new ArrayList<>();
    public LegacyImmediateBuffer(int capacity){if(capacity<0)throw new IllegalArgumentException("Negative source buffer size");}
    static LegacyImmediateBuffer gui(){var scope=Forge1192TooltipGuiBridge.scope();if(scope.immediateBuffer==null){scope.immediateBuffer=new LegacyImmediateBuffer(256);scope.immediateBuffer.gui=LegacyGuiTesselator.getInstance().getBuffer();}return scope.immediateBuffer;}
    boolean building(){return building;}
    public void m_166779_(LegacyVertexFormatMode mode,LegacyVertexFormat format) {
        if(building)throw new IllegalStateException("Nested source immediate batch");
        if(!LegacyWorldRenderScope.active()){guiOwner=Forge1192TooltipGuiBridge.scope();gui=LegacyGuiTesselator.getInstance().getBuffer();gui.begin(mode,format);building=true;guiOwner.sourceBuffers.add(this);return;}
        gui=null;guiOwner=null;
        if(format.target()!=DefaultVertexFormat.POSITION_COLOR)throw new UnsupportedOperationException("Unproven source world vertex format");
        owner=LegacyWorldRenderScope.scope();if(owner.shader!=LegacyGuiShader.POSITION_COLOR)throw new UnsupportedOperationException("Unproven source world shader/format combination");
        if(mode==LegacyVertexFormatMode.LINES||mode==LegacyVertexFormatMode.LINE_STRIP)throw new UnsupportedOperationException("Source expanded world lines require a normal/width adapter");
        this.mode=mode;building=true;current=false;vertices.clear();owner.opened.add(this);
    }
    private void check(){if(!building||owner!=LegacyWorldRenderScope.scope())throw new IllegalStateException("Source world buffer outside owning batch/scope");}
    private void vertex(){if(current){vertices.add(new LegacyWorldGeometry.Vertex(x,y,z,color));current=false;}}
    @Override public VertexConsumer addVertex(float x,float y,float z){if(gui!=null){checkGui();gui.addVertex(x,y,z);return this;}check();vertex();this.x=x;this.y=y;this.z=z;color=-1;current=true;return this;}
    public VertexConsumer m_85982_(Matrix4f pose,float x,float y,float z){if(gui!=null){checkGui();gui.vertex(pose,x,y,z);return this;}var v=pose.transformPosition(new Vector3f(x,y,z));return addVertex(v.x,v.y,v.z);}
    @Override public VertexConsumer setColor(int r,int g,int b,int a){return setColor(ARGB.color(a,r,g,b));}
    @Override public VertexConsumer setColor(int color){if(gui!=null){checkGui();gui.setColor(color);return this;}check();if(!current)throw new IllegalStateException("Color before source vertex");this.color=color;return this;}
    @Override public VertexConsumer setUv(float u,float v){if(gui!=null){checkGui();gui.setUv(u,v);return this;}throw unsupported("UV");}
    @Override public VertexConsumer setUv1(int u,int v){throw unsupported("UV1");}
    @Override public VertexConsumer setUv2(int u,int v){throw unsupported("UV2");}
    @Override public VertexConsumer setUv3(float u,float v){throw unsupported("UV3");}
    @Override public VertexConsumer setNormal(float x,float y,float z){throw unsupported("normal");}
    @Override public VertexConsumer setLineWidth(float width){throw unsupported("line width");}
    public void finish(){
        if(gui!=null){checkGui();gui.finish();abort();return;}
        check();vertex();var state=owner.snapshot();
        var snapshot=vertices.stream().map(v->new LegacyWorldGeometry.Vertex(v.x(),v.y(),v.z(),ARGB.multiply(v.color(),state.color()))).toList();
        var batch=LegacyWorldGeometry.finish(mode,snapshot,state);building=false;current=false;vertices.clear();if(!batch.vertices().isEmpty())owner.batches.add(batch);owner=null;
    }
    private void checkGui(){if(guiOwner!=Forge1192TooltipGuiBridge.scope()||!building)throw new IllegalStateException("Foreign source GUI batch scope");}
    void abort(){building=false;current=false;owner=null;guiOwner=null;gui=null;vertices.clear();}
    private static UnsupportedOperationException unsupported(String channel){return new UnsupportedOperationException("Source immediate world buffer does not support "+channel);}
}
