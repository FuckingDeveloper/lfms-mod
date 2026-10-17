package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.recipe.LegacyIngredient;

/** Exact Minecraft 1.19.2 vanilla armor-material constants and ABI. */
public enum LegacyArmorMaterials implements LegacyArmorMaterial {
    LEATHER("leather", 5, new int[]{1, 2, 3, 1}, 15,
            () -> SoundEvents.ARMOR_EQUIP_LEATHER.value(), 0.0F, 0.0F,
            () -> LegacyIngredient.of(Items.LEATHER)),
    CHAIN("chainmail", 15, new int[]{1, 4, 5, 2}, 12,
            () -> SoundEvents.ARMOR_EQUIP_CHAIN.value(), 0.0F, 0.0F,
            () -> LegacyIngredient.of(Items.IRON_INGOT)),
    IRON("iron", 15, new int[]{2, 5, 6, 2}, 9,
            () -> SoundEvents.ARMOR_EQUIP_IRON.value(), 0.0F, 0.0F,
            () -> LegacyIngredient.of(Items.IRON_INGOT)),
    GOLD("gold", 7, new int[]{1, 3, 5, 2}, 25,
            () -> SoundEvents.ARMOR_EQUIP_GOLD.value(), 0.0F, 0.0F,
            () -> LegacyIngredient.of(Items.GOLD_INGOT)),
    DIAMOND("diamond", 33, new int[]{3, 6, 8, 3}, 10,
            () -> SoundEvents.ARMOR_EQUIP_DIAMOND.value(), 2.0F, 0.0F,
            () -> LegacyIngredient.of(Items.DIAMOND)),
    TURTLE("turtle", 25, new int[]{2, 5, 6, 2}, 9,
            () -> SoundEvents.ARMOR_EQUIP_TURTLE.value(), 0.0F, 0.0F,
            () -> LegacyIngredient.of(Items.TURTLE_SCUTE)),
    NETHERITE("netherite", 37, new int[]{3, 6, 8, 3}, 15,
            () -> SoundEvents.ARMOR_EQUIP_NETHERITE.value(), 3.0F, 0.1F,
            () -> LegacyIngredient.of(Items.NETHERITE_INGOT));

    private static final int[] HEALTH_PER_SLOT = {13, 15, 16, 11};

    private final String name;
    private final int durabilityMultiplier;
    private final int[] slotProtections;
    private final int enchantmentValue;
    private final Supplier<SoundEvent> sound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<LegacyIngredient> repairIngredient;

    LegacyArmorMaterials(String name, int durabilityMultiplier, int[] slotProtections,
                         int enchantmentValue, Supplier<SoundEvent> sound, float toughness,
                         float knockbackResistance, Supplier<LegacyIngredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.slotProtections = slotProtections;
        this.enchantmentValue = enchantmentValue;
        this.sound = sound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int m_7366_(EquipmentSlot slot) {
        return HEALTH_PER_SLOT[slot.getIndex()] * durabilityMultiplier;
    }

    @Override
    public int m_7365_(EquipmentSlot slot) {
        return slotProtections[slot.getIndex()];
    }

    @Override
    public int m_6646_() {
        return enchantmentValue;
    }

    @Override
    public SoundEvent m_7344_() {
        return sound.get();
    }

    @Override
    public LegacyIngredient m_6230_() {
        return repairIngredient.get();
    }

    @Override
    public String m_6082_() {
        return name;
    }

    @Override
    public float m_6649_() {
        return knockbackResistance;
    }

    @Override
    public float m_6651_() {
        return toughness;
    }
}
