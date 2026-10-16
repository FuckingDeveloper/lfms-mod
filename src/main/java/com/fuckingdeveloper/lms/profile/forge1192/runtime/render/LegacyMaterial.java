package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.function.Function;

/** Source-shaped Minecraft 1.19.2 atlas material identity. */
public final class LegacyMaterial {
    private final Identifier atlasLocation;
    private final Identifier texture;
    private LegacyRenderType renderType;

    public LegacyMaterial(Identifier atlasLocation, Identifier texture) {
        this.atlasLocation = Objects.requireNonNull(atlasLocation, "atlasLocation");
        this.texture = Objects.requireNonNull(texture, "texture");
    }

    public Identifier atlasLocation() {
        return atlasLocation;
    }

    public Identifier texture() {
        return texture;
    }

    public LegacyRenderType renderType(Function<Identifier, LegacyRenderType> factory) {
        if (renderType == null) renderType = Objects.requireNonNull(factory.apply(atlasLocation));
        return renderType;
    }

    public TextureAtlasSprite sprite() {
        throw unsupportedAtlasLookup();
    }

    public VertexConsumer buffer(
            LegacyMultiBufferSource buffers,
            Function<Identifier, LegacyRenderType> factory) {
        var sprite=buffers.sprite(new net.minecraft.client.resources.model.sprite.SpriteId(atlasLocation,texture));
        return new net.minecraft.client.renderer.SpriteCoordinateExpander(buffers.getBuffer(renderType(factory)),sprite);
    }

    public VertexConsumer buffer(
            LegacyMultiBufferSource buffers,
            Function<Identifier, LegacyRenderType> factory,
            boolean glint) {
        if(glint)throw new UnsupportedOperationException("Source atlas material glint requires a verified native foil submission adapter");
        return buffer(buffers,factory);
    }

    private UnsupportedOperationException unsupportedAtlasLookup() {
        return new UnsupportedOperationException(
                "Forge 1.19.2 atlas material " + atlasLocation + " / " + texture
                        + " requires a Minecraft 26.3 MaterialBaker context");
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LegacyMaterial material
                && atlasLocation.equals(material.atlasLocation)
                && texture.equals(material.texture);
    }

    @Override
    public int hashCode() {
        return 31 * atlasLocation.hashCode() + texture.hashCode();
    }

    @Override
    public String toString() {
        return "LegacyMaterial{" + atlasLocation + ", " + texture + "}";
    }
}
