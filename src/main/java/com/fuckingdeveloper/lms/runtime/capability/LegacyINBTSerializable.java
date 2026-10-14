package com.fuckingdeveloper.lms.runtime.capability;

import net.minecraft.nbt.Tag;

public interface LegacyINBTSerializable<T extends Tag> {
    T serializeNBT();
    void deserializeNBT(T tag);
}
