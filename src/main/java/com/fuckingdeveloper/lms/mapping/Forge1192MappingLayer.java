package com.fuckingdeveloper.lms.mapping;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Handle;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Optional;

/**
 * Canonical mapping evidence for the Forge 1.19.2 profile.
 *
 * <p>Legacy symbols are never guessed. Minecraft 26.3 classes are inspected
 * as resources from the current runtime without initializing them. A legacy
 * method is resolved by exact owner + descriptor only when that descriptor
 * identifies a single current method.</p>
 */
public final class Forge1192MappingLayer {
    public enum Status {
        UNRESOLVED,
        IDENTITY_CANDIDATE,
        VERIFIED_IDENTITY,
        DESCRIPTOR_MATCH,
        OWNER_MISSING,
        AMBIGUOUS,
        FORGE_BRIDGE_REQUIRED
    }

    public record MethodCandidate(String symbol, String descriptor) {}
    public record Mapping(String legacySymbol, String currentSymbol, Status status, String reason) {}
    public record MethodSearch(String legacySymbol, List<MethodCandidate> candidates, String reason) {}
    public record MethodSemantics(String owner, String method, String descriptor, List<String> operations) {}

    private final Map<String, Mapping> mappings = new LinkedHashMap<>();
    private final ClassLoader runtimeLoader;
    private volatile RuntimeIndex runtimeIndex;

    public record IndexedMethod(String owner, String name, String descriptor, int access) {}
    public record RuntimeIndex(List<IndexedMethod> methods, String provenance) {}

    public Forge1192MappingLayer() {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        runtimeLoader = context != null ? context : Forge1192MappingLayer.class.getClassLoader();
    }

    public Mapping classifyClass(String legacyClass) {
        if (legacyClass.startsWith("net.minecraftforge.")) {
            return mappings.computeIfAbsent("class:" + legacyClass, key ->
                    new Mapping(legacyClass, "", Status.FORGE_BRIDGE_REQUIRED,
                            "Legacy Forge namespace requires NeoForge compatibility bridge or replacement"));
        }
        if (legacyClass.startsWith("net.minecraft.")) {
            return mappings.computeIfAbsent("class:" + legacyClass, key -> {
                if (openClass(legacyClass) != null) {
                    return new Mapping(legacyClass, legacyClass, Status.VERIFIED_IDENTITY,
                            "Class resource exists in the current Minecraft runtime");
                }
                return new Mapping(legacyClass, "", Status.OWNER_MISSING,
                        "Legacy Minecraft class is absent from the current runtime");
            });
        }
        return mappings.computeIfAbsent("class:" + legacyClass, key ->
                new Mapping(legacyClass, "", Status.UNRESOLVED, "No profile mapping evidence"));
    }

    public Mapping classifyMethod(String owner, String legacyMethod, String descriptor) {
        String legacySymbol = owner + "#" + legacyMethod + descriptor;
        return mappings.computeIfAbsent("method:" + legacySymbol,
                key -> inspectMethod(owner, legacyMethod, descriptor, legacySymbol));
    }

    public MethodSearch searchMethods(String owner, String legacyMethod, String descriptor) {
        String legacySymbol = owner + "#" + legacyMethod + descriptor;
        InputStream stream = openClass(owner);
        if (stream == null) {
            return new MethodSearch(legacySymbol, List.of(), "Owner class is absent from the current runtime");
        }

        List<MethodCandidate> candidates = new ArrayList<>();
        try (stream) {
            new ClassReader(stream).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc,
                                                 String signature, String[] exceptions) {
                    if (name.equals("<init>") || name.equals("<clinit>")) return null;
                    if (desc.equals(descriptor) || name.equals(legacyMethod)) {
                        candidates.add(new MethodCandidate(owner + "#" + name + desc, desc));
                    }
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        } catch (IOException | RuntimeException ex) {
            return new MethodSearch(legacySymbol, List.of(),
                    "Could not inspect current owner bytecode: " + ex.getClass().getSimpleName());
        }
        return new MethodSearch(legacySymbol, List.copyOf(candidates),
                candidates.isEmpty() ? "No same-name or same-descriptor current methods"
                        : "Candidates share the legacy method name or descriptor; semantic verification required");
    }

