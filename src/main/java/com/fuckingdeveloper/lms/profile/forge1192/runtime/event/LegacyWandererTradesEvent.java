package com.fuckingdeveloper.lms.profile.forge1192.runtime.event;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.villager.Forge1192VillagerTradeBridge;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.List;

/** Source-shaped Forge 1.19.2 wandering-trader mutation event. */
public final class LegacyWandererTradesEvent extends Event {
    private final List<Object> genericTrades = new ArrayList<>();
    private final List<Object> rareTrades = new ArrayList<>();

    public List<Object> getGenericTrades() {
        return genericTrades;
    }

    public List<Object> getRareTrades() {
        return rareTrades;
    }

    public void preserveForTargetConsumption(MinecraftServer server) {
        Forge1192VillagerTradeBridge.captureWandering(server, genericTrades, rareTrades);
    }
}
