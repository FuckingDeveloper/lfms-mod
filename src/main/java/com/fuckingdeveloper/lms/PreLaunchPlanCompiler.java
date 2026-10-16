package com.fuckingdeveloper.lms;

import com.fuckingdeveloper.lms.profile.InstalledSourceProfiles;

import com.fuckingdeveloper.lms.discovery.LegacyJarScanner;
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
        System.out.println("[LMS/prelaunch] START root=" + root.toAbsolutePath());
        Path legacyMods = root.resolve("legacy-mods");
        Files.createDirectories(legacyMods);

        var profiles = InstalledSourceProfiles.create();
        var specs = new ArrayList<TransformationSpec>();
        System.out.println("[LMS/prelaunch] stage=DISCOVERY");
        var discovered = new LegacyJarScanner().scan(legacyMods);
        System.out.println("[LMS/prelaunch] stage=DISCOVERY_DONE candidates=" + discovered.size());
        for (var mod : discovered) {
            System.out.println("[LMS/prelaunch] stage=MOD_BEGIN id=" + mod.modId()
                    + " file=" + mod.file().getFileName());
            var selected = profiles.resolve(mod);
            if (selected.isEmpty()) {
                System.out.println("[LMS/prelaunch] stage=UNRESOLVED id=" + mod.modId());
                continue;
            }
            System.out.println("[LMS/prelaunch] stage=PROFILE id=" + mod.modId() + " source=" + selected.get().id());
            specs.addAll(selected.get().planLaunch(root,mod));
            System.out.println("[LMS/prelaunch] stage=MOD_DONE id=" + mod.modId());
        }

        Path output = root.resolve("lms/launch-plan.tsv");
        LaunchPlanWriter.write(output, specs);
        long ready = specs.stream().filter(s -> s.readiness() == TransformationSpec.Readiness.READY).count();
        System.out.println("[LMS/prelaunch] plan=" + output.toAbsolutePath()
                + " specs=" + specs.size() + " ready=" + ready);
    }
}
