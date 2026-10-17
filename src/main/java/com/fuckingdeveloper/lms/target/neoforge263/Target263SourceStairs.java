package com.fuckingdeveloper.lms.target.neoforge263;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Approved native construction policy: bind the source base once, without a dummy block. */
public final class Target263SourceStairs {
    private Target263SourceStairs() {}
    public static BlockState resolve(Supplier<BlockState> supplier) {
        return Objects.requireNonNull(Objects.requireNonNull(supplier,"Source stair base supplier").get(),"Source stair base state");
    }
    public static StairBlock create(Supplier<BlockState> supplier,BlockBehaviour.Properties properties) {
        return new StairBlock(resolve(supplier),properties);
    }
}
