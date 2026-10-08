package com.fuckingdeveloper.lms.mapping;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Canonical mapping evidence for the Forge 1.19.2 profile.
 *
 * <p>This layer deliberately does not guess 26.3 symbols. It records legacy
 * symbols first; a mapping is only considered resolved after current-runtime
 * evidence is attached.</p>
 */
public final class Forge1192MappingLayer {
    public enum Status { UNRESOLVED, IDENTITY_CANDIDATE, FORGE_BRIDGE_REQUIRED }

    public record Mapping(String legacySymbol, String currentSymbol, Status status, String reason) {}

    private final Map<String, Mapping> mappings = new LinkedHashMap<>();

    public Mapping classifyClass(String legacyClass) {
        if (legacyClass.startsWith("net.minecraftforge.")) {
            return mappings.computeIfAbsent(legacyClass, key ->
                    new Mapping(key, "", Status.FORGE_BRIDGE_REQUIRED,
                            "Legacy Forge namespace requires NeoForge compatibility bridge or replacement"));
        }
        if (legacyClass.startsWith("net.minecraft.")) {
            return mappings.computeIfAbsent(legacyClass, key ->
                    new Mapping(key, key, Status.IDENTITY_CANDIDATE,
                            "Class name survived as a candidate only; members/signatures still require verification"));
        }
        return mappings.computeIfAbsent(legacyClass, key ->
                new Mapping(key, "", Status.UNRESOLVED, "No profile mapping evidence"));
    }

    public Mapping classifyMethod(String legacyMethod) {
        return mappings.computeIfAbsent("method:" + legacyMethod, key ->
                new Mapping(legacyMethod, "", Status.UNRESOLVED,
                        "Forge 1.19.2 SRG method requires owner/descriptor evidence before 26.3 mapping"));
    }

    public Optional<Mapping> find(String symbol) {
        return Optional.ofNullable(mappings.get(symbol));
    }
}
