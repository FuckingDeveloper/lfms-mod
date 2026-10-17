package com.fuckingdeveloper.lms.profile.forge1192.runtime.chat;

import net.minecraft.ChatFormatting;

import java.util.Locale;

/** Restores the normalized nullable name lookup exposed by Minecraft 1.19.2. */
public final class Forge1192ChatFormattingBridge {
    private Forge1192ChatFormattingBridge() {}

    public static ChatFormatting byName(String name) {
        if (name == null) return null;
        String normalized = normalize(name);
        for (ChatFormatting formatting : ChatFormatting.values()) {
            if (normalize(formatting.name()).equals(normalized)) return formatting;
        }
        return null;
    }

    public static char getChar(ChatFormatting value) { return value.toString().charAt(1); }
    public static int getId(ChatFormatting value) { return "0123456789abcdef".indexOf(getChar(value)); }
    public static boolean isFormat(ChatFormatting value) {
        java.util.Objects.requireNonNull(value);
        return switch(value) { case OBFUSCATED, BOLD, STRIKETHROUGH, UNDERLINE, ITALIC -> true; default -> false; };
    }
    public static boolean isColor(ChatFormatting value) { return !isFormat(value) && value != ChatFormatting.RESET; }
    public static Integer getColor(ChatFormatting value) {
        java.util.Objects.requireNonNull(value);
        var color = net.minecraft.network.chat.TextColor.fromLegacyFormat(value);
        return color == null ? null : color.getValue();
    }
    public static String getName(ChatFormatting value) { return value.name().toLowerCase(Locale.ROOT); }
    public static ChatFormatting byId(int id) {
        if (id < 0) return ChatFormatting.RESET;
        for (var value : ChatFormatting.values()) if (getId(value) == id) return value;
        return null;
    }
    public static ChatFormatting byCode(char code) {
        return ChatFormatting.getByCode(Character.toString(code).toLowerCase(Locale.ROOT).charAt(0));
    }
    public static String stripFormatting(String input) { return ChatFormatting.stripFormatting(input); }
    public static java.util.Collection<String> getNames(boolean colors, boolean formats) {
        var result = new java.util.ArrayList<String>();
        for (var value : ChatFormatting.values()) if ((!isColor(value) || colors) && (!isFormat(value) || formats)) result.add(getName(value));
        return result;
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }
}
