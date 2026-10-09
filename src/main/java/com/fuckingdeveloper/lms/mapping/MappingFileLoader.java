package com.fuckingdeveloper.lms.mapping;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Loads locally supplied mapping data. No downloads or legacy code execution occur.
 * Mojang's ProGuard files alone cannot resolve Forge SRG m_ identifiers.
 */
public final class MappingFileLoader {
    public record LoadResult(Path file, ProguardMappingReader.Index index) {}

    public Optional<LoadResult> load(Path file) throws IOException {
        if (!Files.isRegularFile(file)) return Optional.empty();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return Optional.of(new LoadResult(file, new ProguardMappingReader().read(reader)));
        }
    }
}
