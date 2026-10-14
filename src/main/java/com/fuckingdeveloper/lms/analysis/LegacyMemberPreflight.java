package com.fuckingdeveloper.lms.analysis;

import org.objectweb.asm.*;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipFile;

/**
 * Whole-JAR, non-executing inventory of executable JVM member boundaries.
 * Unlike LegacySymbolPreflight this preserves owner/name/descriptor/opcode,
 * which is the minimum identity required for invocation/field migration.
 */
public final class LegacyMemberPreflight {
    public enum Kind { METHOD, FIELD }
    public record Boundary(String callerClass, String callerMethod, Kind kind,
                           int opcode, String owner, String name, String descriptor,
                           boolean interfaceCall) {
        public String identity() { return owner + "#" + name + descriptor; }
    }
    public record Report(List<Boundary> boundaries, int methods, int fields) {}

    public Report analyze(Path file) throws IOException {
        List<Boundary> out = new ArrayList<>();
        Set<String> owned = new HashSet<>();
        try (ZipFile jar = new ZipFile(file.toFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                var e = entries.nextElement();
                if (!e.isDirectory() && e.getName().endsWith(".class"))
                    owned.add(e.getName().substring(0, e.getName().length() - 6));
            }
            entries = jar.entries();
            while (entries.hasMoreElements()) {
                var e = entries.nextElement();
                if (e.isDirectory() || !e.getName().endsWith(".class")) continue;
                try (InputStream in = jar.getInputStream(e)) {
                    new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM9) {
                        String callerClass;
                        @Override public void visit(int version, int access, String name, String signature,
                                                    String superName, String[] interfaces) {
                            callerClass = name;
                        }
                        @Override public MethodVisitor visitMethod(int access, String methodName,
                                String methodDesc, String signature, String[] exceptions) {
                            String caller = methodName + methodDesc;
                            return new MethodVisitor(Opcodes.ASM9) {
                                @Override public void visitMethodInsn(int opcode, String owner, String name,
                                                                     String desc, boolean itf) {
                                    if (!isPlatform(owner) && !isOwned(owned, owner))
                                        out.add(new Boundary(callerClass, caller, Kind.METHOD,
                                                opcode, owner, name, desc, itf));
                                }
                                @Override public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                                    if (!isPlatform(owner) && !isOwned(owned, owner))
                                        out.add(new Boundary(callerClass, caller, Kind.FIELD,
                                                opcode, owner, name, desc, false));
                                }
                            };
                        }
                    }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                }
            }
        }
        var unique = out.stream().distinct()
                .sorted(Comparator.comparing(Boundary::owner).thenComparing(Boundary::name)
                        .thenComparing(Boundary::descriptor).thenComparing(Boundary::callerClass)
                        .thenComparing(Boundary::callerMethod))
                .toList();
        int methods = (int) unique.stream().filter(b -> b.kind() == Kind.METHOD).count();
        return new Report(unique, methods, unique.size() - methods);
    }

    private static boolean isOwned(Set<String> owned, String owner) {
        if (owned.contains(owner)) return true;
        if (owner.startsWith("[L") && owner.endsWith(";"))
            return owned.contains(owner.substring(2, owner.length() - 1));
        return false;
    }

    private static boolean isPlatform(String owner) {
        // JVM array clone is emitted with an array descriptor as the invocation
        // owner (for example "[Lpkg/Type;"). It is a VM operation, not a target
        // class member and must never enter API migration planning.
        return owner.startsWith("[") || owner.startsWith("java/") || owner.startsWith("javax/") || owner.startsWith("jdk/")
                || owner.startsWith("sun/");
    }
}
