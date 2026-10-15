package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.block.LegacyIForgeBlock;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/** Source child defaults must dominate the native extension inherited by Block. */
final class BlockExtensionDefaultsCheck {
    private static int calls;
    interface SourceCallbacks extends LegacyIForgeBlock {
        @Override default boolean addLandingEffects(BlockState first, ServerLevel level, BlockPos pos,
                BlockState second, LivingEntity entity, int count) { calls++; return count == 17; }
        @Override default boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
            calls++; return true;
        }
    }
    static final class Receiver implements IBlockExtension, SourceCallbacks {}
    static void run() {
        IBlockExtension receiver = new Receiver();
        calls = 0;
        if (!receiver.addLandingEffects(null,null,null,null,null,17)
                || !receiver.addRunningEffects(null,null,null,null) || calls != 2)
            throw new AssertionError("source interface defaults lost native dispatch/count/result");
        System.out.println("PASS: native block extension dispatch selects source child landing/running defaults once; no conflicting defaults or callback substitution");
    }
}
