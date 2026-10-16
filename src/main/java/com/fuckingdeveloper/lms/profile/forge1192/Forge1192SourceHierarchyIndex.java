package com.fuckingdeveloper.lms.profile.forge1192;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.objectweb.asm.*;

/** Immutable source loader and vertex ABI declarations; never substitutes target inheritance for source inheritance. */
final class Forge1192SourceHierarchyIndex {
    private static final Map<String, byte[]> METHODS = new HashMap<>();
    private static final Map<String, byte[]> TYPES = load();
    private Forge1192SourceHierarchyIndex() {}
    static byte[] find(String owner) { return TYPES.get(owner); }
    static byte[] methodBytes(String owner) { return METHODS.get(owner); }
    static Set<String> owners() { return TYPES.keySet(); }
    private static Map<String, byte[]> load() {
        String resource = "/lms/profile/forge1192/loader-hierarchy.tsv";
        try (var input = Forge1192SourceHierarchyIndex.class.getResourceAsStream(resource)) {
            if (input == null) throw new IllegalStateException("Source Forge hierarchy evidence missing: " + resource);
            var result = new HashMap<String, byte[]>(); ClassWriter writer = null, methods = null; String owner = null;
            for (String line : new String(input.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] columns = line.split("\t", -1);
                if (columns[0].equals("C") && columns.length == 5) {
                    if (writer != null) { writer.visitEnd(); result.put(owner, writer.toByteArray()); methods.visitEnd(); METHODS.put(owner, methods.toByteArray()); }
                    owner = columns[1];
                    if ((!owner.startsWith("net/minecraftforge/") && !owner.startsWith("com/mojang/blaze3d/vertex/")) || result.containsKey(owner)) throw new IllegalStateException("Invalid source hierarchy owner " + owner);
                    writer = new ClassWriter(0); methods = new ClassWriter(0);
                    writer.visit(Opcodes.V17, Integer.parseInt(columns[2]), owner, null, columns[3].equals("-") ? null : columns[3], columns[4].equals("-") ? new String[0] : columns[4].split(","));
                    methods.visit(Opcodes.V17, Integer.parseInt(columns[2]), owner, null, columns[3].equals("-") ? null : columns[3], columns[4].equals("-") ? new String[0] : columns[4].split(","));
                } else if (columns[0].equals("M") && columns.length == 4 && methods != null) {
                    methods.visitMethod(Integer.parseInt(columns[1]), columns[2], columns[3], null, null).visitEnd();
                } else if (columns[0].equals("F") && columns.length == 4 && writer != null) {
                    writer.visitField(Integer.parseInt(columns[1]), columns[2], columns[3], null, null).visitEnd();
                } else throw new IllegalStateException("Malformed source hierarchy evidence: " + line);
            }
            if (writer != null) { writer.visitEnd(); result.put(owner, writer.toByteArray()); methods.visitEnd(); METHODS.put(owner, methods.toByteArray()); }
            return Map.copyOf(result);
        } catch (IOException failure) { throw new UncheckedIOException(failure); }
    }
}
