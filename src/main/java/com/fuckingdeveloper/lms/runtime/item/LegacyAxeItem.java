package com.fuckingdeveloper.lms.runtime.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/** Structural base for the removed pre-component AxeItem class. */
public class LegacyAxeItem extends Item {
    public LegacyAxeItem(LegacyTier tier, float attackDamage, float attackSpeed, Properties properties) {
        super(properties.durability(tier.m_6609_()).enchantable(tier.m_6601_()));
    }

    /** SRG-shaped super target retained for legacy subclasses. */
    public InteractionResult m_6225_(UseOnContext context) {
        return super.useOn(context);
    }
}
