package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.LegacyDeferredRegister;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Source captured properties need an actual entry identity before either native constructor consumes them. */
@Mixin({Item.class,BlockItem.class})
abstract class Target263ScopedItemPropertiesMixin {
    @ModifyVariable(method="<init>",at=@At("HEAD"),argsOnly=true)
    private static Item.Properties lms$sourceConstructionProperties(Item.Properties properties) {
        return LegacyDeferredRegister.atItemConstruction(properties);
    }
}
