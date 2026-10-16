package com.fuckingdeveloper.lms.profile.forge1192.runtime.block;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192VanillaConstructionBridge;

import com.mojang.datafixers.types.Type;
import java.util.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.*;

/** The verified source builder stages its factory and valid-block set until build. */
public final class LegacyBlockEntityTypeBuilder<T extends BlockEntity> {
    private final BlockEntityType.BlockEntitySupplier<? extends T> factory;
    private final Set<Block> blocks;
    private LegacyBlockEntityTypeBuilder(BlockEntityType.BlockEntitySupplier<? extends T> factory,Block[] blocks) {
        this.factory=Objects.requireNonNull(factory);this.blocks=Set.copyOf(Arrays.asList(blocks));
    }
    public static <T extends BlockEntity> LegacyBlockEntityTypeBuilder<T> m_155273_(BlockEntityType.BlockEntitySupplier<? extends T> factory,Block... blocks){return new LegacyBlockEntityTypeBuilder<>(factory,blocks);}
    @SuppressWarnings("unchecked")
    public BlockEntityType<T> m_58966_(Type<?> dataFixerType){return (BlockEntityType<T>)Forge1192VanillaConstructionBridge.blockEntityType(factory,blocks,dataFixerType);}
}
