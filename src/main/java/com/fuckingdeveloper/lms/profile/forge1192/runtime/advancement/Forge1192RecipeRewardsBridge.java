package com.fuckingdeveloper.lms.profile.forge1192.runtime.advancement;

import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class Forge1192RecipeRewardsBridge {
    private Forge1192RecipeRewardsBridge() {}
    public static AdvancementRewards.Builder recipe(Identifier id) {
        return AdvancementRewards.Builder.recipe(ResourceKey.create(Registries.RECIPE, java.util.Objects.requireNonNull(id)));
    }
    public static AdvancementRewards.Builder addRecipe(AdvancementRewards.Builder builder, Identifier id) {
        return java.util.Objects.requireNonNull(builder).addRecipe(ResourceKey.create(Registries.RECIPE, java.util.Objects.requireNonNull(id)));
    }
}
