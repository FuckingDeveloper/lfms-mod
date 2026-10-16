package com.fuckingdeveloper.lms.profile.forge1192.runtime.block;

import net.neoforged.bus.api.*;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.minecraft.world.level.chunk.*;

/** Source server unload notification belongs before final save; target notification stays after. */
public final class Forge1192ChunkUnloadBridge {
    private static final IEventBus SOURCE_PHASE=BusBuilder.builder().build();
    static {SOURCE_PHASE.start();}
    private static final class Frame {ChunkAccess delivered;}
    private static final ThreadLocal<Frame> CURRENT=new ThreadLocal<>();
    private Forge1192ChunkUnloadBridge(){}
    public static IEventBus sourceBus(){return SOURCE_PHASE;}
    public static void transaction(Runnable operation) {
        var previous=CURRENT.get();CURRENT.set(new Frame());
        try{operation.run();}finally{if(previous==null)CURRENT.remove();else CURRENT.set(previous);}
    }
    public static void beforeSave(ChunkAccess chunk) {
        if(!(chunk instanceof LevelChunk levelChunk))return;
        var frame=CURRENT.get();if(frame==null)throw new IllegalStateException("Source unload save outside target transaction");
        if(frame.delivered!=null)throw new IllegalStateException("Duplicate final save in target unload transaction");
        frame.delivered=chunk;
        SOURCE_PHASE.post(new ChunkEvent.Unload(levelChunk));
    }
    public static boolean alreadyDelivered(Event event) {
        var frame=CURRENT.get();
        return frame!=null && event instanceof ChunkEvent.Unload unload && unload.getChunk()==frame.delivered;
    }
}