    /**
     * Search the current runtime for the recovered named identity, ignoring the legacy
     * invocation owner. This is intentionally narrow: exact method name + descriptor only.
     * It is useful when the declaring class moved or a former utility class disappeared.
     */
    public MethodSearch searchExactIdentity(String method, String descriptor) {
        RuntimeIndex index = runtimeIndex();
        List<MethodCandidate> candidates = index.methods().stream()
                .filter(candidate -> candidate.name().equals(method))
                .filter(candidate -> candidate.descriptor().equals(descriptor))
                .map(candidate -> new MethodCandidate(
                        candidate.owner() + "#" + candidate.name() + candidate.descriptor(),
                        candidate.descriptor()))
                .toList();
        return new MethodSearch(method + descriptor, List.copyOf(candidates),
                candidates.size() == 1
                        ? "Unique exact identity in target runtime index (" + index.provenance() + ")"
                        : candidates.isEmpty()
                        ? "No exact identity in target runtime index (" + index.provenance() + ")"
                        : "Multiple exact identities in target runtime index; owner evidence required");
    }

    public MethodSearch searchRuntimeByName(String method) {
        RuntimeIndex index = runtimeIndex();
        List<MethodCandidate> candidates = index.methods().stream()
                .filter(candidate -> candidate.name().equals(method))
                .map(candidate -> new MethodCandidate(
                        candidate.owner() + "#" + candidate.name() + candidate.descriptor(),
                        candidate.descriptor()))
                .toList();
        return new MethodSearch(method, List.copyOf(candidates),
                candidates.size() == 1
                        ? "Unique recovered name in target runtime index (" + index.provenance() + ")"
                        : candidates.isEmpty()
                        ? "Recovered name absent from target runtime index (" + index.provenance() + ")"
                        : "Recovered name has multiple target-runtime candidates; semantic verification required");
    }

