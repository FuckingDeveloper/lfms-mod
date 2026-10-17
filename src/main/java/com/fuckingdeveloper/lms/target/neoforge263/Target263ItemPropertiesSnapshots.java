package com.fuckingdeveloper.lms.target.neoforge263;

import com.fuckingdeveloper.lms.mixin.Target263ItemPropertiesAccess;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192VanillaConstructionBridge;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

/** Snapshot captured source properties at construction, without mutating a reused source builder. */
public final class Target263ItemPropertiesSnapshots {
    private Target263ItemPropertiesSnapshots() {}
    public static Item.Properties withId(Item.Properties properties,ResourceKey<Item> id) {
        if(properties.getClass()!=Item.Properties.class)throw new UnsupportedOperationException("Source deferred Item.Properties subclass requires a proven snapshot contract");
        var result=new Item.Properties().setId(id);
        var source=(Target263ItemPropertiesAccess)(Object)properties;var target=(Target263ItemPropertiesAccess)(Object)result;
        result.componentInitializer=properties.componentInitializer;
        target.lms$remainder(source.lms$remainder());target.lms$features(source.lms$features());
        target.lms$description(source.lms$description());target.lms$combineRepair(source.lms$combineRepair());
        // Native model is final ResourceKey::identifier, with no mutable source counterpart.
        Forge1192VanillaConstructionBridge.copyItemPropertiesIntent(properties,result);
        return result;
    }
}
