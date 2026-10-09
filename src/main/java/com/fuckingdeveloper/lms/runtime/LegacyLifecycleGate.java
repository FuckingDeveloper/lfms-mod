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

    public Decision evaluate(LegacyJarAnalyzer.Report report) {
        List<Capability> capabilities = new ArrayList<>();

        capabilities.add(new Capability(
                "legacy-classloading", State.SUPPORTED, true,
                "Managed artifact class linkage is available"));

        // The loader boundary exists, but profile transformations are not yet
        // applied to every legacy-owned class before definition.
        capabilities.add(new Capability(
                "profile-bytecode-transform", State.PARTIAL, true,
                "Legacy-owned class definition is controlled, but per-class profile transformation is not yet complete"));

        capabilities.add(new Capability(
                "forge-1.19.2-lifecycle", State.UNKNOWN, true,
                "Forge 1.19.2 @Mod construction/event lifecycle adapter is not installed yet"));

        capabilities.add(new Capability(
                "legacy-registration", State.UNKNOWN, true,
                "Legacy deferred/registry lifecycle has not been mapped to NeoForge registration boundaries"));

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
