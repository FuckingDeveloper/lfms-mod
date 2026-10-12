package com.fuckingdeveloper.lms.runtime.render;

import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Preserves Forge 1.19.2's string-keyed player renderer layer API. */
public final class Forge1192EntityRendererLayerBridge {
    private Forge1192EntityRendererLayerBridge() {}

    public static Set<String> getSkins(EntityRenderersEvent.AddLayers event) {
        Set<String> legacyNames = new LinkedHashSet<>();
        for (PlayerModelType type : event.getSkins()) {
            legacyNames.add(type == PlayerModelType.SLIM ? "slim" : "default");
        }
        return legacyNames;
    }

    @SuppressWarnings("rawtypes")
    public static LivingEntityRenderer getSkin(
            EntityRenderersEvent.AddLayers event, String legacyName) {
        return event.getPlayerRenderer(PlayerModelType.byLegacyServicesName(legacyName));
    }
}
