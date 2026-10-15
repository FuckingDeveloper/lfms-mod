package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.renderpearl.api.pipeline.*;

/** Source enum ABI plus native blend equations; no mutable GPU/global state. */
public final class LegacyGuiBlend {
    private LegacyGuiBlend() {}
    public static final BlendFunction DEFAULT=new BlendFunction(BlendFactor.SRC_ALPHA,BlendFactor.ONE_MINUS_SRC_ALPHA,BlendFactor.ONE,BlendFactor.ZERO);
    public enum SourceFactor {
        CONSTANT_ALPHA(32771), CONSTANT_COLOR(32769), DST_ALPHA(772), DST_COLOR(774), ONE(1), ONE_MINUS_CONSTANT_ALPHA(32772), ONE_MINUS_CONSTANT_COLOR(32770), ONE_MINUS_DST_ALPHA(773), ONE_MINUS_DST_COLOR(775), ONE_MINUS_SRC_ALPHA(771), ONE_MINUS_SRC_COLOR(769), SRC_ALPHA(770), SRC_ALPHA_SATURATE(776), SRC_COLOR(768), ZERO(0);
        public final int value,f_84751_;
        SourceFactor(int value){this.value=value;this.f_84751_=value;}
    }
    public enum DestFactor {
        CONSTANT_ALPHA(32771), CONSTANT_COLOR(32769), DST_ALPHA(772), DST_COLOR(774), ONE(1), ONE_MINUS_CONSTANT_ALPHA(32772), ONE_MINUS_CONSTANT_COLOR(32770), ONE_MINUS_DST_ALPHA(773), ONE_MINUS_DST_COLOR(775), ONE_MINUS_SRC_ALPHA(771), ONE_MINUS_SRC_COLOR(769), SRC_ALPHA(770), SRC_COLOR(768), ZERO(0);
        public final int value,f_84646_;
        DestFactor(int value){this.value=value;this.f_84646_=value;}
    }
    private static BlendFactor factor(Enum<?> value) {
        String name=java.util.Objects.requireNonNull(value).name();
        if(name.contains("CONSTANT"))throw new UnsupportedOperationException("Source GUI blend constants are not adapted: "+name);
        return BlendFactor.valueOf(name);
    }
    public static BlendFunction equation(SourceFactor src,DestFactor dst,SourceFactor srcAlpha,DestFactor dstAlpha) {
        return new BlendFunction(factor(src),factor(dst),factor(srcAlpha),factor(dstAlpha));
    }
    public static void blendFunc(SourceFactor src,DestFactor dst){blendFuncSeparate(src,dst,src,dst);}
    public static void blendFuncSeparate(SourceFactor src,DestFactor dst,SourceFactor srcAlpha,DestFactor dstAlpha){
        var scope=Forge1192TooltipGuiBridge.scope();scope.blendFunction=equation(src,dst,srcAlpha,dstAlpha);
    }
}
