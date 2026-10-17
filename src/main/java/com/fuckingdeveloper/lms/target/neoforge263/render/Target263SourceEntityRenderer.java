package com.fuckingdeveloper.lms.target.neoforge263.render;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.*;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

/** Source callbacks extract their own geometry; submission never retains the source entity. */
public abstract class Target263SourceEntityRenderer<E extends Entity>
        extends EntityRenderer<E,Target263SourceEntityRenderer.State> {
    public static final class State extends EntityRenderState {
        private LegacyMultiBufferSource.RecordedGeometry geometry;
        private List<NamePass> names=List.of();
    }
    private record NamePass(org.joml.Matrix4fc pose,int light,net.minecraft.network.chat.Component text) {}
    private record Extraction(Entity entity,List<NamePass> names,State state) {}
    private Extraction lms$active;

    protected Target263SourceEntityRenderer(EntityRendererProvider.Context context){super(context);}
    @Override public State createRenderState(){return new State();}
    @Override public void extractRenderState(E entity,State state,float partial){
        super.extractRenderState(entity,state,partial);
        lms$extractSource(entity,state,partial);
    }
    protected LegacyMultiBufferSource lms$newBuffers(E entity) {
        return LegacyMultiBufferSource.recording(Minecraft.getInstance().getAtlasManager(),Target263EntityBlockContexts.forEntity(entity));
    }
    /** Native base metadata/light have already been extracted by the actual integration entry. */
    public final void lms$extractSource(E entity,State state,float partial) {
        if(state.geometry!=null)throw new IllegalStateException("Source entity geometry already extracted for this state");
        var buffers=lms$newBuffers(entity);var names=new ArrayList<NamePass>();var previous=lms$active;
        lms$active=new Extraction(entity,names,state);
        try {
            lmsLegacyRender(entity,entity.getYRot(partial),partial,new PoseStack(),buffers,state.lightCoords);
            state.geometry=buffers.finishRecording();state.names=List.copyOf(names);
        }finally{lms$active=previous;}
    }
    /** Original EntityRenderer.render base behavior is name display, not a replacement model. */
    public void lmsLegacyRender(E entity,float yaw,float partial,PoseStack pose,LegacyMultiBufferSource buffers,int light) {
        lms$baseRender(entity,pose,buffers,light);
    }
    public abstract Identifier lmsLegacyTexture(E entity);
    @SuppressWarnings("unchecked")
    private void lms$baseRender(Entity entity,PoseStack pose,LegacyMultiBufferSource buffers,int light) {
        if(lms$active==null||lms$active.entity()!=entity)
            throw new UnsupportedOperationException("Source entity base render requires its active native extraction");
        var text=lms$active.state().nameTag;
        if(text!=null)lmsLegacyNameTag((E)entity,text,pose,buffers,light);
    }
    protected void lmsLegacyNameTag(E entity,net.minecraft.network.chat.Component text,PoseStack pose,LegacyMultiBufferSource buffers,int light) {
        lms$baseNameTag(entity,text,pose,light);
    }
    private void lms$baseNameTag(Entity entity,net.minecraft.network.chat.Component text,PoseStack pose,int light) {
        if(lms$active==null||lms$active.entity()!=entity)
            throw new UnsupportedOperationException("Source entity name rendering requires its active native extraction");
        lms$active.names().add(new NamePass(new org.joml.Matrix4f(pose.last().pose()),light,Objects.requireNonNull(text)));
    }
    @Override public void submit(State state,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera) {
        if(state.geometry==null)throw new IllegalStateException("Source entity geometry was not extracted");
        state.geometry.submit(collector,pose,state.outlineColor);
        int light=state.lightCoords;var text=state.nameTag;
        try {
            for(var name:state.names) {
                pose.pushPose();
                try {pose.mulPose(name.pose());state.lightCoords=name.light();state.nameTag=name.text();super.submitNameDisplay(state,pose,collector,camera);}
                finally {pose.popPose();}
            }
        }finally {state.lightCoords=light;state.nameTag=text;}
    }
    private static Target263SourceEntityRenderer<?> source(EntityRenderer<?,?> renderer) {
        if(renderer instanceof Target263SourceEntityRenderer<?> source)return source;
        throw new UnsupportedOperationException("Immediate source entity render/texture query on native-only renderer: "+renderer.getClass().getName());
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    public static void render(EntityRenderer<?,?> renderer,Entity entity,float yaw,float partial,PoseStack pose,LegacyMultiBufferSource buffers,int light) {
        ((Target263SourceEntityRenderer)source(renderer)).lmsLegacyRender(entity,yaw,partial,pose,buffers,light);
    }
    public static void baseRender(EntityRenderer<?,?> renderer,Entity entity,float yaw,float partial,PoseStack pose,LegacyMultiBufferSource buffers,int light) {
        source(renderer).lms$baseRender(entity,pose,buffers,light);
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    public static void nameTag(EntityRenderer<?,?> renderer,Entity entity,net.minecraft.network.chat.Component text,PoseStack pose,LegacyMultiBufferSource buffers,int light) {
        ((Target263SourceEntityRenderer)source(renderer)).lmsLegacyNameTag(entity,text,pose,buffers,light);
    }
    public static void baseNameTag(EntityRenderer<?,?> renderer,Entity entity,net.minecraft.network.chat.Component text,PoseStack pose,LegacyMultiBufferSource buffers,int light) {
        source(renderer).lms$baseNameTag(entity,text,pose,light);
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    public static Identifier texture(EntityRenderer<?,?> renderer,Entity entity) {
        return ((Target263SourceEntityRenderer)source(renderer)).lmsLegacyTexture(entity);
    }
    public static void whiteBlock(LegacyBlockRenderDispatcher dispatcher,BlockState state,PoseStack pose,LegacyMultiBufferSource buffers,int light,boolean white) {
        dispatcher.renderSingleBlock(state,pose,buffers,light,
                white?OverlayTexture.pack(OverlayTexture.u(1F),10):OverlayTexture.NO_OVERLAY);
    }
}
