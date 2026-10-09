package com.fuckingdeveloper.lms.mapping;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
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

    private final Map<String, Mapping> mappings = new LinkedHashMap<>();
    private final ClassLoader runtimeLoader;

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
        List<MethodCandidate> candidates = new ArrayList<>();
        var roots = runtimeLoader.getDefinedPackages();
        // ClassLoader does not expose an enumerable class path, so use the already-known
        // Minecraft package roots only as metadata; global class enumeration is unavailable.
        // Callers should use candidate owners discovered from bytecode/mappings instead.
        return new MethodSearch(method + descriptor, List.copyOf(candidates),
                "Global runtime class enumeration is unavailable; candidate-owner discovery required");
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

        // The legacy invocation owner can be a subclass while the actual member is
        // declared by a superclass/interface. First preserve exact recovered method
        // identity across that hierarchy; only then fall back to descriptor shape.
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
