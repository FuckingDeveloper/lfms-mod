package com.fuckingdeveloper.lms.mixin;
import com.fuckingdeveloper.lms.target.neoforge263.render.Target263ArmorTextureBridge;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(EquipmentAssetManager.class)
abstract class Target263ArmorAssetsMixin {
    @Inject(method="get",at=@At("RETURN"),cancellable=true)
    private void lms$derivedAsset(ResourceKey<EquipmentAsset> key,CallbackInfoReturnable<EquipmentClientInfo> callback) {
        if (callback.getReturnValue() == EquipmentAssetManager.MISSING) {
            var asset = Target263ArmorTextureBridge.asset(key);
            if (asset != null) callback.setReturnValue(asset);
        }
    }
}
