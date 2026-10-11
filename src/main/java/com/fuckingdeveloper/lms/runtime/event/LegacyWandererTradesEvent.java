package com.fuckingdeveloper.lms.runtime.event;

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

    public void requireMaterialized() {
        if (!genericTrades.isEmpty() || !rareTrades.isEmpty()) {
            throw new UnsupportedOperationException(
                    "Forge 1.19.2 runtime wandering-trader trades were supplied, but Minecraft 26.3 "
                            + "requires data-driven VillagerTrade/TradeSet registration");
        }
    }
}
