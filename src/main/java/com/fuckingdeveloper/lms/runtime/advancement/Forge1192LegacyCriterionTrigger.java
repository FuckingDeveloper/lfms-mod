package com.fuckingdeveloper.lms.runtime.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.resources.Identifier;

/** Source-side contract retained for hand-written Forge 1.19.2 criterion triggers. */
public interface Forge1192LegacyCriterionTrigger<T extends CriterionTriggerInstance> {
    Identifier lms$criterionId();

    T lms$createInstance(JsonObject json, Forge1192LegacyDeserializationContext context);
}
