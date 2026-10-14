package com.fuckingdeveloper.lms.runtime.itemhandler;

import net.minecraft.world.item.ItemStack;

/** Exact Forge 1.19.2 zero-slot item-handler singleton. */
public final class LegacyEmptyHandler implements LegacyIItemHandlerModifiable {
    public static final LegacyIItemHandler INSTANCE = new LegacyEmptyHandler();

    private LegacyEmptyHandler() {}

    @Override public int getSlots() { return 0; }
    @Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; }
    @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
    @Override public void setStackInSlot(int slot, ItemStack stack) {}
    @Override public int getSlotLimit(int slot) { return 0; }
    @Override public boolean isItemValid(int slot, ItemStack stack) { return false; }
}
