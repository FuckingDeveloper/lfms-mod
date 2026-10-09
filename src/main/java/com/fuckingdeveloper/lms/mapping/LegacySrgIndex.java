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
    public record Match(String sourceOwner, String sourceName, String sourceDescriptor,
                        String mappedName, List<String> namespaces) {}
    private final TsrgMappingReader.Index index;

    private LegacySrgIndex(TsrgMappingReader.Index index) {
        this.index = index;
    }

    public static Optional<LegacySrgIndex> load(Path file) throws IOException {
        return new MappingFileLoader().loadTsrg(file).map(result -> new LegacySrgIndex(result.index()));
    }

    public List<Match> findBySrgName(String name) {
        List<Match> matches = new ArrayList<>();
        for (var entry : index.methods().values()) {
            if (entry.names().contains(name)) {
                matches.add(new Match(entry.owner(), entry.names().getFirst(),
                        entry.descriptor(), entry.names().getLast(), index.namespaces()));
            }
        }
        return List.copyOf(matches);
    }

    public int methodCount() {
        return index.methods().size();
    }

    public List<String> namespaces() {
        return index.namespaces();
    }
}
