package com.fuckingdeveloper.lms.runtime;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import java.util.Objects;

/**
 * Forge 1.19.2 superclass-constructor compatibility boundary.
 *
 * This constructs the modern superclass with the legacy translation key.
 * It intentionally does NOT claim to migrate legacy icon generation,
 * creative inventory population, or registry timing. Those operations
 * must be implemented as separate lifecycle capabilities.
 */
public final class Forge1192CreativeTabBridge {
    private Forge1192CreativeTabBridge() {}

    public static CreativeModeTab.Builder builder(String legacyLabel) {
        String label = Objects.requireNonNull(legacyLabel, "legacy creative tab label");
        if (label.isBlank()) throw new IllegalArgumentException("Empty legacy creative tab label");
        return CreativeModeTab.builder()
                .title(Component.translatable("itemGroup." + label))
                .icon(() -> {
                    throw new IllegalStateException(
                            "Legacy CreativeModeTab icon migration is not installed: " + label);
                })
                .displayItems((parameters, output) -> {
                    throw new IllegalStateException(
                            "Legacy CreativeModeTab contents migration is not installed: " + label);
                });
    }
}
