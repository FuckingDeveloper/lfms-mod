package com.fuckingdeveloper.lms.target.neoforge263;

import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public final class Target263SourceGameEvents {
    private Target263SourceGameEvents() {}
    private static Holder<GameEvent> holder(GameEvent event){return BuiltInRegistries.GAME_EVENT.wrapAsHolder(java.util.Objects.requireNonNull(event));}
    public static void gameEvent(LevelAccessor level,GameEvent event,Vec3 pos,GameEvent.Context context){level.gameEvent(holder(event),pos,context);}
    public static void gameEvent(LevelAccessor level,Entity source,GameEvent event,Vec3 pos){level.gameEvent(source,holder(event),pos);}
    public static void gameEvent(LevelAccessor level,Entity source,GameEvent event,BlockPos pos){level.gameEvent(source,holder(event),pos);}
    public static void gameEvent(LevelAccessor level,GameEvent event,BlockPos pos,GameEvent.Context context){level.gameEvent(holder(event),pos,context);}
    public static void gameEvent(Entity entity,GameEvent event,Entity source){entity.gameEvent(holder(event),source);}
    public static void gameEvent(Entity entity,GameEvent event){entity.gameEvent(holder(event));}
}
