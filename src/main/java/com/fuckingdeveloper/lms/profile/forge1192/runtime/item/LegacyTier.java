package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/** Exact structural contract of the removed Minecraft 1.19.2 Tier interface. */
public interface LegacyTier {
    int m_6609_();

    float m_6624_();

    float m_6631_();

    int m_6604_();

    int m_6601_();

    Ingredient m_6282_();

    default TagKey<Block> getTag() {
        return null;
    }
}
