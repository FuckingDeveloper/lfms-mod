package com.fuckingdeveloper.lms.transform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

/** Writes the loader-neutral early transformation plan without third-party JSON dependencies. */
public final class LaunchPlanWriter {
    private LaunchPlanWriter() {}

    public static void write(Path path, List<TransformationSpec> specs) throws IOException {
        StringBuilder out = new StringBuilder("LMSPLAN\t1\n");
        for (TransformationSpec spec : specs) {
            if (spec.readiness() != TransformationSpec.Readiness.READY) continue;
            if (spec.kind() != TransformationSpec.Kind.METHOD_CALL_REDIRECT) continue;
            append(out, "REDIRECT", spec.id());
            appendRef(out, spec.target());
            appendRef(out, spec.anchor());
            appendRef(out, spec.replacement());
            out.append('\n');
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

    private static void appendRef(StringBuilder out, TransformationSpec.MethodRef ref) {
        append(out, ref.owner(), ref.name(), ref.descriptor(), ref.invocation().name());
    }

    private static void append(StringBuilder out, String... values) {
        for (String value : values) out.append(escape(value)).append('\t');
        out.setLength(out.length() - 1);
        out.append('\t');
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n");
    }
}
