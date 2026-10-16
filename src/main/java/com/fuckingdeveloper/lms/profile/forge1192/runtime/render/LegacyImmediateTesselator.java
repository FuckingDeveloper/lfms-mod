package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

/** Source-shaped ownership permits helpers/lambdas without claiming a native builder ABI. */
public final class LegacyImmediateTesselator {
    private static final LegacyImmediateTesselator INSTANCE=new LegacyImmediateTesselator();
    private LegacyImmediateTesselator(){}
    public static LegacyImmediateTesselator m_85913_(){if(!LegacyWorldRenderScope.active())Forge1192TooltipGuiBridge.scope();return INSTANCE;}
    public LegacyImmediateBuffer m_85915_(){
        if(LegacyWorldRenderScope.active()) {
            var scope=LegacyWorldRenderScope.scope();if(scope.buffer==null)scope.buffer=new LegacyImmediateBuffer(256);return scope.buffer;
        }
        return LegacyImmediateBuffer.gui();
    }
    public void m_85914_(){m_85915_().finish();}
}
