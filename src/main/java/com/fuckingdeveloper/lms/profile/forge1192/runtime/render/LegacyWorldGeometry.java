package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import com.mojang.renderpearl.api.pipeline.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import java.util.*;

/** Source-local vertices are already transformed; native stage supplies view/projection. */
public final class LegacyWorldGeometry {
    private LegacyWorldGeometry(){}
    public record Vertex(float x,float y,float z,int color) {
        public Vertex{if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(z))throw new IllegalArgumentException("Nonfinite source world vertex");}
    }
    public record State(Optional<BlendFunction> blend,boolean depth,boolean depthWrite,boolean cull,int color,int writeMask) {
        public State{Objects.requireNonNull(blend);if((writeMask&~ColorTargetState.WRITE_ALL)!=0)throw new IllegalArgumentException("Invalid source color mask");}
    }
    public record Batch(PrimitiveTopology topology,List<Vertex> vertices,State state) {
        public Batch{vertices=List.copyOf(vertices);Objects.requireNonNull(topology);Objects.requireNonNull(state);}
    }
    private record PipelineKey(PrimitiveTopology topology,State state){}
    private static final Map<PipelineKey,RenderPipeline> PIPELINES=new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.atomic.AtomicLong IDS=new java.util.concurrent.atomic.AtomicLong();
    public static RenderPipeline pipeline(Batch batch) {
        var state=batch.state();
        // Color is already multiplied into vertices; it is not a pipeline/cache dimension.
        var key=new PipelineKey(batch.topology(),new State(state.blend(),state.depth(),state.depthWrite(),state.cull(),-1,state.writeMask()));
        return PIPELINES.computeIfAbsent(key,k->RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("lms","source_world/"+IDS.getAndIncrement()))
                .withPrimitiveTopology(k.topology())
                .withColorTargetState(new ColorTargetState(k.state().blend(),ColorTargetState.DEFAULT.format(),k.state().writeMask()))
                .withDepthStencilState(new DepthStencilState(k.state().depth()?CompareOp.GREATER_THAN_OR_EQUAL:CompareOp.ALWAYS_PASS,k.state().depth()&&k.state().depthWrite()))
                .withCull(k.state().cull()).build());
    }
    public static Batch finish(LegacyVertexFormatMode mode,List<Vertex> input,State state) {
        List<Vertex> out=new ArrayList<>();PrimitiveTopology topology=PrimitiveTopology.TRIANGLES;int n=input.size();
        switch(mode) {
            case TRIANGLES->{if(n%3!=0)throw new IllegalStateException("Incomplete source triangles");out.addAll(input);}
            case QUADS->{if(n%4!=0)throw new IllegalStateException("Incomplete source quads");for(int i=0;i<n;i+=4)for(int offset:new int[]{0,1,2,2,3,0})out.add(input.get(i+offset));}
            case TRIANGLE_STRIP->{if(n>0&&n<3)throw new IllegalStateException("Incomplete source triangle strip");for(int i=2;i<n;i++){out.add(input.get(i%2==0?i-2:i-1));out.add(input.get(i%2==0?i-1:i-2));out.add(input.get(i));}}
            case TRIANGLE_FAN->{if(n>0&&n<3)throw new IllegalStateException("Incomplete source triangle fan");for(int i=2;i<n;i++){out.add(input.get(0));out.add(input.get(i-1));out.add(input.get(i));}}
            case DEBUG_LINES->{if(n%2!=0)throw new IllegalStateException("Incomplete source debug lines");topology=PrimitiveTopology.DEBUG_LINES;out.addAll(input);}
            case DEBUG_LINE_STRIP->{if(n==1)throw new IllegalStateException("Incomplete source debug line strip");topology=PrimitiveTopology.DEBUG_LINE_STRIP;out.addAll(input);}
            default->throw new UnsupportedOperationException("Unproven source world topology: "+mode);
        }
        return new Batch(topology,out,state);
    }
}
