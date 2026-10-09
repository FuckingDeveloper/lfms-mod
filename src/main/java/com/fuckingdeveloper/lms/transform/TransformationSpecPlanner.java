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
            return new TransformationSpec(
                    id,
                    TransformationSpec.Kind.INSTRUCTION_EDIT,
                    parseRef(plan.currentTarget(), TransformationSpec.Invocation.UNKNOWN),
                    null, null,
                    parseInstructionEdits(plan.operations()),
                    TransformationSpec.Readiness.UNRESOLVED,
                    "Instruction edit structure captured; inserted instruction expressions still require parsing: "
                            + plan.mutationKinds());
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

        var anchorInvocation = invocation(anchorResolution.anchor().invocationType());
        if (anchorInvocation == TransformationSpec.Invocation.UNKNOWN) {
            return new TransformationSpec(
                    id,
                    TransformationSpec.Kind.METHOD_CALL_REDIRECT,
                    ref(target, TransformationSpec.Invocation.UNKNOWN),
                    ref(anchor, TransformationSpec.Invocation.UNKNOWN),
                    new TransformationSpec.MethodRef(
                            internal(hook.owner()), hook.method(), hook.descriptor(), hookInvocation),
                    List.of(),
                    TransformationSpec.Readiness.UNRESOLVED,
                    "Target/anchor/hook resolved; legacy anchor invocation opcode is not captured");
        }

        return new TransformationSpec(
                id,
                TransformationSpec.Kind.METHOD_CALL_REDIRECT,
                ref(target, TransformationSpec.Invocation.UNKNOWN),
                ref(anchor, anchorInvocation),
                new TransformationSpec.MethodRef(
                        internal(hook.owner()), hook.method(), hook.descriptor(), hookInvocation),
                List.of(),
                TransformationSpec.Readiness.READY,
                "Target, anchor, invocation opcode and replacement hook are resolved");
    }

    private static List<TransformationSpec.InstructionEdit> parseInstructionEdits(
            List<LegacyInjectionAnalyzer.CoremodOperation> operations) {
        List<TransformationSpec.InstructionEdit> edits = new ArrayList<>();
        for (var operation : operations) {
            List<String> args = splitTopLevelArguments(operation.arguments());
            if (args.isEmpty()) continue;
            String first = args.getFirst();
            var location = first.endsWith(".getPrevious()")
                    ? TransformationSpec.InstructionLocation.PREVIOUS
                    : first.endsWith(".getNext()")
                    ? TransformationSpec.InstructionLocation.NEXT
                    : first.matches("[A-Za-z_$][A-Za-z0-9_$]*")
                    ? TransformationSpec.InstructionLocation.EXACT
                    : TransformationSpec.InstructionLocation.EXPRESSION;
            var kind = switch (operation.kind()) {
                case "remove" -> TransformationSpec.EditKind.REMOVE;
                case "insertBefore" -> TransformationSpec.EditKind.INSERT_BEFORE;
                case "insert" -> TransformationSpec.EditKind.INSERT_AFTER;
                case "set" -> TransformationSpec.EditKind.REPLACE;
                default -> null;
            };
            if (kind == null) continue;
            edits.add(new TransformationSpec.InstructionEdit(
                    kind, location, first, args.size() > 1 ? args.get(1) : ""));
        }
        return List.copyOf(edits);
    }

    private static List<String> splitTopLevelArguments(String source) {
        List<String> result = new ArrayList<>();
        int start = 0, parens = 0, brackets = 0, braces = 0;
        char quote = 0;
        boolean escaped = false;
        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);
            if (quote != 0) {
                if (escaped) escaped = false;
                else if (ch == '\\') escaped = true;
                else if (ch == quote) quote = 0;
                continue;
            }
            if (ch == '\'' || ch == '"' || ch == '`') quote = ch;
            else if (ch == '(') parens++;
            else if (ch == ')') parens--;
            else if (ch == '[') brackets++;
            else if (ch == ']') brackets--;
            else if (ch == '{') braces++;
            else if (ch == '}') braces--;
            else if (ch == ',' && parens == 0 && brackets == 0 && braces == 0) {
                result.add(source.substring(start, i).trim());
                start = i + 1;
            }
        }
        String tail = source.substring(start).trim();
        if (!tail.isEmpty()) result.add(tail);
        return result;
    }

    private static TransformationSpec unresolved(String id, String reason) {
        return new TransformationSpec(
                id,
                TransformationSpec.Kind.METHOD_CALL_REDIRECT,
                null, null, null, List.of(),
                TransformationSpec.Readiness.UNRESOLVED,
                reason);
    }

    private static TransformationSpec.MethodRef parseRef(
            String symbol, TransformationSpec.Invocation invocation) {
        if (symbol == null || symbol.isEmpty()) return null;
        MethodParts method = parseSymbol(symbol);
        return method == null ? null : ref(method, invocation);
    }

    private static TransformationSpec.MethodRef ref(
            MethodParts method, TransformationSpec.Invocation invocation) {
        return new TransformationSpec.MethodRef(
                internal(method.owner()), method.name(), method.descriptor(), invocation);
    }

    private static TransformationSpec.Invocation invocation(String type) {
        return switch (type) {
            case "STATIC", "INVOKESTATIC" -> TransformationSpec.Invocation.STATIC;
            case "VIRTUAL", "INVOKEVIRTUAL" -> TransformationSpec.Invocation.VIRTUAL;
            case "INTERFACE", "INVOKEINTERFACE" -> TransformationSpec.Invocation.INTERFACE;
            case "SPECIAL", "INVOKESPECIAL" -> TransformationSpec.Invocation.SPECIAL;
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
