package com.fuckingdeveloper.lms.target.neoforge263;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.AbstractPackMetadataResources;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackMetadataResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Publishes immutable managed legacy artifacts through the target 26.3 pack lifecycle.
 *
 * <p>The legacy artifact's pack metadata describes its source generation and therefore
 * cannot establish compatibility with the target pack format. LMS owns only the target
 * repository metadata below; resource bytes continue to be read directly from the
 * original artifact by Minecraft's ZIP pack implementation.</p>
 */
public final class Target263LegacyResourcePackBridge {
    private static final Logger LOG = LoggerFactory.getLogger(Target263LegacyResourcePackBridge.class);
    private static final net.minecraft.resources.Identifier BLOCK_ATLAS_DEFINITION =
            net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "atlases/blocks.json");
    private static final byte[] BLOCK_ATLAS_EXTENSION =
            "{\"sources\":[{\"type\":\"lms:legacy_texture_stitch\"}]}"
                    .getBytes(StandardCharsets.UTF_8);
    private final Set<Path> artifacts = new CopyOnWriteArraySet<>();

    public void admit(Path artifact) {
        Path normalized = artifact.toAbsolutePath().normalize();
        if (Files.isRegularFile(normalized)) artifacts.add(normalized);
    }

    public void admitAll(Iterable<Path> admittedArtifacts) {
        for (Path artifact : admittedArtifacts) admit(artifact);
    }

    public void addPackFinders(AddPackFindersEvent event) {
        PackType packType = event.getPackType();
        List<Path> snapshot = artifacts.stream()
                .sorted(Comparator.comparing(Path::toString))
                .toList();
        event.addRepositorySource(output -> {
            if (packType == PackType.CLIENT_RESOURCES) output.accept(createAtlasExtensionPack(snapshot));
            if (packType == PackType.CLIENT_RESOURCES) {
                Pack widgets = Target263LegacyWidgetAssets.createPack(snapshot);
                if (widgets != null) output.accept(widgets);
            }
            snapshot.forEach(artifact -> {
                output.accept(createPack(artifact, packType));
                if (packType == PackType.SERVER_DATA) {
                    Pack aliases = com.fuckingdeveloper.lms.target.neoforge263.worldgen.Target263WorldgenPack.aliases(artifact);
                    if (aliases != null) output.accept(aliases);
                }
                if (packType == PackType.CLIENT_RESOURCES) {
                    Pack signGuiPack = Target263LegacySignGuiResources.createPack(artifact);
                    if (signGuiPack != null) output.accept(signGuiPack);
                    Pack itemModels = Target263LegacyStaticItemResources.createPack(artifact);
                    if (itemModels != null) output.accept(itemModels);
                }
            });
        });
        LOG.info("LMS legacy-resource-packs type={} artifacts={}", packType, snapshot.size());
    }

    private static Pack createPack(Path artifact, PackType packType) {
        String fileName = artifact.getFileName().toString();
        String id = "lms/legacy/" + safeName(fileName) + "-" + stableSuffix(artifact);
        Component title = Component.literal("LMS legacy resources: " + fileName);
        PackLocationInfo location = new PackLocationInfo(id, title, PackSource.BUILT_IN, Optional.empty());
        Pack.Metadata metadata = new Pack.Metadata(
                Component.literal("Resources from managed legacy artifact " + fileName),
                PackCompatibility.COMPATIBLE,
                FeatureFlagSet.of(),
                List.of());
        PackSelectionConfig selection = new PackSelectionConfig(true, Pack.Position.TOP, false);
        return new Pack(location, Target263LegacyTagResources.wrap(new FilePackResources.FileResourcesSupplier(artifact)), metadata, selection);
    }

    private static Pack createAtlasExtensionPack(List<Path> artifacts) {
        return Target263LegacyAtlasSources.createPack(artifacts);
    }

    private static final class AtlasExtensionResources extends AbstractPackMetadataResources
            implements PackResources {
        private AtlasExtensionResources(PackLocationInfo location) {
            super(location);
        }

        @Override
        public net.minecraft.server.packs.resources.IoSupplier<InputStream> getRootResource(String... path) {
            return null;
        }

        @Override
        public net.minecraft.server.packs.resources.IoSupplier<InputStream> getResource(
                PackType type, net.minecraft.resources.Identifier id) {
            if (type != PackType.CLIENT_RESOURCES || !BLOCK_ATLAS_DEFINITION.equals(id)) return null;
            return () -> new ByteArrayInputStream(BLOCK_ATLAS_EXTENSION);
        }

        @Override
        public void listResources(PackType type, String namespace, String directory, ResourceOutput output) {
            if (type == PackType.CLIENT_RESOURCES
                    && "minecraft".equals(namespace)
                    && (directory.isEmpty() || "atlases".startsWith(directory) || directory.startsWith("atlases"))) {
                output.accept(BLOCK_ATLAS_DEFINITION, () -> new ByteArrayInputStream(BLOCK_ATLAS_EXTENSION));
            }
        }

        @Override
        public Set<String> getNamespaces(PackType type) {
            return type == PackType.CLIENT_RESOURCES ? Set.of("minecraft") : Set.of();
        }

        @Override
        public void close() {}
    }

    private static String safeName(String value) {
        return value.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static String stableSuffix(Path artifact) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(artifact.toString().getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest, 0, 6);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
