package com.fuckingdeveloper.lms.runtime;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

/** Semantic bridge for the Forge 1.19.2 String-based key category contract. */
public final class Forge1192KeyMappingBridge {
    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(Forge1192KeyMappingBridge.class);
    private static final Map<String, KeyMapping.Category> CUSTOM_CATEGORIES = new HashMap<>();

    private Forge1192KeyMappingBridge() {}

    public static KeyMapping create(String name, int keyCode, String category) {
        return new KeyMapping(name, com.fuckingdeveloper.lms.runtime.render.Forge1192KeyboardBridge.keyboardCode(keyCode), category(category));
    }

    public static KeyMapping create(
            String name, InputConstants.Type inputType, int keyCode, String category) {
        return new KeyMapping(name, inputType, com.fuckingdeveloper.lms.runtime.render.Forge1192KeyboardBridge.key(inputType,keyCode).getValue(), category(category));
    }

    public static KeyMapping create(
            String name, IKeyConflictContext conflictContext,
            InputConstants.Type inputType, int keyCode, String category) {
        return new KeyMapping(name, conflictContext, inputType, com.fuckingdeveloper.lms.runtime.render.Forge1192KeyboardBridge.key(inputType,keyCode).getValue(), category(category));
    }

    public static KeyMapping create(
            String name, IKeyConflictContext conflictContext,
            InputConstants.Key keyCode, String category) {
        return new KeyMapping(name, conflictContext, keyCode, category(category));
    }

    public static KeyMapping create(
            String name, IKeyConflictContext conflictContext, KeyModifier modifier,
            InputConstants.Type inputType, int keyCode, String category) {
        return new KeyMapping(
                name, conflictContext, modifier, inputType, com.fuckingdeveloper.lms.runtime.render.Forge1192KeyboardBridge.key(inputType,keyCode).getValue(), category(category));
    }

    public static KeyMapping create(
            String name, IKeyConflictContext conflictContext, KeyModifier modifier,
            InputConstants.Key keyCode, String category) {
        return new KeyMapping(name, conflictContext, modifier, keyCode, category(category));
    }

    public static synchronized KeyMapping.Category category(String legacyTranslationKey) {
        return switch (legacyTranslationKey) {
            case "key.categories.movement" -> KeyMapping.Category.MOVEMENT;
            case "key.categories.misc" -> KeyMapping.Category.MISC;
            case "key.categories.multiplayer" -> KeyMapping.Category.MULTIPLAYER;
            case "key.categories.gameplay" -> KeyMapping.Category.GAMEPLAY;
            case "key.categories.inventory" -> KeyMapping.Category.INVENTORY;
            case "key.categories.creative" -> KeyMapping.Category.CREATIVE;
            case "key.categories.spectator" -> KeyMapping.Category.SPECTATOR;
            case "key.categories.debug" -> KeyMapping.Category.DEBUG;
            default -> CUSTOM_CATEGORIES.computeIfAbsent(
                    legacyTranslationKey, Forge1192KeyMappingBridge::registerCustomCategory);
        };
    }

    private static KeyMapping.Category registerCustomCategory(String legacyTranslationKey) {
        Identifier id = customCategoryId(legacyTranslationKey);
        KeyMapping.Category category = KeyMapping.Category.register(id);
        LOG.warn("LMS preserved legacy key category identity '{}' as '{}'; the target label key "
                        + "is '{}' rather than the source translation key and may require a resource alias",
                legacyTranslationKey, id, id.toLanguageKey("key.category"));
        return category;
    }

    private static Identifier customCategoryId(String translationKey) {
        String normalized = translationKey.toLowerCase(Locale.ROOT);
        String[] parts = normalized.split("\\.", 3);
        if (parts.length == 3 && parts[0].equals("key")
                && validNamespace(parts[1]) && validPath(parts[2])) {
            return Identifier.fromNamespaceAndPath(parts[1], parts[2]);
        }
        String readable = normalized.replaceAll("[^a-z0-9/._-]", "_");
        if (readable.isEmpty()) readable = "category";
        return Identifier.fromNamespaceAndPath(
                "lms", "legacy_key_category/" + readable + "_"
                        + Integer.toUnsignedString(translationKey.hashCode(), 16));
    }

    private static boolean validNamespace(String value) {
        return value.matches("[a-z0-9_.-]+");
    }

    private static boolean validPath(String value) {
        return value.matches("[a-z0-9/._-]+");
    }
}
