package com.fuckingdeveloper.lms.profile.forge1192.runtime.resource;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;

import java.util.Objects;

/** Codec-backed target implementation of the Forge 1.19.2 JSON reload contract. */
public abstract class Forge1192SimpleJsonResourceReloadListener
        extends SimpleJsonResourceReloadListener<JsonElement> {
    private static final Codec<JsonElement> JSON_ELEMENT_CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
            element -> new Dynamic<>(JsonOps.INSTANCE, element));

    protected Forge1192SimpleJsonResourceReloadListener(Gson gson, String directory) {
        super(JSON_ELEMENT_CODEC, FileToIdConverter.json(directory));
        Objects.requireNonNull(gson, "gson");
    }
}
