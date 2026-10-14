package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.runtime.villager.Forge1192VillagerTradeBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Enforces the preserved legacy wandering-trade boundary at actual use. */
@Mixin(WanderingTrader.class)
abstract class Target263WanderingTraderTradeMixin {
    @Inject(method = "updateTrades", at = @At("HEAD"))
    private void lms$requireLegacyTradeMaterialization(
            ServerLevel level, CallbackInfo callback) {
        Forge1192VillagerTradeBridge.requireWanderingMaterialized(level.getServer());
    }
}
