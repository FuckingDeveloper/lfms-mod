package com.fuckingdeveloper.lms.runtime.villager;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Preserves Forge 1.19.2 runtime trade factories until target consumption. */
public final class Forge1192VillagerTradeBridge {
    private static final Map<MinecraftServer,
            IdentityHashMap<VillagerProfession, Map<Integer, List<LegacyVillagerItemListing>>>>
            PENDING = new WeakHashMap<>();
    private static final Map<MinecraftServer, WanderingTrades> PENDING_WANDERING =
            new WeakHashMap<>();

    private Forge1192VillagerTradeBridge() {}

    public static synchronized void capture(
            MinecraftServer server,
            VillagerProfession profession,
            Int2ObjectMap<? extends List<?>> trades) {
        if (trades.isEmpty()) return;
        var byProfession = PENDING.computeIfAbsent(server, ignored -> new IdentityHashMap<>());
        var byLevel = byProfession.computeIfAbsent(profession, ignored -> new java.util.HashMap<>());
        for (Int2ObjectMap.Entry<? extends List<?>> entry : trades.int2ObjectEntrySet()) {
            List<LegacyVillagerItemListing> destination =
                    byLevel.computeIfAbsent(entry.getIntKey(), ignored -> new ArrayList<>());
            for (Object listing : entry.getValue()) {
                if (!(listing instanceof LegacyVillagerItemListing legacy)) {
                    throw new IllegalStateException("Legacy villager trade does not implement the "
                            + "Forge 1.19.2 ItemListing contract: " + listing.getClass().getName());
                }
                destination.add(legacy);
            }
        }
    }

    public static synchronized void requireMaterialized(
            MinecraftServer server, VillagerProfession profession, int level) {
        var byProfession = PENDING.get(server);
        if (byProfession == null) return;
        var byLevel = byProfession.get(profession);
        if (byLevel == null || byLevel.getOrDefault(level, List.of()).isEmpty()) return;
        throw new UnsupportedOperationException(
                "Forge 1.19.2 runtime villager trades are preserved for profession "
                        + profession.name().getString() + " level " + level
                        + ", but Minecraft 26.3 requires a semantic runtime-factory adapter "
                        + "for its data-driven VillagerTrade/TradeSet pipeline");
    }

    public static synchronized void captureWandering(
            MinecraftServer server, List<?> genericTrades, List<?> rareTrades) {
        List<LegacyVillagerItemListing> generic = validateListings(genericTrades, "generic");
        List<LegacyVillagerItemListing> rare = validateListings(rareTrades, "rare");
        if (generic.isEmpty() && rare.isEmpty()) return;
        PENDING_WANDERING.put(server, new WanderingTrades(generic, rare));
    }

    public static synchronized void requireWanderingMaterialized(MinecraftServer server) {
        WanderingTrades trades = PENDING_WANDERING.get(server);
        if (trades == null || trades.isEmpty()) return;
        throw new UnsupportedOperationException(
                "Forge 1.19.2 runtime wandering-trader trades are preserved, but Minecraft 26.3 "
                        + "requires a semantic runtime-factory adapter for its data-driven "
                        + "VillagerTrade/TradeSet pipeline");
    }

    private static List<LegacyVillagerItemListing> validateListings(
            List<?> listings, String category) {
        List<LegacyVillagerItemListing> validated = new ArrayList<>(listings.size());
        for (Object listing : listings) {
            if (!(listing instanceof LegacyVillagerItemListing legacy)) {
                throw new IllegalStateException("Legacy " + category + " wandering-trader trade "
                        + "does not implement the Forge 1.19.2 ItemListing contract: "
                        + listing.getClass().getName());
            }
            validated.add(legacy);
        }
        return List.copyOf(validated);
    }

    private record WanderingTrades(
            List<LegacyVillagerItemListing> generic,
            List<LegacyVillagerItemListing> rare) {
        private boolean isEmpty() {
            return generic.isEmpty() && rare.isEmpty();
        }
    }
}
