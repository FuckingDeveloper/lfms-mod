package com.fuckingdeveloper.lms.profile.forge1192.runtime.registry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.*;

/** Source ACTIVE read intent, not a mutable or historical native registry manager. */
public final class LegacyRegistryManagerView {
    public static final LegacyRegistryManagerView ACTIVE = new LegacyRegistryManagerView();
    // Exact standard declarations from Forge 43.4.0 GameData.init/ForgeRegistries.Keys.
    private static final Set<String> STANDARD = Set.of("block", "fluid", "item", "mob_effect", "sound_event",
            "potion", "entity_type", "block_entity_type", "particle_type", "menu", "recipe_type",
            "recipe_serializer", "attribute", "stat_type", "command_argument_type", "villager_profession",
            "point_of_interest_type", "memory_module_type", "sensor_type", "schedule", "activity",
            "worldgen/carver", "worldgen/feature", "chunk_status", "worldgen/block_state_provider_type",
            "worldgen/foliage_placer_type", "worldgen/tree_decorator_type");
    private static final Set<String> DATA_DRIVEN = Set.of("enchantment", "painting_variant", "worldgen/biome");
    private static final Set<String> LOADER = Set.of("entity_data_serializers", "global_loot_modifier_serializers",
            "biome_modifier_serializers", "structure_modifier_serializers", "fluid_type", "holder_set_type",
            "biome_modifier", "structure_modifier");
    private final Map<Identifier, LegacyForgeRegistryView<?>> views = new ConcurrentHashMap<>();
    private LegacyRegistryManagerView() {}
    public String getName() { return "ACTIVE"; }
    @SuppressWarnings("unchecked")
    public <T> LegacyForgeRegistryView<T> getRegistry(Identifier name) {
        if (name == null) return null;
        if (name.getNamespace().equals("minecraft") && DATA_DRIVEN.contains(name.getPath())
                || name.getNamespace().equals("forge") && LOADER.contains(name.getPath()))
            throw new UnsupportedOperationException("Source ACTIVE read view has no process-global counterpart: " + name);
        if (!name.getNamespace().equals("minecraft") || !STANDARD.contains(name.getPath())) return null;
        return (LegacyForgeRegistryView<T>) views.computeIfAbsent(name, LegacyForgeRegistryView::new);
    }
    public <T> LegacyForgeRegistryView<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        return key == null ? null : getRegistry(key.identifier());
    }
    public Identifier getName(LegacyForgeRegistry registry) {
        if (!(registry instanceof LegacyForgeRegistryView<?> view)) return null;
        return views.get(view.getRegistryName()) == view ? view.getRegistryName() : null;
    }
    static Identifier defaultKey(Identifier registry) {
        String value = switch (registry.getPath()) {
            case "block", "item" -> "air";
            case "fluid", "potion", "chunk_status" -> "empty";
            case "entity_type" -> "pig";
            case "villager_profession" -> "none";
            case "memory_module_type", "sensor_type" -> "dummy";
            default -> null;
        };
        return value == null ? null : Identifier.withDefaultNamespace(value);
    }
}
