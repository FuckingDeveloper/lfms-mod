package com.fuckingdeveloper.lms.runtime;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Predicate;

/** Construction adapters for the post-1.19 villager profession data model. */
public final class Forge1192VillagerBridge {
    private Forge1192VillagerBridge() {}

    public static VillagerProfession createProfession(
            String name,
            Predicate<Holder<PoiType>> heldJobSite,
            Predicate<Holder<PoiType>> acquirableJobSite,
            ImmutableSet<Item> requestedItems,
            ImmutableSet<Block> secondaryPoi,
            SoundEvent workSound) {
        return new VillagerProfession(
                Component.literal(name),
                heldJobSite,
                acquirableJobSite,
                requestedItems,
                secondaryPoi,
                workSound,
                Int2ObjectMap.ofEntries());
    }
}
