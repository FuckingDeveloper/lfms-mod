package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.villager.Forge1192VillagerTradeBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Enforces the preserved legacy runtime-trade boundary at actual use. */
@Mixin(Villager.class)
abstract class Target263VillagerTradeMixin {
    @Inject(method = "updateTrades", at = @At("HEAD"))
    private void lms$requireLegacyTradeMaterialization(
            ServerLevel level, CallbackInfo callback) {
        Villager self = (Villager) (Object) this;
        Forge1192VillagerTradeBridge.requireMaterialized(
                level.getServer(),
                self.getVillagerData().profession().value(),
                self.getVillagerData().level());
    }
}
