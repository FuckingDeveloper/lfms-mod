package com.fuckingdeveloper.lms.runtime.recipe;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.Objects;
import java.util.stream.Stream;

/** Target-side materialization boundary for legacy executable serializers. */
public final class Forge1192RecipeSerializerBridge {
    private Forge1192RecipeSerializerBridge() {}

    public static RecipeSerializer<Recipe<?>> adapt(
            Identifier id, LegacyRecipeSerializer<?> legacy) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(legacy, "legacy");
        String gap = "Forge 1.19.2 recipe serializer " + id
                + " requires recipe-id-aware JSON/network adaptation on Minecraft 26.3";

        MapCodec<Recipe<?>> mapCodec = new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.empty();
            }

            @Override
            public <T> DataResult<Recipe<?>> decode(DynamicOps<T> ops, MapLike<T> input) {
                return DataResult.error(() -> gap);
            }

            @Override
            public <T> RecordBuilder<T> encode(
                    Recipe<?> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return prefix.withErrorsFrom(DataResult.error(() -> gap));
            }

            @Override
            public String toString() {
                return "LMS legacy recipe serializer[" + id + "]";
            }
        };

        StreamCodec<RegistryFriendlyByteBuf, Recipe<?>> streamCodec = StreamCodec.of(
                (buffer, recipe) -> { throw new UnsupportedOperationException(gap); },
                buffer -> { throw new UnsupportedOperationException(gap); });
        return new RecipeSerializer<>(mapCodec, streamCodec);
    }
}
