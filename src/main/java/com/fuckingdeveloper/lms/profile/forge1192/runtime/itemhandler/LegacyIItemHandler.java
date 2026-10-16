package com.fuckingdeveloper.lms.profile.forge1192.runtime.itemhandler;

import net.minecraft.world.item.ItemStack;

/** Forge 1.19.2 item capability contract retained for managed legacy code. */
public interface LegacyIItemHandler {
    int getSlots();

    ItemStack getStackInSlot(int slot);

    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

    ItemStack extractItem(int slot, int amount, boolean simulate);

    int getSlotLimit(int slot);

    boolean isItemValid(int slot, ItemStack stack);
}
