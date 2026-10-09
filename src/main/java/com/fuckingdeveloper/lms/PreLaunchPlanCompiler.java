package com.fuckingdeveloper.lms;

import com.fuckingdeveloper.lms.analysis.*;
import com.fuckingdeveloper.lms.compat.LegacyCompatibilityPlanner;
import com.fuckingdeveloper.lms.discovery.LegacyJarScanner;
import com.fuckingdeveloper.lms.mapping.*;
import com.fuckingdeveloper.lms.profile.Forge1192Profile;
import com.fuckingdeveloper.lms.transform.*;

import java.nio.file.*;
import java.util.ArrayList;

/**
 * Development/pre-launch entry point. Produces the loader-neutral plan before
 * FancyModLoader starts, so early processors consume the plan from this launch.
 */
public final class PreLaunchPlanCompiler {
    private PreLaunchPlanCompiler() {}

    public static void main(String[] args) throws Exception {
        Path root = Path.of(System.getProperty("user.dir"));
        Path legacyMods = root.resolve("legacy-mods");
        Files.createDirectories(legacyMods);

        var profile = new Forge1192Profile();
        var srg = LegacySrgIndex.load(root.resolve("legacy-mappings/1.19.2/joined.tsrg"));
        var mojmap = new MappingFileLoader().load(root.resolve("legacy-mappings/1.19.2/client.txt"));
        LegacyRuntimeBytecodeInspector runtime = null;
        Path runtimeJar = root.resolve("legacy-runtime/1.19.2/client.jar");
        if (Files.isRegularFile(runtimeJar)) runtime = LegacyRuntimeBytecodeInspector.read(runtimeJar);

        var specs = new ArrayList<TransformationSpec>();
        for (var mod : new LegacyJarScanner().scan(legacyMods)) {
            if (!profile.supports(mod)) continue;
            var report = new LegacyJarAnalyzer().analyze(mod.file());
            var metadata = new LegacyMetadataAnalyzer().analyze(mod.file(), report.mixinConfigs());
            var injections = new LegacyInjectionAnalyzer().analyze(mod.file(), metadata);
            var compatibility = new LegacyCompatibilityPlanner().plan(
                    metadata, injections, srg.orElse(null),
                    mojmap.map(MappingFileLoader.LoadResult::index).orElse(null), runtime);
            specs.addAll(new TransformationSpecPlanner().plan(compatibility));
        }

        Path output = root.resolve("lms/launch-plan.tsv");
        LaunchPlanWriter.write(output, specs);
        long ready = specs.stream().filter(s -> s.readiness() == TransformationSpec.Readiness.READY).count();
        System.out.println("[LMS/prelaunch] plan=" + output.toAbsolutePath()
                + " specs=" + specs.size() + " ready=" + ready);
    }
}
