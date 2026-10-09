package com.fuckingdeveloper.lms.launch;

import org.objectweb.asm.Opcodes;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

final class LaunchPlanReader {
    private LaunchPlanReader() {}

    static List<GenericMethodCallRedirectProcessor.Spec> readRedirects(Path path) throws IOException {
        if (!Files.isRegularFile(path)) return List.of();
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !lines.getFirst().equals("LMSPLAN\t1")) {
            throw new IOException("Unsupported LMS launch plan format");
        }
        List<GenericMethodCallRedirectProcessor.Spec> result = new ArrayList<>();
        for (int n = 1; n < lines.size(); n++) {
            if (lines.get(n).isBlank()) continue;
            String[] p = lines.get(n).split("\\t", -1);
            if (p.length != 14 || !"REDIRECT".equals(p[0])) continue;
            result.add(new GenericMethodCallRedirectProcessor.Spec(
                    unescape(p[1]), unescape(p[2]), unescape(p[3]), unescape(p[4]),
                    ref(p, 5), ref(p, 9)));
        }
        return List.copyOf(result);
    }

    private static GenericMethodCallRedirectProcessor.MethodRef ref(String[] p, int i) {
        String invocation = unescape(p[i + 3]);
        return new GenericMethodCallRedirectProcessor.MethodRef(
                opcode(invocation), unescape(p[i]), unescape(p[i + 1]), unescape(p[i + 2]),
                "INTERFACE".equals(invocation));
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
}
