package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import java.util.ArrayList;
import java.util.List;

/** Captured Forge 1.19.2 composite render state without target-pipeline claims. */
public final class LegacyCompositeState {
    private final List<LegacyRenderStateShard> shards;
    private final boolean affectsOutline;

    private LegacyCompositeState(List<LegacyRenderStateShard> shards, boolean affectsOutline) {
        this.shards = List.copyOf(shards);
        this.affectsOutline = affectsOutline;
    }

    public List<LegacyRenderStateShard> shards() { return shards; }
    public boolean affectsOutline() { return affectsOutline; }

    public static Builder m_110628_() { return new Builder(); }

    public static final class Builder {
        private final List<LegacyRenderStateShard> shards = new ArrayList<>();

        private Builder add(LegacyRenderStateShard shard) {
            shards.add(shard);
            return this;
        }

        public Builder m_173292_(LegacyRenderStateShard.ShaderStateShard value) { return add(value); }
        public Builder m_173290_(LegacyRenderStateShard.EmptyTextureStateShard value) { return add(value); }
        public Builder m_110687_(LegacyRenderStateShard.WriteMaskStateShard value) { return add(value); }
        public Builder m_110661_(LegacyRenderStateShard.CullStateShard value) { return add(value); }
        public Builder m_110663_(LegacyRenderStateShard.DepthTestStateShard value) { return add(value); }
        public Builder m_110685_(LegacyRenderStateShard.TransparencyStateShard value) { return add(value); }
        public Builder m_110683_(LegacyRenderStateShard.TexturingStateShard value) { return add(value); }
        public Builder m_110675_(LegacyRenderStateShard.OutputStateShard value) { return add(value); }
        public Builder m_110671_(LegacyRenderStateShard.LightmapStateShard value) { return add(value); }
        public Builder m_110677_(LegacyRenderStateShard.OverlayStateShard value) { return add(value); }
        public Builder m_110673_(LegacyRenderStateShard.LineStateShard value) { return add(value); }
        public Builder m_110669_(LegacyRenderStateShard.LayeringStateShard value) { return add(value); }

        public LegacyCompositeState m_110691_(boolean affectsOutline) {
            return new LegacyCompositeState(shards, affectsOutline);
        }
    }
}
