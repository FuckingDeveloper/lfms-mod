package com.fuckingdeveloper.lms.mapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

/**
 * Reads Mojang's ProGuard-format class and method mappings without loading classes.
 * This format maps named symbols to obfuscated symbols; it does not map Forge SRG m_ names.
 */
public final class ProguardMappingReader {
    public record MethodKey(String owner, String name, String parameters) {}
    public record MethodMapping(String namedOwner, String namedName, String parameters,
                                String obfuscatedOwner, String obfuscatedName) {}
    public record Index(Map<String, String> namedToObfuscatedClasses,
                        Map<String, String> obfuscatedToNamedClasses,
                        Map<MethodKey, MethodMapping> namedMethods) {
        public Optional<MethodMapping> find(String owner, String name, String parameters) {
            return Optional.ofNullable(namedMethods.get(new MethodKey(toBinaryName(owner), name, parameters)));
        }
        public Optional<String> obfuscatedClass(String namedClass) {
            return Optional.ofNullable(namedToObfuscatedClasses.get(toBinaryName(namedClass)));
        }
        public Optional<String> namedClass(String obfuscatedClass) {
            return Optional.ofNullable(obfuscatedToNamedClasses.get(toBinaryName(obfuscatedClass)));
        }
        public List<MethodMapping> findByObfuscated(String owner, String name) {
            String normalizedOwner = toBinaryName(owner);
            return namedMethods.values().stream()
                    .filter(method -> method.obfuscatedOwner().equals(normalizedOwner))
                    .filter(method -> method.obfuscatedName().equals(name))
                    .distinct()
                    .toList();
        }
        public String obfuscateDescriptor(String descriptor) {
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < descriptor.length();) {
                char ch = descriptor.charAt(i);
                if (ch != 'L') {
                    out.append(ch);
                    i++;
                    continue;
                }
                int end = descriptor.indexOf(';', i);
                if (end < 0) return descriptor;
                String internalName = descriptor.substring(i + 1, end);
                String binaryName = internalName.replace('/', '.');
                String mapped = namedToObfuscatedClasses.get(binaryName);
                out.append('L').append(mapped != null ? mapped.replace('.', '/') : internalName).append(';');
                i = end + 1;
            }
            return out.toString();
        }
        private static String toBinaryName(String name) {
            return name.replace('/', '.');
        }
    }

    public Index read(Reader input) throws IOException {
        Map<String, String> classes = new LinkedHashMap<>();
        Map<String, String> reverseClasses = new LinkedHashMap<>();
        Map<MethodKey, MethodMapping> methods = new LinkedHashMap<>();
        String owner = null;
        String obfuscatedOwner = null;
        BufferedReader reader = new BufferedReader(input);
        String line;
        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            if (trimmed.endsWith(":")) {
                int arrow = trimmed.indexOf(" -> ");
                if (arrow < 0) {
                    owner = null;
                    obfuscatedOwner = null;
                    continue;
                }
                owner = trimmed.substring(0, arrow).trim();
                obfuscatedOwner = trimmed.substring(arrow + 4, trimmed.length() - 1).trim();
                classes.put(owner, obfuscatedOwner);
                reverseClasses.put(obfuscatedOwner, owner);
                continue;
            }
            if (owner == null) continue;
            int arrow = trimmed.lastIndexOf(" -> ");
            int open = trimmed.indexOf('(');
            int close = trimmed.indexOf(')', open + 1);
            if (arrow < 0 || open < 0 || close < 0 || close > arrow) continue;
            String left = trimmed.substring(0, arrow).trim();
            // ProGuard may prefix methods with source line ranges. Recompute positions
            // after stripping them: the old indexes belonged to the original string.
            left = left.replaceFirst("^(?:[0-9]+:[0-9]+:)+", "");
            open = left.indexOf('(');
            close = left.indexOf(')', open + 1);
            if (open < 0 || close < 0) continue;
            int space = left.lastIndexOf(' ', open);
            if (space < 0) continue;
            String name = left.substring(space + 1, open);
            String params = left.substring(open + 1, close).trim();
            String obfuscatedName = trimmed.substring(arrow + 4).trim();
            MethodKey key = new MethodKey(owner, name, params);
            methods.putIfAbsent(key, new MethodMapping(owner, name, params, obfuscatedOwner, obfuscatedName));
        }
        return new Index(Map.copyOf(classes), Map.copyOf(reverseClasses), Map.copyOf(methods));
    }
}
