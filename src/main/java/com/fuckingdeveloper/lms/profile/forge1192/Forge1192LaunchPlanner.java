package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.analysis.*;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192CompatibilityPlanner;
import com.fuckingdeveloper.lms.discovery.LegacyModDescriptor;
import com.fuckingdeveloper.lms.mapping.*;
import com.fuckingdeveloper.lms.profile.SourceEvidencePaths;
import com.fuckingdeveloper.lms.transform.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/** Source-specific coremod planning/evidence; cached only for this profile/root. */
final class Forge1192LaunchPlanner {
    private final java.util.Optional<LegacySrgIndex> srg;
    private final java.util.Optional<MappingFileLoader.LoadResult> mojmap;
    private final LegacyRuntimeBytecodeInspector runtime;

    Forge1192LaunchPlanner(SourceEvidencePaths paths) throws IOException {
        System.out.println("[LMS/prelaunch] stage=MAPPINGS");
        srg = LegacySrgIndex.load(paths.srg());
        mojmap = new MappingFileLoader().load(paths.mojang());
        runtime = Files.isRegularFile(paths.runtimeJar()) ? LegacyRuntimeBytecodeInspector.read(paths.runtimeJar()) : null;
    }

    List<TransformationSpec> plan(LegacyModDescriptor mod) throws IOException {
        System.out.println("[LMS/prelaunch] stage=JAR_ANALYSIS id=" + mod.modId());
        var report = new LegacyJarAnalyzer().analyze(mod.file());
        System.out.println("[LMS/prelaunch] stage=JAR_ANALYSIS_DONE id=" + mod.modId());
        System.out.println("[LMS/prelaunch] stage=METADATA id=" + mod.modId());
        var metadata = new LegacyMetadataAnalyzer().analyze(mod.file(), report.mixinConfigs());
        System.out.println("[LMS/prelaunch] stage=INJECTIONS id=" + mod.modId());
        var injections = new LegacyInjectionAnalyzer().analyze(mod.file(), metadata);
        System.out.println("[LMS/prelaunch] stage=COMPATIBILITY id=" + mod.modId());
        var compatibility = new Forge1192CompatibilityPlanner().plan(
                metadata, injections, srg.orElse(null),
                mojmap.map(MappingFileLoader.LoadResult::index).orElse(null), runtime);
        return new Forge1192TransformationSpecPlanner().plan(compatibility, mod.modId());
    }
}
