package com.fuckingdeveloper.lms.runtime;

import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

/** Semantic adapters for Forge 1.19.2 living-event contracts that changed shape. */
public final class Forge1192LivingEventBridge {
    private Forge1192LivingEventBridge() {}

    /**
     * The legacy flag controlled whether a successful block damaged the shield.
     * NeoForge now expresses that as an explicit durability amount: -1 selects
     * the vanilla calculation and zero preserves the block without durability loss.
     */
    public static void setShieldTakesDamage(LivingShieldBlockEvent event, boolean takesDamage) {
        event.setShieldDamage(takesDamage ? -1 : 0);
    }
}
