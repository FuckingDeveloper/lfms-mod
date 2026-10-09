package com.fuckingdeveloper.lms.analysis;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

/**
 * Inspects an optional, user-provided 1.19.2 runtime JAR without defining or executing classes.
 * Input classes must use the same namespace as the queried symbols (typically named/Mojang).
 */
public final class LegacyRuntimeBytecodeInspector {
    public record MethodInfo(String name, String descriptor, int access) {}
    public record ClassInfo(String name, String parent, List<String> interfaces, List<MethodInfo> methods) {}
    public record Finding(String owner, String method, String descriptor, String status,
                          String declaringOwner, String detail) {}

    private final Map<String, ClassInfo> classes = new HashMap<>();

    public static LegacyRuntimeBytecodeInspector read(Path jar) throws IOException {
        LegacyRuntimeBytecodeInspector inspector = new LegacyRuntimeBytecodeInspector();
        if (!Files.isRegularFile(jar)) throw new IOException("Runtime JAR not found: " + jar);
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (!entry.getName().endsWith(".class") || entry.isDirectory()) continue;
                try (var stream = zip.getInputStream(entry)) {
                    ClassReader reader = new ClassReader(stream);
                    List<MethodInfo> methods = new ArrayList<>();
                    String[] parent = new String[1];
                    List<String> interfaces = new ArrayList<>();
                    reader.accept(new ClassVisitor(Opcodes.ASM9) {
                        @Override
                        public void visit(int version, int access, String name, String signature,
                                          String superName, String[] implemented) {
                            parent[0] = superName;
                            if (implemented != null) interfaces.addAll(List.of(implemented));
                        }
                        @Override
                        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                         String signature, String[] exceptions) {
                            methods.add(new MethodInfo(name, descriptor, access));
                            return null;
                        }
                    }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    inspector.classes.put(reader.getClassName(),
                            new ClassInfo(reader.getClassName(), parent[0], List.copyOf(interfaces), List.copyOf(methods)));
                } catch (IllegalArgumentException ex) {
                    throw new IOException("Cannot parse class " + entry.getName() + " in " + jar, ex);
                }
            }
        }
        return inspector;
    }

    public int classCount() { return classes.size(); }

    public Finding find(String owner, String method, String descriptor) {
        String internalOwner = owner.replace('.', '/');
        if (!classes.containsKey(internalOwner)) {
            return new Finding(owner, method, descriptor, "OWNER_NOT_IN_JAR", "",
                    "No class at this exact namespace/path; verify that the JAR is deobfuscated and includes this class");
        }
        return findRecursive(internalOwner, owner, method, descriptor, new HashSet<>());
    }

    private Finding findRecursive(String current, String requestedOwner, String method,
                                  String descriptor, Set<String> visited) {
        if (!visited.add(current)) return new Finding(requestedOwner, method, descriptor,
                "HIERARCHY_CYCLE", "", "Cycle in runtime class hierarchy");
        ClassInfo info = classes.get(current);
        if (info == null) return new Finding(requestedOwner, method, descriptor,
                "HIERARCHY_INCOMPLETE", "", "Missing ancestor class: " + current);
        for (MethodInfo candidate : info.methods()) {
            if (candidate.name().equals(method) && candidate.descriptor().equals(descriptor)) {
                return new Finding(requestedOwner, method, descriptor,
                        current.equals(requestedOwner.replace('.', '/')) ? "DECLARED" : "INHERITED",
                        current.replace('/', '.'), "Exact method name and descriptor in runtime bytecode");
            }
        }
        List<String> parents = new ArrayList<>();
        if (info.parent() != null) parents.add(info.parent());
        parents.addAll(info.interfaces());
        boolean incomplete = false;
        for (String parent : parents) {
            Finding result = findRecursive(parent, requestedOwner, method, descriptor, visited);
            if (result.status().equals("DECLARED") || result.status().equals("INHERITED")) {
                return new Finding(requestedOwner, method, descriptor, "INHERITED",
                        result.declaringOwner(), "Exact method in runtime ancestor " + result.declaringOwner());
            }
            if (result.status().equals("HIERARCHY_INCOMPLETE")) incomplete = true;
        }
        return new Finding(requestedOwner, method, descriptor,
                incomplete ? "NOT_FOUND_IN_INCOMPLETE_HIERARCHY" : "NOT_FOUND",
                "", incomplete ? "No match in available classes; at least one ancestor is missing"
                        : "No exact method in the inspected class hierarchy");
    }
}
