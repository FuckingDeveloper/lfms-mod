package com.fuckingdeveloper.lms.mapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

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
            return Optional.ofNullable(namedMethods.get(new MethodKey(owner, name, parameters)));
        }
        public Optional<String> obfuscatedClass(String namedClass) {
            return Optional.ofNullable(namedToObfuscatedClasses.get(namedClass));
        }
        public Optional<String> namedClass(String obfuscatedClass) {
            return Optional.ofNullable(obfuscatedToNamedClasses.get(obfuscatedClass));
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
            if (!Character.isWhitespace(line.charAt(0)) && trimmed.endsWith(":")) {
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
            // ProGuard may prefix methods with source line ranges.
            left = left.replaceFirst("^(?:[0-9]+:[0-9]+:)+", "");
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
