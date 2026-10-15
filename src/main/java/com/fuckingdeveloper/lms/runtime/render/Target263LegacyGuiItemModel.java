package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.*;
import org.joml.*;
import org.jspecify.annotations.Nullable;

/** Proven full-bright GUI extraction; world lighting contexts remain unsupported. */
public record Target263LegacyGuiItemModel(ModelRenderProperties properties, Matrix4fc transformation,
                                          ItemModel missing) implements ItemModel {
    private static final SpecialModelRenderer<LegacyMultiBufferSource.RecordedGeometry> SUBMIT = new SpecialModelRenderer<>() {
        public void submit(LegacyMultiBufferSource.RecordedGeometry geometry, PoseStack pose, SubmitNodeCollector output,
                int light, int overlay, boolean foil, int outline) { geometry.submit(output, pose); }
        public void getExtents(java.util.function.Consumer<Vector3fc> output) { throw new UnsupportedOperationException("GUI extents are extracted from actual source geometry"); }
        public LegacyMultiBufferSource.RecordedGeometry extractArgument(ItemStack stack) { throw new UnsupportedOperationException("GUI geometry requires the native display context"); }
    };

    @Override public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver,
            ItemDisplayContext display, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        if (display != ItemDisplayContext.GUI) {
            missing.update(output, stack, resolver, display, level, owner, seed);
            return;
        }
        var buffers = LegacyMultiBufferSource.recording(Minecraft.getInstance().getAtlasManager());
        Forge1192CustomItemBridge.extensions(stack.getItem()).getCustomRenderer().m_108829_(
                stack.copy(), display, new PoseStack(), buffers, 15728880, OverlayTexture.NO_OVERLAY);
        var geometry = buffers.finishRecording();
        installGeometry(output, stack, geometry);
    }

    public void installGeometry(ItemStackRenderState output, ItemStack stack, LegacyMultiBufferSource.RecordedGeometry geometry) {
        output.appendModelIdentityElement(this);
        // Like native SpecialModelWrapper, include the actual extracted argument.
        // Equal display properties do not identify source-selected geometry/materials.
        output.appendModelIdentityElement(geometry);
        output.appendModelIdentityElement(stack.getItem());
        output.appendModelIdentityElement(stack.getCount());
        output.appendModelIdentityElement(stack.getComponentsPatch());
        output.setAnimated();
        var layer = output.newLayer();
        layer.setExtents(geometry::extents);
        layer.setLocalTransform(transformation);
        layer.setupSpecialModel(SUBMIT, geometry);
        properties.applyToLayer(layer, ItemDisplayContext.GUI);
    }

    public record Unbaked(Identifier base) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = Identifier.CODEC.fieldOf("base").xmap(Unbaked::new, Unbaked::base);
        public void resolveDependencies(ResolvableModel.Resolver resolver) { resolver.markDependency(base); }
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transform) {
            com.mojang.logging.LogUtils.getLogger().warn("LMS source custom item model {} supports GUI extraction only; held/world contexts retain the missing model", base);
            var baker = context.blockModelBaker(); var model = baker.getModel(base);
            var properties = ModelRenderProperties.fromResolvedModel(baker, model, model.getTopTextureSlots());
            return new Target263LegacyGuiItemModel(properties, new Matrix4f(transform), context.missingItemModel(transform));
        }
        public MapCodec<? extends ItemModel.Unbaked> type() { return CODEC; }
    }
}
