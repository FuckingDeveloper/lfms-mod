package com.fuckingdeveloper.lms.runtime.itemhandler;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Receiver-owned source mutable data, detached from target immutable component sharing. */
public final class LegacyMutableTagState {
    private CustomData owned;
    public CompoundTag acquire(ItemStack stack, boolean create) {
        CustomData current = stack.get(DataComponents.CUSTOM_DATA);
        if (current == null) {
            owned = null;
            if (!create) return null;
            current = new CustomData(new CompoundTag());
        } else if (current != owned) current = new CustomData(current.tag.copy());
        if (current != owned) { stack.set(DataComponents.CUSTOM_DATA, current); owned = current; }
        return owned.tag;
    }
    public void assign(ItemStack stack, CompoundTag tag) {
        owned = tag == null ? null : new CustomData(tag);
        if (owned == null) stack.remove(DataComponents.CUSTOM_DATA); else stack.set(DataComponents.CUSTOM_DATA, owned);
    }
    public void detachCopy(ItemStack original, ItemStack copied) {
        if (copied == original || copied.isEmpty() || !owns(original)) return;
        CustomData copiedData = copied.get(DataComponents.CUSTOM_DATA);
        if (copiedData != null && copiedData.tag == owned.tag)
            copied.set(DataComponents.CUSTOM_DATA, new CustomData(owned.tag.copy()));
    }
    public boolean owns(ItemStack stack) {
        if (owned != null && stack.get(DataComponents.CUSTOM_DATA) != owned) owned = null;
        return owned != null;
    }
    public ItemStack resourceSnapshot(ItemStack stack) {
        if (!owns(stack)) return stack;
        ItemStack copy = stack.copy(); detachCopy(stack, copy); return copy;
    }
}
