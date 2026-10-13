package com.fuckingdeveloper.lms.runtime.render;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.List;

/**
 * Structural identity for legacy baked models.
 *
 * <p>The old combined block/item rendering API has no single modern target.
 * Keeping this interface deliberately free of modern rendering promises lets
 * legacy model classes link during common registration without miscasting
 * them to either of the incompatible replacement contracts.</p>
 */
public interface LegacyBakedModel {
    List<BakedQuad> m_213637_(BlockState state, Direction direction, RandomSource random);

    default List<BakedQuad> getQuads(
            BlockState state, Direction direction, RandomSource random,
            ModelData data, LegacyRenderType renderType) {
        return m_213637_(state, direction, random);
    }

    boolean m_7541_();

    boolean m_7539_();

    boolean m_7547_();

    boolean m_7521_();

    TextureAtlasSprite m_6160_();

    default TextureAtlasSprite getParticleIcon(ModelData data) {
        return m_6160_();
    }

    LegacyItemOverrides m_7343_();
}
