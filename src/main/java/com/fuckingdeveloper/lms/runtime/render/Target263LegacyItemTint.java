package com.fuckingdeveloper.lms.runtime.render;

import com.fuckingdeveloper.lms.runtime.event.LegacyRegisterColorHandlersEvent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** Native tint-source evaluation of the source generated-model layer index. */
public record Target263LegacyItemTint(int index) implements ItemTintSource {
    public static final MapCodec<Target263LegacyItemTint> CODEC = Codec.intRange(0, 4)
            .fieldOf("index").xmap(Target263LegacyItemTint::new, Target263LegacyItemTint::index);

    @Override public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        return LegacyRegisterColorHandlersEvent.Item.targetColor(stack, index);
    }
    @Override public MapCodec<? extends ItemTintSource> type() { return CODEC; }
}
