package com.fuckingdeveloper.lms.target.neoforge263;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.block.Forge1192BlockDataBridge;
import com.fuckingdeveloper.lms.target.neoforge263.render.Target263ModelDataTrace;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Explicit source/target binding; the native storage adapter does not interpret Forge keys. */
public final class Target263Forge1192BlockDataBridge {
    private Target263Forge1192BlockDataBridge() {}

    public static void load(BlockEntity entity, ValueInput input) {
        var read = Target263BlockDataStorage.read(input);
        Target263ModelDataTrace.log(entity.getBlockPos(), "native-load", read.root());
        Forge1192BlockDataBridge.load(entity, read.root().copy(), read.context());
    }
    public static void save(BlockEntity entity, ValueOutput output) {
        var context = Target263BlockDataStorage.write(output);
        Forge1192BlockDataBridge.save(entity, context.output().buildResult(), context);
        Target263ModelDataTrace.log(entity.getBlockPos(), "native-save", context.output().buildResult());
    }
    public static CompoundTag updateTag(BlockEntity entity, HolderLookup.Provider lookup) {
        var tag = Forge1192BlockDataBridge.updateTag(entity, Target263BlockDataStorage.lookup(lookup));
        Target263ModelDataTrace.log(entity.getBlockPos(), "server-update-tag", tag);
        return tag;
    }
    public static void handleUpdateTag(BlockEntity entity, ValueInput input) {
        var read = Target263BlockDataStorage.read(input);
        Target263ModelDataTrace.log(entity.getBlockPos(), "client-handle-tag", read.root());
        Forge1192BlockDataBridge.handleUpdateTag(entity, read.root().copy(), read.context());
    }
    public static ValueInput sourceInput(BlockEntity entity, CompoundTag tag) {
        return Target263BlockDataStorage.require(Forge1192BlockDataBridge.context(entity))
                .input(Forge1192BlockDataBridge.sourceInputTag(tag));
    }
    public static ValueOutput sourceOutput(BlockEntity entity, CompoundTag tag) {
        return Target263BlockDataStorage.require(Forge1192BlockDataBridge.prepareSourceOutput(entity, tag))
                .withOutput(tag).output();
    }
}
