package com.fuckingdeveloper.lms.runtime;

import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

/** Semantic adapters for Forge 1.19.2 living-event contracts that changed shape. */
public final class Forge1192LivingEventBridge {
    private Forge1192LivingEventBridge() {}

    /** Preserve source float precision while keeping the authoritative target event. */
    public static float getDistance(net.neoforged.neoforge.event.entity.living.LivingFallEvent event) {
        return (float) event.getDistance();
    }

    public static void setDistance(net.neoforged.neoforge.event.entity.living.LivingFallEvent event, float distance) {
        event.setDistance((double) distance);
    }

    public static net.neoforged.neoforge.event.entity.living.LivingFallEvent newFallEvent(
            net.minecraft.world.entity.LivingEntity entity, float distance, float multiplier) {
        return new net.neoforged.neoforge.event.entity.living.LivingFallEvent(entity, (double) distance, multiplier);
    }

    /**
     * The legacy flag controlled whether a successful block damaged the shield.
     * NeoForge now expresses that as an explicit durability amount: -1 selects
     * the vanilla calculation and zero preserves the block without durability loss.
     */
    public static void setShieldTakesDamage(LivingShieldBlockEvent event, boolean takesDamage) {
        event.setShieldDamage(takesDamage ? -1 : 0);
    }
}
