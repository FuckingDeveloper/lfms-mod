package com.fuckingdeveloper.lms.mixin;
import com.fuckingdeveloper.lms.target.neoforge263.render.Target263ArmorTextureBridge;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(EquipmentClientInfo.Layer.class)
abstract class Target263ArmorLayerTextureMixin {
    @Shadow public abstract Identifier textureId();
    @Inject(method="getTextureLocation",at=@At("HEAD"),cancellable=true)
    private void lms$fullSourcePath(EquipmentClientInfo.LayerType type,CallbackInfoReturnable<Identifier> callback) {
        var texture = Target263ArmorTextureBridge.texture(textureId());
        if (texture != null) callback.setReturnValue(texture);
    }
}
