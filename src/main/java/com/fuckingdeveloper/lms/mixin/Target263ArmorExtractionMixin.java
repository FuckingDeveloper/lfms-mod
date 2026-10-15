package com.fuckingdeveloper.lms.mixin;
import com.fuckingdeveloper.lms.runtime.item.LegacyArmorItem;
import com.fuckingdeveloper.lms.runtime.render.Target263ArmorTextureBridge;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(HumanoidMobRenderer.class)
abstract class Target263ArmorExtractionMixin {
    @Inject(method="getEquipmentIfRenderable",at=@At("HEAD"),cancellable=true)
    private static void lms$sourceArmor(LivingEntity wearer,EquipmentSlot slot,CallbackInfoReturnable<ItemStack> callback) {
        if (wearer.getItemBySlot(slot).getItem() instanceof LegacyArmorItem)
            callback.setReturnValue(Target263ArmorTextureBridge.extract(wearer,slot));
    }
}
