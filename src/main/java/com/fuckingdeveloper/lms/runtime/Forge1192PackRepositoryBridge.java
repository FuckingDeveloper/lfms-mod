package com.fuckingdeveloper.lms.runtime;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackMetadataResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.flag.FeatureFlagSet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/** Target 26.3 implementation of the Forge 1.19.2 pack-construction contract. */
public final class Forge1192PackRepositoryBridge {
    private Forge1192PackRepositoryBridge() {}

    public static PackMetadataSection metadata(Component description, int packFormat) {
        return new PackMetadataSection(description, PackFormat.of(packFormat).minorRange());
    }

    public static Pack create(
            String id,
            Component title,
            boolean required,
            Supplier<?> legacyResources,
            PackMetadataSection metadata,
            Pack.Position position,
            PackSource source) {
        return create(id, title, required, legacyResources, metadata, position, source, false);
    }

    public static Pack create(
            String id,
            Component title,
            boolean required,
            Supplier<?> legacyResources,
            PackMetadataSection metadata,
            Pack.Position position,
            PackSource source,
            boolean hidden) {
        Objects.requireNonNull(legacyResources, "legacyResources");
        PackLocationInfo location = new PackLocationInfo(id, title, source, Optional.empty());
        Pack.Metadata targetMetadata = new Pack.Metadata(
                metadata.description(), PackCompatibility.COMPATIBLE,
                FeatureFlagSet.of(), List.of(), hidden);
        Pack.ResourcesSupplier resources = new Pack.ResourcesSupplier() {
            @Override
            public PackMetadataResources openMetadata(PackLocationInfo ignored) {
                return adapt(location, legacyResources, metadata);
            }

            @Override
            public Stream<PackResources> openResources(PackLocationInfo ignored, Pack.Metadata ignoredMetadata) {
                return Stream.of(new Target263LegacyTagResources(adapt(location, legacyResources, metadata)));
            }
        };
        return new Pack(location, resources, targetMetadata,
                new PackSelectionConfig(required, position, false));
    }

    private static LegacyPackResourcesAdapter adapt(
            PackLocationInfo location, Supplier<?> supplier, PackMetadataSection metadata) {
        Object supplied = Objects.requireNonNull(supplier.get(), "legacy PackResources supplier result");
        if (!(supplied instanceof Forge1192LegacyPackResourcesAccess access)) {
            throw new IllegalStateException("Legacy PackResources was not transformed with typed LMS access: "
                    + supplied.getClass().getName());
        }
        return new LegacyPackResourcesAdapter(location, access, metadata);
    }

    private record LegacyPackResourcesAdapter(
            PackLocationInfo location,
            Forge1192LegacyPackResourcesAccess legacy,
            PackMetadataSection packMetadata) implements PackResources {
        @Override
        public IoSupplier<InputStream> getRootResource(String... path) {
            String joined = String.join("/", path);
            try {
                InputStream stream = legacy.lms$legacyGetRootResource(joined);
                return stream == null ? null : () -> stream;
            } catch (FileNotFoundException missing) {
                return null;
            } catch (IOException failure) {
                return () -> { throw failure; };
            }
        }

        @Override
        public IoSupplier<InputStream> getResource(PackType type, Identifier id) {
            try {
                InputStream stream = legacy.lms$legacyGetResource(type, id);
                return stream == null ? null : () -> stream;
            } catch (FileNotFoundException missing) {
                return null;
            } catch (IOException failure) {
                return () -> { throw failure; };
            }
        }

        @Override
        public void listResources(PackType type, String namespace, String directory, ResourceOutput output) {
            var ids = Objects.requireNonNull(
                    legacy.lms$legacyListResources(type, namespace, directory, ignored -> true),
                    "legacy PackResources list result");
            for (Identifier id : ids) {
                output.accept(id, () -> {
                    InputStream stream = legacy.lms$legacyGetResource(type, id);
                    if (stream == null) throw new FileNotFoundException(id.toString());
                    return stream;
                });
            }
        }

        @Override
        public java.util.Set<String> getNamespaces(PackType type) {
            var declared = legacy.lms$legacyNamespaces(type);
            if (type != PackType.SERVER_DATA || declared.contains("forge")) return declared;
            // Source Forge is an intrinsic mod/resource namespace. Procedural
            // packs may derive namespaces from ModList, where the target now
            // exposes NeoForge. Retain discovery of actual source forge data;
            // listing still decides which resources exist.
            var namespaces = new java.util.LinkedHashSet<>(declared);
            namespaces.add("forge");
            return java.util.Collections.unmodifiableSet(namespaces);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getMetadataSection(MetadataSectionType<T> type) {
            if (type == PackMetadataSection.CLIENT_TYPE
                    || type == PackMetadataSection.SERVER_TYPE
                    || type == PackMetadataSection.FALLBACK_TYPE) {
                return (T) packMetadata;
            }
            return null;
        }

        @Override
        public String packId() {
            String legacyName = legacy.lms$legacyPackName();
            return legacyName == null || legacyName.isBlank() ? location.id() : legacyName;
        }

        @Override
        public void close() {
            legacy.close();
        }
    }
}
