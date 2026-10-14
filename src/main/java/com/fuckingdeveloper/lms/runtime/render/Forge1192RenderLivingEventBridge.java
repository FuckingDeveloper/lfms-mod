package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

import java.util.Objects;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** Restores the exact rendered-entity view removed from target render events. */
public final class Forge1192RenderLivingEventBridge {
    private static final Map<LivingEntityRenderState, LivingEntity> EXACT_EVENT_ENTITIES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private Forge1192RenderLivingEventBridge() {}

    /**
     * Captures the authoritative association while the target renderer still
     * owns both the live entity and the extracted state. Target render events
     * retain only the latter, so reconstructing identity afterwards is not
     * always possible (two entities may have identical extracted properties).
     */
    public static void capture(LivingEntity entity, LivingEntityRenderState state) {
        EXACT_EVENT_ENTITIES.put(
                Objects.requireNonNull(state, "renderState"),
                Objects.requireNonNull(entity, "entity"));
    }

    public static LivingEntity entity(Object rawEvent) {
        if (!(rawEvent instanceof RenderLivingEvent<?, ?, ?> event)) {
            throw new IllegalArgumentException("Not a target render-living event: " + rawEvent);
        }
        LivingEntityRenderState state = Objects.requireNonNull(event.getRenderState(), "renderState");
        return entity(state, event.getPartialTick());
    }

    public static LivingEntity entity(LivingEntityRenderState state, float partialTick) {
        Objects.requireNonNull(state, "renderState");
        var level = Objects.requireNonNull(Minecraft.getInstance().level, "client level");
        LivingEntity exact = EXACT_EVENT_ENTITIES.get(state);
        if (exact != null && exact.level() == level && exact.getType() == state.entityType) {
            return exact;
        }
        LivingEntity match = null;
        for (var candidate : level.entitiesForRendering()) {
            if (!(candidate instanceof LivingEntity living)
                    || !matches(living, state, partialTick)) continue;
            Vec3 position = living.getPosition(partialTick);
            if (Double.compare(position.x, state.x) != 0
                    || Double.compare(position.y, state.y) != 0
                    || Double.compare(position.z, state.z) != 0) continue;
            if (match != null) {
                throw new IllegalStateException("Ambiguous source entity for target render state "
                        + state.entityType + " at " + state.x + "," + state.y + "," + state.z);
            }
            match = living;
        }
        if (match == null) {
            throw new IllegalStateException("No source entity for target render state "
                    + state.entityType + " at " + state.x + "," + state.y + "," + state.z);
        }
        EXACT_EVENT_ENTITIES.put(state, match);
        return match;
    }

    private static boolean matches(
            LivingEntity entity, LivingEntityRenderState state, float partialTick) {
        float eyeHeight = state.bedOrientation == null
                ? entity.getEyeHeight() : entity.getEyeHeight(Pose.STANDING);
        return entity.getType() == state.entityType
                && Float.compare(entity.tickCount + partialTick, state.ageInTicks) == 0
                && Float.compare(entity.getBbWidth(), state.boundingBoxWidth) == 0
                && Float.compare(entity.getBbHeight(), state.boundingBoxHeight) == 0
                && Float.compare(eyeHeight, state.eyeHeight) == 0
                && Float.compare(entity.getScale(), state.scale) == 0
                && Float.compare(entity.getAgeScale(), state.ageScale) == 0
                && entity.isInvisible() == state.isInvisible
                && entity.isDiscrete() == state.isDiscrete
                && entity.getPose() == state.pose
                && Objects.equals(entity.getBedOrientation(), state.bedOrientation)
                && entity.isFullyFrozen() == state.isFullyFrozen
                && entity.isBaby() == state.isBaby
                && entity.isAutoSpinAttack() == state.isAutoSpinAttack
                && (entity.hurtTime > 0 || entity.deathTime > 0) == state.hasRedOverlay;
    }
}
