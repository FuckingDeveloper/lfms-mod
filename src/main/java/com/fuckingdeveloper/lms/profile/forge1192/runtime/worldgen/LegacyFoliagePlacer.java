package com.fuckingdeveloper.lms.profile.forge1192.runtime.worldgen;

import com.fuckingdeveloper.lms.target.neoforge263.worldgen.Target263WorldgenAdapter;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.*;
/** Setter adapters retain the native foliage-set tracking implementation. */
public abstract class LegacyFoliagePlacer extends FoliagePlacer {
    public LegacyFoliagePlacer(IntProvider radius,IntProvider offset){super(radius,offset);}
    protected abstract FoliagePlacerType<?> lms$foliageType();
    @Override protected final FoliagePlacerType<?> type(){return lms$foliageType();}
    protected abstract void lms$createFoliage(LevelSimulatedReader level,BiConsumer<BlockPos,BlockState> setter,RandomSource random,LegacyTreeConfiguration config,int height,FoliageAttachment attachment,int foliageHeight,int radius,int offset);
    public abstract int lms$foliageHeight(RandomSource random,int height,LegacyTreeConfiguration config);
    protected abstract boolean lms$skipLocation(RandomSource random,int dx,int y,int dz,int radius,boolean doubleTrunk);
    @Override protected final void createFoliage(WorldGenLevel level,FoliageSetter setter,RandomSource random,TreeFeature tree,int height,FoliageAttachment attachment,int foliageHeight,int radius,int offset){lms$createFoliage(level,setter::set,random,Target263WorldgenAdapter.configuration(tree),height,attachment,foliageHeight,radius,offset);}
    @Override public final int foliageHeight(RandomSource random,int height,TreeFeature tree){return lms$foliageHeight(random,height,Target263WorldgenAdapter.configuration(tree));}
    @Override protected final boolean shouldSkipLocation(RandomSource random,int dx,int y,int dz,int radius,boolean doubleTrunk){return lms$skipLocation(random,dx,y,dz,radius,doubleTrunk);}
    protected static <P extends FoliagePlacer> com.mojang.datafixers.Products.P2<com.mojang.serialization.codecs.RecordCodecBuilder.Mu<P>,IntProvider,IntProvider> lms$foliageParts(com.mojang.serialization.codecs.RecordCodecBuilder.Instance<P> instance){return foliagePlacerParts(instance);}
}
