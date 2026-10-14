package com.fuckingdeveloper.lms.runtime;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Verifies the mechanically plausible Forge -> NeoForge namespace migration
 * against actual target bytecode without loading or initializing target classes.
 */
public final class Forge1192NeoForgeApiVerifier {
    public enum State { EXACT_TARGET, TARGET_CLASS_MISSING, TARGET_METHOD_MISSING }

    public record Verification(String legacyTarget, String targetOwner, String targetDescriptor,
                               State state, List<String> sameNameCandidates) {}

    public List<Verification> verify(List<Forge1192RegistrationPlanner.Boundary> boundaries,
                                     ClassLoader targetLoader) {
        return boundaries.stream()
                .map(boundary -> verify(boundary, targetLoader))
                .distinct()
                .sorted(java.util.Comparator.comparing(Verification::legacyTarget))
                .toList();
    }

    private Verification verify(Forge1192RegistrationPlanner.Boundary boundary,
                                ClassLoader targetLoader) {
        String legacyOwner = boundary.owner();
        String targetOwner = migrate(legacyOwner);
        String targetDescriptor = migrate(boundary.descriptor());
        String legacyTarget = legacyOwner + "#" + boundary.name() + boundary.descriptor();
        String resource = targetOwner.replace('.', '/') + ".class";

        try (InputStream in = openTargetClass(targetLoader, resource)) {
            if (in == null) {
                return new Verification(legacyTarget, targetOwner, targetDescriptor,
                        State.TARGET_CLASS_MISSING, List.of());
            }
            boolean[] found = {false};
            List<String> sameName = new java.util.ArrayList<>();
            new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                 String signature, String[] exceptions) {
                    if (boundary.name().equals(name)) {
                        sameName.add(descriptor);
                        if (targetDescriptor.equals(descriptor)) found[0] = true;
                    }
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return new Verification(legacyTarget, targetOwner, targetDescriptor,
                    found[0] ? State.EXACT_TARGET : State.TARGET_METHOD_MISSING,
                    List.copyOf(sameName));
        } catch (IOException e) {
            return new Verification(legacyTarget, targetOwner, targetDescriptor,
                    State.TARGET_CLASS_MISSING, List.of());
        }
    }

    private static InputStream openTargetClass(ClassLoader targetLoader, String resource)
            throws IOException {
        InputStream direct = targetLoader.getResourceAsStream(resource);
        if (direct != null) return direct;

        // FML/ModDev uses layered/module class loaders whose resources are not
        // necessarily exposed through ClassLoader#getResourceAsStream. Inspect
        // the actual runtime class path without defining target classes.
        String classPath = System.getProperty("java.class.path", "");
        for (String element : classPath.split(java.io.File.pathSeparator)) {
            if (element.isBlank()) continue;
            Path path = Paths.get(element);
            InputStream found = openFromPath(path, resource);
            if (found != null) return found;
        }

        // Also inspect code-source roots visible from representative NeoForge
        // classes. This covers development/module layers omitted from java.class.path.
        String[] anchors = {
                "net.neoforged.fml.ModList",
                "net.neoforged.neoforge.common.NeoForge"
        };
        for (String anchor : anchors) {
            try {
                Class<?> type = Class.forName(anchor, false, targetLoader);
                var source = type.getProtectionDomain().getCodeSource();
                if (source == null) continue;
                Path path = Paths.get(source.getLocation().toURI());
                InputStream found = openFromPath(path, resource);
                if (found != null) return found;
            } catch (ReflectiveOperationException | java.net.URISyntaxException | SecurityException ignored) {
                // Evidence source unavailable; continue with remaining roots.
            }
        }
        return null;
    }

    private static InputStream openFromPath(Path path, String resource) throws IOException {
        if (Files.isDirectory(path)) {
            Path candidate = path.resolve(resource);
            return Files.isRegularFile(candidate) ? Files.newInputStream(candidate) : null;
        }
        if (!Files.isRegularFile(path) || !path.toString().endsWith(".jar")) return null;
        java.util.jar.JarFile jar = new java.util.jar.JarFile(path.toFile(), false);
        var entry = jar.getJarEntry(resource);
        if (entry == null) {
            jar.close();
            return null;
        }
        InputStream raw = jar.getInputStream(entry);
        return new java.io.FilterInputStream(raw) {
            @Override public void close() throws IOException {
                try { super.close(); } finally { jar.close(); }
            }
        };
    }

    private static String migrate(String value) {
        // NeoForge split the old Forge namespace across the API implementation,
        // event bus and FML modules. A blanket net.minecraftforge -> net.neoforged
        // substitution produces non-existent classes.
        return value
                .replace("net.minecraftforge.eventbus.", "net.neoforged.bus.")
                .replace("net/minecraftforge/eventbus/", "net/neoforged/bus/")
                .replace("net.minecraftforge.fml.", "net.neoforged.fml.")
                .replace("net/minecraftforge/fml/", "net/neoforged/fml/")
                .replace("net.minecraftforge.forgespi.", "net.neoforged.neoforgespi.")
                .replace("net/minecraftforge/forgespi/", "net/neoforged/neoforgespi/")
                .replace("net.minecraftforge.", "net.neoforged.neoforge.")
                .replace("net/minecraftforge/", "net/neoforged/neoforge/");
    }
}
