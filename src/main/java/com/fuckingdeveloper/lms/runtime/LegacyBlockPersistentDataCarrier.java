package com.fuckingdeveloper.lms.runtime;

import net.minecraft.nbt.CompoundTag;

/** Native receiver ownership needed to retain the source base load identity. */
public interface LegacyBlockPersistentDataCarrier {
    void lms$sourcePersistentData(CompoundTag tag);
    CompoundTag lms$sourcePersistentData();
}
