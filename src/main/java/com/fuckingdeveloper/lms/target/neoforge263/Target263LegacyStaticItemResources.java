package com.fuckingdeveloper.lms.target.neoforge263;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipFile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.*;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.flag.FeatureFlagSet;

/** Derives native item definitions for the proven source static-model families. */
public final class Target263LegacyStaticItemResources extends AbstractPackMetadataResources implements PackResources {
    private static final Set<Identifier> PARENTS = Set.of(
            Identifier.withDefaultNamespace("item/generated"),
            Identifier.withDefaultNamespace("item/handheld"),
            Identifier.withDefaultNamespace("item/handheld_rod"),
            Identifier.withDefaultNamespace("builtin/generated"));
    private static final Set<Identifier> BLOCK_PARENTS = Set.of(
            Identifier.withDefaultNamespace("block/cube"),
            Identifier.withDefaultNamespace("block/cube_bottom_top"));
    private final Map<Identifier, byte[]> definitions;

    private Target263LegacyStaticItemResources(PackLocationInfo location, Map<Identifier, byte[]> definitions) {
        super(location);
        this.definitions = definitions;
    }

    public static Map<Identifier, byte[]> definitions(Path artifact) {
        var result = new LinkedHashMap<Identifier, byte[]>();
        try (var archive = new ZipFile(artifact.toFile())) {
            // One artifact-local evidence graph; native baking still resolves original JSON.
            var models = new HashMap<Identifier, JsonObject>();
            for (var entry : Collections.list(archive.entries())) {
                var parts = entry.getName().split("/", 3);
                if (parts.length != 3 || !parts[0].equals("assets")
                        || !parts[2].startsWith("models/") || !parts[2].endsWith(".json")) continue;
                try (var reader = new InputStreamReader(archive.getInputStream(entry), StandardCharsets.UTF_8)) {
                    models.put(Identifier.fromNamespaceAndPath(parts[1],
                            parts[2].substring(7, parts[2].length() - 5)),
                            net.minecraft.util.GsonHelper.parse(reader));
                }
            }
            var admitted = new HashMap<Identifier, Boolean>();
            for (var entry : Collections.list(archive.entries())) {
                var parts = entry.getName().split("/", 3);
                if (parts.length != 3 || !parts[0].equals("assets")
                        || !parts[2].startsWith("models/item/") || !parts[2].endsWith(".json")) continue;
                String itemPath = parts[2].substring("models/item/".length());
                if (archive.getEntry("assets/" + parts[1] + "/items/" + itemPath) != null) continue;
                JsonObject source;
                try (var input = archive.getInputStream(entry);
                     var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                    source = net.minecraft.util.GsonHelper.parse(reader);
                }
                if (source.has("overrides") || source.has("loader")) continue;
                boolean custom = source.has("parent") && Identifier.parse(source.get("parent").getAsString())
                        .equals(Identifier.withDefaultNamespace("builtin/entity"));
                if (!custom && !staticModel(Identifier.fromNamespaceAndPath(parts[1],
                        parts[2].substring(7, parts[2].length() - 5)), models, admitted)) continue;
                var model = new JsonObject();
                if (custom) {
                    String base = "lms_legacy/item/" + itemPath.substring(0, itemPath.length() - 5);
                    // builtin/entity is the source custom-render marker, not geometry.
                    // Keep all actual source display/texture/model properties on a derived base.
                    var baseModel = source.deepCopy();
                    baseModel.remove("parent");
                    if (archive.getEntry("assets/" + parts[1] + "/models/" + base + ".json") != null)
                        throw new IllegalStateException("Derived source item base collides with an input resource: " + base);
                    result.put(Identifier.fromNamespaceAndPath(parts[1], "models/" + base + ".json"),
                            baseModel.toString().getBytes(StandardCharsets.UTF_8));
                    model.addProperty("type", "lms:legacy_gui_item");
                    model.addProperty("base", parts[1] + ":" + base);
                } else {
                model.addProperty("type", "minecraft:model");
                model.addProperty("model", parts[1] + ":" + parts[2].substring("models/".length(), parts[2].length() - 5));
                var tints = new JsonArray();
                for (int i = 0; i < 5; i++) {
                    var tint = new JsonObject();
                    tint.addProperty("type", "lms:legacy_item_layer");
                    tint.addProperty("index", i);
                    tints.add(tint);
                }
                model.add("tints", tints);
                }
                var document = new JsonObject();
                document.add("model", model);
                result.put(Identifier.fromNamespaceAndPath(parts[1], "items/" + itemPath),
                        document.toString().getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException | RuntimeException failure) {
            throw new IllegalStateException("Cannot derive source item definitions from " + artifact, failure);
        }
        return Collections.unmodifiableMap(result);
    }

    private static boolean staticModel(Identifier start, Map<Identifier, JsonObject> models,
                                       Map<Identifier, Boolean> admitted) {
        // Iterative walk avoids a stack overflow on deep or cyclic input inheritance.
        var path = new LinkedHashSet<Identifier>();
        Identifier current = start;
        boolean supported;
        while (true) {
            Boolean known = admitted.get(current);
            if (known != null) { supported = known; break; }
            if (!path.add(current)) { supported = false; break; }
            var model = models.get(current);
            if (model == null || model.has("overrides") || model.has("loader")) {
                supported = false; break;
            }
            if (!model.has("parent")) {
                supported = model.has("elements");
                if (supported) {
                    // A verified native cuboid leaf, not an inferred unknown parent.
                    // Native parsing validates geometry; original JSON is still baked.
                    net.minecraft.client.resources.model.cuboid.CuboidModel.fromStream(new StringReader(model.toString()));
                }
                break;
            }
            var parent = Identifier.parse(model.get("parent").getAsString());
            // Artifact overrides must be inspected before trusting a native terminal.
            if (!models.containsKey(parent) && (PARENTS.contains(parent) || BLOCK_PARENTS.contains(parent))) {
                supported = true; break;
            }
            current = parent;
        }
        for (var id : path) admitted.put(id, supported);
        return supported;
    }

    public static Pack createPack(Path artifact) {
        var definitions = definitions(artifact);
        if (definitions.isEmpty()) return null;
        var location = new PackLocationInfo("lms/legacy-item-definitions/" + artifact.getFileName(),
                Component.literal("LMS legacy item definitions"), PackSource.BUILT_IN, Optional.empty());
        var metadata = new Pack.Metadata(location.title(), PackCompatibility.COMPATIBLE, FeatureFlagSet.of(), List.of());
        var supplier = new Pack.ResourcesSupplier() {
            public PackMetadataResources openMetadata(PackLocationInfo ignored) {
                return new Target263LegacyStaticItemResources(location, definitions);
            }
            public java.util.stream.Stream<PackResources> openResources(PackLocationInfo ignored, Pack.Metadata meta) {
                return java.util.stream.Stream.of(new Target263LegacyStaticItemResources(location, definitions));
            }
        };
        return new Pack(location, supplier, metadata, new PackSelectionConfig(true, Pack.Position.TOP, false));
    }

    @Override public IoSupplier<InputStream> getRootResource(String... path) { return null; }
    @Override public IoSupplier<InputStream> getResource(PackType type, Identifier id) {
        byte[] bytes = type == PackType.CLIENT_RESOURCES ? definitions.get(id) : null;
        return bytes == null ? null : () -> new ByteArrayInputStream(bytes);
    }
    @Override public void listResources(PackType type, String namespace, String directory, ResourceOutput output) {
        if (type != PackType.CLIENT_RESOURCES) return;
        definitions.forEach((id, bytes) -> {
            if (id.getNamespace().equals(namespace) && (directory.isEmpty() || id.getPath().startsWith(directory + "/")))
                output.accept(id, () -> new ByteArrayInputStream(bytes));
        });
    }
    @Override public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? definitions.keySet().stream().map(Identifier::getNamespace)
                .collect(java.util.stream.Collectors.toUnmodifiableSet()) : Set.of();
    }
    @Override public void close() {}
}
