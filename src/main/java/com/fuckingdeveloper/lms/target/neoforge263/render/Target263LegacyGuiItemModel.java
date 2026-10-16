package com.fuckingdeveloper.lms.target.neoforge263.render;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.Forge1192CustomItemBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.Forge1192CustomItemRenderer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.LegacyMultiBufferSource;

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

/** GUI/item-entity geometry extraction plus native late submission for proven contexts. */
public record Target263LegacyGuiItemModel(ModelRenderProperties properties, Matrix4fc transformation,
                                          ItemModel missing) implements ItemModel {
    private static final SpecialModelRenderer<LegacyMultiBufferSource.RecordedGeometry> SUBMIT = new SpecialModelRenderer<>() {
        public void submit(LegacyMultiBufferSource.RecordedGeometry geometry, PoseStack pose, SubmitNodeCollector output,
                int light, int overlay, boolean foil, int outline) {
            geometry.submit(output, pose, outline);
        }
        public void getExtents(java.util.function.Consumer<Vector3fc> output) { throw new UnsupportedOperationException("Extents are extracted from actual source geometry"); }
        public LegacyMultiBufferSource.RecordedGeometry extractArgument(ItemStack stack) { throw new UnsupportedOperationException("Source geometry requires native extraction inputs"); }
    };

    @Override public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver,
            ItemDisplayContext display, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        if (display != ItemDisplayContext.GUI && display != ItemDisplayContext.GROUND && display != ItemDisplayContext.ON_SHELF) {
            if(Target263DeferredCustomItem.supports(display)) {
                installDeferred(output,stack,display);
                return;
            }
            missing.update(output, stack, resolver, display, level, owner, seed);
            return;
        }
        int light = display == ItemDisplayContext.GUI ? 15728880
                : display == ItemDisplayContext.ON_SHELF ? Target263GroundItemExtraction.requireShelfLight(output, stack, owner)
                : Target263GroundItemExtraction.requireLight(output, stack, owner);
        var geometry = recordGeometry(Forge1192CustomItemBridge.extensions(stack.getItem()).getCustomRenderer(),
                stack, display == ItemDisplayContext.ON_SHELF ? ItemDisplayContext.FIXED : display,
                light, Minecraft.getInstance().getAtlasManager());
        installGeometry(output, stack, geometry, display);
    }

    public static LegacyMultiBufferSource.RecordedGeometry recordGeometry(Forge1192CustomItemRenderer renderer,
            ItemStack stack, ItemDisplayContext display, int light, net.minecraft.client.resources.model.sprite.SpriteGetter sprites) {
        var buffers = LegacyMultiBufferSource.recording(sprites);
        renderer.m_108829_(stack.copy(), display, new PoseStack(), buffers, light, OverlayTexture.NO_OVERLAY);
        return buffers.finishRecording();
    }

    public void installDeferred(ItemStackRenderState output,ItemStack stack,ItemDisplayContext display) {
        if(!Target263DeferredCustomItem.supports(display))throw new UnsupportedOperationException("Unsupported source custom item display: "+display);
        var argument=new Target263DeferredCustomItem.Argument(Target263CustomItemSnapshot.capture(stack),display);
        output.appendModelIdentityElement(this);output.appendModelIdentityElement(argument);
        output.setAnimated();
        var layer=output.newLayer();layer.setLocalTransform(transformation);
        layer.setExtents(Target263DeferredCustomItem::unavailableBounds);
        layer.setupSpecialModel(Target263DeferredCustomItem.INSTANCE,argument);
        properties.applyToLayer(layer,display);
    }

    public void installGeometry(ItemStackRenderState output, ItemStack stack, LegacyMultiBufferSource.RecordedGeometry geometry) {
        installGeometry(output, stack, geometry, ItemDisplayContext.GUI);
    }

    public void installGeometry(ItemStackRenderState output, ItemStack stack, LegacyMultiBufferSource.RecordedGeometry geometry, ItemDisplayContext display) {
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
        properties.applyToLayer(layer, display);
    }

    public record Unbaked(Identifier base) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = Identifier.CODEC.fieldOf("base").xmap(Unbaked::new, Unbaked::base);
        public void resolveDependencies(ResolvableModel.Resolver resolver) { resolver.markDependency(base); }
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transform) {
            com.mojang.logging.LogUtils.getLogger().warn("LMS source custom item model {} supports GUI, native item-entity GROUND, shelf FIXED projection, native outlines and deferred hand/head/fixed/nested draws; unscoped bounds remain unsupported", base);
            var baker = context.blockModelBaker(); var model = baker.getModel(base);
            var properties = ModelRenderProperties.fromResolvedModel(baker, model, model.getTopTextureSlots());
            return new Target263LegacyGuiItemModel(properties, new Matrix4f(transform), context.missingItemModel(transform));
        }
        public MapCodec<? extends ItemModel.Unbaked> type() { return CODEC; }
    }
}
