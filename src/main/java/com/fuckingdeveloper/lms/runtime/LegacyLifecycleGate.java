package com.fuckingdeveloper.lms.runtime;

import com.fuckingdeveloper.lms.analysis.LegacyJarAnalyzer;

import java.util.ArrayList;
import java.util.List;

/**
 * Fail-closed boundary between passive legacy class linkage and lifecycle execution.
 *
 * <p>Linkage alone does not prove that Forge 1.19.2 lifecycle semantics exist on
 * the target. The gate records the profile capabilities that must be supplied
 * before a legacy @Mod entrypoint may be initialized.</p>
 */
public final class LegacyLifecycleGate {
    public enum State { SUPPORTED, PARTIAL, UNSUPPORTED, BLOCKED, NOT_APPLICABLE, UNKNOWN }

    public record Capability(String id, State state, boolean mandatory, String reason) {}
    public record Decision(boolean mayInitialize, List<Capability> capabilities) {}

    public Decision evaluate(LegacyJarAnalyzer.Report report, boolean lifecyclePlanReady,
                             boolean registrationPlanComplete, boolean compatibilitySurfaceExecutable) {
        List<Capability> capabilities = new ArrayList<>();

        capabilities.add(new Capability(
                "legacy-classloading", State.SUPPORTED, true,
                "Managed artifact class linkage is available"));

        capabilities.add(new Capability(
                "profile-bytecode-transform", State.SUPPORTED, true,
                "Every managed legacy-owned class passes through the selected profile transformer before definition"));

        capabilities.add(new Capability(
                "forge-1.19.2-lifecycle",
                lifecyclePlanReady && compatibilitySurfaceExecutable ? State.SUPPORTED
                        : lifecyclePlanReady ? State.PARTIAL : State.BLOCKED,
                true,
                lifecyclePlanReady && compatibilitySurfaceExecutable
                        ? "Entrypoint shape and constructor-reachable compatibility surface are executable"
                        : lifecyclePlanReady
                        ? "Entrypoint construction shape is verified; execution remains gated until compatibility surface is executable"
                        : "Entrypoint construction shape cannot be adapted safely"));

        capabilities.add(new Capability(
                "legacy-registration",
                registrationPlanComplete && compatibilitySurfaceExecutable ? State.SUPPORTED
                        : registrationPlanComplete ? State.PARTIAL : State.BLOCKED,
                true,
                registrationPlanComplete && compatibilitySurfaceExecutable
                        ? "Owned registration call graph is complete and all discovered boundaries are executable"
                        : registrationPlanComplete
                        ? "Owned registration call graph is complete but one or more API boundaries still require adapters"
                        : "Owned registration call graph is incomplete"));

        if (!report.transformerHints().isEmpty()) {
            capabilities.add(new Capability(
                    "legacy-transformers", State.PARTIAL, false,
                    "Static transformer requirements are diagnosed; only verified launch transformations execute"));
        } else {
            capabilities.add(new Capability(
                    "legacy-transformers", State.NOT_APPLICABLE, false,
                    "No legacy transformer hints were discovered"));
        }

        boolean mayInitialize = capabilities.stream()
                .filter(Capability::mandatory)
                .allMatch(capability -> capability.state() == State.SUPPORTED);
        return new Decision(mayInitialize, List.copyOf(capabilities));
    }
}
