package com.fuckingdeveloper.lms.mapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Non-executing parser for TSRG v1 and TSRG2 method mappings. */
public final class TsrgMappingReader {
    public record MethodKey(String owner, String name, String descriptor) {}
    public record MethodEntry(String owner, String descriptor, List<String> names) {}
    public record Index(List<String> namespaces, Map<MethodKey, MethodEntry> methods) {
        public Optional<MethodEntry> find(String owner, String name, String descriptor) {
            return Optional.ofNullable(methods.get(new MethodKey(owner, name, descriptor)));
        }
    }

    public Index read(Reader input) throws IOException {
        BufferedReader reader = new BufferedReader(input);
        List<String> namespaces = List.of("source", "target");
        Map<MethodKey, MethodEntry> methods = new LinkedHashMap<>();
        String owner = null;
        String line;
        boolean headerSeen = false;
        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            if (!headerSeen && trimmed.startsWith("tsrg2 ")) {
                String[] header = trimmed.split("\\s+");
                if (header.length < 3) throw new IOException("TSRG2 requires at least two namespaces");
                namespaces = List.of(header).subList(1, header.length);
                headerSeen = true;
                continue;
            }
            headerSeen = true;
            if (!Character.isWhitespace(line.charAt(0))) {
                String[] tokens = trimmed.split("\\s+");
                owner = tokens.length >= 2 ? tokens[0] : null;
                continue;
            }
            if (owner == null || Character.isWhitespace(line.charAt(0))
                    && line.length() > 1 && Character.isWhitespace(line.charAt(1))) continue;
            String[] tokens = trimmed.split("\\s+");
            if (tokens.length < 3 || !tokens[1].startsWith("(")) continue;
            int expected = namespaces.size();
            if (tokens.length != expected + 1) continue;
            List<String> names = new ArrayList<>();
            names.add(tokens[0]);
            for (int i = 2; i < tokens.length; i++) names.add(tokens[i]);
            methods.putIfAbsent(new MethodKey(owner, tokens[0], tokens[1]),
                    new MethodEntry(owner, tokens[1], List.copyOf(names)));
        }
        return new Index(List.copyOf(namespaces), Map.copyOf(methods));
    }
}
