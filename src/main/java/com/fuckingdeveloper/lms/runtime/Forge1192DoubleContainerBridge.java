package com.fuckingdeveloper.lms.runtime;

import java.util.function.*;
import net.minecraft.core.*;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/** Source DirectionProperty became EnumProperty; the unchanged native algorithm accepts Property. */
public final class Forge1192DoubleContainerBridge {
    private Forge1192DoubleContainerBridge(){}
    public static <S extends BlockEntity> DoubleBlockCombiner.NeighborCombineResult<S> combine(
            BlockEntityType<S> type,Function<BlockState,DoubleBlockCombiner.BlockType> typeResolver,
            Function<BlockState,Direction> connectionResolver,EnumProperty<Direction> facing,
            BlockState state,LevelAccessor level,BlockPos pos,BiPredicate<LevelAccessor,BlockPos> blocked) {
        return DoubleBlockCombiner.combineWithNeigbour(type,typeResolver,connectionResolver,facing,state,level,pos,blocked);
    }
}
