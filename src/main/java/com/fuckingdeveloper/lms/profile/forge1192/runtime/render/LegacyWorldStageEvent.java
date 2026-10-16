package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.neoforged.bus.api.Event;
import org.joml.Matrix4f;
import java.util.Objects;

/** Source-only frame extraction view; never posted to the native stage bus. */
public class LegacyWorldStageEvent extends Event {
    private final Forge1192RenderStageBridge.Stage stage;
    private final LevelRenderer renderer;
    private final PoseStack pose;
    private final Matrix4f projection;
    private final Integer renderTick;
    private final float partial;
    private final Camera camera;
    private final Frustum frustum;
    public LegacyWorldStageEvent(Forge1192RenderStageBridge.Stage stage,LevelRenderer renderer,PoseStack pose,Matrix4f projection,int tick,float partial,Camera camera,Frustum frustum) {
        this(stage,renderer,pose,projection,Integer.valueOf(tick),partial,camera,frustum);
    }
    private LegacyWorldStageEvent(Forge1192RenderStageBridge.Stage stage,LevelRenderer renderer,PoseStack pose,Matrix4f projection,Integer tick,float partial,Camera camera,Frustum frustum) {
        this.stage=Objects.requireNonNull(stage);this.renderer=Objects.requireNonNull(renderer);this.pose=Objects.requireNonNull(pose);
        this.projection=Objects.requireNonNull(projection);renderTick=tick;this.partial=partial;this.camera=Objects.requireNonNull(camera);this.frustum=Objects.requireNonNull(frustum);
    }
    /** Extracted target frames have no proven source render-tick counter. */
    public static LegacyWorldStageEvent forExtraction(
            Forge1192RenderStageBridge.Stage stage, LevelRenderer renderer,
            PoseStack pose, Matrix4f projection, float partial,
            Camera camera, Frustum frustum) {
        return new LegacyWorldStageEvent(stage, renderer, pose, projection,
                (Integer) null, partial, camera, frustum);
    }
    public Forge1192RenderStageBridge.Stage getStage(){return stage;}
    public LevelRenderer getLevelRenderer(){return renderer;}
    public PoseStack getPoseStack(){return pose;}
    public Matrix4f getProjectionMatrix(){return projection;}
    public int getRenderTick(){if(renderTick==null)throw new UnsupportedOperationException("Source world-render tick counter has no proven target equivalent");return renderTick;}
    public float getPartialTick(){return partial;}
    public Camera getCamera(){return camera;}
    public Frustum getFrustum(){return frustum;}
}
