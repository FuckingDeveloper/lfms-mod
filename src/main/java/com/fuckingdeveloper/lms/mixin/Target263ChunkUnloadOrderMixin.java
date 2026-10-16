package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.block.Forge1192ChunkUnloadBridge;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.level.*;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkMap.class)
abstract class Target263ChunkUnloadOrderMixin {
    @WrapMethod(method="lambda$scheduleUnload$0")
    private void lms$unloadTransaction(ChunkHolder holder,CompletableFuture<?> future,long pos,Operation<Void> original) {
        Forge1192ChunkUnloadBridge.transaction(()->original.call(holder,future,pos));
    }
    @WrapOperation(method="lambda$scheduleUnload$0",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ChunkMap;save(Lnet/minecraft/world/level/chunk/ChunkAccess;)Z"),require=1,allow=1)
    private boolean lms$beforeFinalSave(ChunkMap map,ChunkAccess chunk,Operation<Boolean> original) {
        Forge1192ChunkUnloadBridge.beforeSave(chunk);
        return original.call(map,chunk);
    }
}
