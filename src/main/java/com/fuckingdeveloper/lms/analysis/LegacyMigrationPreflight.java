package com.fuckingdeveloper.lms.analysis;

import com.fuckingdeveloper.lms.mapping.Forge1192MappingLayer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Classifies whole-JAR references before any legacy entrypoint is initialized. */
public final class LegacyMigrationPreflight {
    public enum State {
        TARGET_AVAILABLE, AUTO_RELOCATABLE, LMS_BRIDGE, FORGE_API_MIGRATION,
        OPTIONAL_DEPENDENCY, LIBRARY_DEPENDENCY, VANILLA_API_MIGRATION, UNRESOLVED
    }
    public record Finding(String target, LegacySymbolPreflight.Use use, State state, String reason) {}
    public record Report(List<Finding> findings, Map<State,Integer> states) {}

    public Report classify(LegacySymbolPreflight.Report symbols, ClassLoader targetLoader) {
        Forge1192MappingLayer mappings = new Forge1192MappingLayer();
        List<Finding> out = new ArrayList<>();
        for (var ref : symbols.references()) {
            String target = ref.target();
            State state;
            String reason;
            if (available(targetLoader, target)) {
                state = State.TARGET_AVAILABLE; reason = "exact target class exists";
            } else if (target.equals("net/minecraft/world/item/ArmorItem")
                    || target.equals("net/minecraftforge/common/extensions/IForgeItem")) {
                state = State.LMS_BRIDGE; reason = "explicit structural LMS compatibility contract";
            } else if (target.startsWith("net/minecraftforge/")) {
                state = State.FORGE_API_MIGRATION;
                reason = "legacy Forge API absent; operation/structure migration required";
            } else if (target.startsWith("top/theillusivec4/") || target.startsWith("mezz/jei/")) {
                state = State.OPTIONAL_DEPENDENCY; reason = "third-party integration dependency";
            } else if (target.startsWith("org/apache/") || target.startsWith("org/lwjgl/")) {
                state = State.LIBRARY_DEPENDENCY; reason = "external library dependency";
            } else if (target.startsWith("com/mojang/")) {
                state = State.VANILLA_API_MIGRATION; reason = "Mojang/client API moved or removed";
            } else if (target.startsWith("net/minecraft/")) {
                var relocation = mappings.resolveRelocatedClass(target);
                if (relocation.status() == Forge1192MappingLayer.Status.VERIFIED_IDENTITY
                        && !relocation.targetInternalName().isEmpty()) {
                    state = State.AUTO_RELOCATABLE;
                    reason = relocation.targetInternalName();
                } else {
                    state = State.VANILLA_API_MIGRATION; reason = relocation.reason();
                }
            } else {
                state = State.UNRESOLVED; reason = "external class not visible in target loader";
            }
            out.add(new Finding(target, ref.use(), state, reason));
        }
        var unique = out.stream().distinct()
                .sorted(java.util.Comparator.comparing(Finding::state)
                        .thenComparing(Finding::target).thenComparing(f -> f.use().name()))
                .toList();
        Map<State,Integer> states = new EnumMap<>(State.class);
        for (var f : unique) states.merge(f.state(), 1, Integer::sum);
        return new Report(unique, Map.copyOf(states));
    }

    private static boolean available(ClassLoader loader, String internalName) {
        String resource = internalName + ".class";
        return loader.getResource(resource) != null
                || LegacyMigrationPreflight.class.getClassLoader().getResource(resource) != null;
    }
}
