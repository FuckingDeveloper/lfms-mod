package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import net.minecraft.world.item.Item;

/** Structural base for the removed pre-component PickaxeItem class. */
public class LegacyPickaxeItem extends Item {
    public LegacyPickaxeItem(LegacyTier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(properties.durability(tier.m_6609_()).enchantable(tier.m_6601_()));
    }
}
