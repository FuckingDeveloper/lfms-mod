package com.fuckingdeveloper.lms.profile.forge1192.runtime.registry;

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
    public enum ExecutionPhase { RUNTIME, DATA_GENERATION }
    public record Boundary(String source, String owner, String name, String descriptor, ExecutionPhase phase) {
        public Boundary(String source,String owner,String name,String descriptor){this(source,owner,name,descriptor,ExecutionPhase.RUNTIME);}
    }
    private record Work(MethodKey method,ExecutionPhase phase){}
    private record Call(MethodKey method,ExecutionPhase phase){}
    public record Plan(int inspectedMethods, boolean complete, List<Boundary> boundaries,
                       List<String> unresolved, String reason) {}

    private static final int MAX_METHODS = 4096;

    public Plan plan(Path artifact, String entrypoint) throws IOException {
        Deque<Work> pending = new ArrayDeque<>();
        pending.add(new Work(new MethodKey(entrypoint.replace('.', '/'), "<init>", "()V"),ExecutionPhase.RUNTIME));
        Set<Work> visited = new HashSet<>();
        Set<Boundary> boundaries = new TreeSet<>(Comparator.comparing(Boundary::source)
                .thenComparing(Boundary::owner).thenComparing(Boundary::name)
                .thenComparing(Boundary::descriptor).thenComparing(Boundary::phase));
        Set<String> unresolved = new TreeSet<>();
        Map<String, ClassInfo> cache = new HashMap<>();

        try (JarFile jar = new JarFile(artifact.toFile(), false)) {
            while (!pending.isEmpty() && visited.size() < MAX_METHODS) {
                Work work = pending.removeFirst();
                if (!visited.add(work)) continue;
                MethodKey current=work.method();
                Resolution resolution = resolveOwnedMethod(jar, cache, current);
                if (resolution == null) {
                    // Reaching a superclass outside the managed artifact is a normal call-graph
                    // boundary (Minecraft/Forge/JDK/dependency), not evidence that an owned
                    // method is missing. Only a genuinely broken owned hierarchy is unresolved.
                    if (ownedHierarchyTerminatesExternally(jar, cache, current.owner())) continue;
                    unresolved.add("unresolved-owned-method:" + current);
                    continue;
                }
                List<Call> calls = resolution.calls();
                for (Call edge : calls) {
                    MethodKey call=edge.method();
                    ExecutionPhase phase=work.phase()==ExecutionPhase.DATA_GENERATION?work.phase():edge.phase();
                    if (call.owner().startsWith("net/minecraftforge/")) {
                        boundaries.add(new Boundary(current.toString(), call.owner().replace('/', '.'),
                                call.name(), call.descriptor(), phase));
                    } else if (jar.getJarEntry(call.owner() + ".class") != null
                            && !visited.contains(new Work(call,phase))) {
                        pending.addLast(new Work(call,phase));
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

    private record ClassInfo(String name, String superName, Map<MethodKey, List<Call>> methods) {}
    private record Resolution(MethodKey declaration, List<Call> calls) {}

    private static boolean ownedHierarchyTerminatesExternally(JarFile jar,
                                                              Map<String, ClassInfo> cache,
                                                              String owner) throws IOException {
        Set<String> seen = new HashSet<>();
        String current = owner;
        while (current != null && seen.add(current)) {
            ClassInfo info = readClass(jar, cache, current);
            if (info == null) return true;
            String parent = info.superName();
            if (parent == null) return true;
            if (jar.getJarEntry(parent + ".class") == null) return true;
            current = parent;
        }
        return false;
    }

    private static Resolution resolveOwnedMethod(JarFile jar, Map<String, ClassInfo> cache,
                                                 MethodKey symbolic) throws IOException {
        String owner = symbolic.owner();
        Set<String> seen = new HashSet<>();
        while (owner != null && seen.add(owner)) {
            ClassInfo info = readClass(jar, cache, owner);
            if (info == null) return null;
            MethodKey declared = new MethodKey(owner, symbolic.name(), symbolic.descriptor());
            List<Call> calls = info.methods().get(declared);
            if (calls != null) return new Resolution(declared, calls);
            String parent = info.superName();
            if (parent == null || jar.getJarEntry(parent + ".class") == null) return null;
            owner = parent;
        }
        return null;
    }

    private static ClassInfo readClass(JarFile jar, Map<String, ClassInfo> cache, String owner)
            throws IOException {
        ClassInfo cached = cache.get(owner);
        if (cached != null) return cached;
        var entry = jar.getJarEntry(owner + ".class");
        if (entry == null) return null;
        byte[] bytes;
        try (var input = jar.getInputStream(entry)) { bytes = input.readAllBytes(); }
        ClassInfo info = inspect(bytes);
        cache.put(owner, info);
        return info;
    }

    private static ClassInfo inspect(byte[] bytes) {
        Map<MethodKey, List<Call>> methods = new HashMap<>();
        String[] identity = new String[2];
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            private String owner;
            @Override public void visit(int version, int access, String name, String signature,
                                        String superName, String[] interfaces) {
                owner = name;
                identity[0] = name;
                identity[1] = superName;
            }
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                        String signature, String[] exceptions) {
                MethodKey key = new MethodKey(owner, name, descriptor);
                List<Call> calls = new ArrayList<>();
                methods.put(key, calls);
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override public void visitMethodInsn(int opcode, String target, String method,
                                                          String desc, boolean itf) {
                        calls.add(new Call(new MethodKey(target, method, desc),ExecutionPhase.RUNTIME));
                    }
                    @Override public void visitInvokeDynamicInsn(String name, String descriptor,
                                                                 Handle bootstrap, Object... args) {
                        for (Object arg : args) {
                            if (arg instanceof Handle handle) {
                                boolean dataCallback=bootstrap.getOwner().equals("java/lang/invoke/LambdaMetafactory")
                                        &&descriptor.endsWith("Ljava/util/function/Consumer;")
                                        &&handle.getDesc().endsWith("Lnet/minecraftforge/data/event/GatherDataEvent;)V");
                                calls.add(new Call(new MethodKey(handle.getOwner(), handle.getName(), handle.getDesc()),
                                        dataCallback?ExecutionPhase.DATA_GENERATION:ExecutionPhase.RUNTIME));
                            }
                        }
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return new ClassInfo(identity[0], identity[1], methods);
    }

}
