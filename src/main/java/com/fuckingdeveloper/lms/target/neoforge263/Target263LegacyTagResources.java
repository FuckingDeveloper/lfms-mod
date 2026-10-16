package com.fuckingdeveloper.lms.target.neoforge263;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;

/** Source 1.19.2 tag-folder projection, applied only to admitted source packs. */
public record Target263LegacyTagResources(PackResources source) implements PackResources {
    private static final Map<String,String> FOLDERS = Map.of(
            "tags/blocks", "tags/block", "tags/items", "tags/item",
            "tags/fluids", "tags/fluid", "tags/entity_types", "tags/entity_type",
            "tags/game_events", "tags/game_event");

    private static boolean under(String path, String directory) {
        return directory.isEmpty() || path.equals(directory) || path.startsWith(directory + "/");
    }
    private static String project(String path, boolean reverse) {
        for (var entry : FOLDERS.entrySet()) {
            String from = reverse ? entry.getValue() : entry.getKey();
            String to = reverse ? entry.getKey() : entry.getValue();
            if (under(path, from)) return to + path.substring(from.length());
        }
        return path;
    }
    private static String tagRoot(String path) {
        for (String registry : java.util.List.of("worldgen/biome", "entity_type", "game_event", "block", "item", "fluid")) {
            String root = "tags/" + registry;
            if (under(path, root)) return root;
        }
        return null;
    }
    private static Identifier registry(String root) {
        return Identifier.fromNamespaceAndPath("minecraft", root.substring(5));
    }
    private static Identifier canonical(Identifier sourceId) {
        String path = project(sourceId.getPath(), false), root = tagRoot(path);
        if (root == null || !path.endsWith(".json") || path.equals(root))
            return Identifier.fromNamespaceAndPath(sourceId.getNamespace(), path);
        var tag = Identifier.fromNamespaceAndPath(sourceId.getNamespace(), path.substring(root.length()+1, path.length()-5));
        var target = Target263CommonTagProjection.location(registry(root), tag);
        return Identifier.fromNamespaceAndPath(target.getNamespace(), root + "/" + target.getPath() + ".json");
    }
    private static IoSupplier<InputStream> contents(Identifier id, IoSupplier<InputStream> source) {
        String root = tagRoot(id.getPath());
        if (root == null || !id.getPath().endsWith(".json")) return source;
        return () -> {
            byte[] original;
            try (var input = source.get()) { original = input.readAllBytes(); }
            String text = new String(original, java.nio.charset.StandardCharsets.UTF_8);
            // Unrelated bytes remain byte-identical; malformed tag documents still reach native diagnostics.
            if (!text.contains("#forge:")) return new java.io.ByteArrayInputStream(original);
            var json = com.google.gson.JsonParser.parseString(text).getAsJsonObject();
            var values = json.getAsJsonArray("values"); boolean changed = false;
            if (values != null) for (int i=0;i<values.size();i++) {
                var value=values.get(i);
                var token=value.isJsonObject()?value.getAsJsonObject().get("id"):value;
                if (token == null || !token.isJsonPrimitive() || !token.getAsJsonPrimitive().isString()) continue;
                String name=token.getAsString(); if (!name.startsWith("#")) continue;
                var old=Identifier.parse(name.substring(1));
                var target=Target263CommonTagProjection.location(registry(root),old);
                if (target.equals(old)) continue;
                var replacement=new com.google.gson.JsonPrimitive("#"+target);
                if (value.isJsonObject()) value.getAsJsonObject().add("id",replacement); else values.set(i,replacement);
                changed=true;
            }
            return new java.io.ByteArrayInputStream(changed ? json.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8) : original);
        };
    }
    private Map<Identifier,IoSupplier<InputStream>> listed(PackType type, String namespace, String directory) {
        var result = new LinkedHashMap<Identifier,IoSupplier<InputStream>>();
        if (type != PackType.SERVER_DATA) {
            source.listResources(type, namespace, directory, (id, supplier) -> {
                if (under(id.getPath(), directory)) result.put(id, supplier);
            });
            return result;
        }
        // Registry-root enumeration also covers proven path renames (e.g. ingots/brick -> bricks/normal).
        String root = tagRoot(directory);
        String query = root == null ? directory : root;
        var namespaces = new java.util.LinkedHashSet<String>(); namespaces.add(namespace);
        if (Target263CommonTagProjection.targetNamespaces().contains(namespace)) namespaces.add("forge");
        var directories = new java.util.LinkedHashSet<String>(); directories.add(query);
        directories.add(project(query, true));
        var ranks = new java.util.HashMap<Identifier,Integer>();
        for (String inputNamespace : namespaces) for (String inputDirectory : directories) {
            source.listResources(type, inputNamespace, inputDirectory, (id, supplier) -> {
                if (!under(id.getPath(),inputDirectory)) return;
                var projected = canonical(id);
                if (!projected.getNamespace().equals(namespace) || !under(projected.getPath(),directory)) return;
                int rank=(id.getNamespace().equals(projected.getNamespace())?0:4)
                        +(id.getPath().equals(project(id.getPath(),false))?0:1);
                if (rank < ranks.getOrDefault(projected,Integer.MAX_VALUE)) {
                    ranks.put(projected,rank); result.put(projected,contents(projected,supplier));
                }
            });
        }
        return result;
    }
    @Override public IoSupplier<InputStream> getResource(PackType type, Identifier id) {
        String root = tagRoot(id.getPath());
        if (type != PackType.SERVER_DATA || root == null) return source.getResource(type, id);
        // Never probe nonexistent procedural IDs: enumerate the exact source registry folder.
        return listed(type,id.getNamespace(),root).get(id);
    }
    @Override public void listResources(PackType type,String namespace,String directory,ResourceOutput output) {
        listed(type,namespace,directory).forEach(output);
    }
    @Override public Set<String> getNamespaces(PackType type) {
        var original=source.getNamespaces(type);
        if (type!=PackType.SERVER_DATA || !original.contains("forge")) return original;
        var projected=new java.util.HashSet<>(original);
        projected.addAll(Target263CommonTagProjection.targetNamespaces());
        return Set.copyOf(projected);
    }
    @Override public PackLocationInfo location() { return source.location(); }
    @Override public String packId() { return source.packId(); }
    @Override public IoSupplier<InputStream> getRootResource(String... path) { return source.getRootResource(path); }
    @Override public <T> T getMetadataSection(MetadataSectionType<T> type) throws IOException { return source.getMetadataSection(type); }
    @Override public void close() { source.close(); }

    public static Pack.ResourcesSupplier wrap(Pack.ResourcesSupplier source) {
        return new Pack.ResourcesSupplier() {
            @Override public PackMetadataResources openMetadata(PackLocationInfo location) { return source.openMetadata(location); }
            @Override public java.util.stream.Stream<PackResources> openResources(PackLocationInfo location,Pack.Metadata metadata) {
                return source.openResources(location,metadata).map(Target263LegacyTagResources::new);
            }
        };
    }
}
