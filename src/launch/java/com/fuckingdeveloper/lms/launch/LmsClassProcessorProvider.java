package com.fuckingdeveloper.lms.launch;

import net.neoforged.neoforgespi.transformation.ClassProcessorProvider;

import java.nio.file.Path;

/** Early FML entry point consuming only serialized, loader-neutral LMS plans. */
public final class LmsClassProcessorProvider implements ClassProcessorProvider {
    @Override
    public void createProcessors(Context context, Collector collector) {
        System.out.println("[LMS/early] FML class processor provider loaded");
        Path plan = Path.of(System.getProperty("user.dir"), "lms", "launch-plan.tsv");
        try {
            var redirects = LaunchPlanReader.readRedirects(plan);
            for (var spec : redirects) collector.add(new GenericMethodCallRedirectProcessor(spec));
            System.out.println("[LMS/early] launch plan=" + plan.toAbsolutePath()
                    + " redirects=" + redirects.size());
        } catch (Exception e) {
            System.err.println("[LMS/early] failed to read launch plan " + plan.toAbsolutePath() + ": " + e);
        }
    }
}
