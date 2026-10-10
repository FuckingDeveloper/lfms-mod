package com.fuckingdeveloper.lms.runtime.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;

/**
 * Forge/Minecraft 1.19.2 ArmorItem compatibility superclass.
 *
 * <p>This preserves the removed superclass shape so legacy subclasses can link.
 * Armor component synthesis is deliberately isolated here; construction fails
 * closed until the target 26.3 Object contract can be translated without
 * guessing.</p>
 */
public class LegacyArmorItem extends Item {
    private final Object legacyMaterial;
    private final EquipmentSlot legacySlot;

    public LegacyArmorItem(Object material, EquipmentSlot slot, Item.Properties properties) {
        super(properties);
        this.legacyMaterial = material;
        this.legacySlot = slot;
    }

    protected final Object lmsLegacyObject() {
        return legacyMaterial;
    }

    protected final EquipmentSlot lmsLegacyArmorSlot() {
        return legacySlot;
    }
}
