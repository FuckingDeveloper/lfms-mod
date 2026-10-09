package com.fuckingdeveloper.lms.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Profile-owned classification of legacy Forge API boundaries.
 *
 * <p>This is deliberately fail-closed: a boundary is executable only after a
 * semantic adapter has been implemented and marked SUPPORTED. Namespace
 * similarity alone is never treated as compatibility proof.</p>
 */
public final class Forge1192CompatibilitySurface {
    public enum State { SUPPORTED, ADAPTER_REQUIRED, UNSUPPORTED }

    public record Requirement(String target, long callSites, State state, String adapter) {}
    public record Assessment(List<Requirement> requirements, boolean executable) {}

    private static final Set<String> LIFECYCLE_PREFIXES = Set.of(
            "net.minecraftforge.eventbus.",
            "net.minecraftforge.fml.",
            "net.minecraftforge.forgespi."
    );
    private static final Set<String> REGISTRATION_PREFIXES = Set.of(
            "net.minecraftforge.registries."
    );
    private static final Set<String> NETWORK_PREFIXES = Set.of(
            "net.minecraftforge.network."
    );
    private static final Set<String> FLUID_PREFIXES = Set.of(
            "net.minecraftforge.fluids."
    );

    public Assessment assess(List<Forge1192RegistrationPlanner.Boundary> boundaries) {
        var grouped = boundaries.stream().collect(java.util.stream.Collectors.groupingBy(
                boundary -> boundary.owner() + "#" + boundary.name() + boundary.descriptor(),
                java.util.TreeMap::new, java.util.stream.Collectors.counting()));
        List<Requirement> requirements = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            String target = entry.getKey();
            requirements.add(new Requirement(target, entry.getValue(),
                    State.ADAPTER_REQUIRED, family(target)));
        }
        // No API family is executable yet. This object is the authoritative
        // inventory consumed by future bridge implementations.
        return new Assessment(List.copyOf(requirements), false);
    }

    private static String family(String target) {
        String owner = target.substring(0, target.indexOf('#'));
        if (startsWithAny(owner, REGISTRATION_PREFIXES)) return "REGISTRY";
        if (startsWithAny(owner, NETWORK_PREFIXES)) return "NETWORK";
        if (startsWithAny(owner, FLUID_PREFIXES)) return "FLUID";
        if (startsWithAny(owner, LIFECYCLE_PREFIXES)) return "LIFECYCLE";
        if (owner.startsWith("net.minecraftforge.api.distmarker.")) return "DIST";
        if (owner.startsWith("net.minecraftforge.common.")) return "COMMON";
        if (owner.startsWith("net.minecraftforge.client.")) return "CLIENT";
        if (owner.startsWith("net.minecraftforge.server.")) return "SERVER";
        if (owner.startsWith("net.minecraftforge.event.")) return "GAME_EVENT";
        return "OTHER";
    }

    private static boolean startsWithAny(String value, Set<String> prefixes) {
        return prefixes.stream().anyMatch(value::startsWith);
    }
}
