package com.fuckingdeveloper.lms.profile.forge1192.runtime.data;

import net.minecraft.nbt.*;

/** Source typed reads require the exact tag type; native numeric conversion is broader. */
public final class Forge1192ListTagBridge {
    private Forge1192ListTagBridge() {}
    private static Tag value(ListTag list, int index) { return index >= 0 && index < list.size() ? list.get(index) : null; }
    public static CompoundTag getCompound(ListTag list, int index) { return list.getCompoundOrEmpty(index); }
    public static ListTag getList(ListTag list, int index) { return list.getListOrEmpty(index); }
    public static short getShort(ListTag list, int index) { return value(list,index) instanceof ShortTag tag ? tag.shortValue() : 0; }
    public static int getInt(ListTag list, int index) { return value(list,index) instanceof IntTag tag ? tag.intValue() : 0; }
    public static double getDouble(ListTag list, int index) { return value(list,index) instanceof DoubleTag tag ? tag.doubleValue() : 0; }
    public static float getFloat(ListTag list, int index) { return value(list,index) instanceof FloatTag tag ? tag.floatValue() : 0; }
    public static int[] getIntArray(ListTag list, int index) { return list.getIntArray(index).orElseGet(() -> new int[0]); }
    public static long[] getLongArray(ListTag list, int index) { return list.getLongArray(index).orElseGet(() -> new long[0]); }
    public static String getString(ListTag list, int index) {
        Tag tag = value(list,index);
        return tag == null ? "" : tag instanceof StringTag ? tag.asString().orElseThrow() : tag.toString();
    }
    public static byte getElementType(ListTag list) {
        if (list.isEmpty()) return Tag.TAG_END;
        byte type = list.getFirst().getId();
        for (Tag tag : list) if (tag.getId() != type)
            throw new UnsupportedOperationException("A heterogeneous native list has no source element type");
        return type;
    }
}
