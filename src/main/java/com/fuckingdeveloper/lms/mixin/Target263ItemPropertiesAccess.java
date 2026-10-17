package com.fuckingdeveloper.lms.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.Properties.class)
public interface Target263ItemPropertiesAccess {
    @Accessor("craftingRemainingItem") net.minecraft.world.item.ItemStackTemplate lms$remainder();
    @Accessor("craftingRemainingItem") void lms$remainder(net.minecraft.world.item.ItemStackTemplate value);
    @Accessor("requiredFeatures") net.minecraft.world.flag.FeatureFlagSet lms$features();
    @Accessor("requiredFeatures") void lms$features(net.minecraft.world.flag.FeatureFlagSet value);
    @Accessor("descriptionId") net.minecraft.resources.DependantName<Item,String> lms$description();
    @Accessor("descriptionId") void lms$description(net.minecraft.resources.DependantName<Item,String> value);
    @Accessor("canCombineRepair") boolean lms$combineRepair();
    @Accessor("canCombineRepair") void lms$combineRepair(boolean value);
    @Accessor("id") ResourceKey<Item> lms$itemId();
}
