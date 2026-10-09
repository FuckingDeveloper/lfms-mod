package com.fuckingdeveloper.lms.mapping;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lookup for Forge SRG method identifiers from an externally supplied TSRG file.
 * Source descriptors in TSRG are in the source namespace; they must not be
 * compared directly with named Minecraft descriptors.
 */
public final class LegacySrgIndex {
    public record Match(List<String> owners, String sourceName, String sourceDescriptor,
                        List<String> names, List<String> namespaces) {}
    private final TsrgMappingReader.Index index;

    private LegacySrgIndex(TsrgMappingReader.Index index) {
        this.index = index;
    }

    public static Optional<LegacySrgIndex> load(Path file) throws IOException {
        return new MappingFileLoader().loadTsrg(file).map(result -> new LegacySrgIndex(result.index()));
    }

    public List<Match> findBySrgName(String name) {
        String normalized = normalizeSrgName(name);
        List<Match> matches = new ArrayList<>();
        for (var entry : index.methods().values()) {
            boolean matched = entry.names().stream()
                    .anyMatch(candidate -> candidate.equals(name)
                            || candidate.equals(normalized)
                            || normalizeSrgName(candidate).equals(normalized));
            if (matched) {
                matches.add(new Match(entry.owners(), entry.names().getFirst(),
                        entry.descriptor(), entry.names(), index.namespaces()));
            }
        }
        return List.copyOf(matches);
    }

    private static String normalizeSrgName(String name) {
        if (name.startsWith("m_") && name.endsWith("_") && name.length() > 3) {
            return name.substring(2, name.length() - 1);
        }
        return name;
    }

    public int methodCount() {
        return index.methods().size();
    }

    public List<String> namespaces() {
        return index.namespaces();
    }
}
