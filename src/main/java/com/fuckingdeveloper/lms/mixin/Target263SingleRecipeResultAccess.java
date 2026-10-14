package com.fuckingdeveloper.lms.mixin;

import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Actual immutable native result, independent of display resolution/world context. */
@Mixin(SingleItemRecipe.class)
public interface Target263SingleRecipeResultAccess {
    @Accessor("result") ItemStackTemplate lms$nativeResult();
}
