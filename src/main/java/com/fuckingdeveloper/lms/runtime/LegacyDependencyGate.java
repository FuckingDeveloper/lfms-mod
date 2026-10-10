package com.fuckingdeveloper.lms.runtime;

import com.fuckingdeveloper.lms.analysis.LegacyMetadataAnalyzer;
import net.neoforged.fml.ModList;

import java.util.List;

/** Resolves declared legacy dependencies before any managed entrypoint executes. */
public final class LegacyDependencyGate {
    public record Missing(String modId, String versionRange, String reason) {}
    public record Decision(boolean satisfied, List<Missing> missing) {}

    public Decision evaluate(List<LegacyMetadataAnalyzer.Dependency> dependencies) {
        var missing = dependencies.stream()
                .filter(LegacyMetadataAnalyzer.Dependency::mandatory)
                .filter(dep -> !providedByProfile(dep.modId()))
                .filter(dep -> !ModList.get().isLoaded(dep.modId()))
                .map(dep -> new Missing(dep.modId(), dep.versionRange(),
                        "mandatory legacy dependency is not present in target runtime"))
                .toList();
        return new Decision(missing.isEmpty(), missing);
    }

    private static boolean providedByProfile(String modId) {
        // The Forge 1.19.2 compatibility profile supplies the loader/API boundary;
        // the running target supplies Minecraft itself. These are not external mods.
        return "forge".equals(modId) || "minecraft".equals(modId);
    }
}
