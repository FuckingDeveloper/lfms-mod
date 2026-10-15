package com.fuckingdeveloper.lms.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.Properties.class)
public interface Target263ItemPropertiesAccess {
    @Accessor("id") ResourceKey<Item> lms$itemId();
}
