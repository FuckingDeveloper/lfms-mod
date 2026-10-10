package com.fuckingdeveloper.lms.runtime;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.Event;

/**
 * Verifier-visible Forge 1.19.2 fuel query adapted to the 26.3 item-component model.
 * Instances are evaluated while default item components are being bound.
 */
public final class LegacyFurnaceFuelBurnTimeEvent extends Event {
    private final ItemStack itemStack;
    private int burnTime;
    private boolean overridden;

    public LegacyFurnaceFuelBurnTimeEvent(ItemStack itemStack, int burnTime) {
        this.itemStack = itemStack;
        this.burnTime = burnTime;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public RecipeType<?> getRecipeType() {
        throw new UnsupportedOperationException(
                "Minecraft 26.3 cooking fuel components are not queried with a legacy RecipeType");
    }

    public void setBurnTime(int burnTime) {
        if (burnTime >= 0) {
            this.burnTime = burnTime;
            this.overridden = true;
        }
    }

    public int getBurnTime() {
        return burnTime;
    }

    public boolean overridden() {
        return overridden;
    }
}
