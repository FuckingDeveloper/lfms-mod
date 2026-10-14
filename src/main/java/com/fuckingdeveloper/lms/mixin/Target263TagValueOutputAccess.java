package com.fuckingdeveloper.lms.mixin;

import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.*;

/** Exact pinned target output context, retaining serialization ops and diagnostics. */
@Mixin(TagValueOutput.class)
public interface Target263TagValueOutputAccess {
    @Accessor("ops") DynamicOps<Tag> lms$ops();
    @Accessor("problemReporter") ProblemReporter lms$reporter();
    @Invoker("<init>") static TagValueOutput lms$withRoot(ProblemReporter reporter, DynamicOps<Tag> ops, CompoundTag tag) {
        throw new AssertionError("Native TagValueOutput constructor invoker was not applied");
    }
}
