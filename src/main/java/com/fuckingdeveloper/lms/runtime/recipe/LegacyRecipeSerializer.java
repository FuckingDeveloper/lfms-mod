package com.fuckingdeveloper.lms.runtime.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;

/** Exact executable contract of the Forge 1.19.2 RecipeSerializer interface. */
public interface LegacyRecipeSerializer<T extends Recipe<?>> {
    T m_6729_(Identifier id, JsonObject json);

    T m_8005_(Identifier id, FriendlyByteBuf buffer);

    void m_6178_(FriendlyByteBuf buffer, T recipe);
}
