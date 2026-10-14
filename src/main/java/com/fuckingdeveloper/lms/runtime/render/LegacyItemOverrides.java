package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Source-shaped Forge 1.19.2 dynamic item-model selector. */
public class LegacyItemOverrides {
    public static final LegacyItemOverrides f_111734_ = new LegacyItemOverrides();

    public LegacyItemOverrides() {}

    public LegacyBakedModel m_173464_(
            LegacyBakedModel original, ItemStack stack, ClientLevel level,
            LivingEntity entity, int seed) {
        return original;
    }
}
