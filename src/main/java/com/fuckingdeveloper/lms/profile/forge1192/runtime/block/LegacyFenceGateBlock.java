package com.fuckingdeveloper.lms.profile.forge1192.runtime.block;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Minecraft 1.19.2 FenceGateBlock construction shape on the 26.3 runtime. */
public class LegacyFenceGateBlock extends FenceGateBlock {
    public LegacyFenceGateBlock(BlockBehaviour.Properties properties) {
        super(properties, SoundEvents.FENCE_GATE_OPEN, SoundEvents.FENCE_GATE_CLOSE);
    }
}
