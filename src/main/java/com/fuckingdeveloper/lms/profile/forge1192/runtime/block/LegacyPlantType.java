package com.fuckingdeveloper.lms.profile.forge1192.runtime.block;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/** Value-compatible replacement for Forge 1.19.2's removed PlantType. */
public final class LegacyPlantType {
    private static final Pattern INVALID_CHARACTERS = Pattern.compile("[^a-z_]");
    private static final Map<String, LegacyPlantType> VALUES = new ConcurrentHashMap<>();

    public static final LegacyPlantType PLAINS = get("plains");
    public static final LegacyPlantType DESERT = get("desert");
    public static final LegacyPlantType BEACH = get("beach");
    public static final LegacyPlantType CAVE = get("cave");
    public static final LegacyPlantType WATER = get("water");
    public static final LegacyPlantType NETHER = get("nether");
    public static final LegacyPlantType CROP = get("crop");

    private final String name;

    private LegacyPlantType(String name) {
        this.name = name;
    }

    public static LegacyPlantType get(String name) {
        if (INVALID_CHARACTERS.matcher(name).find()) {
            throw new IllegalArgumentException("PlantType name contains invalid characters: " + name);
        }
        return VALUES.computeIfAbsent(name, LegacyPlantType::new);
    }

    public String getName() {
        return name;
    }
}
