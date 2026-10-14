package com.fuckingdeveloper.lms.runtime;

import com.fuckingdeveloper.lms.runtime.item.LegacyCraftingItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Source ItemStack queries delegate to source item overrides before native templates. */
public final class Forge1192CraftingRemainderBridge {
    private Forge1192CraftingRemainderBridge() {}
    public static boolean has(Item item, ItemStack stack) {
        if (item instanceof LegacyCraftingItem source) return source.lms$hasCraftingRemainder(stack);
        if (item instanceof com.fuckingdeveloper.lms.runtime.item.LegacyIForgeItem source) return source.hasCraftingRemainingItem(stack);
        return defaultHas(item,stack);
    }
    public static ItemStack get(Item item, ItemStack stack) {
        if (item instanceof LegacyCraftingItem source) return source.lms$craftingRemainder(stack);
        if (item instanceof com.fuckingdeveloper.lms.runtime.item.LegacyIForgeItem source) return source.getCraftingRemainingItem(stack);
        return defaultGet(item,stack);
    }
    public static boolean has(ItemStack stack) { return has(stack.getItem(),stack); }
    public static ItemStack get(ItemStack stack) { return get(stack.getItem(),stack); }
    public static boolean defaultHas(Item item, ItemStack stack) { return item.getCraftingRemainder()!=null; }
    public static ItemStack defaultGet(Item item, ItemStack stack) {
        if (!has(item,stack)) return ItemStack.EMPTY;
        var template=item.getCraftingRemainder();
        if (template==null) throw new IllegalStateException("Source remainder predicate requires a result override or a construction remainder");
        return template.create();
    }
}
