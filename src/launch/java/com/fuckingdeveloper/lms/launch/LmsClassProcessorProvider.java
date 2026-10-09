package com.fuckingdeveloper.lms.launch;

import net.neoforged.neoforgespi.transformation.ClassProcessorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/** Early FML entry point consuming only serialized, loader-neutral LMS plans. */
public final class LmsClassProcessorProvider implements ClassProcessorProvider {
    private static final Logger LOG = LoggerFactory.getLogger(LmsClassProcessorProvider.class);

    @Override
    public void createProcessors(Context context, Collector collector) {
        LOG.info("[LMS/early] FML class processor provider loaded");
        Path plan = Path.of(System.getProperty("user.dir"), "lms", "launch-plan.tsv");
        try {
            var redirects = LaunchPlanReader.readRedirects(plan);
            for (var spec : redirects) collector.add(new GenericMethodCallRedirectProcessor(spec));
            LOG.info("[LMS/early] launch plan={} redirects={}", plan.toAbsolutePath(), redirects.size());
        } catch (Exception e) {
            LOG.error("[LMS/early] failed to read launch plan {}", plan.toAbsolutePath(), e);
        }
    }
}
