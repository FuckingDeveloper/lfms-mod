package com.fuckingdeveloper.lms.runtime.item;

import java.util.stream.StreamSupport;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.crafting.Ingredient;

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
    public Ingredient m_6282_() {
        Iterable<Holder<Item>> holders = BuiltInRegistries.ITEM.getTagOrEmpty(material.repairItems());
        return Ingredient.of(StreamSupport.stream(holders.spliterator(), false).map(Holder::value));
    }
}
