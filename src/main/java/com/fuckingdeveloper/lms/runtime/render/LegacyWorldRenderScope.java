package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.renderpearl.api.pipeline.*;
import java.util.*;

/** Source mutable intent exists only while collecting immutable world geometry. */
public final class LegacyWorldRenderScope {
    private static final ThreadLocal<LegacyWorldRenderScope> ACTIVE=new ThreadLocal<>();
    LegacyGuiShader shader;
    boolean blend,depth=true,depthWrite=true,cull=true,texture=true;
    int color=-1,writeMask=ColorTargetState.WRITE_ALL;
    BlendFunction blendFunction=LegacyGuiBlend.DEFAULT;
    final List<LegacyWorldGeometry.Batch> batches=new ArrayList<>();
    LegacyImmediateBuffer buffer;
    final List<LegacyImmediateBuffer> opened=new ArrayList<>();
    private LegacyWorldRenderScope(){}
    static boolean active(){return ACTIVE.get()!=null&&!Forge1192TooltipGuiBridge.active();}
    static LegacyWorldRenderScope scope(){var scope=ACTIVE.get();if(scope==null)throw new IllegalStateException("Source world primitive outside frame extraction");return scope;}
    public static List<LegacyWorldGeometry.Batch> collect(Runnable callback) {
        var previous=ACTIVE.get();var scope=new LegacyWorldRenderScope();ACTIVE.set(scope);
        try{callback.run();if(scope.opened.stream().anyMatch(LegacyImmediateBuffer::building))throw new IllegalStateException("Unfinished source world batch");return List.copyOf(scope.batches);}
        finally{scope.opened.forEach(LegacyImmediateBuffer::abort);if(previous==null)ACTIVE.remove();else ACTIVE.set(previous);}
    }
    LegacyWorldGeometry.State snapshot(){return new LegacyWorldGeometry.State(blend?Optional.of(blendFunction):Optional.empty(),depth,depthWrite,cull,color,writeMask);}
}
