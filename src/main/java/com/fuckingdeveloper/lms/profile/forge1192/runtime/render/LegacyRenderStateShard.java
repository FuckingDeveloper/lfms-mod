package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import java.util.Objects;
import java.util.OptionalDouble;
import net.minecraft.resources.Identifier;

/** Source-shaped render-state identities; these do not claim target pipeline equivalence. */
public class LegacyRenderStateShard {
    public static class CullStateShard extends LegacyRenderStateShard {}
    public static class DepthTestStateShard extends LegacyRenderStateShard {}
    public static class EmptyTextureStateShard extends LegacyRenderStateShard {}
    public static class LayeringStateShard extends LegacyRenderStateShard {}
    public static class LightmapStateShard extends LegacyRenderStateShard {}
    public static class OutputStateShard extends LegacyRenderStateShard {}
    public static class OverlayStateShard extends LegacyRenderStateShard {}
    public static class ShaderStateShard extends LegacyRenderStateShard {}
    public static class TransparencyStateShard extends LegacyRenderStateShard {}
    public static class WriteMaskStateShard extends LegacyRenderStateShard {}

    public static class TextureStateShard extends EmptyTextureStateShard {
        public final Identifier texture;
        public final boolean blur;
        public final boolean mipmap;

        public TextureStateShard(Identifier texture, boolean blur, boolean mipmap) {
            this.texture = Objects.requireNonNull(texture, "texture");
            this.blur = blur;
            this.mipmap = mipmap;
        }
    }

    public static class TexturingStateShard extends LegacyRenderStateShard {
        public final String name;
        public final Runnable setup;
        public final Runnable clear;

        public TexturingStateShard(String name, Runnable setup, Runnable clear) {
            this.name = Objects.requireNonNull(name, "name");
            this.setup = Objects.requireNonNull(setup, "setup");
            this.clear = Objects.requireNonNull(clear, "clear");
        }
    }

    public static class LineStateShard extends LegacyRenderStateShard {
        public final OptionalDouble width;

        public LineStateShard(OptionalDouble width) {
            this.width = Objects.requireNonNull(width, "width");
        }
    }

    public static final ShaderStateShard f_173080_ = new ShaderStateShard();
    public static final ShaderStateShard f_173108_ = new ShaderStateShard();
    public static final ShaderStateShard f_173104_ = new ShaderStateShard();
    public static final ShaderStateShard f_173095_ = new ShaderStateShard();
    public static final WriteMaskStateShard f_110115_ = new WriteMaskStateShard();
    public static final WriteMaskStateShard f_110114_ = new WriteMaskStateShard();
    public static final CullStateShard f_110110_ = new CullStateShard();
    public static final DepthTestStateShard f_110112_ = new DepthTestStateShard();
    public static final DepthTestStateShard f_110111_ = new DepthTestStateShard();
    public static final TransparencyStateShard f_110137_ = new TransparencyStateShard();
    public static final TransparencyStateShard f_110139_ = new TransparencyStateShard();
    public static final TransparencyStateShard f_110134_ = new TransparencyStateShard();
    public static final LightmapStateShard f_110152_ = new LightmapStateShard();
    public static final OutputStateShard f_110123_ = new OutputStateShard();
    public static final OutputStateShard f_110125_ = new OutputStateShard();
    public static final OutputStateShard f_110129_ = new OutputStateShard();
    public static final OverlayStateShard f_110154_ = new OverlayStateShard();
    public static final LayeringStateShard f_110119_ = new LayeringStateShard();
}
