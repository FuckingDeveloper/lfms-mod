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
    public record FieldResolution(String owner, String name, String descriptor, boolean resolved, String reason) {}

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
        if (!internalOwner.startsWith("net/minecraft/") || !isSrgMethodName(srgName)) {
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

    /**
     * Resolve a source SRG method by Mojang identity when the declaring class
     * has an explicit, independently verified target replacement. Both source
     * and target descriptors are supplied because class relocation may also
     * migrate parameter or return types.
     */
    public Resolution resolveRelocatedMethod(
            String sourceOwner, String srgName, String sourceDescriptor,
            String targetOwner, String targetDescriptor) {
        if (!sourceOwner.startsWith("net/minecraft/") || !isSrgMethodName(srgName)) {
            return unresolved(sourceOwner, srgName, sourceDescriptor,
                    "Not a vanilla SRG method call");
        }
        var legacy = srg.resolveWithNamedOwner(
                sourceOwner.replace('/', '.'), srgName, sourceDescriptor, mojmap);
        if (legacy.status() != LegacySrgIndex.ResolutionStatus.RESOLVED
                || legacy.matches().size() != 1) {
            return unresolved(sourceOwner, srgName, sourceDescriptor,
                    "Legacy mapping unresolved: " + legacy.reason());
        }
        var match = legacy.matches().getFirst();
        if (match.owners().isEmpty()) {
            return unresolved(sourceOwner, srgName, sourceDescriptor,
                    "Legacy mapping has no declaring owner");
        }
        List<ProguardMappingReader.MethodMapping> named = mojmap
                .findByObfuscated(match.owners().getFirst(), match.sourceName()).stream()
                .filter(candidate -> parametersMatch(candidate.parameters(), sourceDescriptor))
                .toList();
        if (named.size() != 1) {
            return unresolved(sourceOwner, srgName, sourceDescriptor,
                    "Mojang named identity is " + (named.isEmpty() ? "missing" : "ambiguous"));
        }
        String canonicalName = named.getFirst().namedName();
        var current = target.classifyMethod(
                targetOwner.replace('/', '.'), canonicalName, targetDescriptor);
        if (current.status() != Forge1192MappingLayer.Status.VERIFIED_IDENTITY) {
            return unresolved(sourceOwner, srgName, sourceDescriptor,
                    "Canonical relocated method does not survive target runtime exactly: "
                            + current.reason());
        }
        String symbol = current.currentSymbol();
        int hash = symbol.indexOf('#');
        int paren = symbol.indexOf('(', hash + 1);
        if (hash < 0 || paren < 0) {
            return unresolved(sourceOwner, srgName, sourceDescriptor,
                    "Malformed target symbol");
        }
        return new Resolution(symbol.substring(0, hash).replace('.', '/'),
                symbol.substring(hash + 1, paren), symbol.substring(paren), true,
                "Verified SRG -> Mojang named -> explicit relocated target method identity");
    }

    /**
     * Resolve an exact SRG/Mojang method identity whose target declaration
     * narrows only its reference return type. Parameters must remain exact and
     * the target return must be JVM-assignable to the legacy return contract.
     */
    public Resolution resolveCovariantReturn(
            String internalOwner, String srgName, String descriptor) {
        if (!internalOwner.startsWith("net/minecraft/") || !isSrgMethodName(srgName)) {
            return unresolved(internalOwner, srgName, descriptor,
                    "Not a vanilla SRG method call");
        }
        var legacy = srg.resolveWithNamedOwner(
                internalOwner.replace('/', '.'), srgName, descriptor, mojmap);
        if (legacy.status() != LegacySrgIndex.ResolutionStatus.RESOLVED
                || legacy.matches().size() != 1
                || legacy.matches().getFirst().owners().isEmpty()) {
            return unresolved(internalOwner, srgName, descriptor,
                    "Legacy mapping missing or ambiguous");
        }
        var match = legacy.matches().getFirst();
        List<ProguardMappingReader.MethodMapping> named = mojmap
                .findByObfuscated(match.owners().getFirst(), match.sourceName()).stream()
                .filter(candidate -> parametersMatch(candidate.parameters(), descriptor))
                .toList();
        if (named.size() != 1) {
            return unresolved(internalOwner, srgName, descriptor,
                    "Mojang named identity is missing or ambiguous");
        }

        String canonicalName = named.getFirst().namedName();
        Type[] sourceArguments = Type.getArgumentTypes(descriptor);
        Type sourceReturn = Type.getReturnType(descriptor);
        var candidates = target.searchNamedMethods(
                        internalOwner.replace('/', '.'), canonicalName).candidates().stream()
                .filter(candidate -> java.util.Arrays.equals(
                        sourceArguments, Type.getArgumentTypes(candidate.descriptor())))
                .filter(candidate -> isAssignableReturn(
                        sourceReturn, Type.getReturnType(candidate.descriptor())))
                .toList();
        if (candidates.size() != 1) {
            return unresolved(internalOwner, srgName, descriptor,
                    "Canonical target with covariant return is "
                            + (candidates.isEmpty() ? "missing" : "ambiguous"));
        }
        String symbol = candidates.getFirst().symbol();
        int hash = symbol.indexOf('#');
        int paren = symbol.indexOf('(', hash + 1);
        if (hash < 0 || paren < 0) {
            return unresolved(internalOwner, srgName, descriptor,
                    "Malformed target symbol");
        }
        return new Resolution(symbol.substring(0, hash).replace('.', '/'),
                symbol.substring(hash + 1, paren), symbol.substring(paren), true,
                "Verified SRG -> Mojang named -> covariant target return identity");
    }

    private static boolean isAssignableReturn(Type source, Type target) {
        if (source.equals(target)) return true;
        if ((source.getSort() != Type.OBJECT && source.getSort() != Type.ARRAY)
                || (target.getSort() != Type.OBJECT && target.getSort() != Type.ARRAY)) return false;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null) loader = Forge1192SrgRuntimeResolver.class.getClassLoader();
            Class<?> sourceClass = Class.forName(source.getClassName(), false, loader);
            Class<?> targetClass = Class.forName(target.getClassName(), false, loader);
            return sourceClass.isAssignableFrom(targetClass);
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }

    /** Resolve only the exact 1.19.2 Mojang method identity. */
    public Optional<String> canonicalMethodName(
            String internalOwner, String srgName, String descriptor) {
        if (!internalOwner.startsWith("net/minecraft/") || !isSrgMethodName(srgName))
            return Optional.empty();
        var legacy = srg.resolveWithNamedOwner(
                internalOwner.replace('/', '.'), srgName, descriptor, mojmap);
        if (legacy.status() != LegacySrgIndex.ResolutionStatus.RESOLVED
                || legacy.matches().size() != 1
                || legacy.matches().getFirst().owners().isEmpty()) return Optional.empty();
        var match = legacy.matches().getFirst();
        List<ProguardMappingReader.MethodMapping> named = mojmap
                .findByObfuscated(match.owners().getFirst(), match.sourceName()).stream()
                .filter(candidate -> parametersMatch(candidate.parameters(), descriptor))
                .toList();
        return named.size() == 1 ? Optional.of(named.getFirst().namedName()) : Optional.empty();
    }

    public FieldResolution resolveField(String internalOwner, String srgName, String descriptor) {
        if (!internalOwner.startsWith("net/minecraft/") || !srgName.startsWith("f_")) {
            return new FieldResolution(internalOwner, srgName, descriptor, false, "Not a vanilla SRG field");
        }
        String namedOwner = internalOwner.replace('/', '.');
        var obfuscatedOwner = mojmap.obfuscatedClass(namedOwner);
        if (obfuscatedOwner.isEmpty()) {
            return new FieldResolution(internalOwner, srgName, descriptor, false, "Legacy owner absent from Mojang mappings");
        }
        var srgFields = srg.findFieldsBySrgName(srgName).stream()
                .filter(field -> !field.owners().isEmpty() && field.owners().getFirst().equals(obfuscatedOwner.get()))
                .toList();
        if (srgFields.size() != 1 || srgFields.getFirst().names().isEmpty()) {
            return new FieldResolution(internalOwner, srgName, descriptor, false, "SRG field identity missing or ambiguous");
        }
        String obfuscatedName = srgFields.getFirst().names().getFirst();
        var namedFields = mojmap.findFieldByObfuscated(obfuscatedOwner.get(), obfuscatedName);
        if (namedFields.size() != 1) {
            return new FieldResolution(internalOwner, srgName, descriptor, false, "Mojang field identity missing or ambiguous");
        }
        var canonical = namedFields.getFirst();
        String targetOwner = canonical.namedOwner().replace('.', '/');
        var targetFields = target.searchExactField(targetOwner, canonical.namedName(), descriptor);
        if (targetFields.size() != 1) {
            return new FieldResolution(internalOwner, srgName, descriptor, false, "Canonical field absent or ambiguous in target runtime");
        }
        var field = targetFields.getFirst();
        return new FieldResolution(field.owner().replace('.', '/'), field.name(), field.descriptor(), true,
                "Verified SRG -> Mojang named -> target runtime field identity");
    }

    private static boolean isSrgMethodName(String name) {
        // Forge 1.19.2 record accessors can share their backing field's f_* SRG
        // identity even though the class-file member is a method.
        return name.startsWith("m_") || name.startsWith("f_");
    }

    /** Resolve only the exact 1.19.2 Mojang field identity, without assuming it survives. */
    public Optional<String> canonicalFieldName(
            String internalOwner, String srgName, String descriptor) {
        if (!internalOwner.startsWith("net/minecraft/") || !srgName.startsWith("f_"))
            return Optional.empty();
        var obfuscatedOwner = mojmap.obfuscatedClass(internalOwner.replace('/', '.'));
        if (obfuscatedOwner.isEmpty()) return Optional.empty();
        var srgFields = srg.findFieldsBySrgName(srgName).stream()
                .filter(field -> !field.owners().isEmpty()
                        && field.owners().getFirst().equals(obfuscatedOwner.get()))
                .toList();
        if (srgFields.size() != 1 || srgFields.getFirst().names().isEmpty())
            return Optional.empty();
        var namedFields = mojmap.findFieldByObfuscated(
                obfuscatedOwner.get(), srgFields.getFirst().names().getFirst());
        if (namedFields.size() != 1) return Optional.empty();
        return Optional.of(namedFields.getFirst().namedName());
    }

    /**
     * Resolve a source SRG field by Mojang identity when the containing class
     * has an explicit, independently verified target replacement. This keeps
     * class relocation policy outside the mapping resolver while still
     * requiring exact owner, member and descriptor evidence at both ends.
     */
    public FieldResolution resolveRelocatedField(
            String sourceOwner, String srgName, String sourceDescriptor,
            String targetOwner, String targetDescriptor) {
        if (!sourceOwner.startsWith("net/minecraft/") || !srgName.startsWith("f_")) {
            return new FieldResolution(sourceOwner, srgName, sourceDescriptor, false,
                    "Not a vanilla SRG field");
        }
        String namedOwner = sourceOwner.replace('/', '.');
        var obfuscatedOwner = mojmap.obfuscatedClass(namedOwner);
        if (obfuscatedOwner.isEmpty()) {
            return new FieldResolution(sourceOwner, srgName, sourceDescriptor, false,
                    "Legacy owner absent from Mojang mappings");
        }
        var srgFields = srg.findFieldsBySrgName(srgName).stream()
                .filter(field -> !field.owners().isEmpty()
                        && field.owners().getFirst().equals(obfuscatedOwner.get()))
                .toList();
        if (srgFields.size() != 1 || srgFields.getFirst().names().isEmpty()) {
            return new FieldResolution(sourceOwner, srgName, sourceDescriptor, false,
                    "SRG field identity missing or ambiguous");
        }
        var namedFields = mojmap.findFieldByObfuscated(
                obfuscatedOwner.get(), srgFields.getFirst().names().getFirst());
        if (namedFields.size() != 1) {
            return new FieldResolution(sourceOwner, srgName, sourceDescriptor, false,
                    "Mojang field identity missing or ambiguous");
        }
        String canonicalName = namedFields.getFirst().namedName();
        var targetFields = target.searchExactField(
                targetOwner.replace('/', '.'), canonicalName, targetDescriptor);
        if (targetFields.size() != 1) {
            return new FieldResolution(sourceOwner, srgName, sourceDescriptor, false,
                    "Canonical relocated field absent or ambiguous in target runtime");
        }
        var field = targetFields.getFirst();
        return new FieldResolution(field.owner().replace('.', '/'), field.name(),
                field.descriptor(), true,
                "Verified SRG -> Mojang named -> explicit relocated target field identity");
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
