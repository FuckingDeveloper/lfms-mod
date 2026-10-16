package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import net.minecraft.world.item.Item;

/** Structural base for the removed pre-component ShovelItem class. */
public class LegacyShovelItem extends Item {
    public LegacyShovelItem(LegacyTier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(properties.durability(tier.m_6609_()).enchantable(tier.m_6601_()));
    }
}
