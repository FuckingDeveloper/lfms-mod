package com.fuckingdeveloper.lms.target.neoforge263.render;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.Forge1192RenderStageBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.LegacyWorldGeometry;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.LegacyWorldRenderScope;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.LegacyWorldStageEvent;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.RenderPass;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

/** Native extract -> prepare GPU data -> draw in the actual matching render pass. */
public final class Target263WorldRenderAdapter {
    private static final ContextKey<Frame> KEY=new ContextKey<>(Identifier.fromNamespaceAndPath("lms","source_world_geometry"));
    private static final IEventBus SOURCE=BusBuilder.builder().build();
    static {SOURCE.start();}
    private static boolean installed;
    private static final List<Forge1192RenderStageBridge.Stage> STAGES=List.of(Forge1192RenderStageBridge.Stage.AFTER_SKY,
            Forge1192RenderStageBridge.Stage.AFTER_CUTOUT_BLOCKS,Forge1192RenderStageBridge.Stage.AFTER_TRANSLUCENT_BLOCKS,
            Forge1192RenderStageBridge.Stage.AFTER_PARTICLES,Forge1192RenderStageBridge.Stage.AFTER_WEATHER);
    private record Prepared(LegacyWorldGeometry.Batch geometry,GpuBuffer buffer){}
    private static final class Frame {
        final Map<Forge1192RenderStageBridge.Stage,List<LegacyWorldGeometry.Batch>> geometry=new EnumMap<>(Forge1192RenderStageBridge.Stage.class);
        final Map<Forge1192RenderStageBridge.Stage,List<Prepared>> prepared=new EnumMap<>(Forge1192RenderStageBridge.Stage.class);
        final Set<Forge1192RenderStageBridge.Stage> drawn=EnumSet.noneOf(Forge1192RenderStageBridge.Stage.class);
        boolean uploaded;
        void close(){for(var batches:prepared.values())for(var batch:batches)batch.buffer.close();prepared.clear();}
    }
    private Target263WorldRenderAdapter(){}
    public static IEventBus sourceBus(){return SOURCE;}
    public static synchronized void install() {
        if(installed)return;
        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,ExtractLevelRenderStateEvent.class,Target263WorldRenderAdapter::extract);
        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,PrepareRenderBuffersEvent.class,Target263WorldRenderAdapter::prepare);
        for(var type:List.of(RenderLevelStageEvent.AfterOpaqueBlocks.class,RenderLevelStageEvent.AfterTranslucentBlocks.class,
                RenderLevelStageEvent.AfterTranslucentParticles.class,RenderLevelStageEvent.AfterWeather.class))registerDraw(type);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,false,RenderLevelStageEvent.AfterLevel.class,event->{var frame=event.getLevelRenderState().getRenderData(KEY);if(frame!=null)frame.close();});
        installed=true;
    }
    private static <T extends RenderLevelStageEvent>void registerDraw(Class<T> type){NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,type,Target263WorldRenderAdapter::draw);}
    private static void extract(ExtractLevelRenderStateEvent event) {
        var state=event.getRenderState();var frame=new Frame();
        for(var stage:STAGES) {
            var source=LegacyWorldStageEvent.forExtraction(stage,Minecraft.getInstance().levelRenderer,new PoseStack(),
                    new Matrix4f(state.cameraRenderState.projectionMatrix),state.worldPartialTicks,event.getCamera(),event.getFrustum());
            var batches=LegacyWorldRenderScope.collect(()->SOURCE.post(source));
            if(!batches.isEmpty()) {
                if(stage==Forge1192RenderStageBridge.Stage.AFTER_SKY)throw new UnsupportedOperationException("Nonempty source sky batches need a verified target output attachment");
                if(Minecraft.getInstance().gameRenderer.useImprovedTransparency()&&stage!=Forge1192RenderStageBridge.Stage.AFTER_CUTOUT_BLOCKS)
                    throw new UnsupportedOperationException("Source world overlay has no proven OIT stage projection");
                frame.geometry.put(stage,batches);
            }
        }
        state.setRenderData(KEY,frame);
    }
    private static void prepare(PrepareRenderBuffersEvent event) {
        var frame=event.getLevelRenderState().getRenderData(KEY);if(frame==null)return;
        if(frame.uploaded)throw new IllegalStateException("Source world frame uploaded twice");
        frame.uploaded=true;
        try {
            for(var entry:frame.geometry.entrySet()) {
                var prepared=new ArrayList<Prepared>();frame.prepared.put(entry.getKey(),prepared);
                for(var batch:entry.getValue()) {
                    var bytes=MemoryUtil.memAlloc(Math.multiplyExact(batch.vertices().size(),16)).order(java.nio.ByteOrder.nativeOrder());
                    try {
                        for(var vertex:batch.vertices()) {int c=vertex.color();bytes.putFloat(vertex.x()).putFloat(vertex.y()).putFloat(vertex.z()).put((byte)(c>>16)).put((byte)(c>>8)).put((byte)c).put((byte)(c>>24));}
                        bytes.flip();var buffer=RenderSystem.getDevice().createBuffer(()->"LMS source world geometry",GpuBuffer.USAGE_VERTEX,bytes);prepared.add(new Prepared(batch,buffer));
                        RenderSystem.getCompiledPipeline(LegacyWorldGeometry.pipeline(batch));
                    }finally{MemoryUtil.memFree(bytes);}
                }
            }
        }catch(RuntimeException|Error failure){frame.close();throw failure;}
    }
    private static void draw(RenderLevelStageEvent event) {
        var frame=event.getLevelRenderState().getRenderData(KEY);if(frame==null)return;
        var stage=Forge1192RenderStageBridge.stage(event);var batches=frame.prepared.get(stage);if(batches==null)return;
        if(!frame.drawn.add(stage))throw new IllegalStateException("Source world stage drawn twice");
        RenderPass pass;
        if(event instanceof RenderLevelStageEvent.AfterOpaqueBlocks typed)pass=typed.getRenderPass();
        else if(event instanceof RenderLevelStageEvent.AfterTranslucentBlocks typed)pass=typed.getRenderPass();
        else if(event instanceof RenderLevelStageEvent.AfterTranslucentParticles typed)pass=typed.getRenderPass();
        else if(event instanceof RenderLevelStageEvent.AfterWeather typed)pass=typed.getRenderPass();
        else throw new UnsupportedOperationException("Unproven source world render pass");
        var transform=RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(event.getModelViewMatrix()));
        for(var batch:batches) {
            pass.setPipeline(RenderSystem.getCompiledPipeline(LegacyWorldGeometry.pipeline(batch.geometry)));
            RenderSystem.bindDefaultUniforms(pass);pass.setUniform("DynamicTransforms",transform);pass.setVertexBuffer(0,batch.buffer.slice());
            pass.draw(batch.geometry.vertices().size(),1,0,0);
        }
        if(Boolean.getBoolean("lms.traceWorldGeometry"))org.slf4j.LoggerFactory.getLogger(Target263WorldRenderAdapter.class).info("LMS world geometry stage={} batches={} vertices={}",stage,batches.size(),batches.stream().mapToInt(b->b.geometry.vertices().size()).sum());
    }
}
