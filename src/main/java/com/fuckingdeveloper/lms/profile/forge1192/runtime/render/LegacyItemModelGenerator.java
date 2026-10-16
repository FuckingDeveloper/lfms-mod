package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * Exact Forge/Minecraft 1.19.2 generated-item element algorithm backed by the
 * target sprite-content API. The resulting source-shaped elements continue
 * through {@link LegacyFaceBakery}, keeping generation separate from the 26.3
 * baked-geometry representation.
 */
public final class LegacyItemModelGenerator {
    public LegacyItemModelGenerator() {}

    public List<LegacyBlockElement> m_111638_(
            int tintIndex, String texture, TextureAtlasSprite sprite) {
        var faces = new EnumMap<Direction, LegacyBlockElementFace>(Direction.class);
        faces.put(Direction.SOUTH, face(tintIndex, texture, 0, 0, 16, 16));
        faces.put(Direction.NORTH, face(tintIndex, texture, 16, 0, 0, 16));
        var result = new ArrayList<LegacyBlockElement>();
        result.add(new LegacyBlockElement(
                new Vector3f(0, 0, 7.5F), new Vector3f(16, 16, 8.5F),
                faces, null, true));
        result.addAll(createSideElements(sprite, texture, tintIndex));
        return result;
    }

    private List<LegacyBlockElement> createSideElements(
            TextureAtlasSprite sprite, String texture, int tintIndex) {
        SpriteContents contents = sprite.contents();
        float width = contents.width();
        float height = contents.height();
        float widthScale = 16.0F / width;
        float heightScale = 16.0F / height;
        var result = new ArrayList<LegacyBlockElement>();
        for (Span span : spans(contents)) {
            float x1 = 0, y1 = 0, x2 = 0, y2 = 0;
            float u1 = 0, u2 = 0, v1 = 0, v2 = 0;
            float min = span.min;
            float max = span.max;
            float anchor = span.anchor;
            switch (span.facing) {
                case UP -> {
                    u1 = x1 = min; x2 = u2 = max + 1;
                    v1 = y1 = y2 = anchor; v2 = anchor + 1;
                }
                case DOWN -> {
                    v1 = anchor; v2 = anchor + 1;
                    u1 = x1 = min; x2 = u2 = max + 1;
                    y1 = y2 = anchor + 1;
                }
                case LEFT -> {
                    u1 = x1 = x2 = anchor; u2 = anchor + 1;
                    v2 = y1 = min; y2 = v1 = max + 1;
                }
                case RIGHT -> {
                    u1 = anchor; u2 = anchor + 1;
                    x1 = x2 = anchor + 1;
                    v2 = y1 = min; y2 = v1 = max + 1;
                }
            }
            x1 *= widthScale;
            x2 *= widthScale;
            y1 = 16.0F - y1 * heightScale;
            y2 = 16.0F - y2 * heightScale;
            u1 *= widthScale;
            u2 *= widthScale;
            v1 *= heightScale;
            v2 *= heightScale;
            var side = new EnumMap<Direction, LegacyBlockElementFace>(Direction.class);
            side.put(span.facing.direction, face(tintIndex, texture, u1, v1, u2, v2));
            LegacyBlockElement element = switch (span.facing) {
                case UP -> element(x1, y1, x2, y1, side);
                case DOWN -> element(x1, y2, x2, y2, side);
                case LEFT -> element(x1, y1, x1, y2, side);
                case RIGHT -> element(x2, y1, x2, y2, side);
            };
            result.add(element);
        }
        return result;
    }

    private static LegacyBlockElementFace face(
            int tintIndex, String texture, float u1, float v1, float u2, float v2) {
        return new LegacyBlockElementFace(null, tintIndex, texture,
                new LegacyBlockFaceUV(new float[]{u1, v1, u2, v2}, 0));
    }

    private static LegacyBlockElement element(
            float x1, float y1, float x2, float y2,
            EnumMap<Direction, LegacyBlockElementFace> faces) {
        return new LegacyBlockElement(
                new Vector3f(x1, y1, 7.5F), new Vector3f(x2, y2, 8.5F),
                faces, null, true);
    }

    private static List<Span> spans(SpriteContents contents) {
        var result = new ArrayList<Span>();
        int width = contents.width();
        int height = contents.height();
        contents.getUniqueFrames().forEach(frame -> {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    boolean opaque = !isTransparent(contents, frame, x, y, width, height);
                    for (SpanFacing facing : SpanFacing.values()) {
                        if (opaque && isTransparent(contents, frame,
                                x + facing.xOffset, y + facing.yOffset, width, height)) {
                            createOrExpandSpan(result, facing, x, y);
                        }
                    }
                }
            }
        });
        return result;
    }

    private static void createOrExpandSpan(
            List<Span> spans, SpanFacing facing, int x, int y) {
        int anchor = facing.horizontal ? y : x;
        int value = facing.horizontal ? x : y;
        for (Span span : spans) {
            if (span.facing == facing && span.anchor == anchor) {
                span.min = Math.min(span.min, value);
                span.max = Math.max(span.max, value);
                return;
            }
        }
        spans.add(new Span(facing, value, anchor));
    }

    private static boolean isTransparent(
            SpriteContents contents, int frame, int x, int y, int width, int height) {
        return x < 0 || y < 0 || x >= width || y >= height
                || contents.isTransparent(frame, x, y);
    }

    private enum SpanFacing {
        UP(Direction.UP, 0, -1, true),
        DOWN(Direction.DOWN, 0, 1, true),
        LEFT(Direction.EAST, -1, 0, false),
        RIGHT(Direction.WEST, 1, 0, false);

        final Direction direction;
        final int xOffset;
        final int yOffset;
        final boolean horizontal;

        SpanFacing(Direction direction, int xOffset, int yOffset, boolean horizontal) {
            this.direction = direction;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.horizontal = horizontal;
        }
    }

    private static final class Span {
        final SpanFacing facing;
        int min;
        int max;
        final int anchor;

        Span(SpanFacing facing, int value, int anchor) {
            this.facing = facing;
            this.min = value;
            this.max = value;
            this.anchor = anchor;
        }
    }
}
