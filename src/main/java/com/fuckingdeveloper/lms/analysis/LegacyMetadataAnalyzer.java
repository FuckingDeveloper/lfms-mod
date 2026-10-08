package com.fuckingdeveloper.lms.analysis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Reads legacy loader metadata/resources as text only; nothing is executed. */
public final class LegacyMetadataAnalyzer {
    private static final Pattern DEP_BLOCK = Pattern.compile(
            "(?s)\\[\\[dependencies\\.[^]]+]](.*?)(?=\\[\\[|\\z)");
    private static final Pattern TOML_ID = Pattern.compile("(?m)^\\s*modId\\s*=\\s*[\"']([^\"']+)[\"']");
    private static final Pattern TOML_RANGE = Pattern.compile("(?m)^\\s*versionRange\\s*=\\s*[\"']([^\"']+)[\"']");
    private static final Pattern TOML_MANDATORY = Pattern.compile("(?m)^\\s*mandatory\\s*=\\s*(true|false)");
    private static final Pattern JSON_STRING_ARRAY = Pattern.compile(
            "\"(mixins|client|server)\"\\s*:\\s*\\[(.*?)]", Pattern.DOTALL);
    private static final Pattern JSON_STRING = Pattern.compile("\"([^\"]+)\"");
    private static final Pattern JSON_PACKAGE = Pattern.compile("\"package\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern COREMOD_SCRIPT = Pattern.compile(
            "\"[^\"]+\"\\s*:\\s*\"([^\"]+\\.js)\"");
    private static final Pattern TARGET_CLASS = Pattern.compile(
            "(?:target|class|className|name)\\s*[:=]\\s*[\"']([A-Za-z0-9_.$/]+)[\"']");

    public record Dependency(String modId, String versionRange, boolean mandatory) {}
    public record Report(List<Dependency> dependencies, List<String> mixinClasses,
                         List<String> coremodScripts, List<String> coremodTargetHints) {}

    public Report analyze(Path file) throws IOException {
        List<Dependency> dependencies = new ArrayList<>();
        List<String> mixinClasses = new ArrayList<>();
        List<String> scripts = new ArrayList<>();
        List<String> targets = new ArrayList<>();

        try (ZipFile jar = new ZipFile(file.toFile())) {
            String modsToml = read(jar, "META-INF/mods.toml");
            if (modsToml != null) parseDependencies(modsToml, dependencies);

            String mixin = read(jar, "ic2.mixins.json");
            if (mixin != null) parseMixins(mixin, mixinClasses);

            String coremods = read(jar, "META-INF/coremods.json");
            if (coremods != null) {
                Matcher matcher = COREMOD_SCRIPT.matcher(coremods);
                while (matcher.find()) scripts.add(matcher.group(1));
            }
            for (String script : scripts) {
                String js = read(jar, script);
                if (js == null) continue;
                Matcher target = TARGET_CLASS.matcher(js);
                while (target.find()) targets.add(target.group(1).replace('/', '.'));
            }
        }
        return new Report(List.copyOf(dependencies), List.copyOf(mixinClasses),
                List.copyOf(scripts), List.copyOf(targets));
    }

    private static void parseDependencies(String text, List<Dependency> out) {
        Matcher blocks = DEP_BLOCK.matcher(text);
        while (blocks.find()) {
            String block = blocks.group(1);
            String id = first(TOML_ID, block);
            if (id.isEmpty()) continue;
            String range = first(TOML_RANGE, block);
            String mandatory = first(TOML_MANDATORY, block);
            out.add(new Dependency(id, range, !"false".equals(mandatory)));
        }
    }

    private static void parseMixins(String text, List<String> out) {
        String pkg = first(JSON_PACKAGE, text);
        Matcher arrays = JSON_STRING_ARRAY.matcher(text);
        while (arrays.find()) {
            Matcher values = JSON_STRING.matcher(arrays.group(2));
            while (values.find()) {
                String name = values.group(1);
                out.add(pkg.isEmpty() ? name : pkg + "." + name);
            }
        }
    }

    private static String first(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String read(ZipFile jar, String name) throws IOException {
        ZipEntry entry = jar.getEntry(name);
        if (entry == null) return null;
        try (var in = jar.getInputStream(entry)) {
            return new String(in.readNBytes(2 * 1024 * 1024), StandardCharsets.UTF_8);
        }
    }
}
