package com.fuckingdeveloper.lms.target.neoforge263;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.*;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.flag.FeatureFlagSet;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipFile;

/** Target sign-edit GUI resource migration from the source 64x32 sign UV layout. */
public final class Target263LegacySignGuiResources extends AbstractPackMetadataResources implements PackResources {
    private final Map<Identifier, byte[]> images;

    private Target263LegacySignGuiResources(PackLocationInfo location, Map<Identifier, byte[]> images) {
        super(location);
        this.images = images;
    }

    public static Pack createPack(Path artifact) {
        Map<Identifier, byte[]> images = derivedImages(artifact);
        if (images.isEmpty()) return null;
        var location = new PackLocationInfo("lms/zz-legacy-sign-gui/" + artifact.getFileName(),
                Component.literal("LMS legacy sign GUI"), PackSource.BUILT_IN, Optional.empty());
        var metadata = new Pack.Metadata(Component.literal("Derived legacy sign-edit textures"),
                PackCompatibility.COMPATIBLE, FeatureFlagSet.of(), List.of());
        var supplier = new Pack.ResourcesSupplier() {
            @Override public PackMetadataResources openMetadata(PackLocationInfo ignored) {
                return new Target263LegacySignGuiResources(location, images);
            }
            @Override public java.util.stream.Stream<PackResources> openResources(PackLocationInfo ignored, Pack.Metadata meta) {
                return java.util.stream.Stream.of(new Target263LegacySignGuiResources(location, images));
            }
        };
        return new Pack(location, supplier, metadata, new PackSelectionConfig(true, Pack.Position.TOP, false));
    }

    private static Map<Identifier, byte[]> derivedImages(Path artifact) {
        var output = new HashMap<Identifier, byte[]>();
        try (var archive = new ZipFile(artifact.toFile())) {
            for (var entry : Collections.list(archive.entries())) {
                String name = entry.getName();
                String[] parts = name.split("/", 3);
                if (parts.length != 3 || !parts[0].equals("assets")
                        || !parts[2].startsWith("textures/entity/signs/") || !name.endsWith(".png")) continue;
                String path = "textures/gui/signs/" + parts[2].substring("textures/entity/signs/".length());
                // Preserve an explicit target-shaped resource supplied by the artifact.
                if (archive.getEntry("assets/" + parts[1] + "/" + path) != null) continue;
                try (var input = archive.getInputStream(entry)) {
                    output.put(Identifier.fromNamespaceAndPath(parts[1], path), convert(input));
                }
            }
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot derive legacy sign GUI resources from " + artifact, failure);
        }
        return Map.copyOf(output);
    }

    static byte[] convert(InputStream input) throws IOException {
        BufferedImage source = ImageIO.read(input);
        if (source == null || source.getWidth() % 64 != 0
                || source.getHeight() * 2 != source.getWidth()) {
            throw new IOException("Legacy sign texture must use the 64x32 UV layout at an integer scale");
        }
        int scale = source.getWidth() / 64;
        var target = new BufferedImage(24 * scale, 26 * scale, BufferedImage.TYPE_INT_ARGB);
        // Source SignRenderer#createSignLayer front UVs; pixel-identical to target vanilla oak GUI.
        copy(source, target, 2 * scale, 2 * scale, 0, 0, 24 * scale, 12 * scale);
        copy(source, target, 2 * scale, 16 * scale, 11 * scale, 12 * scale, 2 * scale, 14 * scale);
        var bytes = new ByteArrayOutputStream();
        if (!ImageIO.write(target, "png", bytes)) throw new IOException("PNG encoder unavailable");
        return bytes.toByteArray();
    }

    private static void copy(BufferedImage source, BufferedImage target, int sx, int sy, int dx, int dy, int width, int height) {
        target.setRGB(dx, dy, width, height, source.getRGB(sx, sy, width, height, null, 0, width), 0, width);
    }

    @Override public IoSupplier<InputStream> getRootResource(String... path) { return null; }
    @Override public IoSupplier<InputStream> getResource(PackType type, Identifier id) {
        byte[] bytes = type == PackType.CLIENT_RESOURCES ? images.get(id) : null;
        return bytes == null ? null : () -> new ByteArrayInputStream(bytes);
    }
    @Override public void listResources(PackType type, String namespace, String directory, ResourceOutput output) {
        if (type != PackType.CLIENT_RESOURCES) return;
        images.forEach((id, bytes) -> {
            if (id.getNamespace().equals(namespace) && (directory.isEmpty() || id.getPath().startsWith(directory + "/")))
                output.accept(id, () -> new ByteArrayInputStream(bytes));
        });
    }
    @Override public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? images.keySet().stream().map(Identifier::getNamespace)
                .collect(java.util.stream.Collectors.toUnmodifiableSet()) : Set.of();
    }
    @Override public void close() {}
}
