package com.fuckingdeveloper.lms.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TagValueInput.class)
public interface Target263TagValueInputAccess {
    @Accessor("input") CompoundTag lms$root();
    @Accessor("problemReporter") ProblemReporter lms$reporter();
}
