package com.fuckingdeveloper.lms.runtime.event;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/** Source-shaped Forge 1.19.2 texture-stitch lifecycle. */
public class LegacyTextureStitchEvent extends Event {
    private final TextureAtlas atlas;

    protected LegacyTextureStitchEvent(TextureAtlas atlas) {
        this.atlas = Objects.requireNonNull(atlas, "atlas");
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    public static final class Pre extends LegacyTextureStitchEvent {
        private final Set<Identifier> sprites;

        public Pre(TextureAtlas atlas, Set<Identifier> sprites) {
            super(atlas);
            this.sprites = Objects.requireNonNull(sprites, "sprites");
        }

        public Pre(TextureAtlas atlas) {
            this(atlas, new LinkedHashSet<>());
        }

        public boolean addSprite(Identifier sprite) {
            return sprites.add(Objects.requireNonNull(sprite, "sprite"));
        }

        /**
         * Minecraft 26.3 obtains atlas members from data-driven sprite sources.
         * Accept legacy additions only when those sources already materialized
         * the exact sprite; otherwise failing is safer than returning missingno.
         */
        public void requireMaterialized() {
            for (Identifier sprite : sprites) {
                Identifier actual = getAtlas().getSprite(sprite).contents().name();
                if (!sprite.equals(actual)) {
                    throw new UnsupportedOperationException(
                            "Forge 1.19.2 texture-stitch request " + sprite
                                    + " is absent from Minecraft 26.3 atlas "
                                    + getAtlas().location()
                                    + "; provide an equivalent data-driven atlas sprite source");
                }
            }
        }
    }

    public static final class Post extends LegacyTextureStitchEvent {
        public Post(TextureAtlas atlas) {
            super(atlas);
        }
    }
}
