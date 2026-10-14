package com.fuckingdeveloper.lms.runtime.chat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Preserve source translation argument rendering across target argument validation. */
public final class Forge1192TranslationBridge {
    private Forge1192TranslationBridge() {}

    public static MutableComponent translatable(String key, Object... arguments) {
        Object[] normalized = arguments.clone();
        for (int i = 0; i < normalized.length; i++) {
            Object value = normalized[i];
            if (!(value instanceof Component || value instanceof Number
                    || value instanceof Boolean || value instanceof String)) {
                // Source TranslatableContents#getArgument: null -> "null", otherwise toString.
                normalized[i] = String.valueOf(value);
            }
        }
        return Component.translatable(key, normalized);
    }
}
