package com.fuckingdeveloper.lms.runtime.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/**
 * NeoForge's break event retained construction and cancellation semantics but
 * removed the mutable legacy experience field. Keep that field on an event
 * subclass so legacy producers and listeners share the same posted object.
 */
public final class LegacyBreakBlockEvent extends BreakBlockEvent {
    private int expToDrop;

    public LegacyBreakBlockEvent(Level level, BlockPos pos, BlockState state, Player player) {
        super(level, pos, state, player);
    }

    public int getExpToDrop() {
        return expToDrop;
    }

    public void setExpToDrop(int expToDrop) {
        this.expToDrop = expToDrop;
    }
}
