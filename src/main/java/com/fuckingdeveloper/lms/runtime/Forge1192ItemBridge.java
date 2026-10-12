package com.fuckingdeveloper.lms.runtime;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.tags.ItemTags;

/** Target adapter for item semantics that moved into default components. */
public final class Forge1192ItemBridge {
    private Forge1192ItemBridge() {}

    private static final ClassValue<java.util.Optional<java.lang.invoke.MethodHandle>>
            LEGACY_DESCRIPTION_ID = new ClassValue<>() {
                @Override
                protected java.util.Optional<java.lang.invoke.MethodHandle> computeValue(Class<?> type) {
                    try {
                        return java.util.Optional.of(java.lang.invoke.MethodHandles.publicLookup()
                                .findVirtual(type, "m_5671_", java.lang.invoke.MethodType.methodType(
                                        String.class, ItemStack.class)));
                    } catch (NoSuchMethodException | IllegalAccessException ignored) {
                        return java.util.Optional.empty();
                    }
                }
            };

    /** Forge 1.19.2 Item#isEdible was equivalent to having food properties. */
    public static boolean isEdible(Item item) {
        if (item.builtInRegistryHolder().areComponentsBound())
            return item.components().has(DataComponents.FOOD);
        return Forge1192EarlyItemStackBridge.create(item)
                .getComponents().has(DataComponents.FOOD);
    }

    /** Preserve the source DiggerItem type family after target class removal. */
    public static boolean isDiggerItem(Object value) {
        if (!(value instanceof Item item)) return false;
        if (item instanceof com.fuckingdeveloper.lms.runtime.item.LegacyAxeItem
                || item instanceof com.fuckingdeveloper.lms.runtime.item.LegacyHoeItem
                || item instanceof com.fuckingdeveloper.lms.runtime.item.LegacyPickaxeItem
                || item instanceof com.fuckingdeveloper.lms.runtime.item.LegacyShovelItem) {
            return true;
        }
        var holder = item.builtInRegistryHolder();
        return holder.is(ItemTags.AXES) || holder.is(ItemTags.HOES)
                || holder.is(ItemTags.PICKAXES) || holder.is(ItemTags.SHOVELS);
    }

    /** Forge 1.19.2 ItemStack#is(Item) was an exact item-identity comparison. */
    public static boolean is(ItemStack stack, Item item) {
        return stack.getItem() == item;
    }

    /**
     * Forge 1.19.2 ItemStack#getDescriptionId delegated virtually to
     * Item#getDescriptionId(ItemStack). The target removed that overload, so
     * retain managed legacy overrides and otherwise use the exact source
     * default of the item's ordinary description id.
     */
    public static String getDescriptionId(ItemStack stack) {
        return getDescriptionId(stack.getItem(), stack);
    }

    public static String getDescriptionId(Item item, ItemStack stack) {
        var legacy = LEGACY_DESCRIPTION_ID.get(item.getClass());
        if (legacy.isEmpty()) return item.getDescriptionId();
        try {
            return (String) legacy.get().invoke(item, stack);
        } catch (Throwable error) {
            throw new IllegalStateException("Legacy Item#getDescriptionId(ItemStack) failed for "
                    + item.getClass().getName(), error);
        }
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

    /** Preserve the exact Forge 1.19.2 ItemStack NBT schema. */
    public static CompoundTag save(ItemStack stack, CompoundTag destination) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        destination.putString("id", id == null ? "minecraft:air" : id.toString());
        destination.putByte("Count", (byte) stack.getCount());
        CompoundTag legacyTag = getTag(stack);
        if (legacyTag != null) destination.put("tag", legacyTag.copy());
        return destination;
    }
}
