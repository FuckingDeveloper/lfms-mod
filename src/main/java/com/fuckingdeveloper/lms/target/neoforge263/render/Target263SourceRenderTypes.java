package com.fuckingdeveloper.lms.target.neoforge263.render;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.LegacyRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/** Verified native pipelines for source intent; names alone do not establish culling semantics. */
public final class Target263SourceRenderTypes {
    private Target263SourceRenderTypes() {}

    public static RenderType resolve(LegacyRenderType source) {
        var texture = source.texture();
        return switch (source.kind()) {
            case ENTITY_SOLID -> RenderTypes.entitySolid(texture);
            case ENTITY_CUTOUT -> RenderTypes.entityCutoutCull(texture);
            case ENTITY_TRANSLUCENT_CULL -> RenderTypes.entityTranslucentCull(texture);
            case ENTITY_CUTOUT_NO_CULL -> RenderTypes.entityCutout(texture, source.affectsOutline());
            case ENTITY_CUTOUT_NO_CULL_Z_OFFSET -> RenderTypes.entityCutoutZOffset(texture, source.affectsOutline());
            case ARMOR_CUTOUT_NO_CULL -> RenderTypes.armorCutoutNoCull(texture);
            case ENTITY_TRANSLUCENT -> RenderTypes.entityTranslucent(texture, source.affectsOutline());
            case EYES -> RenderTypes.eyes(texture);
            case OUTLINE -> RenderTypes.outline(texture);
            case CRUMBLING -> RenderTypes.crumbling(texture);
            case TEXT -> RenderTypes.text(texture);
            case TEXT_SEE_THROUGH -> RenderTypes.textSeeThrough(texture);
            case LEASH -> RenderTypes.leash();
            case WATER_MASK -> RenderTypes.waterMask();
            case LIGHTNING -> RenderTypes.lightning();
            case LINES -> RenderTypes.linesTranslucent();
            default -> throw new UnsupportedOperationException(
                    "Forge 1.19.2 render type " + source
                            + " has no standalone Minecraft 26.3 RenderType");
        };
    }
}
