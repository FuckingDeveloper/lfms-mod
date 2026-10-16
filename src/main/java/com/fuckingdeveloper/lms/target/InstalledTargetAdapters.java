package com.fuckingdeveloper.lms.target;

import com.fuckingdeveloper.lms.runtime.blockdata.BlockDataTargetLayout;
import com.fuckingdeveloper.lms.runtime.blockdata.BlockDataContext;
import com.fuckingdeveloper.lms.target.neoforge263.Target263BlockDataStorage;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.fuckingdeveloper.lms.target.neoforge263.Target263BlockDataLayout;

/** Explicit build composition. This does not discover or guess future runtimes. */
public final class InstalledTargetAdapters {
    private InstalledTargetAdapters() {}
    public static BlockDataContext<CompoundTag, HolderLookup.Provider>
            blockDataContext(BlockEntity entity) {
        return Target263BlockDataStorage.forEntity(entity);
    }
    public static BlockDataContext<CompoundTag, HolderLookup.Provider>
            blockDataLookup(HolderLookup.Provider lookup) {
        return Target263BlockDataStorage.lookup(lookup);
    }
    public static org.objectweb.asm.Handle lifecycleWorkBridge(String sourceDescriptor) {
        return com.fuckingdeveloper.lms.target.neoforge263.Target263LifecycleWorkLayout.bridge(sourceDescriptor);
    }
    public static BlockDataTargetLayout blockDataCallbacks() { return Target263BlockDataLayout.layout(); }
}
