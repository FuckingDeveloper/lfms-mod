package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Function;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

/** Bounded source array iteration, not an emulation of mutable source tab indices. */
final class Forge1192CreativeEnumerationRewriter {
    private static final String TAB = "net/minecraft/world/item/CreativeModeTab";
    private static final String VALUE = "L" + TAB + ";";
    private static final String ARRAY = "[" + VALUE;
    private static final String FILL = "(Lnet/minecraft/core/NonNullList;)V";
    private static final String BRIDGE = "com/fuckingdeveloper/lms/profile/forge1192/runtime/item/Forge1192CreativeTabBridge";

    static int rewrite(ClassNode node, Predicate<String> tabOwner, Function<MethodInsnNode, String> sourceDeclaration) {
        int changes = 0;
        for (var method : node.methods) {
            for (var insn : method.instructions.toArray()) {
                if (insn instanceof FieldInsnNode field && field.owner.equals(TAB)) {
                    if (field.name.equals("f_40748_") && field.desc.equals(ARRAY)) {
                        if (field.getOpcode() != GETSTATIC) throw unsupported(node, "global array assignment");
                        proveIteration(node, method, field);
                        method.instructions.set(field, new MethodInsnNode(INVOKESTATIC, BRIDGE, "tabs", "()" + ARRAY, false));
                        changes++;
                    } else {
                        String token = token(field.owner, field.name, field.desc);
                        if (token != null) {
                            if (field.getOpcode() != GETSTATIC) throw unsupported(node, "special tab assignment");
                            method.instructions.set(field, new MethodInsnNode(INVOKESTATIC, BRIDGE, token, "()" + VALUE, false));
                            changes++;
                        }
                    }
                } else if (insn instanceof MethodInsnNode call && fill(call.owner, call.name, call.desc, tabOwner)) {
                    if (call.getOpcode() != INVOKEVIRTUAL && call.getOpcode() != INVOKESPECIAL)
                        throw unsupported(node, "tab enumeration invocation kind");
                    boolean base = call.getOpcode() == INVOKESPECIAL;
                    if (base && !call.owner.equals(TAB)) {
                        String declaration = sourceDeclaration.apply(call);
                        if (declaration == null || declaration.equals(TAB))
                            throw unsupported(node, "unproven inherited source super declaration " + call.owner);
                        // A real source parent override still exists. Preserve its
                        // invokespecial dispatch rather than skipping its body.
                        continue;
                    }
                    call.setOpcode(INVOKESTATIC); call.owner = BRIDGE;
                    call.name = base ? "fillBaseTab" : "fillTab";
                    call.desc = "(" + VALUE + FILL.substring(1); call.itf = false; changes++;
                } else if (insn instanceof LdcInsnNode ldc) {
                    Object replacement = constant(node, ldc.cst, tabOwner);
                    if (!Objects.equals(replacement, ldc.cst)) { ldc.cst = replacement; changes++; }
                } else if (insn instanceof InvokeDynamicInsnNode indy) {
                    var bootstrap = (Handle) constant(node, indy.bsm, tabOwner);
                    if (!bootstrap.equals(indy.bsm)) { indy.bsm = bootstrap; changes++; }
                    for (int i = 0; i < indy.bsmArgs.length; i++) {
                        Object replacement = constant(node, indy.bsmArgs[i], tabOwner);
                        if (!Objects.equals(replacement, indy.bsmArgs[i])) { indy.bsmArgs[i] = replacement; changes++; }
                    }
                }
            }
        }
        return changes;
    }

    private static boolean fill(String owner, String name, String desc, Predicate<String> tabOwner) {
        return (name.equals("m_6151_") || name.equals("fillItemList")) && desc.equals(FILL) && tabOwner.test(owner);
    }
    private static String token(String owner, String name, String desc) {
        if (!owner.equals(TAB) || !desc.equals(VALUE)) return null;
        return switch (name) {
            case "f_40754_" -> "searchTab";
            case "f_40760_" -> "hotbarTab";
            case "f_40761_" -> "inventoryTab";
            default -> null;
        };
    }
    private static Object constant(ClassNode node, Object value, Predicate<String> tabOwner) {
        if (value instanceof Handle h) {
            if (h.getOwner().equals(TAB) && h.getName().equals("f_40748_"))
                throw unsupported(node, "array handle cannot prove local read-only iteration");
            String token = token(h.getOwner(), h.getName(), h.getDesc());
            if (token != null) {
                if (h.getTag() != H_GETSTATIC) throw unsupported(node, "special tab write handle");
                return new Handle(H_INVOKESTATIC, BRIDGE, token, "()" + VALUE, false);
            }
            if (fill(h.getOwner(), h.getName(), h.getDesc(), tabOwner)) {
                if (h.getTag() != H_INVOKEVIRTUAL) throw unsupported(node, "nonvirtual tab enumeration handle");
                return new Handle(H_INVOKESTATIC, BRIDGE, "fillTab", "(" + VALUE + FILL.substring(1), false);
            }
        } else if (value instanceof ConstantDynamic d) {
            Object[] args = new Object[d.getBootstrapMethodArgumentCount()];
            for (int i = 0; i < args.length; i++) args[i] = constant(node, d.getBootstrapMethodArgument(i), tabOwner);
            return new ConstantDynamic(d.getName(), d.getDescriptor(), (Handle) constant(node, d.getBootstrapMethod(), tabOwner), args);
        }
        return value;
    }

