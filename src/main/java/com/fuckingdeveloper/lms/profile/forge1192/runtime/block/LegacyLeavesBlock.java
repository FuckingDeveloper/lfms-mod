package com.fuckingdeveloper.lms.profile.forge1192.runtime.block;

import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Minecraft 1.19.2 LeavesBlock construction shape on the 26.3 runtime. */
public class LegacyLeavesBlock extends LeavesBlock {
    public LegacyLeavesBlock(BlockBehaviour.Properties properties) {
        super(AmbientLeavesBlockSoundPlayer.noAmbientSound(), properties);
    }
}
