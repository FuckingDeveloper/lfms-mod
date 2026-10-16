package com.fuckingdeveloper.lms.profile.forge1192.runtime.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.ValidationContextSource;

/** Target-valid instance base retaining the source trigger identity and player predicate intent. */
public abstract class Forge1192LegacyCriterionInstance implements CriterionTriggerInstance {
    private final Identifier criterion;
    private final Forge1192LegacyEntityPredicateComposite player;

    protected Forge1192LegacyCriterionInstance(
            Identifier criterion,
            Forge1192LegacyEntityPredicateComposite player) {
        this.criterion = criterion;
        this.player = player;
    }

    public final Identifier lms$criterionId() {
        return criterion;
    }

    public JsonObject serializeToJson(Forge1192LegacySerializationContext context) {
        JsonObject json = new JsonObject();
        json.add("player", player.toJson(context));
        return json;
    }

    public JsonObject m_7683_(Forge1192LegacySerializationContext context) {
        return serializeToJson(context);
    }

    @Override
    public final void validate(ValidationContextSource context) {
        // Non-empty source predicates fail during decoding. ANY has no target
        // validation work and is therefore lossless here.
    }
}
