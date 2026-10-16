package com.fuckingdeveloper.lms.target.neoforge263;

import com.fuckingdeveloper.lms.runtime.blockdata.BlockDataTargetLayout;
import com.fuckingdeveloper.lms.runtime.blockdata.BlockDataTargetLayout.Callback;
import com.fuckingdeveloper.lms.runtime.blockdata.BlockDataTargetLayout.Operation;
import java.util.Map;

/** Pinned native ABI; native smoke verifies these exact methods and bridge descriptors. */
public final class Target263BlockDataLayout {
    private static final BlockDataTargetLayout LAYOUT = new BlockDataTargetLayout(
            Target263SourceBindings.TARGET_ID,"net/minecraft/world/level/block/entity/BlockEntity",
            "com/fuckingdeveloper/lms/target/neoforge263/Target263Forge1192BlockDataBridge",Map.of(
                    Operation.LOAD,new Callback("loadAdditional","Lnet/minecraft/world/level/storage/ValueInput;","V"),
                    Operation.SAVE,new Callback("saveAdditional","Lnet/minecraft/world/level/storage/ValueOutput;","V"),
                    Operation.UPDATE_TAG,new Callback("getUpdateTag","Lnet/minecraft/core/HolderLookup$Provider;","Lnet/minecraft/nbt/CompoundTag;"),
                    Operation.HANDLE_UPDATE_TAG,new Callback("handleUpdateTag","Lnet/minecraft/world/level/storage/ValueInput;","V")));
    private Target263BlockDataLayout() {}
    public static BlockDataTargetLayout layout() { return LAYOUT; }
    public static void requireLayout(BlockDataTargetLayout layout) {
        if (!LAYOUT.equals(layout))
            throw new IllegalStateException("Block-data layout does not match installed target " + LAYOUT.targetId());
    }
}
