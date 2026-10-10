package com.fuckingdeveloper.lms.transform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

/** Writes the loader-neutral early transformation plan without third-party JSON dependencies. */
public final class LaunchPlanWriter {
    private LaunchPlanWriter() {}

    public static void write(Path path, List<TransformationSpec> specs) throws IOException {
        StringBuilder out = new StringBuilder("LMSPLAN\t2\n");
        for (TransformationSpec spec : specs) {
            if (spec.readiness() != TransformationSpec.Readiness.READY) continue;
            if (spec.kind() == TransformationSpec.Kind.METHOD_CALL_REDIRECT) writeRedirect(out, spec);
            else if (spec.kind() == TransformationSpec.Kind.INSTRUCTION_EDIT) writeInstructionEdit(out, spec);
        }
        Files.createDirectories(path.getParent());
        Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(tmp, out.toString(), StandardCharsets.UTF_8);
        try {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void writeRedirect(StringBuilder out, TransformationSpec spec) {
        line(out, "REDIRECT", spec.id(),
                spec.target().owner(), spec.target().name(), spec.target().descriptor(), spec.target().invocation().name(),
                spec.anchor().owner(), spec.anchor().name(), spec.anchor().descriptor(), spec.anchor().invocation().name(),
                spec.replacement().owner(), spec.replacement().name(), spec.replacement().descriptor(), spec.replacement().invocation().name());
    }

    private static void writeInstructionEdit(StringBuilder out, TransformationSpec spec) {
        line(out, "EDIT", spec.id(), spec.target().owner(), spec.target().name(), spec.target().descriptor());
        for (var anchor : spec.anchors()) {
            var m = anchor.method();
            line(out, "ANCHOR", spec.id(), anchor.variable(), m.owner(), m.name(), m.descriptor(), m.invocation().name());
        }
        int index = 0;
        for (var edit : spec.edits()) {
            var r = edit.locationReference();
            line(out, "OP", spec.id(), Integer.toString(index), edit.kind().name(),
                    r.variable(), Integer.toString(r.relativeOffset()), Boolean.toString(r.label()));
            List<TransformationSpec.InstructionSpec> values = edit.values().isEmpty()
                    ? (edit.value() == null ? List.of() : List.of(edit.value()))
                    : edit.values();
            for (var value : values) {
                switch (value.kind()) {
                    case METHOD_CALL -> {
                        var m = value.method();
                        line(out, "VAL", spec.id(), Integer.toString(index), "METHOD_CALL",
                                m.owner(), m.name(), m.descriptor(), m.invocation().name());
                    }
                    case SIMPLE_OPCODE -> line(out, "VAL", spec.id(), Integer.toString(index),
                            "SIMPLE_OPCODE", Integer.toString(value.opcode()));
                    case VARIABLE -> line(out, "VAL", spec.id(), Integer.toString(index),
                            "VARIABLE", Integer.toString(value.opcode()), Integer.toString(value.variable()));
                    case JUMP -> line(out, "VAL", spec.id(), Integer.toString(index),
                            "JUMP", Integer.toString(value.opcode()), value.target().variable(),
                            Integer.toString(value.target().relativeOffset()), Boolean.toString(value.target().label()));
                    case UNRESOLVED -> throw new IllegalStateException("READY spec contains unresolved value: " + spec.id());
                }
            }
            index++;
        }
        line(out, "END", spec.id());
    }

    private static void line(StringBuilder out, String... values) {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) out.append('\t');
            out.append(escape(values[i]));
        }
        out.append('\n');
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n");
    }
}
