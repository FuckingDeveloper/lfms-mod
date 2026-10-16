package com.fuckingdeveloper.lms.profile.forge1192.runtime.container;

import net.minecraft.world.inventory.Slot;

/** Source @Mutable coordinate accessors over the same native menu slot. */
public final class Forge1192SlotBridge {
    private Forge1192SlotBridge() {}
    public static void setX(Slot slot, int x) { slot.x = x; }
    public static void setY(Slot slot, int y) { slot.y = y; }
}
