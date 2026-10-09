package com.fuckingdeveloper.lms.transform;

import com.fuckingdeveloper.lms.analysis.LegacyInjectionAnalyzer;
import com.fuckingdeveloper.lms.compat.LegacyCompatibilityPlanner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
                    parseAnchorBindings(plan),
                    parseInstructionEdits(plan.operations(), plan.values()),
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
                List.of(),
                TransformationSpec.Readiness.READY,
                "Target, anchor, invocation opcode and replacement hook are resolved");
    }

    private static List<TransformationSpec.AnchorBinding> parseAnchorBindings(
            LegacyCompatibilityPlanner.CoremodTransformationPlan plan) {
        List<TransformationSpec.AnchorBinding> result = new ArrayList<>();
        for (var resolution : plan.anchorResolutions()) {
            if (resolution.currentSymbol().isEmpty()) continue;
            String variable = resolution.anchor().variable();
            if (variable == null || variable.isEmpty()) continue;
            MethodParts current = parseSymbol(resolution.currentSymbol());
            if (current == null) continue;
            var invocation = invocation(resolution.anchor().invocationType());
            result.add(new TransformationSpec.AnchorBinding(
                    variable, ref(current, invocation)));
        }
        return List.copyOf(result);
    }

    private static List<TransformationSpec.InstructionEdit> parseInstructionEdits(
            List<LegacyInjectionAnalyzer.CoremodOperation> operations,
            List<LegacyInjectionAnalyzer.CoremodValue> values) {
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
            String valueExpression = args.size() > 1 ? args.get(1) : "";
            edits.add(new TransformationSpec.InstructionEdit(
                    kind, location, first, valueExpression,
                    resolveInstructionValue(valueExpression, values),
                    resolveInstructionValues(valueExpression, values)));
        }
        return List.copyOf(edits);
    }

    private static List<TransformationSpec.InstructionSpec> resolveInstructionValues(
            String expression, List<LegacyInjectionAnalyzer.CoremodValue> values) {
        if (expression == null || expression.isEmpty()) return List.of();
        var definition = values.stream()
                .filter(value -> value.name().equals(expression.trim()))
                .findFirst();
        if (definition.isEmpty() || !"new InsnList()".equals(definition.get().expression().trim())) {
            return List.of();
        }
        List<TransformationSpec.InstructionSpec> result = new ArrayList<>();
        for (String mutation : definition.get().mutations()) {
            if (!mutation.startsWith("add(") || !mutation.endsWith(")")) continue;
            String item = mutation.substring(4, mutation.length() - 1).trim();
            result.add(resolveInstructionValue(item, values));
        }
        return List.copyOf(result);
    }

    private static TransformationSpec.InstructionSpec resolveInstructionValue(
            String expression, List<LegacyInjectionAnalyzer.CoremodValue> values) {
        if (expression == null || expression.isEmpty()) return null;
        String resolved = expression.trim();
        for (int depth = 0; depth < 8; depth++) {
            String current = resolved;
            var definition = values.stream()
                    .filter(value -> value.name().equals(current))
                    .findFirst();
            if (definition.isEmpty()) break;
            resolved = definition.get().expression().trim();
        }

        if (resolved.startsWith("ASMAPI.buildMethodCall")) {
            List<String> args = callArguments(resolved, "ASMAPI.buildMethodCall");
            if (args.size() >= 4) {
                String owner = stringLiteral(args.get(0));
                String name = stringLiteral(args.get(1));
                String descriptor = stringLiteral(args.get(2));
                var invocation = invocation(args.get(3).replace("ASMAPI.MethodType.", "").trim());
                if (owner != null && name != null && descriptor != null
                        && invocation != TransformationSpec.Invocation.UNKNOWN) {
                    return new TransformationSpec.InstructionSpec(
                            TransformationSpec.InstructionKind.METHOD_CALL,
                            new TransformationSpec.MethodRef(internal(owner), name, descriptor, invocation),
                            null, null, resolved, null);
                }
            }
        }

        var varInsn = Pattern.compile(
                "new\\s+VarInsnNode\\s*\\(\\s*(?:Opcodes\\.)?([A-Z_]+)\\s*,\\s*(\\d+)\\s*\\)")
                .matcher(resolved);
        if (varInsn.matches()) {
            return new TransformationSpec.InstructionSpec(
                    TransformationSpec.InstructionKind.VARIABLE, null,
                    asmOpcode(varInsn.group(1)), Integer.parseInt(varInsn.group(2)), resolved, null);
        }

        var insn = Pattern.compile(
                "new\\s+InsnNode\\s*\\(\\s*(?:Opcodes\\.)?([A-Z][A-Z0-9_]*)\\s*\\)")
                .matcher(resolved);
        if (insn.matches()) {
            Integer opcode = asmOpcode(insn.group(1));
            return new TransformationSpec.InstructionSpec(
                    opcode == null ? TransformationSpec.InstructionKind.UNRESOLVED
                            : TransformationSpec.InstructionKind.SIMPLE_OPCODE,
                    null, opcode, null, resolved, null);
        }

        var jump = Pattern.compile(
                "new\\s+JumpInsnNode\\s*\\(\\s*(?:Opcodes\\.)?([A-Z_]+)\\s*,\\s*(.+)\\)")
                .matcher(resolved);
        if (jump.matches()) {
            String targetExpression = jump.group(2).trim();
            if (targetExpression.startsWith("new LabelNode(") && targetExpression.endsWith(")")) {
                targetExpression = targetExpression.substring("new LabelNode(".length(),
                        targetExpression.length() - 1).trim();
            }
            return new TransformationSpec.InstructionSpec(
                    TransformationSpec.InstructionKind.JUMP, null,
                    asmOpcode(jump.group(1)), null, resolved,
                    parseInstructionReference(targetExpression));
        }

        return new TransformationSpec.InstructionSpec(
                TransformationSpec.InstructionKind.UNRESOLVED, null, null, null, resolved, null);
    }

    private static TransformationSpec.InstructionReference parseInstructionReference(String expression) {
        String text = expression.trim();
        boolean label = false;
        if (text.endsWith(".getLabel()")) {
            label = true;
            text = text.substring(0, text.length() - ".getLabel()".length());
        }
        var root = Pattern.compile("^([A-Za-z_$][A-Za-z0-9_$]*)(.*)$").matcher(text);
        if (!root.matches()) return null;
        String variable = root.group(1);
        String navigation = root.group(2);
        int offset = 0, position = 0;
        while (position < navigation.length()) {
            if (navigation.startsWith(".getNext()", position)) {
                offset++;
                position += ".getNext()".length();
            } else if (navigation.startsWith(".getPrevious()", position)) {
                offset--;
                position += ".getPrevious()".length();
            } else {
                return null;
            }
        }
        return new TransformationSpec.InstructionReference(variable, offset, label);
    }

    private static List<String> callArguments(String expression, String call) {
        int start = expression.indexOf(call);
        if (start < 0) return List.of();
        int open = expression.indexOf('(', start + call.length());
        int close = expression.lastIndexOf(')');
        if (open < 0 || close <= open) return List.of();
        return splitTopLevelArguments(expression.substring(open + 1, close));
    }

    private static String stringLiteral(String value) {
        String text = value.trim();
        if (text.length() < 2) return null;
        char first = text.charAt(0), last = text.charAt(text.length() - 1);
        if ((first == '\'' || first == '"') && last == first) {
            return text.substring(1, text.length() - 1);
        }
        return null;
    }

    private static Integer asmOpcode(String name) {
        return switch (name) {
            case "NOP" -> 0;
            case "ACONST_NULL" -> 1;
            case "FCONST_0" -> 11;
            case "FCONST_1" -> 12;
            case "FCONST_2" -> 13;
            case "DCONST_0" -> 14;
            case "DCONST_1" -> 15;
            case "ICONST_M1" -> 2;
            case "ICONST_0" -> 3;
            case "ICONST_1" -> 4;
            case "ICONST_2" -> 5;
            case "ICONST_3" -> 6;
            case "ICONST_4" -> 7;
            case "ICONST_5" -> 8;
            case "LCONST_0" -> 9;
            case "LCONST_1" -> 10;
            default -> {
                try {
                    yield org.objectweb.asm.Opcodes.class.getField(name).getInt(null);
                } catch (ReflectiveOperationException ignored) {
                    yield null;
                }
            }
        };
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
