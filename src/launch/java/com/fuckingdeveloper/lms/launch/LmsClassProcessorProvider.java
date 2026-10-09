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
        LOG.info("[LMS/early] build-fingerprint=2026-10-09T20:23Z providerSource={} processorSource={}",
                codeSource(LmsClassProcessorProvider.class),
                codeSource(GenericInstructionEditProcessor.class));
        Path plan = Path.of(System.getProperty("user.dir"), "lms", "launch-plan.tsv");
        try {
            var launchPlan = LaunchPlanReader.read(plan);
            for (var spec : launchPlan.redirects()) collector.add(new GenericMethodCallRedirectProcessor(spec));
            for (var spec : launchPlan.edits()) collector.add(new GenericInstructionEditProcessor(spec));
            LOG.info("[LMS/early] launch plan={} redirects={} instructionEdits={}",
                    plan.toAbsolutePath(), launchPlan.redirects().size(), launchPlan.edits().size());
        } catch (Exception e) {
            LOG.error("[LMS/early] failed to read launch plan {}", plan.toAbsolutePath(), e);
        }
    }

    private static String codeSource(Class<?> type) {
        try {
            var domain = type.getProtectionDomain();
            var source = domain == null ? null : domain.getCodeSource();
            return source == null || source.getLocation() == null
                    ? "<unknown>"
                    : source.getLocation().toExternalForm();
        } catch (RuntimeException ex) {
            return "<error:" + ex.getClass().getSimpleName() + ">";
        }
    }
}
