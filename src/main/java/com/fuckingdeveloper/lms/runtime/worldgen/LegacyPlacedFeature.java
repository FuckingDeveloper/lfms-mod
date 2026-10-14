package com.fuckingdeveloper.lms.runtime.worldgen;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.*;
/** Source holder intent and immutable native placement-modifier order. */
public final class LegacyPlacedFeature {
    private final Holder<LegacyConfiguredFeature<?,?>> feature;private final List<PlacementModifier> placement;
    public LegacyPlacedFeature(Holder<LegacyConfiguredFeature<?,?>> feature,List<PlacementModifier> placement){this.feature=java.util.Objects.requireNonNull(feature);this.placement=List.copyOf(placement);}
    public Holder<LegacyConfiguredFeature<?,?>> feature(){return feature;}public List<PlacementModifier> placement(){return placement;}
    public PlacedFeature target(){return new PlacedFeature(Holder.direct(feature.value().target()),placement);}
    public boolean place(WorldGenLevel level,ChunkGenerator generator,RandomSource random,BlockPos pos){return target().place(level,generator,random,pos);}
}
