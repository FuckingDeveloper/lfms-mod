package com.fuckingdeveloper.lms.profile.forge1192.runtime.itemhandler;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Receiver-owned source mutable data, detached from target immutable component sharing. */
public final class LegacyMutableTagState {
    private CustomData owned;
    private Integer lastDamage;
    private static net.minecraft.core.component.DataComponentMap components(ItemStack stack) {
        if ((Object) stack instanceof LegacyMutableTagCarrier carrier) return carrier.lms$sourceComponents();
        // The native public map is authoritative for nonempty stacks, including
        // isolated non-FML probes. Only empty visibility needs the target accessor.
        if (!stack.isEmpty()) return stack.getComponents();
        throw new IllegalStateException("Source empty stack component access mixin was not applied");
    }
    public CompoundTag acquire(ItemStack stack, boolean create) {
        CustomData current = components(stack).get(DataComponents.CUSTOM_DATA);
        int nativeDamage = components(stack).getOrDefault(DataComponents.DAMAGE, 0);
        if (current == null) {
            owned = null;
            if (!create && nativeDamage == 0) { lastDamage = nativeDamage; return null; }
            current = new CustomData(new CompoundTag());
        } else if (current != owned) current = new CustomData(current.tag.copy());
        if (current != owned) {
            if (!current.tag.contains("Damage") && nativeDamage != 0) current.tag.putInt("Damage", nativeDamage);
        } else if (lastDamage != null && lastDamage != nativeDamage) {
            // A later native component write supersedes the previously observed mirror.
            if (nativeDamage == 0) current.tag.remove("Damage");
            else current.tag.putInt("Damage", nativeDamage);
        }
        if (current != owned) { stack.set(DataComponents.CUSTOM_DATA, current); owned = current; }
        lastDamage = nativeDamage;
        return owned.tag;
    }
    public void assign(ItemStack stack, CompoundTag tag) {
        owned = tag == null ? null : new CustomData(tag);
        if (owned == null) stack.remove(DataComponents.CUSTOM_DATA); else stack.set(DataComponents.CUSTOM_DATA, owned);
        mirrorDamage(stack, tag == null ? 0 : tag.getIntOr("Damage", 0));
    }
    /** Source raw damage may exceed max; the native component mirror has a bounded representation. */
    public void mirrorDamage(ItemStack stack, int sourceDamage) {
        int max = components(stack).getOrDefault(DataComponents.MAX_DAMAGE, 0);
        int value = Math.clamp(sourceDamage, 0, max);
        boolean present = components(stack).has(DataComponents.DAMAGE);
        if ((present || max > 0) && (!present || components(stack).getOrDefault(DataComponents.DAMAGE, 0) != value))
            stack.set(DataComponents.DAMAGE, value);
        lastDamage = components(stack).getOrDefault(DataComponents.DAMAGE, 0);
    }
    public void detachCopy(ItemStack original, ItemStack copied) {
        if (copied == original || copied.isEmpty() || !owns(original)) return;
        CustomData copiedData = copied.get(DataComponents.CUSTOM_DATA);
        if (copiedData != null && copiedData.tag == owned.tag)
            copied.set(DataComponents.CUSTOM_DATA, new CustomData(owned.tag.copy()));
    }
    public boolean owns(ItemStack stack) {
        if (owned != null && components(stack).get(DataComponents.CUSTOM_DATA) != owned) owned = null;
        return owned != null;
    }
    public ItemStack resourceSnapshot(ItemStack stack) {
        if (!owns(stack)) return stack;
        ItemStack copy = stack.copy(); detachCopy(stack, copy); return copy;
    }
}
