package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Proven Forge 1.19.2 unbaked item-element operations on target geometry. */
public final class Forge1192UnbakedGeometryBridge {
    private Forge1192UnbakedGeometryBridge() {}

    public static List<LegacyBlockElement> createUnbakedItemElements(
            int layerIndex, TextureAtlasSprite sprite) {
        return new LegacyItemModelGenerator().m_111638_(
                layerIndex, "layer" + layerIndex, sprite);
    }

    public static List<BakedQuad> bakeElements(
            List<LegacyBlockElement> elements,
            Function<LegacyMaterial, TextureAtlasSprite> spriteGetter,
            ModelState state, Identifier debugName) {
        var bakery = new LegacyFaceBakery();
        var result = new ArrayList<BakedQuad>();
        for (LegacyBlockElement element : elements) {
            for (var entry : element.f_111310_.entrySet()) {
                Direction direction = entry.getKey();
                LegacyBlockElementFace face = entry.getValue();
                Identifier texture = Identifier.parse(face.f_111356_);
                TextureAtlasSprite sprite = spriteGetter.apply(
                        new LegacyMaterial(TextureAtlas.LOCATION_BLOCKS, texture));
                result.add(bakery.m_111600_(
                        element.f_111308_, element.f_111309_, face, sprite,
                        direction, state, element.f_111311_, element.f_111312_, debugName));
            }
        }
        return result;
    }
}
