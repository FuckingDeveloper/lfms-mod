package com.fuckingdeveloper.lms.runtime;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.*;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

/** A resource view for the loot listener only; all other reload consumers see original packs. */
public final class Target263LegacyLootResources {
    public static final Identifier LIST = Identifier.fromNamespaceAndPath("forge", "loot_modifiers/global_loot_modifiers.json");
    private Target263LegacyLootResources() {}

    public record Plan(ResourceManager resources, List<Identifier> order, boolean active) {}

    public static Plan plan(ResourceManager resources) {
        var definitions = resources.listResources("loot_modifiers", id -> id.getPath().endsWith(".json"));
        var stack = resources.getResourceStack(LIST);
        boolean active = definitions.values().stream().anyMatch(Target263LegacyLootResources::legacy)
                || stack.stream().anyMatch(Target263LegacyLootResources::legacy);
        if (!active) return new Plan(resources, List.of(), false);
        var order = new ArrayList<Identifier>();
        for (var resource : stack) {
            try (var reader = resource.openAsReader()) {
                var json = JsonParser.parseReader(reader).getAsJsonObject();
                if (json.get("replace").getAsBoolean()) order.clear();
                for (var entry : json.getAsJsonArray("entries")) {
                    var id = Identifier.parse(entry.getAsString());
                    order.remove(id);
                    order.add(id);
                }
            } catch (IOException | RuntimeException failure) {
                throw new IllegalStateException("Cannot read legacy global loot list in " + resource.sourcePackId(), failure);
            }
        }
        var selected = new HashSet<>(order);
        for (var id : order) {
            if (!definitions.containsKey(file(id))) {
                throw new IllegalStateException("Selected legacy global loot modifier is missing: " + id);
            }
        }
        return new Plan(new View(resources, selected), List.copyOf(order), true);
    }

    private static boolean legacy(Resource resource) { return resource.source() instanceof Target263LegacyTagResources; }
    private static Identifier file(Identifier id) {
        return Identifier.fromNamespaceAndPath(id.getNamespace(), "loot_modifiers/" + id.getPath() + ".json");
    }
    private static Identifier id(Identifier file) {
        return Identifier.fromNamespaceAndPath(file.getNamespace(), file.getPath().substring("loot_modifiers/".length(), file.getPath().length() - 5));
    }

    public static List<IGlobalLootModifier> ordered(Map<Identifier, IGlobalLootModifier> decoded,
            List<IGlobalLootModifier> nativeOrder, List<Identifier> sourceOrder) {
        var source = new ArrayList<IGlobalLootModifier>();
        var selected = Collections.newSetFromMap(new IdentityHashMap<IGlobalLootModifier, Boolean>());
        for (var id : sourceOrder) {
            var modifier = decoded.get(id);
            if (modifier == null) throw new IllegalStateException("Selected legacy global loot modifier did not decode: " + id);
            source.add(modifier);
            selected.add(modifier);
        }
        var result = new ArrayList<IGlobalLootModifier>();
        boolean inserted = false;
        for (var modifier : nativeOrder) {
            if (!inserted && modifier.priority() <= IGlobalLootModifier.DEFAULT_PRIORITY) {
                result.addAll(source);
                inserted = true;
            }
            if (!selected.contains(modifier)) result.add(modifier);
        }
        if (!inserted) result.addAll(source);
        return List.copyOf(result);
    }

    private record View(ResourceManager source, Set<Identifier> selected) implements ResourceManager {
        private boolean allowed(Identifier file, Resource resource) {
            if (file.equals(LIST)) return false;
            if (!file.getPath().startsWith("loot_modifiers/") || !file.getPath().endsWith(".json")) return true;
            return !legacy(resource) || selected.contains(id(file));
        }
        @Override public Optional<Resource> getResource(Identifier id) { return source.getResource(id).filter(r -> allowed(id, r)); }
        @Override public List<Resource> getResourceStack(Identifier id) { return source.getResourceStack(id).stream().filter(r -> allowed(id, r)).toList(); }
        @Override public Map<Identifier, Resource> listResources(String directory, Selector selector) {
            var result = new LinkedHashMap<>(source.listResources(directory, selector));
            result.entrySet().removeIf(e -> !allowed(e.getKey(), e.getValue()));
            return result;
        }
        @Override public Map<Identifier, List<Resource>> listResourceStacks(String directory, Selector selector) {
            var result = new LinkedHashMap<Identifier, List<Resource>>();
            source.listResourceStacks(directory, selector).forEach((id, stack) -> {
                var filtered = stack.stream().filter(r -> allowed(id, r)).toList();
                if (!filtered.isEmpty()) result.put(id, filtered);
            });
            return result;
        }
        @Override public Set<String> getNamespaces() { return source.getNamespaces(); }
        @Override public Stream<PackResources> listPacks() { return source.listPacks(); }
    }
}
