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

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }
}
