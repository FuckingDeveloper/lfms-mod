package com.fuckingdeveloper.lms.discovery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class LegacyJarScanner {
    private static final Pattern MOD_ID = Pattern.compile("(?m)^\\s*modId\\s*=\\s*[\"']([a-zA-Z0-9_]+)[\"']");
    private static final Pattern MC_RANGE = Pattern.compile("(?s)\\[\\[dependencies\\.[^]]+]](?:(?!\\[\\[).)*?modId\\s*=\\s*[\"']minecraft[\"'](?:(?!\\[\\[).)*?versionRange\\s*=\\s*[\"']([^\"']+)[\"']");

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
            Matcher id = MOD_ID.matcher(metadata);
            Matcher range = MC_RANGE.matcher(metadata);
            String modId = id.find() ? id.group(1) : "";
            String version = range.find() ? range.group(1) : "";
            return new LegacyModDescriptor(file, modId, "FORGE_METADATA", version,
                    "META-INF/mods.toml");
        } catch (IOException ex) {
            return new LegacyModDescriptor(file, "", "INVALID", "", ex.toString());
        }
    }
}
