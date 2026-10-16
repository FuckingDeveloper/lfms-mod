package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.minecraft.client.renderer.culling.Frustum;

import java.util.Objects;

/** Proven source-stage identities for the target subclass event model. */
public final class Forge1192RenderStageBridge {
    private Forge1192RenderStageBridge() {}

    public enum Stage {
        AFTER_SKY,
        AFTER_SOLID_BLOCKS,
        AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS,
        AFTER_CUTOUT_BLOCKS,
        AFTER_TRANSLUCENT_BLOCKS,
        AFTER_TRIPWIRE_BLOCKS,
        AFTER_PARTICLES,
        AFTER_WEATHER
    }

    public static Stage stage(RenderLevelStageEvent event) {
        Objects.requireNonNull(event, "event");
        if (event instanceof RenderLevelStageEvent.AfterSky) return Stage.AFTER_SKY;
        // Target renders all opaque chunk layers before this single callback.
        if (event instanceof RenderLevelStageEvent.AfterOpaqueBlocks) return Stage.AFTER_CUTOUT_BLOCKS;
        if (event instanceof RenderLevelStageEvent.AfterTranslucentBlocks) return Stage.AFTER_TRANSLUCENT_BLOCKS;
        if (event instanceof RenderLevelStageEvent.AfterTranslucentParticles) return Stage.AFTER_PARTICLES;
        if (event instanceof RenderLevelStageEvent.AfterWeather) return Stage.AFTER_WEATHER;
        throw new IllegalArgumentException("Target render stage has no Forge 1.19.2 equivalent: "
                + event.getClass().getName());
    }

    public static Stage stage(LegacyWorldStageEvent event){return Objects.requireNonNull(event).getStage();}
    public static Frustum frustum(LegacyWorldStageEvent event){return new Frustum(Objects.requireNonNull(event).getFrustum());}

    /** Copy the target-authoritative, already prepared culling frustum. */
    public static Frustum frustum(RenderLevelStageEvent event) {
        Objects.requireNonNull(event, "event");
        var levelState = Objects.requireNonNull(event.getLevelRenderState(), "levelRenderState");
        var cameraState = Objects.requireNonNull(levelState.cameraRenderState, "cameraRenderState");
        return new Frustum(Objects.requireNonNull(cameraState.cullFrustum, "cullFrustum"));
    }
}
