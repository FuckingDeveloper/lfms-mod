package com.fuckingdeveloper.lms.launch;

import org.objectweb.asm.Opcodes;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

final class LaunchPlanReader {
    record Plan(List<GenericMethodCallRedirectProcessor.Spec> redirects,
                List<GenericInstructionEditProcessor.Spec> edits) {}

    private LaunchPlanReader() {}

    static Plan read(Path path) throws IOException {
        if (!Files.isRegularFile(path)) return new Plan(List.of(), List.of());
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !lines.getFirst().equals("LMSPLAN\t2")) {
            throw new IOException("Unsupported LMS launch plan format");
        }
        List<GenericMethodCallRedirectProcessor.Spec> redirects = new ArrayList<>();
        Map<String, EditBuilder> edits = new LinkedHashMap<>();
        for (int n = 1; n < lines.size(); n++) {
            if (lines.get(n).isBlank()) continue;
            String[] p = split(lines.get(n));
            switch (p[0]) {
                case "REDIRECT" -> {
                    if (p.length != 14) throw malformed(n, p);
                    redirects.add(new GenericMethodCallRedirectProcessor.Spec(
                            p[1], p[2], p[3], p[4], ref(p, 6), ref(p, 10)));
                }
                case "EDIT" -> {
                    if (p.length != 5) throw malformed(n, p);
                    edits.put(p[1], new EditBuilder(p[1], p[2], p[3], p[4]));
                }
                case "ANCHOR" -> {
                    if (p.length != 7) throw malformed(n, p);
                    builder(edits, p[1], n).anchors.add(new GenericInstructionEditProcessor.Anchor(
                            p[2], editRef(p, 3)));
                }
                case "OP" -> {
                    if (p.length != 7) throw malformed(n, p);
                    var b = builder(edits, p[1], n);
                    int index = Integer.parseInt(p[2]);
                    if (index != b.operations.size()) throw new IOException("Non-sequential OP at line " + (n + 1));
                    b.operations.add(new EditOperation(
                            GenericInstructionEditProcessor.EditKind.valueOf(p[3]),
                            reference(p, 4), new ArrayList<>()));
                }
                case "VAL" -> readValue(edits, p, n);
                case "END" -> {
                    if (p.length != 2) throw malformed(n, p);
                }
                default -> throw new IOException("Unknown LMS launch plan record at line " + (n + 1) + ": " + p[0]);
            }
        }
        List<GenericInstructionEditProcessor.Spec> editSpecs = edits.values().stream()
                .map(EditBuilder::build).toList();
        return new Plan(List.copyOf(redirects), List.copyOf(editSpecs));
    }

    private static void readValue(Map<String, EditBuilder> edits, String[] p, int n) throws IOException {
        if (p.length < 5) throw malformed(n, p);
        var b = builder(edits, p[1], n);
        int index = Integer.parseInt(p[2]);
        if (index < 0 || index >= b.operations.size()) throw malformed(n, p);
        GenericInstructionEditProcessor.Value value;
        switch (p[3]) {
            case "METHOD_CALL" -> {
                if (p.length != 8) throw malformed(n, p);
                value = new GenericInstructionEditProcessor.Value(
                        GenericInstructionEditProcessor.ValueKind.METHOD_CALL, editRef(p, 4), null, null, null);
            }
            case "SIMPLE_OPCODE" -> {
                if (p.length != 5) throw malformed(n, p);
                value = new GenericInstructionEditProcessor.Value(
                        GenericInstructionEditProcessor.ValueKind.SIMPLE_OPCODE, null,
                        Integer.parseInt(p[4]), null, null);
            }
            case "VARIABLE" -> {
                if (p.length != 6) throw malformed(n, p);
                value = new GenericInstructionEditProcessor.Value(
                        GenericInstructionEditProcessor.ValueKind.VARIABLE, null,
                        Integer.parseInt(p[4]), Integer.parseInt(p[5]), null);
            }
            case "JUMP" -> {
                if (p.length != 8) throw malformed(n, p);
                value = new GenericInstructionEditProcessor.Value(
                        GenericInstructionEditProcessor.ValueKind.JUMP, null,
                        Integer.parseInt(p[4]), null, reference(p, 5));
            }
            default -> throw new IOException("Unknown VAL kind at line " + (n + 1) + ": " + p[3]);
        }
        b.operations.get(index).values.add(value);
    }

    private static EditBuilder builder(Map<String, EditBuilder> edits, String id, int n) throws IOException {
        var b = edits.get(id);
        if (b == null) throw new IOException("Record before EDIT at line " + (n + 1) + ": " + id);
        return b;
    }

    private static GenericMethodCallRedirectProcessor.MethodRef ref(String[] p, int i) {
        String invocation = p[i + 3];
        return new GenericMethodCallRedirectProcessor.MethodRef(
                opcode(invocation), p[i], p[i + 1], p[i + 2], "INTERFACE".equals(invocation));
    }

    private static GenericInstructionEditProcessor.MethodRef editRef(String[] p, int i) {
        String invocation = p[i + 3];
        return new GenericInstructionEditProcessor.MethodRef(
                opcode(invocation), p[i], p[i + 1], p[i + 2], "INTERFACE".equals(invocation));
    }

    private static GenericInstructionEditProcessor.Reference reference(String[] p, int i) {
        return new GenericInstructionEditProcessor.Reference(
                p[i], Integer.parseInt(p[i + 1]), Boolean.parseBoolean(p[i + 2]));
    }

    private static int opcode(String invocation) {
        return switch (invocation) {
            case "STATIC" -> Opcodes.INVOKESTATIC;
            case "INTERFACE" -> Opcodes.INVOKEINTERFACE;
            case "SPECIAL" -> Opcodes.INVOKESPECIAL;
            case "VIRTUAL" -> Opcodes.INVOKEVIRTUAL;
            default -> throw new IllegalArgumentException("Unsupported invocation: " + invocation);
        };
    }

    private static String[] split(String line) {
        return Arrays.stream(line.split("\\t", -1)).map(LaunchPlanReader::unescape).toArray(String[]::new);
    }

    private static IOException malformed(int n, String[] p) {
        return new IOException("Malformed LMS launch plan record at line " + (n + 1) + ": " + Arrays.toString(p));
    }

    private static String unescape(String value) {
        StringBuilder out = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (escaped) {
                out.append(ch == 't' ? '\t' : ch == 'n' ? '\n' : ch);
                escaped = false;
            } else if (ch == '\\') escaped = true;
            else out.append(ch);
        }
        if (escaped) out.append('\\');
        return out.toString();
    }

    private static final class EditBuilder {
        final String id, targetClass, targetMethod, targetDescriptor;
        final List<GenericInstructionEditProcessor.Anchor> anchors = new ArrayList<>();
        final List<EditOperation> operations = new ArrayList<>();

        EditBuilder(String id, String targetClass, String targetMethod, String targetDescriptor) {
            this.id = id; this.targetClass = targetClass; this.targetMethod = targetMethod; this.targetDescriptor = targetDescriptor;
        }

        GenericInstructionEditProcessor.Spec build() {
            return new GenericInstructionEditProcessor.Spec(id, targetClass, targetMethod, targetDescriptor,
                    List.copyOf(anchors), operations.stream()
                    .map(op -> new GenericInstructionEditProcessor.Edit(op.kind, op.location, List.copyOf(op.values)))
                    .toList());
        }
    }

    private static final class EditOperation {
        final GenericInstructionEditProcessor.EditKind kind;
        final GenericInstructionEditProcessor.Reference location;
        final List<GenericInstructionEditProcessor.Value> values;
        EditOperation(GenericInstructionEditProcessor.EditKind kind,
                      GenericInstructionEditProcessor.Reference location,
                      List<GenericInstructionEditProcessor.Value> values) {
            this.kind = kind; this.location = location; this.values = values;
        }
    }
}
