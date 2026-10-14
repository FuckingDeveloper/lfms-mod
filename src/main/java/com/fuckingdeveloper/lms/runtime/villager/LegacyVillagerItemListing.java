package com.fuckingdeveloper.lms.runtime.villager;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.MerchantOffer;

/** Exact source-side Forge 1.19.2 villager offer factory contract. */
@FunctionalInterface
public interface LegacyVillagerItemListing {
    MerchantOffer m_213663_(Entity trader, RandomSource random);
}
