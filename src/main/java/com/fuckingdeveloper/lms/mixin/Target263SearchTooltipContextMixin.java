package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192SideBridge;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.List;
import net.minecraft.client.multiplayer.SessionSearchTrees;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Both creative and recipe search use this pinned client tooltip extraction site. */
@Mixin(SessionSearchTrees.class)
abstract class Target263SearchTooltipContextMixin {
    @WrapOperation(method="lambda$getTooltipLines$0",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ItemStack;getTooltipLines(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;"),require=1,allow=1)
    private static List<Component> lms$searchTooltip(ItemStack stack,Item.TooltipContext context,Player player,TooltipFlag flag,Operation<List<Component>> original){
        return Forge1192SideBridge.clientTooltip(()->original.call(stack,context,player,flag));
    }
}
