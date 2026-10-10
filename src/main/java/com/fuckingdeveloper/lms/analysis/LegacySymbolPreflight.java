package com.fuckingdeveloper.lms.analysis;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipFile;

/**
 * Whole-JAR, non-executing inventory of external JVM type dependencies.
 * It intentionally separates structural uses from ordinary references so a
 * removed interface/superclass is visible before an entrypoint is defined.
 */
public final class LegacySymbolPreflight {
    public enum Use { SUPER, INTERFACE, NEW, DESCRIPTOR, TYPE_INSTRUCTION, METHOD_OWNER, FIELD_OWNER }
    public record Reference(String owner, String target, Use use) {}
    public record Report(List<Reference> references, Map<Use, Integer> counts,
                         int ownedClasses, int externalTypes) {}

    public Report analyze(Path file) throws IOException {
        List<Reference> refs = new ArrayList<>();
        TreeSet<String> owned = new TreeSet<>();
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
                        String owner;
                        @Override public void visit(int version, int access, String name, String signature,
                                                    String superName, String[] interfaces) {
                            owner = name;
                            add(refs, owned, owner, superName, Use.SUPER);
                            if (interfaces != null) for (String itf : interfaces) add(refs, owned, owner, itf, Use.INTERFACE);
                        }
                        @Override public org.objectweb.asm.FieldVisitor visitField(int access, String name,
                                String descriptor, String signature, Object value) {
                            addDescriptor(refs, owned, owner, descriptor);
                            return null;
                        }
                        @Override public MethodVisitor visitMethod(int access, String name, String descriptor,
                                String signature, String[] exceptions) {
                            addMethodDescriptor(refs, owned, owner, descriptor);
                            return new MethodVisitor(Opcodes.ASM9) {
                                @Override public void visitTypeInsn(int opcode, String type) {
                                    add(refs, owned, owner, type, opcode == Opcodes.NEW ? Use.NEW : Use.TYPE_INSTRUCTION);
                                }
                                @Override public void visitFieldInsn(int opcode, String targetOwner, String name, String desc) {
                                    add(refs, owned, owner, targetOwner, Use.FIELD_OWNER);
                                    addDescriptor(refs, owned, owner, desc);
                                }
                                @Override public void visitMethodInsn(int opcode, String targetOwner, String name,
                                        String desc, boolean itf) {
                                    add(refs, owned, owner, targetOwner, Use.METHOD_OWNER);
                                    addMethodDescriptor(refs, owned, owner, desc);
                                }
                            };
                        }
                    }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                }
            }
        }
        var unique = refs.stream().distinct()
                .sorted(java.util.Comparator.comparing(Reference::target)
                        .thenComparing(r -> r.use().name()).thenComparing(Reference::owner))
                .toList();
        Map<Use,Integer> counts = new EnumMap<>(Use.class);
        for (var r : unique) counts.merge(r.use(), 1, Integer::sum);
        int externalTypes = (int) unique.stream().map(Reference::target).distinct().count();
        return new Report(unique, Map.copyOf(counts), owned.size(), externalTypes);
    }

    private static void add(List<Reference> out, TreeSet<String> owned, String owner, String target, Use use) {
        if (target == null || target.startsWith("[") || owned.contains(target)
                || target.startsWith("java/") || target.startsWith("javax/")) return;
        out.add(new Reference(owner, target, use));
    }
    private static void addDescriptor(List<Reference> out, TreeSet<String> owned, String owner, String desc) {
        Type t = Type.getType(desc);
        addType(out, owned, owner, t);
    }
    private static void addMethodDescriptor(List<Reference> out, TreeSet<String> owned, String owner, String desc) {
        Type m = Type.getMethodType(desc);
        addType(out, owned, owner, m.getReturnType());
        for (Type t : m.getArgumentTypes()) addType(out, owned, owner, t);
    }
    private static void addType(List<Reference> out, TreeSet<String> owned, String owner, Type t) {
        while (t.getSort() == Type.ARRAY) t = t.getElementType();
        if (t.getSort() == Type.OBJECT) add(out, owned, owner, t.getInternalName(), Use.DESCRIPTOR);
    }
}
