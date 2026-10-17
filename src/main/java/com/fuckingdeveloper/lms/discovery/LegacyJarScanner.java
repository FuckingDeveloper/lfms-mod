package com.fuckingdeveloper.lms.discovery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class LegacyJarScanner {

    public List<LegacyModDescriptor> scan(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) return List.of();
        List<LegacyModDescriptor> result = new ArrayList<>();
        try (var stream = Files.list(directory)) {
            for (Path file : stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
                    .sorted(Comparator.comparing(Path::toString)).toList()) {
                result.add(inspect(file));
            }
        }
        return List.copyOf(result);
    }

    public LegacyModDescriptor inspect(Path file) {
        try (ZipFile jar = new ZipFile(file.toFile())) {
            ZipEntry entry = jar.getEntry("META-INF/mods.toml");
            if (entry == null) return new LegacyModDescriptor(file, "", "UNKNOWN", "", "No Forge mods.toml");
            String metadata;
            try (var in = jar.getInputStream(entry)) {
                metadata = new String(in.readNBytes(1024 * 1024), StandardCharsets.UTF_8);
            }
            var config = new com.electronwill.nightconfig.toml.TomlParser().parse(metadata);
            List<com.electronwill.nightconfig.core.UnmodifiableConfig> mods = config.getOrElse("mods", List.of());
            String modId = mods.isEmpty() ? "" : mods.getFirst().getOrElse("modId", "");
            String version = "";
            List<com.electronwill.nightconfig.core.UnmodifiableConfig> dependencies = config.getOrElse(List.of("dependencies", modId), List.of());
            for (var dependency : dependencies) if ("minecraft".equals(dependency.getOrElse("modId", ""))) {
                String declared = dependency.getOrElse("versionRange", "");
                if (declared.isBlank() || !version.isEmpty())
                    return new LegacyModDescriptor(file, modId, "INVALID", "", "Missing/duplicate Minecraft dependency range");
                version = declared;
            }
            String language = config.getOrElse("modLoader", "");
            String languageVersion = config.getOrElse("loaderVersion", "");
            return new LegacyModDescriptor(file, modId, "FORGE_METADATA", version,
                    "META-INF/mods.toml; modLoader=" + language + "; loaderVersion=" + languageVersion,
                    language, languageVersion);
        } catch (IOException | com.electronwill.nightconfig.core.io.ParsingException ex) {
            return new LegacyModDescriptor(file, "", "INVALID", "", ex.toString());
        }
    }
}
