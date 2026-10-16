package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Coordinate/button listener ABI, independently of widget hook and list adapters. */
final class Forge1192GuiPointerRewriter implements Opcodes {
    static final String ROOT = "net/minecraft/client/gui/components/events/GuiEventListener";
    private static final String EVENT = "Lnet/minecraft/client/input/MouseButtonEvent;";
    private static final String BRIDGE = "com/fuckingdeveloper/lms/profile/forge1192/runtime/render/Forge1192GuiPointerBridge";
    private static final Map<String, String> SOURCE = Map.of(
            "mouseClicked", "(DDI)Z", "mouseReleased", "(DDI)Z", "mouseDragged", "(DDIDD)Z");
    interface Evidence {
        boolean listener(String owner);
        String canonical(String name, String descriptor);
        ClassNode managed(String owner);
        boolean nativeMember(String owner, String name, String descriptor);
    }
    private static String operation(String owner, String name, String desc, Evidence evidence) {
        if (!evidence.listener(owner)) return null;
        String canonical = name.startsWith("m_") ? evidence.canonical(name, desc) : name;
        return canonical != null && desc.equals(SOURCE.get(canonical)) ? canonical : null;
    }
    private static String target(String op) {
        return "(" + EVENT + (op.equals("mouseClicked") ? "Z" : op.equals("mouseDragged") ? "DD" : "") + ")Z";
    }
    private static String marker(String op) {
        return BRIDGE + "$" + switch (op) { case "mouseClicked" -> "Click"; case "mouseReleased" -> "Release"; default -> "Drag"; };
    }
    private static String entryName(String op) {
        return switch (op) { case "mouseClicked" -> "clicked"; case "mouseReleased" -> "released"; default -> "dragged"; };
    }
    private static boolean managedOverride(String owner, String op, Evidence evidence, Set<String> visited) {
        if (owner == null || !visited.add(owner)) return false;
        var node = evidence.managed(owner);
        if (node == null) return false;
        for (var method : node.methods)
            if ((method.access & (ACC_STATIC | ACC_PRIVATE)) == 0 && op.equals(operation(owner, method.name, method.desc, evidence))) return true;
        if (managedOverride(node.superName, op, evidence, visited)) return true;
        for (String parent : node.interfaces) if (managedOverride(parent, op, evidence, visited)) return true;
        return false;
    }
    private static boolean nativeSuper(String owner, String op, Evidence evidence, Set<String> visited) {
        if (owner == null || !visited.add(owner)) return false;
        if (evidence.nativeMember(owner, op, target(op))) return true;
        var node = evidence.managed(owner);
        if (node == null) return false;
        if (nativeSuper(node.superName, op, evidence, visited)) return true;
        for (String parent : node.interfaces) if (nativeSuper(parent, op, evidence, visited)) return true;
        return false;
    }
    static int rewrite(ClassNode node, Evidence evidence) {
        int count = 0;
        var added = new ArrayList<MethodNode>();
        for (var method : List.copyOf(node.methods)) {
            String op = operation(node.name, method.name, method.desc, evidence);
            if (op != null && (method.access & (ACC_STATIC | ACC_PRIVATE)) == 0) {
                String alias = "lms$" + op;
                if (node.methods.stream().anyMatch(other -> other != method &&
                        (other.name.equals(alias) && other.desc.equals(method.desc) || other.name.equals(op) && other.desc.equals(target(op)))))
                    throw new IllegalStateException("Source/native GUI pointer callback collision: " + node.name + "#" + op);
                method.name = alias;
                method.access = (method.access & ~ACC_PROTECTED) | ACC_PUBLIC;
                if (!node.interfaces.contains(marker(op))) node.interfaces.add(marker(op));
                var entry = new MethodNode(ACC_PUBLIC | ACC_SYNTHETIC, op, target(op), null, null);
                entry.instructions.add(new VarInsnNode(ALOAD, 0));
                entry.instructions.add(new VarInsnNode(ALOAD, 1));
                if (op.equals("mouseClicked")) entry.instructions.add(new VarInsnNode(ILOAD, 2));
                if (op.equals("mouseDragged")) {
                    entry.instructions.add(new VarInsnNode(DLOAD, 2));
                    entry.instructions.add(new VarInsnNode(DLOAD, 4));
                }
                entry.instructions.add(new MethodInsnNode(INVOKESTATIC, BRIDGE, entryName(op), "(L" + marker(op) + ";" + target(op).substring(1), false));
                entry.instructions.add(new InsnNode(IRETURN));
                entry.maxStack = 6; entry.maxLocals = op.equals("mouseDragged") ? 6 : 3;
                added.add(entry); count++;
            }
            for (var instruction : method.instructions.toArray()) {
                if (instruction instanceof MethodInsnNode call) {
                    String action = operation(call.owner, call.name, call.desc, evidence);
                    if (action == null) continue;
                    if (call.getOpcode() == INVOKESPECIAL) {
                        if (managedOverride(call.owner, action, evidence, new HashSet<>())) {
                            call.name = "lms$" + action;
                        } else {
                            if (!nativeSuper(call.owner, action, evidence, new HashSet<>()))
                                throw new UnsupportedOperationException("Unproven native GUI pointer super member: " + call.owner + "#" + action);
                            nativeArguments(method, call, action);
                            call.name = action; call.desc = target(action);
                        }
                        count++;
                    } else if (call.getOpcode() == INVOKEVIRTUAL || call.getOpcode() == INVOKEINTERFACE) {
                        call.setOpcode(INVOKESTATIC); call.owner = BRIDGE; call.name = action;
                        call.desc = "(L" + ROOT + ";" + SOURCE.get(action).substring(1); call.itf = false; count++;
                    }
                } else if (instruction instanceof LdcInsnNode ldc) {
                    Object next = constant(node, ldc.cst, evidence, added);
                    if (next != ldc.cst) { ldc.cst = next; count++; }
                } else if (instruction instanceof InvokeDynamicInsnNode indy) {
                    for (int i = 0; i < indy.bsmArgs.length; i++) {
                        Object next = constant(node, indy.bsmArgs[i], evidence, added);
                        if (next != indy.bsmArgs[i]) { indy.bsmArgs[i] = next; count++; }
                    }
                }
            }
        }
        node.methods.addAll(added);
        return count;
    }
    /** Leave the original special receiver on stack; replace only its argument tuple. */
    private static void nativeArguments(MethodNode method, MethodInsnNode call, String op) {
        int base = method.maxLocals; method.maxLocals += op.equals("mouseDragged") ? 9 : 5;
        var code = new InsnList();
        if (op.equals("mouseDragged")) {
            code.add(new VarInsnNode(DSTORE, base + 7)); code.add(new VarInsnNode(DSTORE, base + 5));
        }
        code.add(new VarInsnNode(ISTORE, base + 4));
        code.add(new VarInsnNode(DSTORE, base + 2)); code.add(new VarInsnNode(DSTORE, base));
        code.add(new VarInsnNode(DLOAD, base)); code.add(new VarInsnNode(DLOAD, base + 2));
        code.add(new VarInsnNode(ILOAD, base + 4));
        code.add(new MethodInsnNode(INVOKESTATIC, BRIDGE, "event", "(DDI)" + EVENT, false));
        if (op.equals("mouseClicked")) code.add(new MethodInsnNode(INVOKESTATIC, BRIDGE, "twice", "()Z", false));
        if (op.equals("mouseDragged")) {
            code.add(new VarInsnNode(DLOAD, base + 5)); code.add(new VarInsnNode(DLOAD, base + 7));
        }
        method.instructions.insertBefore(call, code);
    }
    private static Object constant(ClassNode node, Object value, Evidence evidence, List<MethodNode> added) {
        if (value instanceof Handle handle) {
            String op = operation(handle.getOwner(), handle.getName(), handle.getDesc(), evidence);
            if (op == null) return value;
            if (handle.getTag() == H_INVOKESPECIAL)
                throw new UnsupportedOperationException("Source GUI pointer special handle requires exact lookup context");
            if (handle.getTag() != H_INVOKEVIRTUAL && handle.getTag() != H_INVOKEINTERFACE) return value;
            String name = "lms$guiPointerHandle$" + added.size();
            while (true) { String candidate = name; if (node.methods.stream().noneMatch(m -> m.name.equals(candidate)) && added.stream().noneMatch(m -> m.name.equals(candidate))) break; name += "$"; }
            String desc = "(L" + handle.getOwner() + ";" + handle.getDesc().substring(1);
            var helper = new MethodNode(ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC, name, desc, null, null);
            helper.instructions.add(new VarInsnNode(ALOAD, 0));
            helper.instructions.add(new VarInsnNode(DLOAD, 1)); helper.instructions.add(new VarInsnNode(DLOAD, 3));
            helper.instructions.add(new VarInsnNode(ILOAD, 5));
            if (op.equals("mouseDragged")) {
                helper.instructions.add(new VarInsnNode(DLOAD, 6)); helper.instructions.add(new VarInsnNode(DLOAD, 8));
            }
            helper.instructions.add(new MethodInsnNode(INVOKESTATIC, BRIDGE, op, "(L" + ROOT + ";" + SOURCE.get(op).substring(1), false));
            helper.instructions.add(new InsnNode(IRETURN)); helper.maxStack = 10; helper.maxLocals = op.equals("mouseDragged") ? 10 : 6;
            added.add(helper);
            return new Handle(H_INVOKESTATIC, node.name, name, desc, (node.access & ACC_INTERFACE) != 0);
        }
        if (value instanceof ConstantDynamic dynamic) {
            Object[] arguments = new Object[dynamic.getBootstrapMethodArgumentCount()]; boolean changed = false;
            for (int i = 0; i < arguments.length; i++) { Object old = dynamic.getBootstrapMethodArgument(i); arguments[i] = constant(node, old, evidence, added); changed |= arguments[i] != old; }
            return changed ? new ConstantDynamic(dynamic.getName(), dynamic.getDescriptor(), dynamic.getBootstrapMethod(), arguments) : value;
        }
        return value;
    }
}
