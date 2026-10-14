package com.fuckingdeveloper.lms.runtime;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;

/** Static Forge 1.19.2 entrypoint planning. Never defines or initializes the class. */
public final class Forge1192LifecyclePlanner {
    public enum Status { READY, BLOCKED }

    public record Constructor(String descriptor, int access, List<String> argumentTypes) {}
    public record Plan(String entrypoint, Status status, List<String> interfaces,
                       List<Constructor> constructors, List<String> forgeReferences,
                       String reason) {}

    public Plan plan(Path artifact, String binaryName) throws IOException {
        String entry = binaryName.replace('.', '/') + ".class";
        try (JarFile jar = new JarFile(artifact.toFile(), false)) {
            var jarEntry = jar.getJarEntry(entry);
            if (jarEntry == null) {
                return new Plan(binaryName, Status.BLOCKED, List.of(), List.of(), List.of(),
                        "Entrypoint class is absent from managed artifact");
            }
            try (InputStream in = jar.getInputStream(jarEntry)) {
                return inspect(binaryName, in.readAllBytes());
            }
        }
    }

    private Plan inspect(String binaryName, byte[] bytes) {
        List<String> interfaces = new ArrayList<>();
        List<Constructor> constructors = new ArrayList<>();
        List<String> forgeRefs = new ArrayList<>();

        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public void visit(int version, int access, String name, String signature,
                              String superName, String[] implemented) {
                if (implemented != null) {
                    Arrays.stream(implemented)
                            .map(value -> value.replace('/', '.'))
                            .forEach(interfaces::add);
                }
                collectForge(superName, forgeRefs);
                if (implemented != null) {
                    for (String value : implemented) collectForge(value, forgeRefs);
                }
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                if ("<init>".equals(name)) {
                    List<String> args = Arrays.stream(Type.getArgumentTypes(descriptor))
                            .map(Type::getClassName).toList();
                    constructors.add(new Constructor(descriptor, access, args));
                    for (Type type : Type.getArgumentTypes(descriptor)) {
                        if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
                            collectForge(type.getInternalName(), forgeRefs);
                        }
                    }
                }
                return null;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        constructors.sort(java.util.Comparator.comparing(Constructor::descriptor));
        interfaces.sort(String::compareTo);
        forgeRefs.sort(String::compareTo);

        if (constructors.isEmpty()) {
            return new Plan(binaryName, Status.BLOCKED, List.copyOf(interfaces),
                    List.of(), List.copyOf(forgeRefs), "No constructor found");
        }

        List<Constructor> publicConstructors = constructors.stream()
                .filter(c -> (c.access() & Opcodes.ACC_PUBLIC) != 0).toList();
        if (publicConstructors.isEmpty()) {
            return new Plan(binaryName, Status.BLOCKED, List.copyOf(interfaces),
                    List.copyOf(constructors), List.copyOf(forgeRefs),
                    "No public Forge mod constructor");
        }

        boolean supportedShape = publicConstructors.stream().anyMatch(c ->
                c.argumentTypes().isEmpty()
                        || c.argumentTypes().stream().allMatch(Forge1192LifecyclePlanner::knownForgeConstructorType));
        return new Plan(binaryName, supportedShape ? Status.READY : Status.BLOCKED,
                List.copyOf(interfaces), List.copyOf(constructors), List.copyOf(forgeRefs),
                supportedShape
                        ? "Public constructor shape can be adapted by Forge 1.19.2 lifecycle profile"
                        : "Public constructor requires unresolved Forge/FML services");
    }

    private static boolean knownForgeConstructorType(String type) {
        return type.equals("net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext")
                || type.equals("net.minecraftforge.eventbus.api.IEventBus")
                || type.equals("net.minecraftforge.fml.ModContainer");
    }

    private static void collectForge(String internalName, List<String> out) {
        if (internalName == null) return;
        String name = internalName.replace('/', '.');
        if ((name.startsWith("net.minecraftforge.") || name.startsWith("net.neoforged."))
                && !out.contains(name)) out.add(name);
    }
}
