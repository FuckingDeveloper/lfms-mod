package com.fuckingdeveloper.lms.runtime;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Target adapter for item semantics that moved into default components. */
public final class Forge1192ItemBridge {
    private Forge1192ItemBridge() {}

    /** Forge 1.19.2 Item#isEdible was equivalent to having food properties. */
    public static boolean isEdible(Item item) {
        if (!item.builtInRegistryHolder().areComponentsBound()) {
            throw new IllegalStateException("Legacy Item#isEdible queried before default components were bound: "
                    + item);
        }
        return item.components().has(DataComponents.FOOD);
    }

    /** Forge 1.19.2 ItemStack#is(Item) was an exact item-identity comparison. */
    public static boolean is(ItemStack stack, Item item) {
        return stack.getItem() == item;
    }

    /** Store a source-era arbitrary stack tag in the target custom-data component. */
    public static void addTagElement(ItemStack stack, String key, Tag value) {
        getOrCreateTag(stack).put(key, value);
    }

    public static boolean hasTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.tag.isEmpty();
    }

    /** Return the live source-era tag view, not a detached target copy. */
    public static net.minecraft.nbt.CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.tag;
    }

    public static net.minecraft.nbt.CompoundTag getOrCreateTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) return data.tag;
        var tag = new net.minecraft.nbt.CompoundTag();
        stack.set(DataComponents.CUSTOM_DATA, new CustomData(tag));
        return tag;
    }

    public static net.minecraft.nbt.CompoundTag getOrCreateTagElement(
            ItemStack stack, String key) {
        var root = getOrCreateTag(stack);
        Tag current = root.get(key);
        if (current instanceof net.minecraft.nbt.CompoundTag compound) return compound;
        var compound = new net.minecraft.nbt.CompoundTag();
        root.put(key, compound);
        return compound;
    }

    public static net.minecraft.nbt.CompoundTag getTagElement(ItemStack stack, String key) {
        var root = getTag(stack);
        if (root == null) return null;
        Tag current = root.get(key);
        return current instanceof net.minecraft.nbt.CompoundTag compound ? compound : null;
    }

    public static void removeTagKey(ItemStack stack, String key) {
        var root = getTag(stack);
        if (root == null) return;
        root.remove(key);
        if (root.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
    }

    public static void setTag(ItemStack stack, net.minecraft.nbt.CompoundTag tag) {
        if (tag == null) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, new CustomData(tag));
        }
    }
}
