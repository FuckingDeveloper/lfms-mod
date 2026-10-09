package com.fuckingdeveloper.lms;

import com.fuckingdeveloper.lms.analysis.LegacyJarAnalyzer;
import com.fuckingdeveloper.lms.analysis.LegacyInjectionAnalyzer;
import com.fuckingdeveloper.lms.analysis.LegacyMetadataAnalyzer;
import com.fuckingdeveloper.lms.analysis.LegacyRuntimeBytecodeInspector;
import com.fuckingdeveloper.lms.compat.LegacyCompatibilityPlanner;
import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.runtime.LegacyLifecycleGate;
import com.fuckingdeveloper.lms.runtime.Forge1192LifecyclePlanner;
import com.fuckingdeveloper.lms.discovery.LegacyJarScanner;
import com.fuckingdeveloper.lms.discovery.LegacyModDescriptor;
import com.fuckingdeveloper.lms.profile.Forge1192Profile;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.transform.TransformationSpecPlanner;
import com.fuckingdeveloper.lms.transform.LaunchPlanWriter;
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
                        // First controlled-classloading milestone. Link only the statically
                        // discovered legacy @Mod candidate classes and never initialize them.
                        // This deliberately happens before lifecycle adaptation: linkage errors
                        // become attributable compatibility evidence rather than accidental execution.
                        ClassLoader targetLoader = Thread.currentThread().getContextClassLoader();
                        if (targetLoader == null) targetLoader = LmsMod.class.getClassLoader();
                        try (var legacyLoader = new ManagedLegacyClassLoader(
                                mod.file(), targetLoader, new Forge1192LegacyClassTransformer())) {
                            for (String candidate : report.modAnnotationCandidates()) {
                                try {
                                    Class<?> linked = legacyLoader.linkOwnedClass(candidate);
                                    LOG.info("LMS classlink id={} class={} status=LINKED initialized=false loader={}",
                                            mod.modId(), candidate, linked.getClassLoader().getClass().getSimpleName());
                                } catch (LinkageError | ClassNotFoundException e) {
                                    LOG.info("LMS classlink id={} class={} status=BLOCKED initialized=false error={} message={}",
                                            mod.modId(), candidate, e.getClass().getName(), e.getMessage());
                                }
                            }
                        }
                        var lifecyclePlanner = new Forge1192LifecyclePlanner();
                        boolean lifecyclePlanReady = !report.modAnnotationCandidates().isEmpty();
                        for (String candidate : report.modAnnotationCandidates()) {
                            var lifecyclePlan = lifecyclePlanner.plan(mod.file(), candidate);
                            lifecyclePlanReady &= lifecyclePlan.status()
                                    == Forge1192LifecyclePlanner.Status.READY;
                            LOG.info("LMS lifecycle-plan id={} entrypoint={} status={} constructors={} interfaces={} forgeRefs={} reason={}",
                                    mod.modId(), candidate, lifecyclePlan.status(),
                                    lifecyclePlan.constructors(), lifecyclePlan.interfaces(),
                                    lifecyclePlan.forgeReferences(), lifecyclePlan.reason());
                        }
                        var lifecycleDecision = new LegacyLifecycleGate().evaluate(report);
                        for (var capability : lifecycleDecision.capabilities()) {
                            LOG.info("LMS capability id={} artifact={} capability={} state={} mandatory={} reason={}",
                                    mod.modId(), mod.file().getFileName(), capability.id(),
                                    capability.state(), capability.mandatory(), capability.reason());
                        }
                        LOG.info("LMS lifecycle id={} state={} entrypointInitialization=false reason={}",
                                mod.modId(),
                                lifecycleDecision.mayInitialize() ? "READY" : "BLOCKED",
                                lifecycleDecision.mayInitialize()
                                        ? "all mandatory profile capabilities are supported"
                                        : "one or more mandatory profile capabilities are not supported");
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
                        LegacyRuntimeBytecodeInspector legacyRuntime = null;
                        if (Files.isRegularFile(runtimeJar)) {
                            try {
                                legacyRuntime = LegacyRuntimeBytecodeInspector.read(runtimeJar);
                                var runtime = legacyRuntime;
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
                                mojmap.map(com.fuckingdeveloper.lms.mapping.MappingFileLoader.LoadResult::index).orElse(null),
                                legacyRuntime);
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
                        var transformationSpecs = new TransformationSpecPlanner().plan(compatibility);
                        Path launchPlan = Path.of(System.getProperty("user.dir"), "lms", "launch-plan.tsv");
                        LaunchPlanWriter.write(launchPlan, transformationSpecs);
                        LOG.info("LMS launch plan written file={}", launchPlan.toAbsolutePath());
                                                for (var spec : transformationSpecs) {
                            LOG.info("LMS transformation spec id={} kind={} readiness={} target={} anchor={} replacement={} edits={} reason={}",
                                    spec.id(), spec.kind(), spec.readiness(), spec.target(),
                                    spec.anchor(), spec.replacement(), spec.edits(), spec.reason());
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
                            for (var transform : coremod.transforms()) {
                                LOG.info("LMS coremod values path={} transform={} values={}",
                                        coremod.path(), transform.name(), transform.values());
                            }
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
