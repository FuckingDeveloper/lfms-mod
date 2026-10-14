package com.fuckingdeveloper.lms.runtime.itemhandler;

import net.minecraft.world.item.ItemStack;

/** Mutable extension of the Forge 1.19.2 item capability contract. */
public interface LegacyIItemHandlerModifiable extends LegacyIItemHandler {
    void setStackInSlot(int slot, ItemStack stack);
}
