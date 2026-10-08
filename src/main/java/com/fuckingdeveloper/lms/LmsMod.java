package com.fuckingdeveloper.lms;

import com.fuckingdeveloper.lms.analysis.LegacyJarAnalyzer;
import com.fuckingdeveloper.lms.analysis.LegacyInjectionAnalyzer;
import com.fuckingdeveloper.lms.analysis.LegacyMetadataAnalyzer;
import com.fuckingdeveloper.lms.discovery.LegacyJarScanner;
import com.fuckingdeveloper.lms.discovery.LegacyModDescriptor;
import com.fuckingdeveloper.lms.profile.Forge1192Profile;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Mod(LmsMod.MOD_ID)
public final class LmsMod {
    public static final String MOD_ID = "lms";
    private static final Logger LOG = LoggerFactory.getLogger(LmsMod.class);

    public LmsMod() {
        // Discovery-only milestone: never load or transform arbitrary legacy classes.
        Path directory = Path.of(System.getProperty("user.dir"), "legacy-mods");
        try {
            Files.createDirectories(directory);
            List<LegacyModDescriptor> mods = new LegacyJarScanner().scan(directory);
            LOG.info("LMS discovery: {} candidate(s) in {}", mods.size(), directory.toAbsolutePath());
            Forge1192Profile profile = new Forge1192Profile();
            for (LegacyModDescriptor mod : mods) {
                LOG.info("LMS candidate={} id={} loader={} minecraft={} profile={}",
                        mod.file().getFileName(), mod.modId(), mod.loader(), mod.minecraftVersion(),
                        profile.supports(mod) ? profile.id() : "UNRESOLVED");
                if (profile.supports(mod)) {
                    try {
                        var report = new LegacyJarAnalyzer().analyze(mod.file());
                        LOG.info("LMS analysis id={} classes={} forgeRefClasses={} minecraftRefClasses={} unreadable={}",
                                mod.modId(), report.classes(), report.forgeReferenceClasses(),
                                report.minecraftReferenceClasses(), report.unreadableClasses());
                        LOG.info("LMS analysis id={} modAnnotationCandidates={} mixinConfigs={} nestedJars={} transformerHints={}",
                                mod.modId(), report.modAnnotationCandidates(), report.mixinConfigs(),
                                report.nestedJars(), report.transformerHints());
                        var metadata = new LegacyMetadataAnalyzer().analyze(mod.file());
                        LOG.info("LMS metadata id={} dependencies={}", mod.modId(), metadata.dependencies());
                        LOG.info("LMS metadata id={} mixinClasses={} coremodScripts={} coremodTargetHints={}",
                                mod.modId(), metadata.mixinClasses(), metadata.coremodScripts(),
                                metadata.coremodTargetHints());
                        var injectionReport = new LegacyInjectionAnalyzer().analyze(mod.file(), metadata);
                        LOG.info("LMS injections id={} mixins={} coremods={}",
                                mod.modId(), injectionReport.mixins().size(), injectionReport.coremods().size());
                        for (var mixin : injectionReport.mixins()) {
                            LOG.info("LMS mixin source={} targets={} mechanisms={} injections={} error={}",
                                    mixin.source(), mixin.targets(), mixin.mechanisms(),
                                    mixin.injections(), mixin.error());
                        }
                        for (var coremod : injectionReport.coremods()) {
                            LOG.info("LMS coremod path={} targets={} referencedClasses={} asmApiCalls={}",
                                    coremod.path(), coremod.declaredTargets(),
                                    coremod.referencedClasses(), coremod.asmApiCalls());
                            LOG.info("LMS coremod operations path={} transformKinds={} mappedMethods={} builtMethodCalls={}",
                                    coremod.path(), coremod.transformKinds(),
                                    coremod.mappedMethods(), coremod.builtMethodCalls());
                        }
                    } catch (IOException e) {
                        LOG.warn("LMS static analysis failed for {}", mod.file(), e);
                    }
                }
            }
        } catch (IOException e) {
            LOG.error("LMS legacy discovery failed for {}", directory, e);
        }
    }
}
