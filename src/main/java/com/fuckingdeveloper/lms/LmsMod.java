package com.fuckingdeveloper.lms;

import com.fuckingdeveloper.lms.analysis.LegacyJarAnalyzer;
import com.fuckingdeveloper.lms.analysis.LegacyInjectionAnalyzer;
import com.fuckingdeveloper.lms.analysis.LegacyMetadataAnalyzer;
import com.fuckingdeveloper.lms.analysis.LegacyRuntimeBytecodeInspector;
import com.fuckingdeveloper.lms.compat.LegacyCompatibilityPlanner;
import com.fuckingdeveloper.lms.discovery.LegacyJarScanner;
import com.fuckingdeveloper.lms.discovery.LegacyModDescriptor;
import com.fuckingdeveloper.lms.profile.Forge1192Profile;
import com.fuckingdeveloper.lms.mapping.LegacySrgIndex;
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
                        var metadata = new LegacyMetadataAnalyzer().analyze(mod.file(), report.mixinConfigs());
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
                        Path srgFile = Path.of(System.getProperty("user.dir"), "legacy-mappings", "1.19.2", "joined.tsrg");
                        var srgIndex = LegacySrgIndex.load(srgFile);
                        Path mojmapFile = Path.of(System.getProperty("user.dir"), "legacy-mappings", "1.19.2", "client.txt");
                        var mojmap = new com.fuckingdeveloper.lms.mapping.MappingFileLoader().load(mojmapFile);
                        // Optional evidence from a user-provided, named 1.19.2 runtime JAR.
                        // This does not load classes or execute legacy coremods.
                        Path runtimeJar = Path.of(System.getProperty("user.dir"), "legacy-runtime", "1.19.2", "client.jar");
                        if (Files.isRegularFile(runtimeJar)) {
                            try {
                                var runtime = LegacyRuntimeBytecodeInspector.read(runtimeJar);
                                LOG.info("LMS legacy runtime bytecode file={} classes={}",
                                        runtimeJar.toAbsolutePath(), runtime.classCount());
                                LOG.info("LMS legacy runtime probe={}", runtime.find(
                                        "net.minecraft.world.entity.player.Player", "getDamageAfterArmorAbsorb",
                                        "(Lnet/minecraft/world/damagesource/DamageSource;F)F"));
                                LOG.info("LMS legacy runtime probe={}", runtime.find(
                                        "net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase",
                                        "skipRendering",
                                        "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"));
                                LOG.info("LMS legacy runtime probe={}", runtime.find(
                                        "net.minecraft.world.entity.player.Player", "getStepHeight", "()F"));
                            } catch (IOException | RuntimeException e) {
                                LOG.warn("LMS optional legacy runtime inspection failed for {}", runtimeJar, e);
                            }
                        }
                        var compatibility = new LegacyCompatibilityPlanner().plan(
                                metadata, injectionReport, srgIndex.orElse(null),
                                mojmap.map(com.fuckingdeveloper.lms.mapping.MappingFileLoader.LoadResult::index).orElse(null));
                        LOG.info("LMS compatibility id={} minecraftTargets={} forgeTargets={} accessMixins={} overwrites={} injections={} coremodTransforms={} requiredDependencies={}",
                                mod.modId(), compatibility.minecraftTargets(), compatibility.forgeTargets(),
                                compatibility.accessMixins(), compatibility.overwrites(), compatibility.injections(),
                                compatibility.coremodTransforms(), compatibility.requiredDependencies());
                        for (var requirement : compatibility.requirements()) {
                            LOG.info("LMS requirement kind={} source={} target={} detail={}",
                                    requirement.kind(), requirement.source(), requirement.target(), requirement.detail());
                        }
                        LOG.info("LMS SRG mappings file={} loaded={} methods={} namespaces={} mojmapFile={} mojmapLoaded={}",
                                srgFile.toAbsolutePath(), srgIndex.isPresent(),
                                srgIndex.map(LegacySrgIndex::methodCount).orElse(0),
                                srgIndex.map(LegacySrgIndex::namespaces).orElse(List.of()),
                                mojmapFile.toAbsolutePath(), mojmap.isPresent());
                        LOG.info("LMS Mojang mappings recognized={} classes={} methods={} preview={}",
                                mojmap.map(com.fuckingdeveloper.lms.mapping.MappingFileLoader.LoadResult::recognized).orElse(false),
                                mojmap.map(result -> result.index().namedToObfuscatedClasses().size()).orElse(0),
                                mojmap.map(result -> result.index().namedMethods().size()).orElse(0),
                                mojmap.map(com.fuckingdeveloper.lms.mapping.MappingFileLoader.LoadResult::preview).orElse(List.of()));
                        for (var plan : compatibility.coremodPlans()) {
                            LOG.info("LMS coremod plan source={} target={} canonicalLegacyTarget={} legacyTargetResolution={} anchors={} anchorResolutions={} hooks={} mutations={} mapping={} current={}",
                                    plan.source(), plan.target(), plan.canonicalLegacyTarget(), plan.legacyTargetResolution(), plan.anchors(),
                                    plan.anchorResolutions(), plan.hooks(), plan.mutationKinds(),
                                    plan.mappingStatus(), plan.currentTarget());
                        }
                        for (var coremod : injectionReport.coremods()) {
                            LOG.info("LMS coremod path={} targets={} referencedClasses={} asmApiCalls={}",
                                    coremod.path(), coremod.declaredTargets(),
                                    coremod.referencedClasses(), coremod.asmApiCalls());
                            LOG.info("LMS coremod operations path={} transformKinds={} mappedMethods={} builtMethodCalls={}",
                                    coremod.path(), coremod.transformKinds(),
                                    coremod.mappedMethods(), coremod.builtMethodCalls());
                            LOG.info("LMS coremod methodTargets path={} targets={}",
                                    coremod.path(), coremod.methodTargets());
                            LOG.info("LMS coremod hookCalls path={} calls={}",
                                    coremod.path(), coremod.hookCalls());
                            LOG.info("LMS coremod transforms path={} transforms={}",
                                    coremod.path(), coremod.transforms());
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
