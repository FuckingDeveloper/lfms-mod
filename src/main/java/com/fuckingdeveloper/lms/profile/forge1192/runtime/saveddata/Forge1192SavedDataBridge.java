package com.fuckingdeveloper.lms.profile.forge1192.runtime.saveddata;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import java.util.function.Function;
import java.util.function.Supplier;

/** Adapts the Forge/Minecraft 1.19.2 saved-data factory family to 26.3 codecs. */
public final class Forge1192SavedDataBridge {
    private Forge1192SavedDataBridge() {}

    public static SavedData computeIfAbsent(
            SavedDataStorage storage,
            Function<CompoundTag, ? extends SavedData> loader,
            Supplier<? extends SavedData> constructor,
            String name) {
        Codec<SavedData> codec = CompoundTag.CODEC.xmap(
                loader::apply,
                Forge1192SavedDataBridge::save);
        SavedDataType<SavedData> type = new SavedDataType<>(
                Identifier.withDefaultNamespace(name), constructor::get, codec);
        return storage.computeIfAbsent(type);
    }

    private static CompoundTag save(SavedData data) {
        if (!(data instanceof LegacySavedData legacy)) {
            throw new IllegalStateException("Legacy saved data does not expose the verified "
                    + "Forge/Minecraft 1.19.2 writer contract: " + data.getClass().getName());
        }
        return legacy.lms$saveLegacy(new CompoundTag());
    }
}
