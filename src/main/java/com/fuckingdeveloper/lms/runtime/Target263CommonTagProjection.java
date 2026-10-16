package com.fuckingdeveloper.lms.runtime;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.*;
import net.minecraft.tags.TagKey;

/** Exact source declarations paired with actual target declarations, never prefix aliases. */
public final class Target263CommonTagProjection {
    private Target263CommonTagProjection() {}
    private record Identity(Identifier registry, Identifier tag) {}
    private static final class Index {
        static final Map<Identity, TagKey<?>> TAGS = load();
        private static Map<Identity, TagKey<?>> load() {
            var result = new HashMap<Identity, TagKey<?>>();
            try (var input = Objects.requireNonNull(Target263CommonTagProjection.class.getResourceAsStream(
                    "/lms/profile/forge1192/common-tags.tsv"), "Missing source common tag declarations");
                 var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                for (String line; (line = reader.readLine()) != null;) {
                    if (line.isBlank() || line.startsWith("#")) continue;
                    var row = line.split("\t", -1);
                    if (row.length != 4) throw new IllegalStateException("Malformed source common tag declaration: " + line);
                    var registry = Identifier.parse(row[0]); var source = Identifier.parse(row[3]);
                    String renamed = Target263CommonTagNames.renamed(row[1], row[2]);
                    String member = renamed == null ? row[2] : renamed;
                    var owner = Class.forName("net.neoforged.neoforge.common.Tags$" + row[1]);
                    java.lang.reflect.Field field;
                    try { field = owner.getField(member); }
                    catch (NoSuchFieldException absent) { continue; } // Not admitted by this tested target.
                    if (field.getType() != TagKey.class || !java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                        throw new IllegalStateException("Native common tag declaration changed: " + field);
                    var target = (TagKey<?>) field.get(null);
                    if (!target.registry().identifier().equals(registry))
                        throw new IllegalStateException("Native common tag registry changed: " + field);
                    // Equal member alone is insufficient: require the exact source path or a proven rename.
                    if (renamed == null && !source.getPath().equals(target.location().getPath())) continue;
                    var previous = result.put(new Identity(registry, source), target);
                    if (previous != null && previous != target) throw new IllegalStateException("Ambiguous common tag: " + line);
                }
            } catch (IOException | ReflectiveOperationException failure) {
                throw new IllegalStateException("Cannot bind source common tags to the tested target", failure);
            }
            return Map.copyOf(result);
        }
    }
    @SuppressWarnings("unchecked")
    public static <T> TagKey<T> create(ResourceKey<? extends Registry<T>> registry, Identifier id) {
        var projected = Index.TAGS.get(new Identity(registry.identifier(), id));
        return projected == null ? TagKey.create(registry, id) : (TagKey<T>) projected;
    }
    public static Identifier projectLocation(ResourceKey<?> registry, Identifier id) {
        return location(registry.identifier(), id);
    }
    @SuppressWarnings("deprecation")
    public static <T> TagKey<T> construct(ResourceKey<? extends Registry<T>> registry, Identifier id) {
        return new TagKey<>(registry, projectLocation(registry,id));
    }
    public static <T> com.mojang.serialization.Codec<TagKey<T>> codec(ResourceKey<? extends Registry<T>> registry) {
        return TagKey.codec(registry).xmap(tag -> create(tag.registry(),tag.location()), java.util.function.Function.identity());
    }
    public static <T> com.mojang.serialization.Codec<TagKey<T>> hashedCodec(ResourceKey<? extends Registry<T>> registry) {
        return TagKey.hashedCodec(registry).xmap(tag -> create(tag.registry(),tag.location()), java.util.function.Function.identity());
    }
    public static TagKey<net.minecraft.world.item.Item> item(Identifier id) {
        return create(net.minecraft.core.registries.Registries.ITEM, id);
    }
    public static TagKey<net.minecraft.world.level.block.Block> block(Identifier id) {
        return create(net.minecraft.core.registries.Registries.BLOCK, id);
    }
    public static TagKey<net.minecraft.world.level.material.Fluid> fluid(Identifier id) {
        return create(net.minecraft.core.registries.Registries.FLUID, id);
    }
    public static Identifier location(Identifier registry, Identifier id) {
        var projected = Index.TAGS.get(new Identity(registry, id));
        return projected == null ? id : projected.location();
    }
    public static Set<String> targetNamespaces() {
        var result = new HashSet<String>();
        Index.TAGS.values().forEach(tag -> result.add(tag.location().getNamespace()));
        return Set.copyOf(result);
    }
}
