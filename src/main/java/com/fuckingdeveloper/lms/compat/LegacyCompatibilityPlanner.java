package com.fuckingdeveloper.lms.compat;

import com.fuckingdeveloper.lms.analysis.LegacyInjectionAnalyzer;
import com.fuckingdeveloper.lms.analysis.LegacyMetadataAnalyzer;
import com.fuckingdeveloper.lms.mapping.Forge1192MappingLayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Produces a non-executing compatibility requirement plan from static legacy evidence.
 * This is intentionally descriptive: it does not load or transform legacy classes.
 */
public final class LegacyCompatibilityPlanner {
    public enum Kind {
        MINECRAFT_MIXIN_TARGET,
        FORGE_MIXIN_TARGET,
        MIXIN_ACCESS,
        MIXIN_OVERWRITE,
        MIXIN_INJECTION,
        COREMOD_METHOD_TRANSFORM,
        REQUIRED_DEPENDENCY
    }

    public record Requirement(Kind kind, String source, String target, String detail) {}
    public record AnchorResolution(LegacyInjectionAnalyzer.CoremodAnchor anchor,
                                   Forge1192MappingLayer.Status mappingStatus,
                                   String currentSymbol,
                                   List<Forge1192MappingLayer.MethodCandidate> candidates,
                                   String reason) {}
    public record CoremodTransformationPlan(String source, String target,
                                            List<LegacyInjectionAnalyzer.CoremodAnchor> anchors,
                                            List<AnchorResolution> anchorResolutions,
                                            List<LegacyInjectionAnalyzer.CoremodHookCall> hooks,
                                            List<String> mutationKinds, Forge1192MappingLayer.Status mappingStatus,
                                            String currentTarget) {}
    public record Plan(List<Requirement> requirements, List<CoremodTransformationPlan> coremodPlans,
                       int minecraftTargets, int forgeTargets, int accessMixins, int overwrites,
                       int injections, int coremodTransforms, int requiredDependencies) {}

    public Plan plan(LegacyMetadataAnalyzer.Report metadata, LegacyInjectionAnalyzer.Report injections) {
        List<Requirement> requirements = new ArrayList<>();
        List<CoremodTransformationPlan> coremodPlans = new ArrayList<>();
        Forge1192MappingLayer mappings = new Forge1192MappingLayer();
        int minecraftTargets = 0, forgeTargets = 0, accessMixins = 0;
        int overwrites = 0, injectionCount = 0, coremods = 0, dependencies = 0;

        for (var mixin : injections.mixins()) {
            for (String target : mixin.targets()) {
                if (target.startsWith("net.minecraft.")) {
                    minecraftTargets++;
                    var mapping = mappings.classifyClass(target);
                    requirements.add(new Requirement(Kind.MINECRAFT_MIXIN_TARGET, mixin.source(), target,
                            String.join(",", mixin.mechanisms()) + " mapping=" + mapping.status()));
                } else if (target.startsWith("net.minecraftforge.")) {
                    forgeTargets++;
                    var mapping = mappings.classifyClass(target);
                    requirements.add(new Requirement(Kind.FORGE_MIXIN_TARGET, mixin.source(), target,
                            String.join(",", mixin.mechanisms()) + " mapping=" + mapping.status()));
                }
            }
            if (mixin.mechanisms().stream().anyMatch(m -> m.equals("Accessor") || m.equals("Invoker") || m.equals("Shadow"))) {
                accessMixins++;
                requirements.add(new Requirement(Kind.MIXIN_ACCESS, mixin.source(),
                        String.join(",", mixin.targets()), String.join(",", mixin.mechanisms())));
            }
            if (mixin.mechanisms().contains("Overwrite")) {
                overwrites++;
                requirements.add(new Requirement(Kind.MIXIN_OVERWRITE, mixin.source(),
                        String.join(",", mixin.targets()), "Overwrite"));
            }
            if (!mixin.injections().isEmpty()) {
                injectionCount += mixin.injections().size();
                for (var injection : mixin.injections()) {
                    requirements.add(new Requirement(Kind.MIXIN_INJECTION, mixin.source(),
                            String.join(",", mixin.targets()),
                            injection.annotation() + " " + injection.selectors() + " @" + injection.at()));
                }
            }
        }

        for (var coremod : injections.coremods()) {
            // Plan the structured transformer, not the global mapMethod/buildMethodCall inventories.
            // This keeps each target tied to the hook calls that actually occur in its JS body.
            for (var transform : coremod.transforms()) {
                coremods++;
                var target = transform.target();
                var mapping = mappings.classifyMethod(target.owner(), target.method(), target.descriptor());
                String qualifiedTarget = target.owner() + "#" + target.method() + target.descriptor();
                String mapped = mapping.currentSymbol().isEmpty() ? "" : " current=" + mapping.currentSymbol();
                String hooks = transform.hookCalls().isEmpty() ? "" : " hooks=" + transform.hookCalls();
                List<LegacyInjectionAnalyzer.CoremodAnchor> anchors = transform.anchors();
                List<AnchorResolution> anchorResolutions = anchors.stream()
                        .map(anchor -> {
                            var anchorMapping = mappings.classifyMethod(
                                    anchor.owner(), anchor.method(), anchor.descriptor());
                            var search = mappings.searchMethods(
                                    anchor.owner(), anchor.method(), anchor.descriptor());
                            return new AnchorResolution(anchor, anchorMapping.status(),
                                    anchorMapping.currentSymbol(), search.candidates(), anchorMapping.reason());
                        })
                        .toList();
                List<String> mutationKinds = transform.operations().stream()
                        .map(LegacyInjectionAnalyzer.CoremodOperation::kind)
                        .distinct()
                        .toList();
                coremodPlans.add(new CoremodTransformationPlan(
                        coremod.path() + "#" + transform.name(), qualifiedTarget, anchors, anchorResolutions,
                        transform.hookCalls(), mutationKinds, mapping.status(), mapping.currentSymbol()));
                requirements.add(new Requirement(Kind.COREMOD_METHOD_TRANSFORM,
                        coremod.path() + "#" + transform.name(), qualifiedTarget,
                        "types=" + coremod.transformKinds() + " ASMAPI=" + coremod.asmApiCalls()
                                + hooks + " mapping=" + mapping.status() + mapped
                                + " reason=" + mapping.reason()));
            }
        }

        for (var dependency : metadata.dependencies()) {
            if (dependency.mandatory()
                    && !dependency.modId().equals("minecraft")
                    && !dependency.modId().equals("forge")) {
                dependencies++;
                requirements.add(new Requirement(Kind.REQUIRED_DEPENDENCY, "mods.toml",
                        dependency.modId(), dependency.versionRange()));
            }
        }

        return new Plan(List.copyOf(requirements), List.copyOf(coremodPlans), minecraftTargets, forgeTargets,
                accessMixins, overwrites, injectionCount, coremods, dependencies);
    }
}
