package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.target.neoforge263.Target263SourceExplosions;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
abstract class Target263SourceExplosionMixin {
    @WrapOperation(method="explode",at=@At(value="NEW",target="(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;Lnet/minecraft/world/phys/Vec3;FZLnet/minecraft/world/level/Explosion$BlockInteraction;)Lnet/minecraft/world/level/ServerExplosion;"))
    private ServerExplosion lms$sourceMode(ServerLevel level,Entity source,DamageSource damage,ExplosionDamageCalculator calculator,
            Vec3 center,float radius,boolean fire,Explosion.BlockInteraction mode,Operation<ServerExplosion> original) {
        return Target263SourceExplosions.construct(level,mode,
                actualMode -> original.call(level,source,damage,calculator,center,radius,fire,actualMode));
    }
}
