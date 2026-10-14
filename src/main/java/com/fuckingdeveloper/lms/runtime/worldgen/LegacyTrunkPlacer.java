package com.fuckingdeveloper.lms.runtime.worldgen;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.trunkplacers.*;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer.FoliageAttachment;
/** Native base placement dispatches to the exact source override with scoped intent. */
public abstract class LegacyTrunkPlacer extends TrunkPlacer {
    public LegacyTrunkPlacer(int base,int a,int b){super(base,a,b);}
    protected abstract TrunkPlacerType<?> lms$trunkType();
    @Override protected final TrunkPlacerType<?> type(){return lms$trunkType();}
    public abstract List<FoliageAttachment> lms$placeTrunk(LevelSimulatedReader level,BiConsumer<BlockPos,BlockState> setter,RandomSource random,int height,BlockPos origin,LegacyTreeConfiguration config);
    @Override public final List<FoliageAttachment> placeTrunk(WorldGenLevel level,BiConsumer<BlockPos,BlockState> setter,RandomSource random,int height,BlockPos origin,TreeFeature tree){return lms$placeTrunk(level,setter,random,height,origin,Target263WorldgenAdapter.configuration(tree));}
    protected static <P extends TrunkPlacer> com.mojang.datafixers.Products.P3<com.mojang.serialization.codecs.RecordCodecBuilder.Mu<P>,Integer,Integer,Integer> lms$trunkParts(com.mojang.serialization.codecs.RecordCodecBuilder.Instance<P> instance){return trunkPlacerParts(instance);}
    protected static void lms$belowTrunk(LevelSimulatedReader level,BiConsumer<BlockPos,BlockState> setter,RandomSource random,BlockPos pos,LegacyTreeConfiguration config){placeBelowTrunkBlock(Target263WorldgenAdapter.world(level),setter,random,pos,Target263WorldgenAdapter.tree(config));}
}
