package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.analysis.LegacyJarAnalyzer;
import com.fuckingdeveloper.lms.discovery.LegacyJarScanner;
import com.fuckingdeveloper.lms.profile.InstalledSourceProfiles;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192Profile;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.*;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.Forge1192RegistrationPlanner;
import java.nio.file.*;
import java.util.*;

/** Offline admission inventory using production planners; never executes a legacy entrypoint. */
public final class CompatibilityBatchReport {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Expected artifact directory and output TSV");
        var catalog = InstalledSourceProfiles.create();
        var lines = new ArrayList<String>();
        lines.add("kind\tartifact\tmodId\tprofile\tstate\tfamily\tsymbol\tcallSites\tconsumers");
        record Gap(String family, String symbol) {}
        var consumers = new TreeMap<Gap, Set<String>>(Comparator.comparing(Gap::family).thenComparing(Gap::symbol));
        var counts = new HashMap<Gap, Long>();
        int total = 0, ready = 0;
        for (var mod : new LegacyJarScanner().scan(Path.of(args[0]))) {
            total++;
            var selected = catalog.resolve(mod);
            String state = "PROFILE_UNRESOLVED"; var gaps = new TreeMap<String,String>();
            if (selected.isPresent()) {
                if (!(selected.get() instanceof Forge1192Profile)) throw new UnsupportedOperationException("No offline admission assessor for " + selected.get().id());
                state = "SURFACE_READY_UNVERIFIED";
                var report = new LegacyJarAnalyzer().analyze(mod.file());
                if (report.modAnnotationCandidates().isEmpty()) state = "ENTRYPOINT_UNRESOLVED";
                for (String entrypoint : report.modAnnotationCandidates()) {
                    var lifecycle = new Forge1192LifecyclePlanner().plan(mod.file(), entrypoint);
                    if (lifecycle.status() != Forge1192LifecyclePlanner.Status.READY) { state = "LIFECYCLE_BLOCKED"; continue; }
                    var plan = new Forge1192RegistrationPlanner().plan(mod.file(), entrypoint);
                    if (!plan.complete()) state = "CALL_GRAPH_INCOMPLETE";
                    for (var requirement : new Forge1192CompatibilitySurface().assess(plan.boundaries(), mod.file()).requirements()) {
                        if (requirement.state() == Forge1192CompatibilitySurface.State.SUPPORTED
                                || requirement.state() == Forge1192CompatibilitySurface.State.INACTIVE_DATA_GENERATION) continue;
                        if (state.equals("SURFACE_READY_UNVERIFIED")) state = "API_BLOCKED";
                        gaps.put(requirement.target(), requirement.adapter());
                        var key = new Gap(requirement.adapter(), requirement.target());
                        consumers.computeIfAbsent(key, ignored -> new TreeSet<>()).add(mod.modId());
                        counts.merge(key, requirement.callSites(), Long::sum);
                    }
                }
            }
            if (state.equals("SURFACE_READY_UNVERIFIED")) ready++;
            lines.add(String.join("\t", "artifact", mod.file().getFileName().toString(), mod.modId(), selected.map(p -> p.id()).orElse("-"), state,
                    "-", selected.isEmpty() ? "minecraft=" + Objects.toString(mod.minecraftVersion(), "") : "-", Integer.toString(gaps.size()), "-"));
            System.out.println("BATCH " + mod.modId() + " " + state + " groupedBlockers=" + gaps.size());
        }
        for (var entry : consumers.entrySet()) {
            var key = entry.getKey();
            lines.add(String.join("\t", "blocker", "-", "-", "forge-1.19.2", "ADAPTER_REQUIRED", key.family(), key.symbol(), counts.get(key).toString(), String.join(",", entry.getValue())));
        }
        var output = Path.of(args[1]); Files.createDirectories(output.toAbsolutePath().getParent()); Files.write(output, lines);
        System.out.println("BATCH inventory only: artifacts=" + total + " surfaceReady=" + ready + " groups=" + consumers.size() + " report=" + output);
    }
}
