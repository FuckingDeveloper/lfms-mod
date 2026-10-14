package com.fuckingdeveloper.lms.runtime.render;

/** Method-local source GUI batch ownership; never supplied to native world buffers. */
public final class LegacyGuiTesselator {
    private static final LegacyGuiTesselator INSTANCE=new LegacyGuiTesselator();
    private LegacyGuiTesselator() {}
    public static LegacyGuiTesselator getInstance(){Forge1192TooltipGuiBridge.scope();return INSTANCE;}
    public LegacyGuiBuffer getBuffer(){var scope=Forge1192TooltipGuiBridge.scope();if(scope.buffer==null)scope.buffer=new LegacyGuiBuffer();return scope.buffer;}
    public void end(){getBuffer().finish();}
}
