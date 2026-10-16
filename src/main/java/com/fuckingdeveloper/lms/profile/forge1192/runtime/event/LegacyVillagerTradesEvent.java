package com.fuckingdeveloper.lms.profile.forge1192.runtime.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.Event;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.villager.Forge1192VillagerTradeBridge;

import java.util.List;
import java.util.Objects;

/** Source-shaped Forge 1.19.2 villager-trade mutation event. */
public final class LegacyVillagerTradesEvent extends Event {
    private final VillagerProfession type;
    private final Int2ObjectMap<List<Object>> trades = new Int2ObjectOpenHashMap<>();

    public LegacyVillagerTradesEvent(VillagerProfession type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    public VillagerProfession getType() {
        return type;
    }

    public Int2ObjectMap<List<Object>> getTrades() {
        return trades;
    }

    public void preserveForTargetConsumption(MinecraftServer server) {
        Forge1192VillagerTradeBridge.capture(server, type, trades);
    }
}
