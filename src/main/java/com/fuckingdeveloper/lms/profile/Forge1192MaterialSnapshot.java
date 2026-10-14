package com.fuckingdeveloper.lms.profile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Immutable source-generation facts; contains no target-runtime classes. */
public final class Forge1192MaterialSnapshot {
    public record MaterialDefinition(int colorId, boolean liquid, boolean solid,
            boolean blocksMotion, boolean flammable, boolean replaceable,
            boolean solidBlocking, String pushReaction) {}

    private record Snapshot(Map<String, MaterialDefinition> materials, Map<String, String> blocks) {}
    private static final Snapshot SOURCE = load();

    private Forge1192MaterialSnapshot() {}

    public static MaterialDefinition material(String name) {
        MaterialDefinition definition = SOURCE.materials().get(name);
        if (definition == null) throw new IllegalStateException("Unknown Forge 1.19.2 material: " + name);
        return definition;
    }

    public static Optional<String> materialForBlock(String registryId) {
        return Optional.ofNullable(SOURCE.blocks().get(registryId));
    }

    private static Snapshot load() {
        String path = "/lms/profile/forge1192/materials.tsv";
        var stream = Forge1192MaterialSnapshot.class.getResourceAsStream(path);
        if (stream == null) throw new IllegalStateException("Missing source material snapshot: " + path);
        Map<String, MaterialDefinition> materials = new HashMap<>();
        Map<String, String> blocks = new HashMap<>();
        try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            for (String line; (line = reader.readLine()) != null;) {
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] columns = line.split("\t", -1);
                if (columns[0].equals("M") && columns.length == 10) {
                    var definition = new MaterialDefinition(Integer.parseInt(columns[2]),
                            bool(columns[3]), bool(columns[4]), bool(columns[5]), bool(columns[6]),
                            bool(columns[7]), bool(columns[8]), columns[9]);
                    if (materials.putIfAbsent(columns[1], definition) != null)
                        throw new IllegalStateException("Duplicate material: " + columns[1]);
                } else if (columns[0].equals("B") && columns.length == 3) {
                    if (blocks.putIfAbsent(columns[1], columns[2]) != null)
                        throw new IllegalStateException("Duplicate source block: " + columns[1]);
                } else throw new IllegalStateException("Malformed source material row: " + line);
            }
        } catch (java.io.IOException error) {
            throw new IllegalStateException("Cannot read source material snapshot", error);
        }
        for (String name : blocks.values()) {
            if (!materials.containsKey(name)) throw new IllegalStateException("Undefined source material: " + name);
        }
        return new Snapshot(Map.copyOf(materials), Map.copyOf(blocks));
    }

    private static boolean bool(String value) {
        return switch (value) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new IllegalStateException("Invalid source material flag: " + value);
        };
    }
}
