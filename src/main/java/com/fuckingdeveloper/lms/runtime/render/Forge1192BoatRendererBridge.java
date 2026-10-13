package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.Boat;
import org.joml.Quaternionf;

/**
 * Target renderer for the Forge 1.19.2 BoatRenderer subclass contract.
 *
 * <p>The source renderer selected its texture from the live boat during
 * rendering. Target 26.3 extracts entity data before submission, so the exact
 * legacy texture decision is captured into the target render state.</p>
 */
public class Forge1192BoatRendererBridge
        extends EntityRenderer<AbstractBoat, Forge1192BoatRendererBridge.LegacyBoatRenderState> {
    private final Identifier defaultTexture;
    private final Model.Simple waterPatchModel;
    private final EntityModel<BoatRenderState> model;

    public Forge1192BoatRendererBridge(EntityRendererProvider.Context context, boolean chestBoat) {
        super(context);
        ModelLayerLocation modelLayer = chestBoat
                ? ModelLayers.OAK_CHEST_BOAT : ModelLayers.OAK_BOAT;
        this.defaultTexture = modelLayer.model()
                .withPath(path -> "textures/entity/" + path + ".png");
        this.waterPatchModel = new Model.Simple(
                context.bakeLayer(ModelLayers.BOAT_WATER_PATCH),
                ignored -> RenderTypes.waterMask());
        this.model = new BoatModel(context.bakeLayer(modelLayer));
        this.shadowRadius = 0.8F;
    }

    /** Source SRG identity for BoatRenderer#getTextureLocation(Boat). */
    public Identifier m_5478_(Boat boat) {
        return this.defaultTexture;
    }

    @Override
    public LegacyBoatRenderState createRenderState() {
        return new LegacyBoatRenderState();
    }

    @Override
    public void extractRenderState(
            AbstractBoat entity, LegacyBoatRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (!(entity instanceof Boat boat)) {
            throw new IllegalStateException(
                    "Forge 1.19.2 BoatRenderer bridge requires a Boat entity, got "
                            + entity.getClass().getName());
        }
        state.texture = this.m_5478_(boat);
        if (state.texture == null) {
            throw new IllegalStateException(
                    "Forge 1.19.2 BoatRenderer returned a null texture for "
                            + entity.getClass().getName());
        }
        state.yRot = entity.getYRot(partialTicks);
        state.hurtTime = entity.getHurtTime() - partialTicks;
        state.hurtDir = entity.getHurtDir();
        state.damageTime = Math.max(entity.getDamage() - partialTicks, 0.0F);
        state.bubbleAngle = entity.getBubbleAngle(partialTicks);
        state.isUnderWater = entity.isUnderWater();
        state.rowingTimeLeft = entity.getRowingTime(0, partialTicks);
        state.rowingTimeRight = entity.getRowingTime(1, partialTicks);
    }

    @Override
    public void submit(
            LegacyBoatRenderState state, PoseStack poseStack,
            SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.375F, 0.0F);
        poseStack.rotateDegrees(Axis.YP, 180.0F - state.yRot);
        float hurt = state.hurtTime;
        if (hurt > 0.0F) {
            poseStack.rotateDegrees(
                    Axis.XP,
                    Mth.sin(hurt) * hurt * state.damageTime / 10.0F * state.hurtDir);
        }
        if (!state.isUnderWater && !Mth.equal(state.bubbleAngle, 0.0F)) {
            poseStack.rotate(new Quaternionf().setAngleAxis(
                    state.bubbleAngle * (float) (Math.PI / 180.0),
                    1.0F, 0.0F, 1.0F));
        }
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.rotateDegrees(Axis.YP, 90.0F);
        collector.submitModel(
                this.model, state, poseStack, state.texture,
                state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        if (!state.isUnderWater) {
            collector.submitModel(
                    this.waterPatchModel, Unit.INSTANCE, poseStack, state.texture,
                    state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        }
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    public static final class LegacyBoatRenderState extends BoatRenderState {
        private Identifier texture;
    }
}
