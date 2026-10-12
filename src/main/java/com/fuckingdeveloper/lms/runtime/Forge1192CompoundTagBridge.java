package com.fuckingdeveloper.lms.runtime;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/** Restores Forge 1.19.2's default-valued CompoundTag read contract. */
public final class Forge1192CompoundTagBridge {
    private static final int ANY_NUMERIC_TAG = 99;

    private Forge1192CompoundTagBridge() {}

    public static byte getTagType(CompoundTag tag, String key) {
        Tag value = tag.get(key);
        return value == null ? Tag.TAG_END : value.getId();
    }

    public static boolean contains(CompoundTag tag, String key, int expectedType) {
        byte actualType = getTagType(tag, key);
        if (actualType == expectedType) return true;
        return expectedType == ANY_NUMERIC_TAG
                && actualType >= Tag.TAG_BYTE && actualType <= Tag.TAG_DOUBLE;
    }

    public static byte getByte(CompoundTag tag, String key) {
        return tag.getByteOr(key, (byte) 0);
    }

    public static short getShort(CompoundTag tag, String key) {
        return tag.getShortOr(key, (short) 0);
    }

    public static int getInt(CompoundTag tag, String key) {
        return tag.getIntOr(key, 0);
    }

    public static long getLong(CompoundTag tag, String key) {
        return tag.getLongOr(key, 0L);
    }

    public static float getFloat(CompoundTag tag, String key) {
        return tag.getFloatOr(key, 0.0F);
    }

    public static double getDouble(CompoundTag tag, String key) {
        return tag.getDoubleOr(key, 0.0D);
    }

    public static String getString(CompoundTag tag, String key) {
        return tag.getStringOr(key, "");
    }

    public static byte[] getByteArray(CompoundTag tag, String key) {
        return tag.getByteArray(key).orElseGet(() -> new byte[0]);
    }

    public static int[] getIntArray(CompoundTag tag, String key) {
        return tag.getIntArray(key).orElseGet(() -> new int[0]);
    }

    public static long[] getLongArray(CompoundTag tag, String key) {
        return tag.getLongArray(key).orElseGet(() -> new long[0]);
    }

    public static CompoundTag getCompound(CompoundTag tag, String key) {
        return tag.getCompoundOrEmpty(key);
    }

    public static ListTag getList(CompoundTag tag, String key, int expectedElementType) {
        ListTag list = tag.getListOrEmpty(key);
        if (!list.isEmpty() && list.getFirst().getId() != expectedElementType) return new ListTag();
        return list;
    }

    public static boolean getBoolean(CompoundTag tag, String key) {
        return tag.getBooleanOr(key, false);
    }

    /** Forge 1.19.2 discarded the removed value. */
    public static void remove(CompoundTag tag, String key) {
        tag.remove(key);
    }
}
