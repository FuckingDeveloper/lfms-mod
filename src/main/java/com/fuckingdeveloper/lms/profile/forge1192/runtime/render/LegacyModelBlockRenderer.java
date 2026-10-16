package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;

/** Explicit functional boundary for removed immediate block tessellation. */
public final class LegacyModelBlockRenderer {
    public void tesselateBlock(BlockGetter level, LegacyBakedModel model,
            BlockState state, BlockPos pos, PoseStack poseStack,
            VertexConsumer consumer, boolean checkSides, RandomSource random,
            long seed, int overlay, ModelData modelData, LegacyRenderType renderType) {
        throw new UnsupportedOperationException(
                "Forge 1.19.2 immediate block tessellation requires migration to the "
                        + "Minecraft 26.3 extracted block render-state pipeline");
    }
}
