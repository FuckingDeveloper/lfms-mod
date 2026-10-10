package com.fuckingdeveloper.lms.runtime;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Semantic field adapters for the Forge 1.19.2 tick-event family. */
public final class Forge1192TickEventBridge {
    /** Forge 1.19.2 represented tick timing as a field-backed enum. */
    public enum Phase { START, END }

    private Forge1192TickEventBridge() {}

    /**
     * NeoForge split each old phase-bearing event into explicit Pre and Post
     * subclasses. Reconstructing the legacy enum preserves the old handler
     * contract without manufacturing or redispatching events.
     */
    public static Phase phase(Object event) {
        if (event instanceof ServerTickEvent.Pre
                || event instanceof LevelTickEvent.Pre
                || event instanceof PlayerTickEvent.Pre
                || event instanceof ClientTickEvent.Pre) {
            return Phase.START;
        }
        if (event instanceof ServerTickEvent.Post
                || event instanceof LevelTickEvent.Post
                || event instanceof PlayerTickEvent.Post
                || event instanceof ClientTickEvent.Post) {
            return Phase.END;
        }
        throw new IllegalArgumentException("Unsupported NeoForge tick event subtype: "
                + event.getClass().getName());
    }

    public static LogicalSide side(PlayerTickEvent event) {
        return event.getEntity().level().isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER;
    }

    public static LogicalSide side(LevelTickEvent event) {
        return event.getLevel().isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER;
    }

    public static Player player(PlayerTickEvent event) {
        return event.getEntity();
    }

    public static Level level(LevelTickEvent event) {
        return event.getLevel();
    }
}
