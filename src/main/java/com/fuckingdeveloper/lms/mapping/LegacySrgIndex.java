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
    public enum ResolutionStatus { RESOLVED, INHERITED_CANDIDATE, OWNER_MISMATCH, DESCRIPTOR_MISMATCH, AMBIGUOUS, NOT_FOUND }
    public record Match(List<String> owners, String sourceName, String sourceDescriptor,
                        List<String> names, List<String> namespaces) {}
    public record Resolution(ResolutionStatus status, List<Match> matches, String reason) {}
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


    public Resolution resolveWithNamedOwner(String legacyOwner, String legacyName, String legacyDescriptor,
                                            ProguardMappingReader.Index mojmap) {
        var obfuscatedOwner = mojmap.obfuscatedClass(legacyOwner);
        if (obfuscatedOwner.isEmpty()) {
            return new Resolution(ResolutionStatus.NOT_FOUND, List.of(),
                    "Named owner is absent from Mojang mappings");
        }
        List<Match> byName = findBySrgName(legacyName);
        List<Match> ownerMatches = byName.stream()
                .filter(match -> !match.owners().isEmpty()
                        && match.owners().getFirst().equals(obfuscatedOwner.get()))
                .toList();
        List<Match> exact = ownerMatches.stream()
                .filter(match -> descriptorShape(match.sourceDescriptor()).equals(descriptorShape(legacyDescriptor)))
                .toList();
        if (exact.size() == 1) {
            return new Resolution(ResolutionStatus.RESOLVED, exact,
                    "Named owner, SRG name and descriptor shape match; object types still need exact remapping");
        }
        if (exact.size() > 1) {
            return new Resolution(ResolutionStatus.AMBIGUOUS, exact,
                    "Multiple methods remain after named owner and descriptor-shape filtering");
        }
        if (!ownerMatches.isEmpty()) {
            return new Resolution(ResolutionStatus.DESCRIPTOR_MISMATCH, ownerMatches,
                    "SRG method exists on named owner but descriptor shape differs: legacy="
                            + descriptorShape(legacyDescriptor));
        }
        List<Match> compatible = byName.stream()
                .filter(match -> descriptorShape(match.sourceDescriptor()).equals(descriptorShape(legacyDescriptor)))
                .toList();
        if (compatible.size() == 1) {
            Match match = compatible.getFirst();
            String declaringOwner = mojmap.namedClass(match.owners().getFirst()).orElse("<unknown>");
            return new Resolution(ResolutionStatus.INHERITED_CANDIDATE, compatible,
                    "Method found on different declaring owner " + declaringOwner
                            + " (obf=" + match.owners().getFirst() + "); inheritance not yet verified");
        }
        if (!compatible.isEmpty()) {
            return new Resolution(ResolutionStatus.OWNER_MISMATCH, compatible,
                    "Descriptor-shape candidates exist on other owners; inheritance not verified");
        }
        return new Resolution(ResolutionStatus.NOT_FOUND, byName,
                "No SRG method matches named owner or descriptor shape; Mojang owner="
                        + obfuscatedOwner.get() + ", legacy shape=" + descriptorShape(legacyDescriptor)
                        + ", TSRG candidate shapes=" + byName.stream()
                                .map(match -> descriptorShape(match.sourceDescriptor()))
                                .distinct().toList());
    }

    public Resolution resolve(String legacyOwner, String legacyName, String legacyDescriptor) {
        List<Match> byName = findBySrgName(legacyName);
        if (byName.isEmpty()) {
            return new Resolution(ResolutionStatus.NOT_FOUND, List.of(), "No TSRG method with legacy name");
        }

        List<Match> descriptorCompatible = byName.stream()
                .filter(match -> descriptorShape(match.sourceDescriptor()).equals(descriptorShape(legacyDescriptor)))
                .toList();
        List<Match> candidates = descriptorCompatible.isEmpty() ? byName : descriptorCompatible;
        if (candidates.size() == 1) {
            return new Resolution(ResolutionStatus.RESOLVED, candidates,
                    descriptorCompatible.isEmpty()
                            ? "Unique SRG name match; owner mapping still requires named class evidence"
                            : "Unique SRG name and descriptor-shape match; owner mapping still requires named class evidence");
        }
        return new Resolution(ResolutionStatus.AMBIGUOUS, candidates,
                "Multiple TSRG candidates remain; named owner mapping is required");
    }

    private static String descriptorShape(String descriptor) {
        StringBuilder shape = new StringBuilder();
        for (int i = 0; i < descriptor.length();) {
            char ch = descriptor.charAt(i);
            if (ch == 'L') {
                int end = descriptor.indexOf(';', i);
                if (end < 0) return descriptor;
                shape.append('L');
                i = end + 1;
            } else if (ch == '[') {
                shape.append('[');
                i++;
            } else {
                shape.append(ch);
                i++;
            }
        }
        return shape.toString();
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
