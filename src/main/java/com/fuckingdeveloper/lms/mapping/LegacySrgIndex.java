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
    public enum ResolutionStatus { RESOLVED, INHERITED_CANDIDATE, OWNER_MISMATCH, DESCRIPTOR_MISMATCH, SOURCE_DESCRIPTOR_MISMATCH, FORGE_SYMBOL, AMBIGUOUS, NOT_FOUND }
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
            List<Match> byName = findBySrgName(legacyName);
            String obfuscatedDescriptor = mojmap.obfuscateDescriptor(legacyDescriptor);
            List<Match> descriptorMatches = byName.stream()
                    .filter(match -> match.sourceDescriptor().equals(obfuscatedDescriptor))
                    .toList();
            if (descriptorMatches.size() == 1) {
                Match match = descriptorMatches.getFirst();
                String correctedOwner = mojmap.namedClass(match.owners().getFirst()).orElse("<unknown>");
                return new Resolution(ResolutionStatus.OWNER_MISMATCH, descriptorMatches,
                        "Legacy owner is absent from Mojang mappings; SRG name + fully remapped descriptor identify owner "
                                + correctedOwner + " (obf=" + match.owners().getFirst() + ")");
            }
            if (!descriptorMatches.isEmpty()) {
                return new Resolution(ResolutionStatus.AMBIGUOUS, descriptorMatches,
                        "Legacy owner is absent from Mojang mappings; multiple SRG name + descriptor matches remain");
            }
            if (!byName.isEmpty()) {
                return new Resolution(ResolutionStatus.OWNER_MISMATCH, byName,
                        "Legacy owner is absent from Mojang mappings; SRG name exists but descriptor does not identify a unique owner");
            }
            return new Resolution(ResolutionStatus.FORGE_SYMBOL, List.of(),
                    "Owner or method is outside Mojang mappings; requires Forge/API compatibility resolution");
        }
        List<Match> byName = findBySrgName(legacyName);
        List<Match> ownerMatches = byName.stream()
                .filter(match -> !match.owners().isEmpty()
                        && match.owners().getFirst().equals(obfuscatedOwner.get()))
                .toList();
        String obfuscatedDescriptor = mojmap.obfuscateDescriptor(legacyDescriptor);
        List<Match> exact = ownerMatches.stream()
                .filter(match -> match.sourceDescriptor().equals(obfuscatedDescriptor))
                .toList();
        if (exact.size() == 1) {
            return new Resolution(ResolutionStatus.RESOLVED, exact,
                    "Named owner, SRG name and fully remapped descriptor match");
        }
        if (exact.size() > 1) {
            return new Resolution(ResolutionStatus.AMBIGUOUS, exact,
                    "Multiple methods remain after named owner and descriptor-shape filtering");
        }
        if (!ownerMatches.isEmpty()) {
            return new Resolution(ResolutionStatus.DESCRIPTOR_MISMATCH, ownerMatches,
                    "SRG method exists on named owner but descriptor differs: mappedLegacy="
                            + obfuscatedDescriptor);
        }
        List<Match> compatible = byName.stream()
                .filter(match -> match.sourceDescriptor().equals(obfuscatedDescriptor))
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
        if (!byName.isEmpty()) {
            return new Resolution(ResolutionStatus.SOURCE_DESCRIPTOR_MISMATCH, byName,
                    "Coremod method name exists in 1.19.2 TSRG, but its source invocation descriptor "
                            + "does not match any mapped declaration; do not rewrite the descriptor "
                            + "without inspecting the original coremod instruction. Mojang owner="
                            + obfuscatedOwner.get() + ", mappedLegacyDescriptor=" + obfuscatedDescriptor
                            + ", TSRG candidateDescriptors=" + byName.stream()
                                    .map(Match::sourceDescriptor).distinct().toList());
        }
        return new Resolution(ResolutionStatus.NOT_FOUND, byName,
                "No SRG method matches named owner or descriptor shape; Mojang owner="
                        + obfuscatedOwner.get() + ", mappedLegacyDescriptor=" + obfuscatedDescriptor
                        + ", TSRG candidateDescriptors=" + byName.stream()
                                .map(Match::sourceDescriptor)
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
        if ((name.startsWith("m_") || name.startsWith("f_")) && name.endsWith("_") && name.length() > 3) {
            return name.substring(2, name.length() - 1);
        }
        return name;
    }

    public List<TsrgMappingReader.FieldEntry> findFieldsBySrgName(String name) {
        String normalized = normalizeSrgName(name);
        return index.fields().values().stream()
                .filter(entry -> entry.names().stream().anyMatch(candidate ->
                        candidate.equals(name) || normalizeSrgName(candidate).equals(normalized)))
                .distinct()
                .toList();
    }

    public int methodCount() {
        return index.methods().size();
    }

    public List<String> namespaces() {
        return index.namespaces();
    }
}
