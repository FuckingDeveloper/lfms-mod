package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.target.neoforge263.Target263SourceEntityFactories;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPacketListener.class)
abstract class Target263ClientEntityFactoryMixin {
    @WrapOperation(method="createEntityFromPacket",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnRequest;)Lnet/minecraft/world/entity/Entity;"))
    private Entity lms$sourceFactory(EntityType<?> type,Level level,EntitySpawnRequest request,Operation<Entity> original,
            @Local(argsOnly=true) ClientboundAddEntityPacket packet) {
        return Target263SourceEntityFactories.create(type,level,packet,()->original.call(type,level,request));
    }
}
