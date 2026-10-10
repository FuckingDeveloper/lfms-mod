package com.fuckingdeveloper.lms.mapping;

import org.objectweb.asm.Type;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Resolves Forge 1.19.2 SRG method calls through version-scoped mapping evidence:
 * SRG -> 1.19.2 obfuscated declaration -> Mojang named identity -> target runtime.
 * Descriptor-only target matches are never authorized for rewriting.
 */
public final class Forge1192SrgRuntimeResolver {
    public record Resolution(String owner, String name, String descriptor, boolean resolved, String reason) {}

    private final LegacySrgIndex srg;
    private final ProguardMappingReader.Index mojmap;
    private final Forge1192MappingLayer target;

    private Forge1192SrgRuntimeResolver(LegacySrgIndex srg, ProguardMappingReader.Index mojmap) {
        this.srg = srg;
        this.mojmap = mojmap;
        this.target = new Forge1192MappingLayer();
    }

    public static Optional<Forge1192SrgRuntimeResolver> load(Path root) {
        try {
            var srg = LegacySrgIndex.load(root.resolve("legacy-mappings/1.19.2/joined.tsrg"));
            var moj = new MappingFileLoader().load(root.resolve("legacy-mappings/1.19.2/client.txt"));
            if (srg.isEmpty() || moj.isEmpty()) return Optional.empty();
            return Optional.of(new Forge1192SrgRuntimeResolver(srg.get(), moj.get().index()));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public Resolution resolve(String internalOwner, String srgName, String descriptor) {
        if (!internalOwner.startsWith("net/minecraft/") || !srgName.startsWith("m_")) {
            return unresolved(internalOwner, srgName, descriptor, "Not a vanilla SRG method call");
        }
        String owner = internalOwner.replace('/', '.');
        var legacy = srg.resolveWithNamedOwner(owner, srgName, descriptor, mojmap);
        if (legacy.status() != LegacySrgIndex.ResolutionStatus.RESOLVED || legacy.matches().size() != 1) {
            return unresolved(internalOwner, srgName, descriptor, "Legacy mapping unresolved: " + legacy.reason());
        }
        var match = legacy.matches().getFirst();
        if (match.owners().isEmpty()) {
            return unresolved(internalOwner, srgName, descriptor, "Legacy mapping has no declaring owner");
        }

        List<ProguardMappingReader.MethodMapping> named = mojmap
                .findByObfuscated(match.owners().getFirst(), match.sourceName()).stream()
                .filter(candidate -> parametersMatch(candidate.parameters(), descriptor))
                .toList();
        if (named.size() != 1) {
            return unresolved(internalOwner, srgName, descriptor,
                    "Mojang named identity is " + (named.isEmpty() ? "missing" : "ambiguous"));
        }

        var canonical = named.getFirst();
        String targetOwner = canonical.namedOwner();
        String targetDescriptor = descriptor;
        var current = target.classifyMethod(targetOwner, canonical.namedName(), targetDescriptor);
        if (current.status() != Forge1192MappingLayer.Status.VERIFIED_IDENTITY) {
            return unresolved(internalOwner, srgName, descriptor,
                    "Canonical method does not survive target runtime exactly: " + current.reason());
        }
        String symbol = current.currentSymbol();
        int hash = symbol.indexOf('#');
        int paren = symbol.indexOf('(', hash + 1);
        if (hash < 0 || paren < 0) return unresolved(internalOwner, srgName, descriptor, "Malformed target symbol");
        return new Resolution(symbol.substring(0, hash).replace('.', '/'),
                symbol.substring(hash + 1, paren), symbol.substring(paren), true,
                "Verified SRG -> Mojang named -> target runtime identity");
    }

    private static boolean parametersMatch(String parameters, String descriptor) {
        Type[] args = Type.getArgumentTypes(descriptor);
        if (parameters.isBlank()) return args.length == 0;
        String[] expected = parameters.split("\\s*,\\s*");
        if (expected.length != args.length) return false;
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equals(args[i].getClassName())) return false;
        }
        return true;
    }

    private static Resolution unresolved(String owner, String name, String descriptor, String reason) {
        return new Resolution(owner, name, descriptor, false, reason);
    }
}
