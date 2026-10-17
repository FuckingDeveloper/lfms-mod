package com.fuckingdeveloper.lms.profile.forge1192.runtime.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;

/** Source serializer identity only; native ingredient codec registration is a separate operation. */
public interface LegacyIngredientSerializer<T extends LegacyIngredient> {
    T parse(FriendlyByteBuf buffer);
    T parse(JsonObject json);
    void write(FriendlyByteBuf buffer,T ingredient);
}
