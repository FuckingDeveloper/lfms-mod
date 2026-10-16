package com.fuckingdeveloper.lms.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Optional native save boundary evidence; no mutation of chunks or serialization. */
@Mixin(SerializableChunkData.class)
abstract class Target263ChunkSaveTraceMixin {
    @Inject(method="copyOf",at=@At("RETURN"))
    private static void lms$saveTrace(ServerLevel level,ChunkAccess chunk,CallbackInfoReturnable<SerializableChunkData> callback) {
        String position=System.getenv("LMS_TRACE_MODEL_POS");if(position==null)return;
        String[] coords=position.split(",");if(coords.length!=3)return;
        var pos=new net.minecraft.core.BlockPos(Integer.parseInt(coords[0]),Integer.parseInt(coords[1]),Integer.parseInt(coords[2]));
        if(!chunk.getPos().equals(new net.minecraft.world.level.ChunkPos(pos.getX() >> 4,pos.getZ() >> 4)))return;
        com.fuckingdeveloper.lms.runtime.render.Target263ModelDataTrace.log(pos,"chunk-save-"+chunk.getClass().getSimpleName(),
                "positions="+chunk.getBlockEntitiesPos()+" serialized="+callback.getReturnValue().blockEntities());
    }
}
