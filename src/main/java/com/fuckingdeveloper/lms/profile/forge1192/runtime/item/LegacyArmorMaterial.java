package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.recipe.LegacyIngredient;

/**
 * Source-compatible Minecraft 1.19.2 armor-material contract.
 *
 * <p>The source type was an interface implemented by both vanilla and mod
 * materials. Minecraft 26.3 replaced it with a final record, so relocating
 * implementations to that record would be structurally invalid. This facade
 * retains the exact source ABI until an armor item is translated into the
 * target component model.</p>
 */
public interface LegacyArmorMaterial {
    int m_7366_(EquipmentSlot slot);

    int m_7365_(EquipmentSlot slot);

    int m_6646_();

    SoundEvent m_7344_();

    LegacyIngredient m_6230_();

    String m_6082_();

    float m_6649_();

    float m_6651_();
}
