package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import net.minecraft.world.item.ToolMaterial;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.recipe.LegacyIngredient;

/** Explicit adapter for the removed 1.19.2 vanilla Tiers enum. */
public enum LegacyTiers implements LegacyTier {
    WOOD(0, ToolMaterial.WOOD),
    STONE(1, ToolMaterial.STONE),
    IRON(2, ToolMaterial.IRON),
    DIAMOND(3, ToolMaterial.DIAMOND),
    GOLD(0, ToolMaterial.GOLD),
    NETHERITE(4, ToolMaterial.NETHERITE);

    private final int level;
    private final ToolMaterial material;

    LegacyTiers(int level, ToolMaterial material) {
        this.level = level;
        this.material = material;
    }

    @Override public int m_6609_() { return material.durability(); }
    @Override public float m_6624_() { return material.speed(); }
    @Override public float m_6631_() { return material.attackDamageBonus(); }
    @Override public int m_6604_() { return level; }
    @Override public int m_6601_() { return material.enchantmentValue(); }

    @Override
    public LegacyIngredient m_6282_() {
        return LegacyIngredient.of(material.repairItems());
    }
}
