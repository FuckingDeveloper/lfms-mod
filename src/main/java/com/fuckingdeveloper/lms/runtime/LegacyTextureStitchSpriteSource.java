package com.fuckingdeveloper.lms.runtime;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.data.AtlasIds;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;

/** Target 26.3 sprite source that executes legacy per-atlas Pre intent before stitching. */
public record LegacyTextureStitchSpriteSource(net.minecraft.resources.Identifier atlasId) implements SpriteSource {
    public static final LegacyTextureStitchSpriteSource INSTANCE = new LegacyTextureStitchSpriteSource(AtlasIds.BLOCKS);
    public static final MapCodec<LegacyTextureStitchSpriteSource> CODEC = net.minecraft.resources.Identifier.CODEC.optionalFieldOf("atlas",AtlasIds.BLOCKS).xmap(LegacyTextureStitchSpriteSource::new,LegacyTextureStitchSpriteSource::atlasId);


    public static void register(RegisterSpriteSourcesEvent event) {
        event.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(
                "lms", "legacy_texture_stitch"), CODEC);
    }

    @Override
    public void run(ResourceManager resources, Output output) {
        TextureAtlas atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(atlasId);
        for (var sprite : Forge1192EventBusBridge.collectLegacyTextureSprites(atlas)) {
            new SingleFile(sprite).run(resources, output);
        }
    }

    @Override
    public MapCodec<? extends SpriteSource> codec() {
        return CODEC;
    }
}
