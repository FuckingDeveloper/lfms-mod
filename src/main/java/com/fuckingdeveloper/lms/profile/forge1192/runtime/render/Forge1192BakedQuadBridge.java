package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Vector3f;

/** Representation bridge for the Forge 1.19.2 packed BakedQuad contract. */
public final class Forge1192BakedQuadBridge {
    private static final Map<BakedQuad, Boolean> LEGACY_SHADE =
            Collections.synchronizedMap(new WeakHashMap<>());

    private Forge1192BakedQuadBridge() {}

    public static BakedQuad create(
            int[] vertices, int tintIndex, Direction direction,
            TextureAtlasSprite sprite, boolean shade) {
        return create(vertices, tintIndex, direction, sprite, shade, true);
    }

    public static BakedQuad create(
            int[] vertices, int tintIndex, Direction direction,
            TextureAtlasSprite sprite, boolean shade, boolean ambientOcclusion) {
        if (vertices == null || vertices.length == 0 || vertices.length % 4 != 0) {
            throw new IllegalArgumentException("Invalid Forge 1.19.2 BakedQuad vertex array");
        }
        int stride = vertices.length / 4;
        if (stride < 6) {
            throw new IllegalArgumentException("Unsupported Forge 1.19.2 BakedQuad vertex stride: " + stride);
        }
        Vector3f[] positions = new Vector3f[4];
        long[] uvs = new long[4];
        int[] colors = new int[4];
        int[] normals = new int[4];
        int lightEmission = 0;
        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * stride;
            positions[vertex] = new Vector3f(
                    Float.intBitsToFloat(vertices[offset]),
                    Float.intBitsToFloat(vertices[offset + 1]),
                    Float.intBitsToFloat(vertices[offset + 2]));
            colors[vertex] = stride > 3 ? vertices[offset + 3] : -1;
            uvs[vertex] = UVPair.pack(
                    Float.intBitsToFloat(vertices[offset + 4]),
                    Float.intBitsToFloat(vertices[offset + 5]));
            if (stride > 6) lightEmission = Math.max(lightEmission, vertices[offset + 6] >>> 4 & 15);
            normals[vertex] = stride > 7 ? vertices[offset + 7] : 0;
        }
        BakedQuad.MaterialInfo materialInfo = BakedQuad.MaterialInfo.of(
                new Material.Baked(sprite, false), sprite.transparency(), tintIndex,
                null, lightEmission, ambientOcclusion);
        BakedQuad quad = new BakedQuad(
                positions[0], positions[1], positions[2], positions[3],
                uvs[0], uvs[1], uvs[2], uvs[3], direction, materialInfo,
                BakedNormals.of(normals[0], normals[1], normals[2], normals[3]),
                BakedColors.of(colors[0], colors[1], colors[2], colors[3]));
        return rememberShade(quad, shade);
    }

    public static BakedQuad rememberShade(BakedQuad quad, boolean shade) {
        LEGACY_SHADE.put(quad, shade);
        return quad;
    }

    public static TextureAtlasSprite sprite(BakedQuad quad) {
        return quad.materialInfo().sprite();
    }

    public static int[] vertices(BakedQuad quad) {
        int[] result = new int[32];
        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * 8;
            var position = quad.position(vertex);
            result[offset] = Float.floatToRawIntBits(position.x());
            result[offset + 1] = Float.floatToRawIntBits(position.y());
            result[offset + 2] = Float.floatToRawIntBits(position.z());
            result[offset + 3] = quad.bakedColors().color(vertex);
            result[offset + 4] = Float.floatToRawIntBits(UVPair.unpackU(quad.packedUV(vertex)));
            result[offset + 5] = Float.floatToRawIntBits(UVPair.unpackV(quad.packedUV(vertex)));
            result[offset + 6] = quad.materialInfo().lightEmission() << 4;
            result[offset + 7] = quad.bakedNormals().normal(vertex);
        }
        return result;
    }

    public static boolean isTinted(BakedQuad quad) {
        return quad.materialInfo().isTinted();
    }

    public static int tintIndex(BakedQuad quad) {
        return quad.materialInfo().tintIndex();
    }

    public static Direction direction(BakedQuad quad) {
        return quad.direction();
    }

    public static boolean isShade(BakedQuad quad) {
        return LEGACY_SHADE.getOrDefault(quad, true);
    }

    public static boolean hasAmbientOcclusion(BakedQuad quad) {
        return quad.materialInfo().ambientOcclusion();
    }
}
