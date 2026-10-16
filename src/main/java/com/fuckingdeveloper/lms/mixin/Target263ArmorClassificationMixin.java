package com.fuckingdeveloper.lms.mixin;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.LegacyArmorItem;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Classify source armor before full wearer-dependent texture extraction; avoid head-item duplication. */
@Mixin(HumanoidArmorLayer.class)
abstract class Target263ArmorClassificationMixin {
    @Inject(method="shouldRender(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;)Z",at=@At("HEAD"),cancellable=true)
    private static void lms$sourceArmorKind(ItemStack stack,EquipmentSlot slot,CallbackInfoReturnable<Boolean> callback) {
        if(stack.getItem() instanceof LegacyArmorItem) {
            var equip=stack.get(DataComponents.EQUIPPABLE);
            callback.setReturnValue(equip!=null && equip.slot()==slot);
        }
    }
}
