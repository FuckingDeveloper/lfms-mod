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
    public static String throwableItemBase() { return "net/minecraft/world/entity/projectile/throwableitemprojectile/ThrowableItemProjectile"; }
    public static String throwableItemAdapter() { return "com/fuckingdeveloper/lms/target/neoforge263/entity/Target263Forge1192ThrowableItemProjectile"; }
    public static String registryQueriesBridge() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263SourceRegistryQueries"; }
    public static String configSpecOwner() { return com.fuckingdeveloper.lms.target.neoforge263.Target263ConfigLayout.NATIVE; }
    public static String configValuesBridge() { return "com/fuckingdeveloper/lms/target/neoforge263/Target263SourceConfigValues"; }
    public static boolean hasPublicConfigMethod(String owner, String name, String descriptor) {
        return com.fuckingdeveloper.lms.target.neoforge263.Target263ConfigLayout.hasMethod(owner, name, descriptor);
    }
    public static BlockDataTargetLayout blockDataCallbacks() { return Target263BlockDataLayout.layout(); }
}
