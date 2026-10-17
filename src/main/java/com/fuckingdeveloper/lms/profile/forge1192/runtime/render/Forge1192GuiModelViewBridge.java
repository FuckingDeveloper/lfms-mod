package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import com.fuckingdeveloper.lms.target.neoforge263.render.Target263GuiModelViewState;

/** Source relative transaction. Global matrix observation is excluded by source dataflow proof. */
public final class Forge1192GuiModelViewBridge {
    private Forge1192GuiModelViewBridge() {}
    static final class State {
        final PoseStack stack=new PoseStack();
        final Target263GuiModelViewState target;
        final Matrix4f applied=new Matrix4f();
        State(Forge1192TooltipGuiBridge.Scope scope){target=new Target263GuiModelViewState(scope.graphics);}
        boolean restored(){return stack.isEmpty()&&stack.last().pose().equals(new Matrix4f())&&applied.equals(new Matrix4f());}
    }
    private static State state(){var scope=Forge1192TooltipGuiBridge.scope();if(scope.modelView==null)scope.modelView=new State(scope);return scope.modelView;}
    public static PoseStack stack(){return state().stack;}
    public static void apply(){
        var state=state();var matrix=state.stack.last().pose();
        // Only XY changes retain the source GUI projection's fixed Z baseline.
        if(matrix.m22()!=1||matrix.m32()!=0)throw new UnsupportedOperationException("Source global GUI model-view Z transform is not adapted");
        state.target.apply(Forge1192GuiPlaneBridge.planar(matrix));state.applied.set(matrix);
    }
    static void beforeNested(Forge1192TooltipGuiBridge.Scope previous){
        if(previous!=null&&previous.modelView!=null&&!previous.modelView.restored())
            throw new UnsupportedOperationException("Nested GUI extraction during a source global model-view transaction is not adapted");
    }
    static void finish(Forge1192TooltipGuiBridge.Scope scope,boolean success){
        if(scope.modelView==null)return;
        var state=scope.modelView;
        try{if(success&&!state.restored())throw new IllegalStateException("Source GUI model-view stack/application was not restored");}
        finally{state.target.restore();scope.modelView=null;}
    }
}
