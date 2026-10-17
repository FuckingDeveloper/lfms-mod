package com.fuckingdeveloper.lms.target.neoforge263;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/** Only already-proven process-global identities; never substitute a world registry. */
public final class Target263ForgeRegistryReads {
    private Target263ForgeRegistryReads() {}
    private static final java.util.Set<Identifier> PROJECTED_LOADER_KEYS = java.util.Set.of(
            net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS.identifier(),
            net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS.identifier(),
            net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS.identifier(),
            net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.STRUCTURE_MODIFIER_SERIALIZERS.identifier(),
            net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.FLUID_TYPES.identifier(),
            net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.HOLDER_SET_TYPES.identifier(),
            net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.BIOME_MODIFIERS.identifier(),
            net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.STRUCTURE_MODIFIERS.identifier());
    public static boolean projectedUnsupported(Identifier identity) { return PROJECTED_LOADER_KEYS.contains(identity); }
    public static Registry<?> resolve(Identifier identity) {
        return BuiltInRegistries.REGISTRY.getOptional(identity).orElseThrow(() -> new UnsupportedOperationException(
                "No native process-global registry for source identity " + identity));
    }
}
