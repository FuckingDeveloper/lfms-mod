package com.fuckingdeveloper.lms.profile.forge1192.runtime.advancement;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.server.PlayerAdvancements;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/** Target 26.3 adapter for the hand-written Forge 1.19.2 criterion-trigger contract. */
public final class Forge1192CriterionBridge {
    private static final Map<Forge1192LegacyCriterionTrigger<?>, Adapter<?>> BY_TRIGGER =
            java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());
    private static final Map<net.minecraft.resources.Identifier, Adapter<?>> BY_ID =
            java.util.Collections.synchronizedMap(new LinkedHashMap<>());

    private Forge1192CriterionBridge() {}

    public static <T extends Forge1192LegacyCriterionTrigger<?>> T register(T trigger) {
        Adapter<?> adapter = new Adapter<>(trigger);
        Adapter<?> previous = BY_ID.putIfAbsent(trigger.lms$criterionId(), adapter);
        if (previous != null && previous.legacy != trigger) {
            throw new IllegalArgumentException(
                    "Duplicate Forge 1.19.2 criterion trigger " + trigger.lms$criterionId());
        }
        BY_TRIGGER.put(trigger, previous == null ? adapter : previous);
        return trigger;
    }

    public static Advancement.Builder addCriterion(
            Advancement.Builder builder,
            String name,
            Object value) {
        if (value instanceof Criterion<?> criterion) {
            return builder.addCriterion(name, criterion);
        }
        if (!(value instanceof CriterionTriggerInstance instance)) {
            throw new IllegalArgumentException(
                    "Expected a Forge 1.19.2 criterion value, got " + value.getClass().getName());
        }
        if (!(instance instanceof Forge1192LegacyCriterionInstance legacyInstance)) {
            throw new IllegalArgumentException(
                    "Expected a Forge 1.19.2 criterion instance, got " + instance.getClass().getName());
        }
        Adapter<?> adapter = BY_ID.get(legacyInstance.lms$criterionId());
        if (adapter == null) {
            throw new IllegalStateException(
                    "Forge 1.19.2 criterion trigger was not registered: "
                            + legacyInstance.lms$criterionId());
        }
        return addCriterionUnchecked(builder, name, adapter, instance);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Advancement.Builder addCriterionUnchecked(
            Advancement.Builder builder,
            String name,
            Adapter adapter,
            CriterionTriggerInstance instance) {
        return builder.addCriterion(name, adapter.createCriterion(instance));
    }

    /**
     * Reconstructs the source listener bucket from the modern player's active
     * trigger map. The source bucket then performs its original typed test and
     * awards the exact modern advancement/criterion key through the listener.
     */
    public static Object activeListeners(
            Map<?, ?> sourceCache,
            PlayerAdvancements advancements,
            Forge1192LegacyCriterionTrigger<?> trigger) {
        Adapter<?> adapter = BY_TRIGGER.get(trigger);
        if (adapter == null) {
            throw new IllegalStateException(
                    "Forge 1.19.2 criterion trigger was not registered: " + trigger.lms$criterionId());
        }
        Map<PlayerAdvancements.TriggerInstanceKey, ? extends CriterionTriggerInstance> active =
                advancements.getTriggerMapForType(adapter);
        if (active == null || active.isEmpty()) {
            return null;
        }

        Class<?> bucketType = sourceCache.values().stream()
                .filter(java.util.Objects::nonNull)
                .<Class<?>>map(Object::getClass)
                .findFirst()
                .orElseGet(() -> findBucketType(trigger.getClass()));
        try {
            Constructor<?> constructor = bucketType.getDeclaredConstructor(PlayerAdvancements.class);
            constructor.setAccessible(true);
            Object bucket = constructor.newInstance(advancements);
            Method add = bucketType.getDeclaredMethod("add", Forge1192LegacyCriterionListener.class);
            add.setAccessible(true);
            for (var entry : active.entrySet()) {
                add.invoke(bucket, new Forge1192LegacyCriterionListener<>(entry.getValue(), entry.getKey()));
            }
            return bucket;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Unsupported Forge 1.19.2 manual criterion listener bucket " + bucketType.getName(),
                    exception);
        }
    }

    private static Class<?> findBucketType(Class<?> triggerType) {
        for (Class<?> nested : triggerType.getDeclaredClasses()) {
            try {
                nested.getDeclaredConstructor(PlayerAdvancements.class);
                nested.getDeclaredMethod("add", Forge1192LegacyCriterionListener.class);
                return nested;
            } catch (NoSuchMethodException ignored) {
                // Keep looking for the structurally proven source listener bucket.
            }
        }
        throw new IllegalStateException(
                "No Forge 1.19.2 manual criterion listener bucket in " + triggerType.getName());
    }

    private static final class Adapter<T extends CriterionTriggerInstance>
            implements CriterionTrigger<T> {
        private final Forge1192LegacyCriterionTrigger<T> legacy;
        private final Codec<T> codec;

        @SuppressWarnings("unchecked")
        private Adapter(Forge1192LegacyCriterionTrigger<?> legacy) {
            this.legacy = (Forge1192LegacyCriterionTrigger<T>) legacy;
            this.codec = Codec.PASSTHROUGH.xmap(this::decode, this::encode);
        }

        @Override
        public Codec<T> codec() {
            return codec;
        }

        private T decode(Dynamic<?> dynamic) {
            JsonElement value = dynamic.convert(JsonOps.INSTANCE).getValue();
            if (!value.isJsonObject()) {
                throw new IllegalArgumentException(
                        "Forge 1.19.2 criterion payload is not a JSON object: " + value);
            }
            return legacy.lms$createInstance(
                    value.getAsJsonObject(), Forge1192LegacyDeserializationContext.INSTANCE);
        }

        private Dynamic<?> encode(T instance) {
            if (!(instance instanceof Forge1192LegacyCriterionInstance legacyInstance)) {
                throw new IllegalArgumentException(
                        "Forge 1.19.2 criterion instance does not retain source serialization: "
                                + instance.getClass().getName());
            }
            return new Dynamic<>(
                    JsonOps.INSTANCE,
                    legacyInstance.serializeToJson(Forge1192LegacySerializationContext.INSTANCE));
        }
    }
}