    /**
     * Enumerates the actual target runtime without loading or initializing classes.
     * Java 9+ exposes module contents through the jrt filesystem even when the game
     * classes are supplied by a custom loader; Minecraft itself is additionally
     * discoverable from the loader's package resources when available. The index is
     * deliberately evidence-only: it never turns a candidate into a verified mapping.
     */
    public RuntimeIndex runtimeIndex() {
        RuntimeIndex cached = runtimeIndex;
        if (cached != null) return cached;
        synchronized (this) {
            if (runtimeIndex != null) return runtimeIndex;
            List<IndexedMethod> methods = new ArrayList<>();
            Set<String> visited = new HashSet<>();
            // ModDev/FML exposes already-defined Minecraft packages. Enumerate package
            // resources where the backing URL is a directory or jar filesystem.
            for (Package pkg : runtimeLoader.getDefinedPackages()) {
                String name = pkg.getName();
                if (!name.startsWith("net.minecraft.")) continue;
                String resource = name.replace('.', '/');
                try {
                    var urls = runtimeLoader.getResources(resource);
                    while (urls.hasMoreElements()) {
                        var url = urls.nextElement();
                        if ("file".equals(url.getProtocol())) {
                            Path dir = Path.of(url.toURI());
                            indexDirectory(dir, resource, methods, visited);
                        } else if ("jar".equals(url.getProtocol())) {
                            var connection = (java.net.JarURLConnection) url.openConnection();
                            try (var jar = connection.getJarFile()) {
                                var entries = jar.entries();
                                while (entries.hasMoreElements()) {
                                    var entry = entries.nextElement();
                                    String entryName = entry.getName();
                                    if (entryName.startsWith("net/minecraft/") && entryName.endsWith(".class")
                                            && visited.add(entryName)) {
                                        try (InputStream in = jar.getInputStream(entry)) {
                                            indexClass(in, methods);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                    // Partial coverage is represented in provenance and must not prove absence.
                }
            }
            runtimeIndex = new RuntimeIndex(List.copyOf(methods),
                    "defined-package resources, classes=" + visited.size() + ", methods=" + methods.size());
            return runtimeIndex;
        }
    }

    private void indexDirectory(Path packageDir, String resourcePrefix,
                                List<IndexedMethod> methods, Set<String> visited) {
        if (!Files.isDirectory(packageDir)) return;
        try (var walk = Files.walk(packageDir)) {
            walk.filter(path -> path.toString().endsWith(".class")).forEach(path -> {
                Path relative = packageDir.relativize(path);
                String entry = resourcePrefix + "/" + relative.toString().replace('\\', '/');
                if (!visited.add(entry)) return;
                try (InputStream in = Files.newInputStream(path)) {
                    indexClass(in, methods);
                } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {}
    }

    private void indexClass(InputStream in, List<IndexedMethod> methods) throws IOException {
        new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM9) {
            private String owner;
            @Override public void visit(int version, int access, String name, String signature,
                                        String superName, String[] interfaces) {
                owner = name.replace('/', '.');
            }
            @Override public MethodVisitor visitMethod(int access, String name, String desc,
                                                       String signature, String[] exceptions) {
                if (!name.equals("<init>") && !name.equals("<clinit>")) {
                    methods.add(new IndexedMethod(owner, name, desc, access));
                }
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }

    public MethodSemantics semantics(String symbol) {
        int hash = symbol.indexOf('#');
        int paren = symbol.indexOf('(', hash + 1);
        if (hash < 0 || paren < 0) return new MethodSemantics("", "", "", List.of());
        String owner = symbol.substring(0, hash);
        String method = symbol.substring(hash + 1, paren);
        String descriptor = symbol.substring(paren);
        InputStream stream = openClass(owner);
        if (stream == null) return new MethodSemantics(owner, method, descriptor, List.of());
        List<String> operations = new ArrayList<>();
        try (stream) {
            new ClassReader(stream).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override public MethodVisitor visitMethod(int access, String name, String desc,
                                                           String signature, String[] exceptions) {
                    if (!name.equals(method) || !desc.equals(descriptor)) return null;
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override public void visitMethodInsn(int opcode, String calledOwner, String calledName,
                                                              String calledDescriptor, boolean itf) {
                            operations.add("CALL " + calledOwner + "#" + calledName + calledDescriptor);
                        }
                        @Override public void visitFieldInsn(int opcode, String fieldOwner, String fieldName,
                                                             String fieldDescriptor) {
                            operations.add("FIELD " + fieldOwner + "#" + fieldName + ":" + fieldDescriptor);
                        }
                        @Override public void visitTypeInsn(int opcode, String type) {
                            operations.add("TYPE " + opcode + " " + type);
                        }
                        @Override public void visitInvokeDynamicInsn(String name, String desc, Handle bootstrap,
                                                                     Object... args) {
                            operations.add("INDY " + name + desc);
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        } catch (IOException | RuntimeException ignored) {
            return new MethodSemantics(owner, method, descriptor, List.of());
        }
        return new MethodSemantics(owner, method, descriptor, List.copyOf(operations));
    }

    /**
     * Find a unique current method whose descriptor and normalized bytecode-operation
     * fingerprint match a legacy runtime method. This is semantic evidence, not a
     * descriptor fallback: ambiguous or empty fingerprints never resolve.
     */
    public MethodSearch findUniqueSemanticMatch(String owner, String descriptor, List<String> legacyOperations) {
        String legacySymbol = owner + "#" + descriptor;
        if (legacyOperations == null || legacyOperations.isEmpty()) {
            return new MethodSearch(legacySymbol, List.of(), "Legacy semantic fingerprint is empty");
        }
        InputStream stream = openClass(owner);
        if (stream == null) {
            return new MethodSearch(legacySymbol, List.of(), "Owner class is absent from the current runtime");
        }

        List<MethodCandidate> descriptorCandidates = new ArrayList<>();
        try (stream) {
            new ClassReader(stream).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override public MethodVisitor visitMethod(int access, String name, String desc,
                                                           String signature, String[] exceptions) {
                    if (!name.equals("<init>") && !name.equals("<clinit>") && desc.equals(descriptor)) {
                        descriptorCandidates.add(new MethodCandidate(owner + "#" + name + desc, desc));
                    }
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        } catch (IOException | RuntimeException ex) {
            return new MethodSearch(legacySymbol, List.of(),
                    "Could not inspect current owner bytecode: " + ex.getClass().getSimpleName());
        }

        List<MethodCandidate> semanticMatches = descriptorCandidates.stream()
                .filter(candidate -> semantics(candidate.symbol()).operations().equals(legacyOperations))
                .toList();
        return new MethodSearch(legacySymbol, List.copyOf(semanticMatches),
                semanticMatches.size() == 1
                        ? "Unique current method matches exact legacy operation fingerprint"
                        : semanticMatches.isEmpty()
                        ? "No current same-descriptor method matches exact legacy operation fingerprint"
                        : "Multiple current methods match exact legacy operation fingerprint");
    }

    /**
     * Inspect a named current-runtime method even when its descriptor changed.
     * Exact name is strong migration evidence; overloaded names remain ambiguous.
     */
    public MethodSearch searchNamedMethods(String owner, String method) {
        String legacySymbol = owner + "#" + method;
        InputStream stream = openClass(owner);
        if (stream == null) {
            return new MethodSearch(legacySymbol, List.of(), "Owner class is absent from the current runtime");
        }
        List<MethodCandidate> candidates = new ArrayList<>();
        try (stream) {
            new ClassReader(stream).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override public MethodVisitor visitMethod(int access, String name, String desc,
                                                           String signature, String[] exceptions) {
                    if (name.equals(method)) {
                        candidates.add(new MethodCandidate(owner + "#" + name + desc, desc));
                    }
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        } catch (IOException | RuntimeException ex) {
            return new MethodSearch(legacySymbol, List.of(),
                    "Could not inspect current owner bytecode: " + ex.getClass().getSimpleName());
        }
        return new MethodSearch(legacySymbol, List.copyOf(candidates),
                candidates.size() == 1 ? "Unique current method preserves recovered legacy name"
                        : candidates.isEmpty() ? "Recovered legacy name is absent from current owner"
                        : "Recovered legacy name is overloaded in current owner");
    }

    /** Retained for callers that do not yet have owner/descriptor evidence. */
    public Mapping classifyMethod(String legacyMethod) {
        return mappings.computeIfAbsent("method:" + legacyMethod, key ->
                new Mapping(legacyMethod, "", Status.UNRESOLVED,
                        "Owner and descriptor are required for runtime method resolution"));
    }

    public Optional<Mapping> find(String symbol) {
        return Optional.ofNullable(mappings.get(symbol));
    }

    private Mapping inspectMethod(String owner, String legacyMethod, String descriptor, String legacySymbol) {
        InputStream stream = openClass(owner);
        if (stream == null) {
            return new Mapping(legacySymbol, "", Status.OWNER_MISSING,
                    "Legacy owner class is absent from the current runtime");
        }

        List<String> descriptorMatches = new ArrayList<>();
        boolean[] exact = {false};
        try (stream) {
            new ClassReader(stream).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc,
                                                 String signature, String[] exceptions) {
                    if (name.equals(legacyMethod) && desc.equals(descriptor)) exact[0] = true;
                    if (desc.equals(descriptor)) descriptorMatches.add(name);
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        } catch (IOException | RuntimeException ex) {
            return new Mapping(legacySymbol, "", Status.UNRESOLVED,
                    "Could not inspect current owner bytecode: " + ex.getClass().getSimpleName());
        }

        if (exact[0]) {
            return new Mapping(legacySymbol, owner + "#" + legacyMethod + descriptor,
                    Status.VERIFIED_IDENTITY, "Exact method name and descriptor exist in current runtime");
        }
        // The legacy invocation owner can be a subclass while the actual member is
        // declared by a superclass/interface. Exact recovered identity is stronger
        // evidence than any descriptor-only match declared directly on the subclass,
        // so resolve it before structural descriptor fallbacks.
        List<String> inheritedExactMatches = new ArrayList<>();
        collectHierarchyExactMatches(owner, legacyMethod, descriptor, new HashSet<>(), true, inheritedExactMatches);
        List<String> uniqueInheritedExactMatches = new ArrayList<>(new LinkedHashSet<>(inheritedExactMatches));
        if (uniqueInheritedExactMatches.size() == 1) {
            return new Mapping(legacySymbol, uniqueInheritedExactMatches.getFirst(), Status.VERIFIED_IDENTITY,
                    "Exact method name and descriptor exist in current runtime ancestor");
        }
        if (uniqueInheritedExactMatches.size() > 1) {
            return new Mapping(legacySymbol, "", Status.AMBIGUOUS,
                    "Exact method identity appears in multiple current ancestors: " + uniqueInheritedExactMatches);
        }

        if (descriptorMatches.size() == 1) {
            String current = descriptorMatches.getFirst();
            return new Mapping(legacySymbol, owner + "#" + current + descriptor,
                    Status.DESCRIPTOR_MATCH,
                    "Unique current method with the legacy descriptor; candidate requires semantic verification");
        }
        if (descriptorMatches.size() > 1) {
            return new Mapping(legacySymbol, "", Status.AMBIGUOUS,
                    "Multiple current methods share legacy descriptor: " + descriptorMatches);
        }

        // No exact identity survived; descriptor matching is structural evidence only.
        List<String> inheritedMatches = new ArrayList<>();
        collectHierarchyDescriptorMatches(owner, descriptor, new HashSet<>(), true, inheritedMatches);
        List<String> uniqueInheritedMatches = new ArrayList<>(new LinkedHashSet<>(inheritedMatches));
        if (uniqueInheritedMatches.size() == 1) {
            return new Mapping(legacySymbol, uniqueInheritedMatches.getFirst(), Status.DESCRIPTOR_MATCH,
                    "Unique current inherited method with the legacy descriptor; candidate requires semantic verification");
        }
        if (uniqueInheritedMatches.size() > 1) {
            return new Mapping(legacySymbol, "", Status.AMBIGUOUS,
                    "Multiple distinct inherited current methods share legacy descriptor: " + uniqueInheritedMatches);
        }
        return new Mapping(legacySymbol, "", Status.UNRESOLVED,
                "Owner exists, but no current method in its hierarchy has the legacy descriptor");
    }

    private void collectHierarchyExactMatches(String owner, String method, String descriptor,
                                              Set<String> visited, boolean skipOwnerMethods,
                                              List<String> matches) {
        String internalOwner = owner.replace('.', '/');
        if (!visited.add(internalOwner)) return;
        InputStream stream = openClass(owner);
        if (stream == null) return;
        try (stream) {
            new ClassReader(stream).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public void visit(int version, int access, String name, String signature,
                                  String superName, String[] interfaces) {
                    if (superName != null) {
                        collectHierarchyExactMatches(superName.replace('/', '.'), method, descriptor,
                                visited, false, matches);
                    }
                    if (interfaces != null) {
                        for (String implemented : interfaces) {
                            collectHierarchyExactMatches(implemented.replace('/', '.'), method, descriptor,
                                    visited, false, matches);
                        }
                    }
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String desc,
                                                 String signature, String[] exceptions) {
                    if (!skipOwnerMethods && name.equals(method) && desc.equals(descriptor)) {
                        matches.add(owner + "#" + name + desc);
                    }
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        } catch (IOException | RuntimeException ignored) {
            // Missing/unreadable ancestors cannot prove an exact identity.
        }
    }

    private void collectHierarchyDescriptorMatches(String owner, String descriptor, Set<String> visited,
                                                   boolean skipOwnerMethods, List<String> matches) {
        String internalOwner = owner.replace('.', '/');
        if (!visited.add(internalOwner)) return;
        InputStream stream = openClass(owner);
        if (stream == null) return;
        try (stream) {
            new ClassReader(stream).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public void visit(int version, int access, String name, String signature,
                                  String superName, String[] interfaces) {
                    if (superName != null) {
                        collectHierarchyDescriptorMatches(superName.replace('/', '.'), descriptor,
                                visited, false, matches);
                    }
                    if (interfaces != null) {
                        for (String implemented : interfaces) {
                            collectHierarchyDescriptorMatches(implemented.replace('/', '.'), descriptor,
                                    visited, false, matches);
                        }
                    }
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String desc,
                                                 String signature, String[] exceptions) {
                    if (!skipOwnerMethods && !name.equals("<init>") && !name.equals("<clinit>")
                            && desc.equals(descriptor)) {
                        matches.add(owner + "#" + name + desc);
                    }
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        } catch (IOException | RuntimeException ignored) {
            // A missing/unreadable ancestor is insufficient evidence for a match.
        }
    }

    private InputStream openClass(String className) {
        return runtimeLoader.getResourceAsStream(className.replace('.', '/') + ".class");
    }
}
