package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192CraftingRemainderBridge;

/** Source overrides remain stack-sensitive and return their original mutable result. */
public interface LegacyCraftingItem {
    default boolean lms$hasCraftingRemainder(ItemStack stack) {
        return Forge1192CraftingRemainderBridge.defaultHas((Item)this, stack);
    }
    default ItemStack lms$craftingRemainder(ItemStack stack) {
        return Forge1192CraftingRemainderBridge.defaultGet((Item)this, stack);
    }
}
