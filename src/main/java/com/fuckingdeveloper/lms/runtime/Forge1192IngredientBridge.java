package com.fuckingdeveloper.lms.runtime;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/** Preserves the item-identity semantics of Forge 1.19.2 vanilla ingredients. */
public final class Forge1192IngredientBridge {
    private Forge1192IngredientBridge() {}

    public static Ingredient ofStacks(ItemStack[] stacks) {
        Objects.requireNonNull(stacks, "stacks");
        return ofStackStream(Arrays.stream(stacks));
    }

    public static Ingredient ofStackStream(Stream<ItemStack> stacks) {
        Objects.requireNonNull(stacks, "stacks");
        Stream<? extends ItemLike> items = stacks
                .filter(Objects::nonNull)
                .filter(stack -> !stack.isEmpty())
                .map(ItemStack::getItem);
        return Ingredient.of(items);
    }

    public static ItemStack[] items(Ingredient ingredient) {
        Objects.requireNonNull(ingredient, "ingredient");
        return ingredient.items().map(ItemStack::new).toArray(ItemStack[]::new);
    }
}
