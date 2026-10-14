package com.fuckingdeveloper.lms.runtime.item;

import net.minecraft.world.item.Item;

/** Structural base for the removed pre-component SwordItem class. */
public class LegacySwordItem extends Item {
    public LegacySwordItem(LegacyTier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(properties.durability(tier.m_6609_()).enchantable(tier.m_6601_()));
    }
}
