package com.fuckingdeveloper.lms.runtime.render;

/**
 * Structural identity for legacy baked models.
 *
 * <p>The old combined block/item rendering API has no single modern target.
 * Keeping this interface deliberately free of modern rendering promises lets
 * legacy model classes link during common registration without miscasting
 * them to either of the incompatible replacement contracts.</p>
 */
public interface LegacyBakedModel {
}
