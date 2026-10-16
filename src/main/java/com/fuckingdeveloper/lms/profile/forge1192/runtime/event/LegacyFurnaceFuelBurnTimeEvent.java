package com.fuckingdeveloper.lms.profile.forge1192.runtime.event;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.Event;

/**
 * Verifier-visible Forge 1.19.2 fuel query adapted to the 26.3 item-component model.
 * Real source queries carry their recipe type; native component binding has no recipe context.
 */
public final class LegacyFurnaceFuelBurnTimeEvent extends Event implements net.neoforged.bus.api.ICancellableEvent {
    private final ItemStack itemStack;
    private final RecipeType<?> recipeType;
    private final boolean query;
    private int burnTime;
    private boolean overridden;

    public LegacyFurnaceFuelBurnTimeEvent(ItemStack itemStack, int burnTime) {
        this(itemStack, burnTime, null, false);
    }

    public LegacyFurnaceFuelBurnTimeEvent(ItemStack itemStack, int burnTime, RecipeType<?> recipeType) {
        this(itemStack, burnTime, recipeType, true);
    }

    private LegacyFurnaceFuelBurnTimeEvent(ItemStack itemStack, int burnTime, RecipeType<?> recipeType, boolean query) {
        this.recipeType = recipeType;
        this.query = query;
        this.itemStack = itemStack;
        this.burnTime = burnTime;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public RecipeType<?> getRecipeType() {
        if (query) return recipeType;
        throw new UnsupportedOperationException(
                "Minecraft 26.3 cooking fuel components are not queried with a legacy RecipeType");
    }

    public void setBurnTime(int burnTime) {
        if (burnTime >= 0) {
            this.burnTime = burnTime;
            this.overridden = true;
            setCanceled(true);
        }
    }

    public int getBurnTime() {
        return burnTime;
    }

    public boolean overridden() {
        return overridden;
    }
}
