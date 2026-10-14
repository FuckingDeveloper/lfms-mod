package com.fuckingdeveloper.lms.runtime;

import com.google.gson.*;
import com.mojang.serialization.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.GsonHelper;

/** Semantic component JSON information; custom Gson serializers and old rich-event JSON remain unsupported. */
public final class Forge1192ComponentJsonBridge {
    private Forge1192ComponentJsonBridge() {}
    private static DynamicOps<JsonElement> ops() {
        var lookup = Target263DataComponentBindingBridge.currentRegistries();
        return lookup.<DynamicOps<JsonElement>>map(provider -> RegistryOps.create(JsonOps.INSTANCE, provider)).orElse(JsonOps.INSTANCE);
    }
    public static JsonElement toJsonTree(Component component) {
        return sourceTextArguments(ComponentSerialization.CODEC.encodeStart(ops(), component).getOrThrow(JsonParseException::new));
    }
    public static String toJson(Component component) { return toJsonTree(component).toString(); }
    public static String toStableJson(Component component) { return GsonHelper.toStableString(toJsonTree(component)); }
    public static MutableComponent fromJson(JsonElement json) {
        if (json == null || json.isJsonNull()) return null; // Verified source null information result.
        rejectOldEvents(json);
        return ComponentSerialization.CODEC.parse(ops(), sourceTextArguments(json)).getOrThrow(JsonParseException::new).copy();
    }
    public static MutableComponent fromJson(String json) {
        java.util.Objects.requireNonNull(json, "json");
        return fromJson(JsonParser.parseString(json));
    }
    /** Source Serializer writes non-component translation arguments with String.valueOf. */
    private static JsonElement sourceTextArguments(JsonElement json) {
        if (json.isJsonPrimitive()) return new JsonPrimitive(json.getAsString());
        if (json.isJsonArray()) {
            var array = new JsonArray();
            json.getAsJsonArray().forEach(value -> array.add(sourceTextArguments(value)));
            return array;
        }
        if (json.isJsonObject()) {
            var object = json.getAsJsonObject().deepCopy();
            for (String field : new String[]{"with", "extra", "separator"}) {
                if (object.has(field)) object.add(field, sourceTextArguments(object.get(field)));
            }
            return object;
        }
        return json.deepCopy();
    }
    private static void rejectOldEvents(JsonElement json) {
        if (json.isJsonObject()) {
            var object = json.getAsJsonObject();
            if (object.has("hoverEvent") || object.has("clickEvent"))
                throw new UnsupportedOperationException("Source rich component event JSON needs semantic migration");
            object.entrySet().forEach(entry -> rejectOldEvents(entry.getValue()));
        } else if (json.isJsonArray()) json.getAsJsonArray().forEach(Forge1192ComponentJsonBridge::rejectOldEvents);
    }
}
