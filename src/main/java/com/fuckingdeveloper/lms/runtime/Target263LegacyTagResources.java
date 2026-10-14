package com.fuckingdeveloper.lms.runtime;

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
    private Map<Identifier,IoSupplier<InputStream>> listed(PackType type, String namespace, String directory) {
        var result = new LinkedHashMap<Identifier,IoSupplier<InputStream>>();
        // Filter returned paths: older procedural packs may use loose startsWith checks.
        source.listResources(type, namespace, directory, (id, supplier) -> {
            if (under(id.getPath(), directory)) result.put(id, supplier);
        });
        if (type == PackType.SERVER_DATA) {
            new LinkedHashMap<>(result).forEach((id, supplier) -> {
                String projected = project(id.getPath(), false);
                if (under(projected, directory)) result.putIfAbsent(
                        Identifier.fromNamespaceAndPath(id.getNamespace(), projected), supplier);
            });
        }
        if (type == PackType.SERVER_DATA && !project(directory, true).equals(directory)) {
            String legacyDirectory = project(directory, true);
            source.listResources(type, namespace, legacyDirectory, (id, supplier) -> {
                String projected = project(id.getPath(), false);
                if (under(projected, directory)) {
                    result.putIfAbsent(Identifier.fromNamespaceAndPath(id.getNamespace(), projected), supplier);
                }
            });
        }
        return result;
    }
    @Override public IoSupplier<InputStream> getResource(PackType type, Identifier id) {
        String old = project(id.getPath(), true);
        if (type != PackType.SERVER_DATA || old.equals(id.getPath())) return source.getResource(type, id);
        // Do not probe nonexistent procedural resources: source APIs may assume listed IDs.
        String directory = id.getPath().substring(0,id.getPath().lastIndexOf('/'));
        return listed(type, id.getNamespace(), directory).get(id);
    }
    @Override public void listResources(PackType type,String namespace,String directory,ResourceOutput output) {
        listed(type,namespace,directory).forEach(output);
    }
    @Override public Set<String> getNamespaces(PackType type) { return source.getNamespaces(type); }
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
