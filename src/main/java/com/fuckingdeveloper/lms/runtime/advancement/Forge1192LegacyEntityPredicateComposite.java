package com.fuckingdeveloper.lms.runtime.advancement;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * Fail-closed representation of the removed 1.19.2 composite player predicate.
 * The empty/ANY form is losslessly representable; non-empty legacy predicate
 * JSON requires a separate loot-condition migration.
 */
public final class Forge1192LegacyEntityPredicateComposite {
    public static final Forge1192LegacyEntityPredicateComposite ANY =
            new Forge1192LegacyEntityPredicateComposite();
    public static final Forge1192LegacyEntityPredicateComposite f_36667_ = ANY;

    private Forge1192LegacyEntityPredicateComposite() {}

    public static Forge1192LegacyEntityPredicateComposite fromJson(
            JsonObject json,
            String member,
            Forge1192LegacyDeserializationContext context) {
        JsonElement value = json.get(member);
        if (value == null || value.isJsonNull()) {
            return ANY;
        }
        throw new UnsupportedOperationException(
                "Forge 1.19.2 non-empty advancement player predicates are not adapted");
    }

    public static Forge1192LegacyEntityPredicateComposite m_36677_(
            JsonObject json,
            String member,
            Forge1192LegacyDeserializationContext context) {
        return fromJson(json, member, context);
    }

    public JsonElement toJson(Forge1192LegacySerializationContext context) {
        return JsonNull.INSTANCE;
    }
}
