package com.fuckingdeveloper.lms.runtime;

import com.fuckingdeveloper.lms.analysis.LegacyMetadataAnalyzer;
import net.neoforged.fml.ModList;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * Resolves the complete managed class path for a Forge 1.19.2 artifact.
 *
 * <p>There are two independent dependency channels in legacy Forge artifacts:
 * declared mod dependencies from mods.toml and implementation libraries bundled
 * with JarJar. The latter are not mods and therefore must not be gated through
 * ModList. LMS extracts them into a deterministic cache and gives them to the
 * managed classloader as owned dependency artifacts.</p>
 */
public final class LegacyDependencyGate {
    public record Missing(String modId, String versionRange, String reason) {}
    public record Decision(boolean satisfied, List<Missing> missing, List<Path> artifacts) {}

    public Decision evaluate(List<LegacyMetadataAnalyzer.Dependency> dependencies) {
        return evaluate(dependencies, Map.of(), null);
    }

    public Decision evaluate(List<LegacyMetadataAnalyzer.Dependency> dependencies,
                             Map<String, Path> legacyArtifacts) {
        return evaluate(dependencies, legacyArtifacts, null);
    }

    public Decision evaluate(List<LegacyMetadataAnalyzer.Dependency> dependencies,
                             Map<String, Path> legacyArtifacts,
                             Path ownerArtifact) {
        var missing = dependencies.stream()
                .filter(LegacyMetadataAnalyzer.Dependency::mandatory)
                .filter(dep -> !providedByProfile(dep.modId()))
                .filter(dep -> !ModList.get().isLoaded(dep.modId()))
                .filter(dep -> !legacyArtifacts.containsKey(dep.modId()))
                .map(dep -> new Missing(dep.modId(), dep.versionRange(),
                        "mandatory legacy dependency is not present in target runtime"))
                .toList();

        LinkedHashSet<Path> artifacts = new LinkedHashSet<>();
        dependencies.stream()
                .filter(LegacyMetadataAnalyzer.Dependency::mandatory)
                .map(LegacyMetadataAnalyzer.Dependency::modId)
                .map(legacyArtifacts::get)
                .filter(Objects::nonNull)
                .map(LegacyDependencyGate::normalized)
                .forEach(artifacts::add);

        if (ownerArtifact != null) {
            try {
                artifacts.addAll(extractNestedLibraries(ownerArtifact));
            } catch (IOException e) {
                return new Decision(false,
                        append(missing, new Missing("<nested-library>", "",
                                "cannot materialize bundled dependency: " + e.getMessage())),
                        List.copyOf(artifacts));
            }
        }
        return new Decision(missing.isEmpty(), List.copyOf(missing), List.copyOf(artifacts));
    }

    /**
     * Materialize every nested JAR into run/lms/dependencies/<owner>/.
     *
     * <p>ForgeGradle/JarJar normally consumes metadata describing these entries,
     * but treating the physical nested JAR as the source of truth is deliberately
     * more tolerant: old artifacts with incomplete JarJar metadata remain usable,
     * while a SHA-256 file name makes extraction deterministic and collision-safe.</p>
     */
    private static List<Path> extractNestedLibraries(Path ownerArtifact) throws IOException {
        Path owner = normalized(ownerArtifact);
        if (!Files.isRegularFile(owner)) return List.of();

        Path root = Path.of(System.getProperty("user.dir"), "lms", "dependencies",
                safeBaseName(owner.getFileName().toString()));
        Files.createDirectories(root);

        Map<String, Path> materialized = new LinkedHashMap<>();
        try (JarFile jar = new JarFile(owner.toFile(), false)) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".jar")) {
                    continue;
                }
                // Nested JARs are implementation dependencies. A nested full Forge
                // mod remains isolated by its own metadata and must not accidentally
                // become a sibling mod's library namespace.
                byte[] bytes;
                try (InputStream in = jar.getInputStream(entry)) {
                    bytes = in.readAllBytes();
                }
                if (!looksLikeZip(bytes)) continue;

                String hash = sha256(bytes);
                String original = Path.of(entry.getName()).getFileName().toString();
                Path target = root.resolve(hash.substring(0, 16) + "-" + original);
                if (!Files.isRegularFile(target) || Files.size(target) != bytes.length) {
                    Path temp = target.resolveSibling(target.getFileName() + ".tmp");
                    Files.write(temp, bytes);
                    try {
                        Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING,
                                StandardCopyOption.ATOMIC_MOVE);
                    } catch (java.nio.file.AtomicMoveNotSupportedException ex) {
                        Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                if (isLibraryJar(target)) materialized.put(hash, target);
            }
        }
        return List.copyOf(materialized.values());
    }

    private static boolean isLibraryJar(Path jarPath) throws IOException {
        try (JarFile nested = new JarFile(jarPath.toFile(), false)) {
            // A nested artifact with Forge mods.toml is a mod, not a transparent
            // implementation library. It belongs in the dependency graph instead.
            if (nested.getJarEntry("META-INF/mods.toml") != null) return false;
            return nested.stream().anyMatch(e -> !e.isDirectory() && e.getName().endsWith(".class"));
        }
    }

    private static boolean looksLikeZip(byte[] bytes) {
        return bytes.length >= 4 && bytes[0] == 'P' && bytes[1] == 'K';
    }

    private static String sha256(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            return java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private static Path normalized(Path path) {
        return path.toAbsolutePath().normalize();
    }

    private static String safeBaseName(String name) {
        return name.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static List<Missing> append(List<Missing> source, Missing value) {
        ArrayList<Missing> result = new ArrayList<>(source);
        result.add(value);
        return List.copyOf(result);
    }

    private static boolean providedByProfile(String modId) {
        return "forge".equals(modId) || "minecraft".equals(modId);
    }
}
