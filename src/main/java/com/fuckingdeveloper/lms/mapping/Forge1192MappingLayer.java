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

    public record Mapping(String legacySymbol, String currentSymbol, Status status, String reason) {}

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
        return new Mapping(legacySymbol, "", Status.UNRESOLVED,
                "Owner exists, but no current method has the legacy descriptor");
    }

    private InputStream openClass(String className) {
        return runtimeLoader.getResourceAsStream(className.replace('.', '/') + ".class");
    }
}
