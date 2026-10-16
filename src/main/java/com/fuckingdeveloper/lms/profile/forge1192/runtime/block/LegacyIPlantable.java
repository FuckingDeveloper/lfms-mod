package com.fuckingdeveloper.lms.profile.forge1192.runtime.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.TriState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Compatibility contract for Forge 1.19.2's removed IPlantable family. */
public interface LegacyIPlantable {
    default LegacyPlantType getPlantType(BlockGetter level, BlockPos pos) {
        return LegacyPlantType.PLAINS;
    }

    BlockState getPlant(BlockGetter level, BlockPos pos);

    /**
     * Adapts the old boolean soil query to the modern tri-state hook. A
     * definitive modern answer wins; DEFAULT falls back to the old PlantType
     * rules so legacy custom plants retain their placement contract.
     */
    static boolean canSustainPlant(BlockState soil, BlockGetter level, BlockPos soilPos,
                                   Direction facing, LegacyIPlantable plant) {
        BlockPos plantPos = soilPos.relative(facing);
        BlockState plantState = plant.getPlant(level, plantPos);
        TriState decision = soil.canSustainPlant(level, soilPos, facing, plantState);
        if (!decision.isDefault()) return decision.isTrue();

        LegacyPlantType type = plant.getPlantType(level, plantPos);
        if (type == LegacyPlantType.DESERT) {
            return soil.is(BlockTags.SAND) || soil.getBlock() instanceof GlazedTerracottaBlock;
        }
        if (type == LegacyPlantType.NETHER) return soil.is(Blocks.SOUL_SAND);
        if (type == LegacyPlantType.CROP) return soil.is(Blocks.FARMLAND);
        if (type == LegacyPlantType.CAVE) return soil.isFaceSturdy(level, soilPos, Direction.UP);
        if (type == LegacyPlantType.WATER) return soil.getFluidState().is(FluidTags.WATER);

        boolean plainsSoil = soil.is(Blocks.GRASS_BLOCK)
                || soil.is(BlockTags.DIRT)
                || soil.is(Blocks.FARMLAND);
        if (type == LegacyPlantType.PLAINS) return plainsSoil;
        if (type == LegacyPlantType.BEACH && plainsSoil) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (level.getFluidState(soilPos.relative(direction)).is(FluidTags.WATER)) return true;
            }
        }
        return false;
    }
}
