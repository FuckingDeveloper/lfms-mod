package com.fuckingdeveloper.lms.target.neoforge263.render;

import java.util.Optional;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

/** Verified vanilla renderer/context associations; no integration fixture selects a context. */
public final class Target263EntityBlockContexts {
    private Target263EntityBlockContexts() {}
    public static Optional<BlockDisplayContext> forEntity(Entity entity) {
        if(entity instanceof PrimedTnt)return Optional.of(TntRenderer.BLOCK_DISPLAY_CONTEXT);
        if(entity instanceof AbstractMinecart)return Optional.of(AbstractMinecartRenderer.BLOCK_DISPLAY_CONTEXT);
        return Optional.empty();
    }
}
