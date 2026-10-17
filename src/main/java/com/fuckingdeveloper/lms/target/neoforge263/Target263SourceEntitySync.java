package com.fuckingdeveloper.lms.target.neoforge263;

import net.minecraft.world.entity.Entity;

/** Live target tracker flags, not detached source aliases. */
public final class Target263SourceEntitySync {
    private Target263SourceEntitySync() {}
    public static boolean getHurtMarked(Entity entity) { return entity.syncVelocity; }
    public static void setHurtMarked(Entity entity, boolean value) { entity.syncVelocity = value; }
    public static boolean getHasImpulse(Entity entity) { return entity.needsSync; }
    public static void setHasImpulse(Entity entity, boolean value) { entity.needsSync = value; }
}
