package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/** Adapts the proven 1.19.2 cuboid-face bake operation to 26.3 geometry. */
public final class LegacyFaceBakery {
    private static final ModelBaker.Interner IDENTITY_INTERNER = new ModelBaker.Interner() {
        @Override public Vector3fc vector(Vector3fc value) { return value; }
        @Override public BakedQuad.MaterialInfo materialInfo(BakedQuad.MaterialInfo value) { return value; }
    };

    public LegacyFaceBakery() {}

    public BakedQuad m_111600_(
            Vector3f from, Vector3f to, LegacyBlockElementFace face,
            TextureAtlasSprite sprite, Direction direction, ModelState state,
            LegacyBlockElementRotation rotation, boolean shade,
            Identifier debugName) {
        if (face == null || sprite == null || direction == null || state == null) {
            throw new IllegalArgumentException("Incomplete Forge 1.19.2 face bake: " + debugName);
        }
        BakedQuad.MaterialInfo material = BakedQuad.MaterialInfo.of(
                new Material.Baked(sprite, false), sprite.transparency(),
                face.f_111355_, null, 0, shade);
        BakedQuad quad = FaceBakery.bakeQuad(
                IDENTITY_INTERNER, from, to, face.f_111357_.target(),
                face.f_111357_.rotation(), material, direction, state,
                rotation == null ? null : rotation.target());
        return Forge1192BakedQuadBridge.rememberShade(quad, shade);
    }
}
