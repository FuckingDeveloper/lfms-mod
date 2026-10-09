package com.fuckingdeveloper.lms.compat;

import com.fuckingdeveloper.lms.analysis.LegacyInjectionAnalyzer;
import com.fuckingdeveloper.lms.analysis.LegacyMetadataAnalyzer;
import com.fuckingdeveloper.lms.analysis.LegacyRuntimeBytecodeInspector;
import com.fuckingdeveloper.lms.mapping.Forge1192MappingLayer;
import com.fuckingdeveloper.lms.mapping.LegacySrgIndex;
import com.fuckingdeveloper.lms.mapping.ProguardMappingReader;

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
                                   String reason,
                                   LegacySrgIndex.Resolution legacyResolution,
                                   LegacyRuntimeBytecodeInspector.Finding legacyRuntimeFinding) {}
    public record CoremodTransformationPlan(String source, String target, String canonicalLegacyTarget,
                                            List<LegacyInjectionAnalyzer.CoremodAnchor> anchors,
                                            List<AnchorResolution> anchorResolutions,
                                            List<LegacyInjectionAnalyzer.CoremodHookCall> hooks,
                                            List<LegacyInjectionAnalyzer.CoremodOperation> operations,
                                            List<LegacyInjectionAnalyzer.CoremodValue> values,
                                            List<String> mutationKinds, Forge1192MappingLayer.Status mappingStatus,
                                            String currentTarget, LegacySrgIndex.Resolution legacyTargetResolution) {}
    public record Plan(List<Requirement> requirements, List<CoremodTransformationPlan> coremodPlans,
                       int minecraftTargets, int forgeTargets, int accessMixins, int overwrites,
                       int injections, int coremodTransforms, int requiredDependencies) {}

    public Plan plan(LegacyMetadataAnalyzer.Report metadata, LegacyInjectionAnalyzer.Report injections) {
        return plan(metadata, injections, null, null, null);
    }

    public Plan plan(LegacyMetadataAnalyzer.Report metadata, LegacyInjectionAnalyzer.Report injections,
                     LegacySrgIndex srgIndex, ProguardMappingReader.Index mojmap) {
        return plan(metadata, injections, srgIndex, mojmap, null);
    }

    public Plan plan(LegacyMetadataAnalyzer.Report metadata, LegacyInjectionAnalyzer.Report injections,
                     LegacySrgIndex srgIndex, ProguardMappingReader.Index mojmap,
                     LegacyRuntimeBytecodeInspector legacyRuntime) {
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
                var sourceMapping = mappings.classifyMethod(target.owner(), target.method(), target.descriptor());
                var legacyTargetResolution = resolveLegacy(srgIndex, mojmap,
                        target.owner(), target.method(), target.descriptor());
                String qualifiedTarget = target.owner() + "#" + target.method() + target.descriptor();
                String canonicalLegacyTarget = canonicalLegacyTarget(
                        target.owner(), target.method(), target.descriptor(), legacyTargetResolution, mojmap);
                String canonicalOwner = ownerOf(canonicalLegacyTarget);
                var mapping = canonicalOwner.equals(target.owner())
                        ? sourceMapping
                        : mappings.classifyMethod(canonicalOwner, target.method(), target.descriptor());

                // Coremod targets use SRG names too. Recover the 1.19.2 Mojang name
                // and retry current-runtime identity before accepting descriptor-only
                // evidence. This mirrors anchor resolution and prevents e.g. two
                // same-descriptor methods from making an otherwise stable target ambiguous.
                String targetOfficialName = officialTargetMethodName(
                        target.owner(), target.method(), target.descriptor(),
                        legacyTargetResolution, mojmap);
                if (targetOfficialName != null) {
                    var namedTarget = mappings.classifyMethod(
                            canonicalOwner, targetOfficialName, target.descriptor());
                    if (namedTarget.status() == Forge1192MappingLayer.Status.VERIFIED_IDENTITY) {
                        mapping = namedTarget;
                    } else {
                        var migratedTarget = mappings.searchNamedMethods(canonicalOwner, targetOfficialName);
                        if (migratedTarget.candidates().size() == 1) {
                            var candidate = migratedTarget.candidates().getFirst();
                            mapping = new Forge1192MappingLayer.Mapping(
                                    canonicalOwner + "#" + target.method() + target.descriptor(),
                                    candidate.symbol(),
                                    Forge1192MappingLayer.Status.IDENTITY_CANDIDATE,
                                    "Recovered legacy target name survives with a changed descriptor: "
                                            + candidate.descriptor());
                        }
                    }
                }
                // If the canonical owner disappeared, consult the target-runtime index.
                // This is candidate discovery only: exact global identity can recover a moved
                // declaration, while name-only results remain diagnostic evidence and never
                // authorize an executable transformation.
                String targetIndexEvidence = "";
                if ((mapping.currentSymbol() == null || mapping.currentSymbol().isEmpty())
                        && targetOfficialName != null) {
                    var exactRuntimeIdentity = mappings.searchExactIdentity(
                            targetOfficialName, target.descriptor());
                    if (exactRuntimeIdentity.candidates().size() == 1) {
                        var candidate = exactRuntimeIdentity.candidates().getFirst();
                        var legacyTargetSemantics = legacyRuntime == null
                                ? new LegacyRuntimeBytecodeInspector.MethodSemantics(
                                        canonicalOwner, targetOfficialName, target.descriptor(), List.of())
                                : legacyRuntime.semantics(
                                        canonicalOwner, targetOfficialName, target.descriptor());
                        var currentCandidateSemantics = mappings.semantics(candidate.symbol());
                        boolean semanticVerified = !legacyTargetSemantics.operations().isEmpty()
                                && legacyTargetSemantics.operations().equals(currentCandidateSemantics.operations());
                        if (semanticVerified) {
                            mapping = new Forge1192MappingLayer.Mapping(
                                    canonicalOwner + "#" + target.method() + target.descriptor(),
                                    candidate.symbol(),
                                    Forge1192MappingLayer.Status.VERIFIED_IDENTITY,
                                    "Target owner moved; unique exact recovered identity and exact semantic fingerprint match");
                        }
                        targetIndexEvidence = " targetRuntimeIndex exact=" + exactRuntimeIdentity.reason()
                                + " candidate=" + candidate.symbol()
                                + " semanticVerified=" + semanticVerified
                                + " legacyOps=" + legacyTargetSemantics.operations()
                                + " currentOps=" + currentCandidateSemantics.operations();
                    } else {
                        var runtimeByName = mappings.searchRuntimeByName(targetOfficialName);
                        targetIndexEvidence = " targetRuntimeIndex exact=" + exactRuntimeIdentity.reason()
                                + " candidates=" + exactRuntimeIdentity.candidates()
                                + " byName=" + runtimeByName.reason()
                                + " candidates=" + runtimeByName.candidates();
                    }
                }

                String targetSemanticEvidence = "";
                if (legacyRuntime != null && targetOfficialName != null && !mapping.currentSymbol().isEmpty()) {
                    var legacyTargetSemantics = legacyRuntime.semantics(
                            canonicalOwner, targetOfficialName, target.descriptor());
                    // A malformed source owner (old MCP-style package) can differ from
                    // the canonical 1.19.2 Mojang owner recovered from mappings.
                    if (legacyTargetSemantics.operations().isEmpty()
                            && !canonicalOwner.equals(target.owner())) {
                        legacyTargetSemantics = legacyRuntime.semantics(
                                target.owner(), targetOfficialName, target.descriptor());
                    }
                    var currentTargetSemantics = mappings.semantics(mapping.currentSymbol());
                    targetSemanticEvidence = " targetSemanticFingerprint legacyOps="
                            + legacyTargetSemantics.operations()
                            + " currentOps=" + currentTargetSemantics.operations();
                }
                String mapped = mapping.currentSymbol().isEmpty() ? "" : " current=" + mapping.currentSymbol();
                String hooks = transform.hookCalls().isEmpty() ? "" : " hooks=" + transform.hookCalls();
                List<LegacyInjectionAnalyzer.CoremodAnchor> anchors = transform.anchors();
                List<AnchorResolution> anchorResolutions = anchors.stream()
                        .map(anchor -> {
                            var anchorMapping = mappings.classifyMethod(
                                    anchor.owner(), anchor.method(), anchor.descriptor());
                            var search = mappings.searchMethods(
                                    anchor.owner(), anchor.method(), anchor.descriptor());
                            var legacyResolution = resolveLegacy(srgIndex, mojmap,
                                    anchor.owner(), anchor.method(), anchor.descriptor());
                            var runtimeFinding = legacyRuntime == null ? null
                                    : findRuntimeAnchor(legacyRuntime, anchor, legacyResolution, mojmap);
                            boolean runtimeVerified = isRuntimeVerified(runtimeFinding);
                            boolean legacyPatchMember = anchor.owner().startsWith("net.minecraft.")
                                    && !anchor.method().matches("m_\\d+_")
                                    && legacyResolution.status() == LegacySrgIndex.ResolutionStatus.NOT_FOUND;
                            // Prefer the recovered official legacy name when that exact
                            // member still exists in the current hierarchy. Descriptor-only
                            // matching is ambiguous for methods such as armor/magic absorption.
                            var currentIdentity = runtimeVerified
                                    ? mappings.classifyMethod(anchor.owner(), runtimeFinding.method(), anchor.descriptor())
                                    : anchorMapping;
                            boolean currentIdentityVerified =
                                    currentIdentity.status() == Forge1192MappingLayer.Status.VERIFIED_IDENTITY;
                            var migratedByName = runtimeVerified && !currentIdentityVerified
                                    ? mappings.searchNamedMethods(anchor.owner(), runtimeFinding.method())
                                    : new Forge1192MappingLayer.MethodSearch("", List.of(), "");
                            boolean uniqueNamedMigration = migratedByName.candidates().size() == 1;

                            // A Forge-patched legacy member can disappear while its semantic
                            // successor survives under a different current name. When the
                            // recovered legacy method name itself is gone, compare the legacy
                            // method body against current same-descriptor candidates. Accept
                            // only one exact semantic fingerprint; never choose by descriptor alone.
                            // Compare the semantic body of the *declaring* legacy method.
                            // Forge extension methods are inherited by the Minecraft invocation
                            // owner, and the legacy runtime inspector intentionally only indexes
                            // classes physically present in the supplied runtime JAR. Querying
                            // Player#getStepHeight therefore cannot recover the interface body.
                            String semanticOwner = runtimeFinding != null
                                    && runtimeFinding.declaringOwner() != null
                                    && !runtimeFinding.declaringOwner().isEmpty()
                                    ? runtimeFinding.declaringOwner()
                                    : anchor.owner();
                            var legacySemanticOperations = runtimeVerified && legacyRuntime != null
                                    ? legacyRuntime.semantics(
                                            semanticOwner, runtimeFinding.method(), anchor.descriptor()).operations()
                                    : List.<String>of();
                            var semanticMigration = runtimeVerified && !currentIdentityVerified
                                    && !uniqueNamedMigration && legacyRuntime != null
                                    ? mappings.findUniqueSemanticMatch(
                                            anchor.owner(), anchor.descriptor(), legacySemanticOperations)
                                    : new Forge1192MappingLayer.MethodSearch("", List.of(), "");
                            boolean uniqueSemanticMigration = semanticMigration.candidates().size() == 1;

                            var effectiveMapping = currentIdentityVerified ? currentIdentity
                                    : uniqueNamedMigration
                                    ? new Forge1192MappingLayer.Mapping(
                                            anchor.owner() + "#" + runtimeFinding.method() + anchor.descriptor(),
                                            migratedByName.candidates().getFirst().symbol(),
                                            Forge1192MappingLayer.Status.IDENTITY_CANDIDATE,
                                            "Recovered legacy method name survives with a changed descriptor: "
                                                    + migratedByName.candidates().getFirst().descriptor())
                                    : uniqueSemanticMigration
                                    ? new Forge1192MappingLayer.Mapping(
                                            anchor.owner() + "#" + runtimeFinding.method() + anchor.descriptor(),
                                            semanticMigration.candidates().getFirst().symbol(),
                                            Forge1192MappingLayer.Status.IDENTITY_CANDIDATE,
                                            "Unique current method matches the legacy semantic fingerprint")
                                    : anchorMapping;
                            // A member absent from vanilla 1.19.2 mappings is a Forge patch.
                            // Keep bridge classification only when the recovered Forge member
                            // does not survive as the same exact identity in the current runtime.
                            boolean bridgeRequired = legacyPatchMember
                                    && !currentIdentityVerified
                                    && !uniqueNamedMigration
                                    && !uniqueSemanticMigration;
                            var status = bridgeRequired
                                    ? Forge1192MappingLayer.Status.FORGE_BRIDGE_REQUIRED
                                    : effectiveMapping.status();
                            String reason;
                            if (runtimeVerified) {
                                reason = "Legacy Forge 1.19.2 runtime verifies exact method"
                                        + (runtimeFinding.status().equals("INHERITED")
                                        ? " inherited from " + runtimeFinding.declaringOwner()
                                        : " declared by " + runtimeFinding.declaringOwner())
                                        + "; current-runtime mapping=" + effectiveMapping.status()
                                        + (effectiveMapping.currentSymbol().isEmpty()
                                        ? ""
                                        : " candidate=" + effectiveMapping.currentSymbol())
                                        + "; current-runtime evidence: " + effectiveMapping.reason()
                                        + "; semantic-owner=" + semanticOwner
                                        + "; legacy-semantic-ops=" + legacySemanticOperations
                                        + "; semantic-search=" + semanticMigration.reason()
                                        + "; semantic-candidates=" + semanticMigration.candidates();
                                if (!effectiveMapping.currentSymbol().isEmpty()) {
                                    var legacySemantics = legacyRuntime.semantics(
                                            anchor.owner(), runtimeFinding.method(), anchor.descriptor());
                                    var currentSemantics = mappings.semantics(effectiveMapping.currentSymbol());
                                    reason += "; semantic-fingerprint legacyOps=" + legacySemantics.operations()
                                            + " currentOps=" + currentSemantics.operations();
                                }
                            } else if (legacyPatchMember) {
                                reason = "Non-SRG coremod anchor is absent from vanilla 1.19.2 mappings; "
                                      + "treat as a Forge-patched member requiring a compatibility bridge";
                            } else {
                                reason = effectiveMapping.reason();
                            }
                            return new AnchorResolution(anchor, status,
                                    bridgeRequired ? "" : effectiveMapping.currentSymbol(),
                                    bridgeRequired ? List.of() : search.candidates(), reason,
                                    legacyResolution, runtimeFinding);
                        })
                        .toList();
                List<String> mutationKinds = transform.operations().stream()
                        .map(LegacyInjectionAnalyzer.CoremodOperation::kind)
                        .distinct()
                        .toList();
                // The source owner can be an obsolete MCP/package alias while the
                // 1.19.2 mappings recover the canonical Mojang owner. Target lookup
                // above starts with canonicalOwner, but OWNER_MISMATCH previously left
                // mapping/currentSymbol unresolved even when the canonical target is
                // uniquely identifiable. Re-run the recovered official name against
                // that canonical owner before emitting the plan.
                if ((mapping.currentSymbol() == null || mapping.currentSymbol().isEmpty())
                        && targetOfficialName != null && !canonicalOwner.equals(target.owner())) {
                    var canonicalIdentity = mappings.classifyMethod(
                            canonicalOwner, targetOfficialName, target.descriptor());
                    if (!canonicalIdentity.currentSymbol().isEmpty()) {
                        mapping = canonicalIdentity;
                    } else {
                        var canonicalMigration = mappings.searchNamedMethods(canonicalOwner, targetOfficialName);
                        if (canonicalMigration.candidates().size() == 1) {
                            var candidate = canonicalMigration.candidates().getFirst();
                            mapping = new Forge1192MappingLayer.Mapping(
                                    canonicalOwner + "#" + target.method() + target.descriptor(),
                                    candidate.symbol(),
                                    Forge1192MappingLayer.Status.IDENTITY_CANDIDATE,
                                    "Canonical legacy owner and recovered method name uniquely identify current target");
                        }
                    }
                }

                coremodPlans.add(new CoremodTransformationPlan(
                        coremod.path() + "#" + transform.name(), qualifiedTarget, canonicalLegacyTarget, anchors, anchorResolutions,
                        transform.hookCalls(), transform.operations(), transform.values(), mutationKinds, mapping.status(), mapping.currentSymbol(),
                        legacyTargetResolution));
                requirements.add(new Requirement(Kind.COREMOD_METHOD_TRANSFORM,
                        coremod.path() + "#" + transform.name(), qualifiedTarget,
                        "types=" + coremod.transformKinds() + " ASMAPI=" + coremod.asmApiCalls()
                                + hooks + " mapping=" + mapping.status() + mapped
                                + " reason=" + mapping.reason()
                                + targetSemanticEvidence + targetIndexEvidence
                                + " legacy=" + legacyTargetResolution.status()));
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

    private static LegacyRuntimeBytecodeInspector.Finding findRuntimeAnchor(
            LegacyRuntimeBytecodeInspector runtime,
            LegacyInjectionAnalyzer.CoremodAnchor anchor,
            LegacySrgIndex.Resolution legacyResolution,
            ProguardMappingReader.Index mojmap) {
        // ForgeGradle's mapped_official runtime contains official/Mojang method names,
        // while coremod source uses SRG names. Resolve the official name by the
        // legacy descriptor first; obfuscated method letters are not globally unique.
        String officialName = officialMethodName(anchor, legacyResolution, mojmap);
        if (officialName == null) {
            if (legacyResolution.status() == LegacySrgIndex.ResolutionStatus.SOURCE_DESCRIPTOR_MISMATCH) {
                var descriptorFinding = runtime.findUniqueByDescriptor(anchor.owner(), anchor.descriptor());
                if (isRuntimeVerified(descriptorFinding)) return descriptorFinding;
            }
            return runtime.find(anchor.owner(), anchor.method(), anchor.descriptor());
        }

        // Always probe the source invocation owner first. This preserves inherited
        // evidence (Player -> LivingEntity, BlockState -> BlockStateBase/IForge...).
        var sourceFinding = runtime.find(anchor.owner(), officialName, anchor.descriptor());
        if (isRuntimeVerified(sourceFinding)) return sourceFinding;

        // For exact single mapping matches, also probe the mapped declaring owner.
        if (mojmap != null && legacyResolution.matches().size() == 1) {
            var match = legacyResolution.matches().getFirst();
            if (!match.owners().isEmpty()) {
                String declaringOwner = mojmap.namedClass(match.owners().getFirst()).orElse(anchor.owner());
                var declaringFinding = runtime.find(declaringOwner, officialName, anchor.descriptor());
                if (isRuntimeVerified(declaringFinding)) return declaringFinding;
            }
        }
        return sourceFinding;
    }

    private static String officialTargetMethodName(
            String owner, String method, String descriptor,
            LegacySrgIndex.Resolution legacyResolution,
            ProguardMappingReader.Index mojmap) {
        if (mojmap == null || legacyResolution.matches().isEmpty()) return null;
        var names = legacyResolution.matches().stream()
                .flatMap(match -> {
                    if (match.owners().isEmpty()) return java.util.stream.Stream.empty();
                    String obfuscatedOwner = match.owners().getFirst();
                    String obfuscatedName = match.sourceName();
                    return mojmap.namedMethods().values().stream()
                            .filter(mapped -> mapped.obfuscatedOwner().equals(obfuscatedOwner))
                            .filter(mapped -> mapped.obfuscatedName().equals(obfuscatedName))
                            .filter(mapped -> parametersMatchDescriptor(mapped.parameters(), descriptor))
                            .map(ProguardMappingReader.MethodMapping::namedName);
                })
                .distinct()
                .toList();
        return names.size() == 1 ? names.getFirst() : null;
    }

    private static String officialMethodName(
            LegacyInjectionAnalyzer.CoremodAnchor anchor,
            LegacySrgIndex.Resolution legacyResolution,
            ProguardMappingReader.Index mojmap) {
        if (mojmap == null || legacyResolution.matches().isEmpty()) return null;

        // A coremod SRG invocation may name an inherited member. Therefore the
        // invocation owner is not necessarily one of the TSRG declaring owners.
        // Translate each legacy match by its exact obfuscated owner+name identity.
        // Restrict with the named parameter list when possible, then accept only
        // one unanimous official name.
        var exactNames = mappedOfficialNames(anchor, legacyResolution, mojmap, true);
        if (exactNames.size() == 1) return exactNames.getFirst();

        // Forge can patch an overload whose invocation descriptor is intentionally
        // absent from vanilla mappings. SOURCE_DESCRIPTOR_MISMATCH is exactly that
        // evidence shape for e.g. BlockState#skipRendering(BlockState, Direction).
        // In that case use the vanilla SRG identity only to recover the unanimous
        // official method name, then require the *source descriptor* to exist in
        // the Forge runtime bytecode before considering the anchor verified.
        if (legacyResolution.status() == LegacySrgIndex.ResolutionStatus.SOURCE_DESCRIPTOR_MISMATCH) {
            var identityNames = mappedOfficialNames(anchor, legacyResolution, mojmap, false);
            if (identityNames.size() == 1) return identityNames.getFirst();
        }
        return null;
    }

    private static List<String> mappedOfficialNames(
            LegacyInjectionAnalyzer.CoremodAnchor anchor,
            LegacySrgIndex.Resolution legacyResolution,
            ProguardMappingReader.Index mojmap,
            boolean requireSourceParameters) {
        return legacyResolution.matches().stream()
                .flatMap(match -> {
                    if (match.owners().isEmpty()) return java.util.stream.Stream.empty();
                    String obfuscatedOwner = match.owners().getFirst();
                    String obfuscatedName = match.sourceName();
                    return mojmap.namedMethods().values().stream()
                            .filter(method -> method.obfuscatedOwner().equals(obfuscatedOwner))
                            .filter(method -> method.obfuscatedName().equals(obfuscatedName))
                            .filter(method -> !requireSourceParameters
                                    || parametersMatchDescriptor(method.parameters(), anchor.descriptor()))
                            .map(ProguardMappingReader.MethodMapping::namedName);
                })
                .distinct()
                .toList();
    }

    private static boolean parametersMatchDescriptor(String parameters, String descriptor) {
        org.objectweb.asm.Type[] argumentTypes = org.objectweb.asm.Type.getArgumentTypes(descriptor);
        if (parameters.isBlank()) return argumentTypes.length == 0;
        String[] namedParameters = parameters.split("\\s*,\\s*");
        if (namedParameters.length != argumentTypes.length) return false;
        for (int i = 0; i < namedParameters.length; i++) {
            if (!namedParameters[i].equals(argumentTypes[i].getClassName())) return false;
        }
        return true;
    }

    private static boolean isRuntimeVerified(LegacyRuntimeBytecodeInspector.Finding finding) {
        return finding != null
                && (finding.status().equals("DECLARED") || finding.status().equals("INHERITED"));
    }

    private static String ownerOf(String qualifiedMethod) {
        int hash = qualifiedMethod.indexOf('#');
        return hash < 0 ? qualifiedMethod : qualifiedMethod.substring(0, hash);
    }

    private static String canonicalLegacyTarget(String sourceOwner, String name, String descriptor,
                                                LegacySrgIndex.Resolution resolution,
                                                ProguardMappingReader.Index mojmap) {
        if (mojmap == null || resolution.matches().size() != 1) {
            return sourceOwner + "#" + name + descriptor;
        }
        var match = resolution.matches().getFirst();
        if (match.owners().isEmpty()) return sourceOwner + "#" + name + descriptor;
        String owner = mojmap.namedClass(match.owners().getFirst()).orElse(sourceOwner);
        return owner + "#" + name + descriptor;
    }

    private static LegacySrgIndex.Resolution resolveLegacy(LegacySrgIndex srgIndex,
                                                            ProguardMappingReader.Index mojmap,
                                                            String owner, String name, String descriptor) {
        if (srgIndex == null) {
            return new LegacySrgIndex.Resolution(LegacySrgIndex.ResolutionStatus.NOT_FOUND,
                    List.of(), "Legacy mapping index unavailable");
        }
        return mojmap != null
                ? srgIndex.resolveWithNamedOwner(owner, name, descriptor, mojmap)
                : srgIndex.resolve(owner, name, descriptor);
    }

}
