package com.fuckingdeveloper.lms.runtime;

/** Verified common-tag member renames; pure metadata, safe during transformation. */
public final class Target263CommonTagNames {
    private Target263CommonTagNames() {}
    public static String renamed(String bucket, String member) {
        return switch (bucket + '#' + member) {
            case "Blocks#COBBLESTONE", "Items#COBBLESTONE" -> "COBBLESTONES";
            case "Blocks#GLASS", "Items#GLASS" -> "GLASS_BLOCKS";
            case "Blocks#GRAVEL", "Items#GRAVEL" -> "GRAVELS";
            case "Blocks#SANDSTONE" -> "SANDSTONE_BLOCKS";
            case "Blocks#STONE", "Items#STONE" -> "STONES";
            case "Items#INGOTS_BRICK" -> "BRICKS_NORMAL";
            case "Items#INGOTS_NETHER_BRICK" -> "BRICKS_NETHER";
            case "Items#OBSIDIAN" -> "OBSIDIANS";
            case "Items#SHEARS" -> "TOOLS_SHEAR";
            case "Biomes#IS_WATER" -> "IS_AQUATIC";
            default -> null;
        };
    }
}
