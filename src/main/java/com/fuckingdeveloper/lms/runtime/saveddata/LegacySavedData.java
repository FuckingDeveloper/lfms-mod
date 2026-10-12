package com.fuckingdeveloper.lms.runtime.saveddata;

import net.minecraft.nbt.CompoundTag;

/** Exact Forge/Minecraft 1.19.2 saved-data writer contract. */
public interface LegacySavedData {
    CompoundTag lms$saveLegacy(CompoundTag tag);
}
