package com.fuckingdeveloper.lms.analysis;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Static-only legacy JAR inventory. Never defines or executes classes. */
public final class LegacyJarAnalyzer {
    public record Report(int classes, int forgeClasses, int minecraftClasses,
                         int forgeReferenceClasses, int minecraftReferenceClasses,
                         List<String> modAnnotationCandidates, List<String> mixinConfigs,
                         List<String> nestedJars, List<String> transformerHints,
                         int unreadableClasses) {}

    public Report analyze(Path file) throws IOException {
        int classes = 0, forgeClasses = 0, minecraftClasses = 0;
        int forgeRefs = 0, minecraftRefs = 0, unreadable = 0;
        Set<String> mods = new TreeSet<>();
        Set<String> mixins = new TreeSet<>();
        Set<String> nested = new TreeSet<>();
        Set<String> transformers = new TreeSet<>();
        try (ZipFile jar = new ZipFile(file.toFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                String lower = name.toLowerCase(Locale.ROOT);
                if (lower.endsWith(".jar")) nested.add(name);
                if (isMixinConfig(lower)) mixins.add(name);
                if (isTransformerHint(lower)) transformers.add(name);
                if (!name.endsWith(".class")) continue;
                classes++;
                if (name.startsWith("net/minecraftforge/")) forgeClasses++;
                if (name.startsWith("net/minecraft/")) minecraftClasses++;
                try (DataInputStream in = new DataInputStream(jar.getInputStream(entry))) {
                    if (in.readInt() != 0xCAFEBABE) throw new IOException("Invalid class header");
                    in.readUnsignedShort(); // minor
                    in.readUnsignedShort(); // major
                    int count = in.readUnsignedShort();
                    List<String> strings = new ArrayList<>();
                    for (int i = 1; i < count; i++) {
                        int tag = in.readUnsignedByte();
                        switch (tag) {
                            case 1 -> {
                                int length = in.readUnsignedShort();
                                if (length > 65535) throw new IOException("Invalid UTF8 constant");
                                byte[] bytes = new byte[length];
                                in.readFully(bytes);
                                strings.add(new String(bytes, StandardCharsets.UTF_8));
                            }
                            case 3, 4, 9, 10, 11, 12, 17, 18 -> in.skipNBytes(4);
                            case 5, 6 -> { in.skipNBytes(8); i++; }
                            case 7, 8, 16, 19, 20 -> in.skipNBytes(2);
                            case 15 -> in.skipNBytes(3);
                            default -> throw new IOException("Unknown constant pool tag " + tag);
                        }
                    }
                    boolean forge = strings.stream().anyMatch(s -> s.contains("net/minecraftforge/") || s.contains("net.minecraftforge."));
                    boolean minecraft = strings.stream().anyMatch(s -> s.contains("net/minecraft/") || s.contains("net.minecraft."));
                    if (forge) forgeRefs++;
                    if (minecraft) minecraftRefs++;
                    if (strings.stream().anyMatch(s -> s.contains("Lnet/minecraftforge/fml/common/Mod;"))) {
                        mods.add(name.substring(0, name.length() - 6).replace('/', '.'));
                    }
                } catch (IOException | RuntimeException e) {
                    unreadable++;
                }
            }
        }
        return new Report(classes, forgeClasses, minecraftClasses, forgeRefs, minecraftRefs,
                List.copyOf(mods), List.copyOf(mixins), List.copyOf(nested),
                List.copyOf(transformers), unreadable);
    }

    private static boolean isMixinConfig(String lower) {
        if (!lower.endsWith(".json")) return false;
        String fileName = lower.substring(lower.lastIndexOf('/') + 1);
        return (fileName.startsWith("mixins.") || fileName.endsWith(".mixins.json")
                || (fileName.startsWith("mixin") && fileName.endsWith(".json")))
                && !fileName.contains("refmap");
    }

    private static boolean isTransformerHint(String lower) {
        return lower.equals("meta-inf/coremods.json")
                || lower.startsWith("meta-inf/services/cpw.mods.modlauncher")
                || lower.startsWith("coremods/")
                || lower.startsWith("meta-inf/coremods/");
    }
}
