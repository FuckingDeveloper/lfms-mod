package com.fuckingdeveloper.lms.profile.forge1192.runtime.recipe;

import net.minecraft.world.item.ItemStack;


import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/** Preserves the item-identity semantics of Forge 1.19.2 vanilla ingredients. */
public final class Forge1192IngredientBridge {
    private Forge1192IngredientBridge() {}

    public static LegacyIngredient ofStacks(ItemStack[] stacks) {
        Objects.requireNonNull(stacks, "stacks");
        return ofStackStream(Arrays.stream(stacks));
    }

    public static LegacyIngredient ofStackStream(Stream<ItemStack> stacks) {
        Objects.requireNonNull(stacks, "stacks");
        return LegacyIngredient.of(stacks);
    }

    public static ItemStack[] items(LegacyIngredient ingredient) {
        Objects.requireNonNull(ingredient, "ingredient");
        return ingredient.getItems();
    }
}
