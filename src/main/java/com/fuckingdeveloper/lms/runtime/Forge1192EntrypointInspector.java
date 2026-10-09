package com.fuckingdeveloper.lms.runtime;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarFile;

/**
 * Static call inventory for a Forge 1.19.2 @Mod constructor.
 * It never defines the class and therefore cannot trigger mod initialization.
 */
public final class Forge1192EntrypointInspector {
    public enum Kind { FORGE, MINECRAFT, OWNED, JDK, OTHER }

    public record Call(int opcode, String owner, String name, String descriptor, Kind kind) {}
    public record Report(String entrypoint, List<Call> calls, List<Call> boundaryCalls) {}

    public Report inspect(Path artifact, String binaryName) throws IOException {
        String internalName = binaryName.replace('.', '/');
        String entryName = internalName + ".class";
        try (JarFile jar = new JarFile(artifact.toFile(), false)) {
            var entry = jar.getJarEntry(entryName);
            if (entry == null) throw new IOException("Entrypoint class missing: " + binaryName);
            byte[] bytes;
            try (InputStream in = jar.getInputStream(entry)) {
                bytes = in.readAllBytes();
            }

            List<Call> calls = new ArrayList<>();
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                 String signature, String[] exceptions) {
                    if (!"<init>".equals(name) || !"()V".equals(descriptor)) return null;
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String methodName,
                                                    String methodDescriptor, boolean isInterface) {
                            calls.add(new Call(opcode, owner.replace('/', '.'), methodName,
                                    methodDescriptor, classify(jar, owner)));
                        }

                        @Override
                        public void visitInvokeDynamicInsn(String name, String descriptor,
                                                           Handle bootstrapMethodHandle,
                                                           Object... bootstrapMethodArguments) {
                            for (Object argument : bootstrapMethodArguments) {
                                if (argument instanceof Handle handle) {
                                    calls.add(new Call(handle.getTag(), handle.getOwner().replace('/', '.'),
                                            handle.getName(), handle.getDesc(), classify(jar, handle.getOwner())));
                                } else if (argument instanceof Type type && type.getSort() == Type.METHOD) {
                                    // Method type alone has no owner and is therefore not a call boundary.
                                }
                            }
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

            List<Call> boundaries = calls.stream()
                    .filter(call -> call.kind() == Kind.FORGE || call.kind() == Kind.MINECRAFT)
                    .distinct()
                    .sorted(Comparator.comparing(Call::owner).thenComparing(Call::name)
                            .thenComparing(Call::descriptor))
                    .toList();
            return new Report(binaryName, List.copyOf(calls), boundaries);
        }
    }

    private static Kind classify(JarFile jar, String owner) {
        if (owner.startsWith("net/minecraftforge/")) return Kind.FORGE;
        if (owner.startsWith("net/minecraft/")) return Kind.MINECRAFT;
        if (owner.startsWith("java/") || owner.startsWith("javax/") || owner.startsWith("jdk/")) return Kind.JDK;
        if (jar.getJarEntry(owner + ".class") != null) return Kind.OWNED;
        return Kind.OTHER;
    }
}
