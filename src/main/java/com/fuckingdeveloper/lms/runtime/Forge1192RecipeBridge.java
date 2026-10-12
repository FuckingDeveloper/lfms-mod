package com.fuckingdeveloper.lms.runtime;

import com.fuckingdeveloper.lms.runtime.recipe.LegacyRecipeIngredients;
import com.fuckingdeveloper.lms.runtime.recipe.LegacyRecipeResult;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import java.util.Objects;
import java.util.Collection;

/** Source-era full client recipe access backed by a co-resident server. */
public final class Forge1192RecipeBridge {
    private Forge1192RecipeBridge() {}

    public static RecipeManager clientRecipeManager(ClientPacketListener connection) {
        Objects.requireNonNull(connection, "connection");
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            throw new UnsupportedOperationException(
                    "Forge 1.19.2 full client recipe access requires a co-resident integrated server; "
                            + "Minecraft 26.3 only synchronizes a reduced RecipeAccess view");
        }
        return server.getRecipeManager();
    }

    public static Collection<Recipe<?>> legacyRecipes(RecipeManager manager) {
        Objects.requireNonNull(manager, "manager");
        java.util.ArrayList<Recipe<?>> recipes = new java.util.ArrayList<>();
        for (RecipeHolder<?> holder : manager.getRecipes()) recipes.add(holder.value());
        return java.util.List.copyOf(recipes);
    }

    public static ItemStack legacyResult(Recipe<?> recipe) {
        Objects.requireNonNull(recipe, "recipe");
        if (recipe instanceof LegacyRecipeResult legacy) return legacy.lms$legacyResult();
        var server = Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer(),
                "Cannot resolve a target recipe display without a co-resident server");
        var context = SlotDisplayContext.fromLevel(server.overworld());
        return recipe.display().stream()
                .map(display -> display.result().resolveForFirstStack(context))
                .filter(stack -> !stack.isEmpty())
                .findFirst().orElse(ItemStack.EMPTY);
    }

    public static NonNullList<Ingredient> legacyIngredients(Recipe<?> recipe) {
        Objects.requireNonNull(recipe, "recipe");
        if (recipe instanceof LegacyRecipeIngredients legacy) return legacy.lms$legacyIngredients();
        NonNullList<Ingredient> result = NonNullList.create();
        result.addAll(recipe.placementInfo().ingredients());
        return result;
    }
}
