package com.fuckingdeveloper.lms.profile.forge1192.runtime.worldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
/** Retains the exact source kind/configuration pair and its native placement. */
public final class LegacyConfiguredFeature<C extends LegacyFeatureConfiguration,F extends LegacyFeature<C>> {
    private final F feature; private final C config;
    private final net.minecraft.world.level.levelgen.feature.Feature target;
    public LegacyConfiguredFeature(F feature,C config){this.feature=java.util.Objects.requireNonNull(feature);this.config=java.util.Objects.requireNonNull(config);this.target=feature.bind(config);}
    public F feature(){return feature;} public C config(){return config;}
    public F f_65377_(){return feature;} public C f_65378_(){return config;}
    public F m_65394_(){return feature;} public C m_65397_(){return config;}
    public net.minecraft.world.level.levelgen.feature.Feature target(){return target;}
    public boolean place(WorldGenLevel level,ChunkGenerator generator,RandomSource random,BlockPos pos){return target.place(level,generator,random,pos);}
    public boolean m_224953_(WorldGenLevel level,ChunkGenerator generator,RandomSource random,BlockPos pos){return place(level,generator,random,pos);}
}
