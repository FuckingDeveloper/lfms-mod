package com.fuckingdeveloper.lms.transform;

import com.fuckingdeveloper.lms.analysis.LegacyInjectionAnalyzer;
import com.fuckingdeveloper.lms.compat.LegacyCompatibilityPlanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts descriptive compatibility plans into loader-neutral executable specs.
 *
 * Conservative by design: a transform is emitted READY only when the planner has
 * one current target, one resolved current anchor and one hook. Unsupported JS
 * mutation shapes remain UNRESOLVED instead of being guessed.
 */
public final class TransformationSpecPlanner {
    public List<TransformationSpec> plan(LegacyCompatibilityPlanner.Plan compatibility) {
        List<TransformationSpec> result = new ArrayList<>();
        for (var plan : compatibility.coremodPlans()) {
            result.add(toSpec(plan));
        }
        return List.copyOf(result);
    }

    private static TransformationSpec toSpec(LegacyCompatibilityPlanner.CoremodTransformationPlan plan) {
        String id = sanitize(plan.source());

        if (plan.currentTarget().isEmpty()) {
            return unresolved(id, "Current target method is unresolved");
        }
        if (plan.anchorResolutions().size() != 1) {
            return unresolved(id, "METHOD_CALL_REDIRECT requires exactly one anchor; found "
                    + plan.anchorResolutions().size());
        }
        if (plan.hooks().size() != 1) {
            return unresolved(id, "METHOD_CALL_REDIRECT requires exactly one hook; found "
                    + plan.hooks().size());
        }
        if (!plan.mutationKinds().contains("set")) {
            return unresolved(id, "No instruction-set mutation was detected: " + plan.mutationKinds());
        }

        var anchorResolution = plan.anchorResolutions().getFirst();
        if (anchorResolution.currentSymbol().isEmpty()) {
            return unresolved(id, "Current anchor is unresolved: " + anchorResolution.reason());
        }

        MethodParts target = parseSymbol(plan.currentTarget());
        MethodParts anchor = parseSymbol(anchorResolution.currentSymbol());
        if (target == null || anchor == null) {
            return unresolved(id, "Planner produced an unparsable current method symbol");
        }

        LegacyInjectionAnalyzer.CoremodHookCall hook = plan.hooks().getFirst();
        var hookInvocation = invocation(hook.invocationType());
        if (hookInvocation == TransformationSpec.Invocation.UNKNOWN) {
            return unresolved(id, "Unsupported hook invocation type: " + hook.invocationType());
        }

        // The legacy anchor invocation opcode is not currently retained by the JS
        // analyzer. Keep the spec unresolved rather than assuming INVOKEVIRTUAL.
        return new TransformationSpec(
                id,
                TransformationSpec.Kind.METHOD_CALL_REDIRECT,
                ref(target, TransformationSpec.Invocation.UNKNOWN),
                ref(anchor, TransformationSpec.Invocation.UNKNOWN),
                new TransformationSpec.MethodRef(
                        internal(hook.owner()), hook.method(), hook.descriptor(), hookInvocation),
                TransformationSpec.Readiness.UNRESOLVED,
                "Target/anchor/hook resolved; legacy anchor invocation opcode is not captured yet");
    }

    private static TransformationSpec unresolved(String id, String reason) {
        return new TransformationSpec(
                id,
                TransformationSpec.Kind.METHOD_CALL_REDIRECT,
                null, null, null,
                TransformationSpec.Readiness.UNRESOLVED,
                reason);
    }

    private static TransformationSpec.MethodRef ref(
            MethodParts method, TransformationSpec.Invocation invocation) {
        return new TransformationSpec.MethodRef(
                internal(method.owner()), method.name(), method.descriptor(), invocation);
    }

    private static TransformationSpec.Invocation invocation(String type) {
        return switch (type) {
            case "STATIC" -> TransformationSpec.Invocation.STATIC;
            case "VIRTUAL" -> TransformationSpec.Invocation.VIRTUAL;
            case "INTERFACE" -> TransformationSpec.Invocation.INTERFACE;
            case "SPECIAL" -> TransformationSpec.Invocation.SPECIAL;
            default -> TransformationSpec.Invocation.UNKNOWN;
        };
    }

    private static MethodParts parseSymbol(String symbol) {
        int hash = symbol.indexOf('#');
        int paren = symbol.indexOf('(', hash + 1);
        if (hash <= 0 || paren <= hash + 1) return null;
        return new MethodParts(
                symbol.substring(0, hash),
                symbol.substring(hash + 1, paren),
                symbol.substring(paren));
    }

    private static String internal(String owner) {
        return owner.replace('.', '/');
    }

    private static String sanitize(String source) {
        String value = source.replaceAll("[^A-Za-z0-9_.-]+", "_");
        return value.isEmpty() ? "legacy_transform" : value;
    }

    private record MethodParts(String owner, String name, String descriptor) {}
}
