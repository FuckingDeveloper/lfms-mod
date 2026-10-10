package com.fuckingdeveloper.lms.analysis;

import com.fuckingdeveloper.lms.discovery.LegacyModDescriptor;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Resolves declared legacy mod dependencies before any legacy class initialization. */
public final class LegacyDependencyPreflight {
    public record Report(List<LegacyMetadataAnalyzer.Dependency> missingMandatory,
                         List<LegacyMetadataAnalyzer.Dependency> missingOptional) {
        public boolean mayInitialize() { return missingMandatory.isEmpty(); }
    }

    public Report evaluate(String ownerModId, LegacyMetadataAnalyzer.Report metadata,
                           List<LegacyModDescriptor> discovered) {
        Set<String> available = new TreeSet<>();
        available.add("minecraft");
        available.add("forge"); // satisfied by the Forge-1.19.2 compatibility profile
        available.add(ownerModId);
        for (var mod : discovered) available.add(mod.modId());

        var mandatory = metadata.dependencies().stream()
                .filter(LegacyMetadataAnalyzer.Dependency::mandatory)
                .filter(dep -> !available.contains(dep.modId()))
                .toList();
        var optional = metadata.dependencies().stream()
                .filter(dep -> !dep.mandatory())
                .filter(dep -> !available.contains(dep.modId()))
                .toList();
        return new Report(mandatory, optional);
    }
}
