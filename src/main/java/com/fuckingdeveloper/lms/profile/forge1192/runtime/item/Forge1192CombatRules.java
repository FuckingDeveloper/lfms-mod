package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

/** Verified pure source helpers; no victim/weapon context is fabricated for the new target API. */
public class Forge1192CombatRules {
    public static final float MAX_ARMOR = 20.0F, ARMOR_PROTECTION_DIVIDER = 25.0F,
            BASE_ARMOR_TOUGHNESS = 2.0F, MIN_ARMOR_RATIO = 0.2F;
    public static final float f_146688_ = MAX_ARMOR, f_146689_ = ARMOR_PROTECTION_DIVIDER,
            f_146690_ = BASE_ARMOR_TOUGHNESS, f_146691_ = MIN_ARMOR_RATIO;
    public static float m_19272_(float damage, float armor, float toughness) {
        return getDamageAfterAbsorb(damage, armor, toughness);
    }
    public static float getDamageAfterAbsorb(float damage, float armor, float toughness) {
        float divisor = BASE_ARMOR_TOUGHNESS + toughness / 4.0F;
        float effectiveArmor = net.minecraft.util.Mth.clamp(armor - damage / divisor,
                armor * MIN_ARMOR_RATIO, MAX_ARMOR);
        return damage * (1.0F - effectiveArmor / ARMOR_PROTECTION_DIVIDER);
    }
    public static float m_19269_(float damage, float protection) {
        return getDamageAfterMagicAbsorb(damage, protection);
    }
    public static float getDamageAfterMagicAbsorb(float damage, float protection) {
        return net.minecraft.world.damagesource.CombatRules.getDamageAfterMagicAbsorb(damage, protection);
    }
}
