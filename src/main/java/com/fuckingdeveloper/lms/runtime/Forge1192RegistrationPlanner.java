package com.fuckingdeveloper.lms.runtime;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;

/**
 * Bounded static call-graph walk from a legacy entrypoint constructor.
 * The walk follows only methods physically owned by the same artifact;
 * no legacy bytecode is defined or executed.
 */
public final class Forge1192RegistrationPlanner {
    public record MethodKey(String owner, String name, String descriptor) {}
    public record Boundary(String source, String owner, String name, String descriptor) {}
    public record Plan(int inspectedMethods, boolean complete, List<Boundary> boundaries,
                       List<String> unresolved, String reason) {}

    private static final int MAX_METHODS = 4096;

    public Plan plan(Path artifact, String entrypoint) throws IOException {
        Deque<MethodKey> pending = new ArrayDeque<>();
        pending.add(new MethodKey(entrypoint.replace('.', '/'), "<init>", "()V"));
        Set<MethodKey> visited = new HashSet<>();
        Set<Boundary> boundaries = new TreeSet<>(Comparator.comparing(Boundary::source)
                .thenComparing(Boundary::owner).thenComparing(Boundary::name)
                .thenComparing(Boundary::descriptor));
        Set<String> unresolved = new TreeSet<>();
        Map<String, Map<MethodKey, List<MethodKey>>> cache = new HashMap<>();

        try (JarFile jar = new JarFile(artifact.toFile(), false)) {
            while (!pending.isEmpty() && visited.size() < MAX_METHODS) {
                MethodKey current = pending.removeFirst();
                if (!visited.add(current)) continue;
                Map<MethodKey, List<MethodKey>> methods = cache.get(current.owner());
                if (methods == null) {
                    var entry = jar.getJarEntry(current.owner() + ".class");
                    if (entry == null) {
                        unresolved.add("missing-owned-class:" + current.owner());
                        continue;
                    }
                    byte[] bytes;
                    try (var input = jar.getInputStream(entry)) { bytes = input.readAllBytes(); }
                    methods = inspect(bytes);
                    cache.put(current.owner(), methods);
                }
                List<MethodKey> calls = methods.get(current);
                if (calls == null) {
                    unresolved.add("missing-method:" + current);
                    continue;
                }
                for (MethodKey call : calls) {
                    if (call.owner().startsWith("net/minecraftforge/")) {
                        boundaries.add(new Boundary(current.toString(), call.owner().replace('/', '.'),
                                call.name(), call.descriptor()));
                    } else if (jar.getJarEntry(call.owner() + ".class") != null
                            && !visited.contains(call)) {
                        pending.addLast(call);
                    }
                }
            }
        }
        boolean complete = pending.isEmpty() && unresolved.isEmpty();
        return new Plan(visited.size(), complete, List.copyOf(boundaries),
                List.copyOf(unresolved),
                complete ? "Owned call graph inspected; dynamic dispatch and reflection remain unproven"
                        : "Owned call graph incomplete; lifecycle execution must remain blocked");
    }

    private static Map<MethodKey, List<MethodKey>> inspect(byte[] bytes) {
        Map<MethodKey, List<MethodKey>> methods = new HashMap<>();
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            private String owner;
            @Override public void visit(int version, int access, String name, String signature,
                                        String superName, String[] interfaces) { owner = name; }
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                        String signature, String[] exceptions) {
                MethodKey key = new MethodKey(owner, name, descriptor);
                List<MethodKey> calls = new ArrayList<>();
                methods.put(key, calls);
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(int opcode, String target, String method,
                                                          String desc, boolean itf) {
                        calls.add(new MethodKey(target, method, desc));
                    }
                    @Override public void visitInvokeDynamicInsn(String name, String descriptor,
                                                                 Handle bootstrap, Object... args) {
                        for (Object arg : args) {
                            if (arg instanceof Handle handle) {
                                calls.add(new MethodKey(handle.getOwner(), handle.getName(), handle.getDesc()));
                            }
                        }
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return methods;
    }
}