    /** Admit the compiler's enhanced-for loop only. Array/index/length locals cannot escape or be reassigned. */
    private static void proveIteration(ClassNode node, MethodNode method, FieldInsnNode root) {
        AbstractInsnNode[] p = new AbstractInsnNode[16];
        p[0] = next(root);
        for (int i = 1; i < p.length; i++) p[i] = next(p[i - 1]);
        if (!(p[0] instanceof VarInsnNode array) || array.getOpcode() != ASTORE
                || !local(p[1], ALOAD, array.var) || opcode(p[2]) != ARRAYLENGTH
                || !(p[3] instanceof VarInsnNode length) || length.getOpcode() != ISTORE
                || opcode(p[4]) != ICONST_0 || !(p[5] instanceof VarInsnNode index) || index.getOpcode() != ISTORE
                || !local(p[6], ILOAD, index.var) || !local(p[7], ILOAD, length.var)
                || !(p[8] instanceof JumpInsnNode exit) || exit.getOpcode() != IF_ICMPGE
                || !local(p[9], ALOAD, array.var) || !local(p[10], ILOAD, index.var)
                || opcode(p[11]) != AALOAD || opcode(p[12]) != ASTORE
                || array.var == length.var || array.var == index.var || length.var == index.var)
            throw unsupported(node, "array use is not a proven enhanced-for iteration in " + method.name);
        Set<AbstractInsnNode> permitted = Set.of(p[0], p[1], p[3], p[5], p[6], p[7], p[9], p[10]);
        IincInsnNode increment = null;
        for (var insn : method.instructions) {
            if (insn instanceof VarInsnNode var && (var.var == array.var || var.var == length.var || var.var == index.var)
                    && !permitted.contains(insn)) throw unsupported(node, "array/index/length escape or reassignment");
            if (insn instanceof IincInsnNode inc && (inc.var == array.var || inc.var == length.var || inc.var == index.var)) {
                if (inc.var != index.var || inc.incr != 1 || increment != null)
                    throw unsupported(node, "non-enumeration array index mutation");
                increment = inc;
            }
        }
        if (increment == null || !(next(increment) instanceof JumpInsnNode back) || back.getOpcode() != GOTO
                || next(back.label) != p[6] || next(back) != next(exit.label)
                || method.instructions.indexOf(increment) <= method.instructions.indexOf(p[12]))
            throw unsupported(node, "unproven enumeration loop boundary");
        // An external jump must not bypass the initialization and guard. Ordinary
        // branches/continues within the loop body remain intact.
        int start = method.instructions.indexOf(root), end = method.instructions.indexOf(back);
        for (var handler : method.tryCatchBlocks) {
            int to = method.instructions.indexOf(next(handler.handler));
            if (to > start && to <= end && (method.instructions.indexOf(handler.start) <= start
                    || method.instructions.indexOf(handler.end) > end))
                throw unsupported(node, "external exception entry into enumeration loop");
        }
        for (var insn : method.instructions) {
            List<LabelNode> targets = insn instanceof JumpInsnNode j ? List.of(j.label)
                    : insn instanceof TableSwitchInsnNode s ? labels(s.dflt, s.labels)
                    : insn instanceof LookupSwitchInsnNode s ? labels(s.dflt, s.labels) : List.of();
            int from = method.instructions.indexOf(insn);
            for (var label : targets) {
                int to = method.instructions.indexOf(next(label));
                if ((from < start || from > end) && to > start && to <= end)
                    throw unsupported(node, "external entry into enumeration loop");
            }
        }
    }
    private static List<LabelNode> labels(LabelNode dflt, List<LabelNode> labels) {
        var result = new ArrayList<>(labels); result.add(dflt); return result;
    }
    private static boolean local(AbstractInsnNode n, int opcode, int slot) {
        return n instanceof VarInsnNode v && v.getOpcode() == opcode && v.var == slot;
    }
    private static int opcode(AbstractInsnNode n) { return n == null ? -1 : n.getOpcode(); }
    private static AbstractInsnNode next(AbstractInsnNode n) {
        if (n == null) return null;
        do { n = n.getNext(); } while (n != null && n.getOpcode() < 0);
        return n;
    }
    private static UnsupportedOperationException unsupported(ClassNode node, String reason) {
        return new UnsupportedOperationException("Creative tab enumeration: " + node.name + ": " + reason);
    }
}
