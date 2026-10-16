package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/** Raw source effect identity over the target's registered holder identity. */
public final class Forge1192MobEffectBridge {
    private Forge1192MobEffectBridge() {}

    public static Holder<MobEffect> holder(MobEffect effect) {
        var key = BuiltInRegistries.MOB_EFFECT.getResourceKey(effect).orElseThrow(() ->
                new IllegalStateException("Legacy effect is not registered: " + effect));
        return BuiltInRegistries.MOB_EFFECT.getOrThrow(key);
    }

    public static MobEffectInstance getEffect(LivingEntity entity, MobEffect effect) {
        return entity.getEffect(holder(effect));
    }

    public static boolean hasEffect(LivingEntity entity, MobEffect effect) {
        return entity.hasEffect(holder(effect));
    }

    public static MobEffectInstance removeEffectNoUpdate(LivingEntity entity, MobEffect effect) {
        return entity.removeEffectNoUpdate(holder(effect));
    }

    public static boolean removeEffect(LivingEntity entity, MobEffect effect) {
        return entity.removeEffect(holder(effect));
    }

    public static MobEffect getEffect(MobEffectInstance instance) {
        return instance.getEffect().value();
    }

    public static java.util.Map<MobEffect, MobEffectInstance> getActiveEffectsMap(LivingEntity entity) {
        throw new IllegalStateException("Mutable source raw-key effect map is not adapted to target holder keys");
    }
}
