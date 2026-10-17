package com.fuckingdeveloper.lms.target.neoforge263;

import java.util.Objects;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;

/** Source create(Level) is raw allocation, not spawn validation or world insertion. */
public final class Target263SourceEntityAllocation {
    private Target263SourceEntityAllocation() {}
    private static final EntitySpawnRequest RAW=new EntitySpawnRequest(EntitySpawnReason.TRIGGERED,true);
    public static Entity create(EntityType<?> type,Level level) {
        Objects.requireNonNull(type);
        if(type.getClass()!=EntityType.class)
            throw new UnsupportedOperationException("Custom source/native EntityType allocation override requires a separate contract: "+type.getClass().getName());
        return type.create(level,RAW);
    }
}
