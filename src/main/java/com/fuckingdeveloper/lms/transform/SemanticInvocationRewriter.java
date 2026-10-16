package com.fuckingdeveloper.lms.transform;

import java.util.Objects;
import java.util.function.BiPredicate;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** ASM-only invocation traversal; source profiles supply exact member and ancestry proof. */
public final class SemanticInvocationRewriter {
    private final BiPredicate<String,String> subtype;

    public SemanticInvocationRewriter(BiPredicate<String,String> subtype) {
        this.subtype = Objects.requireNonNull(subtype);
    }

    @FunctionalInterface
    public interface MemberMapping {
        Handle resolve(int opcode, String owner, String name, String descriptor);
    }

    /** One evidence-selected traversal for instance calls and all callable constant shapes. */
    public int rewrite(ClassNode node, MemberMapping member) {
        var adapters = new java.util.ArrayList<org.objectweb.asm.tree.MethodNode>();
        int rewrites = 0;
        for (var method : node.methods) for (var instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call && (call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                    || call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEINTERFACE
                    || call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL)) {
                Handle bridge = member.resolve(call.getOpcode(), call.owner, call.name, call.desc);
                if (bridge != null) {
                    verifySemanticInstanceShape(call.desc, bridge);
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC); call.owner = bridge.getOwner();
                    call.name = bridge.getName(); call.desc = bridge.getDesc(); call.itf = bridge.isInterface(); rewrites++;
                }
            } else if (instruction instanceof InvokeDynamicInsnNode indy) {
                for (int i = 0; i < indy.bsmArgs.length; i++) {
                    Object original = indy.bsmArgs[i];
                    Object adapted = semanticInstanceConstant(node, original, member, adapters, capturedBridgeReceiver(indy, original));
                    if (adapted != original) { indy.bsmArgs[i] = adapted; rewrites++; }
                }
            } else if (instruction instanceof LdcInsnNode ldc) {
                Object adapted = semanticInstanceConstant(node, ldc.cst, member, adapters, null);
                if (adapted != ldc.cst) { ldc.cst = adapted; rewrites++; }
            }
        }
        node.methods.addAll(adapters); return rewrites;
    }
    private Object semanticInstanceConstant(ClassNode node, Object value, MemberMapping member,
            java.util.List<org.objectweb.asm.tree.MethodNode> adapters, String receiver) {
        if (value instanceof Handle handle) {
            int opcode = switch (handle.getTag()) {
                case org.objectweb.asm.Opcodes.H_INVOKEVIRTUAL -> org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
                case org.objectweb.asm.Opcodes.H_INVOKEINTERFACE -> org.objectweb.asm.Opcodes.INVOKEINTERFACE;
                case org.objectweb.asm.Opcodes.H_INVOKESPECIAL -> org.objectweb.asm.Opcodes.INVOKESPECIAL;
                default -> -1;
            };
            if (opcode == -1) return value;
            Handle bridge = member.resolve(opcode, handle.getOwner(), handle.getName(), handle.getDesc());
            if (bridge == null) return value;
            verifySemanticInstanceShape(handle.getDesc(), bridge);
            return preserveBridgeHandleType(node, handle, bridge, receiver, adapters);
        }
        if (value instanceof ConstantDynamic dynamic) {
            Object[] args = new Object[dynamic.getBootstrapMethodArgumentCount()]; boolean changed = false;
            for (int i = 0; i < args.length; i++) {
                Object original = dynamic.getBootstrapMethodArgument(i);
                args[i] = semanticInstanceConstant(node, original, member, adapters, null); changed |= args[i] != original;
            }
            return changed ? new ConstantDynamic(dynamic.getName(), dynamic.getDescriptor(), dynamic.getBootstrapMethod(), args) : value;
        }
        return value;
    }
    private static void verifySemanticInstanceShape(String source, Handle bridge) {
        Type[] before = Type.getArgumentTypes(source), after = Type.getArgumentTypes(bridge.getDesc());
        if (bridge.getTag() != org.objectweb.asm.Opcodes.H_INVOKESTATIC || after.length != before.length + 1
                || after[0].getSort() != Type.OBJECT || !Type.getReturnType(source).equals(Type.getReturnType(bridge.getDesc()))
                || !java.util.Arrays.equals(before, java.util.Arrays.copyOfRange(after, 1, after.length)))
            throw new IllegalStateException("Semantic bridge does not preserve the source invocation shape: " + source + " -> " + bridge);
    }

    /** Adapt the exact 1.19.2 Level#playSound player exclusion to Entity. */
    public String capturedBridgeReceiver(InvokeDynamicInsnNode indy, Object value) {
        if (!(value instanceof Handle handle)
                || (handle.getTag() != org.objectweb.asm.Opcodes.H_INVOKEVIRTUAL
                    && handle.getTag() != org.objectweb.asm.Opcodes.H_INVOKEINTERFACE
                    && handle.getTag() != org.objectweb.asm.Opcodes.H_INVOKESPECIAL)
                || !indy.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory")) return null;
        Type[] captured = Type.getArgumentTypes(indy.desc);
        if (captured.length == 0 || captured[0].getSort() != Type.OBJECT) return null;
        String receiver = captured[0].getInternalName();
        return subtype.test(receiver, handle.getOwner()) ? receiver : null;
    }

    /** Preserve the original callable type, including LambdaMetafactory's exact capture rule. */
    public static Handle preserveBridgeHandleType(ClassNode node, Handle original, Handle bridge,
            String capturedReceiver, java.util.List<org.objectweb.asm.tree.MethodNode> adapters) {
        String receiver = capturedReceiver == null
                ? original.getTag() == org.objectweb.asm.Opcodes.H_INVOKESPECIAL ? node.name : original.getOwner()
                : capturedReceiver;
        String descriptor = "(L" + receiver + ";" + original.getDesc().substring(1);
        if (descriptor.equals(bridge.getDesc())) return bridge;
        String name = "lms$semanticHandle$" + (node.methods.size() + adapters.size());
        while (hasMethod(node, adapters, name, descriptor)) name += "$";
        var adapter = new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC
                | org.objectweb.asm.Opcodes.ACC_STATIC | org.objectweb.asm.Opcodes.ACC_SYNTHETIC,
                name, descriptor, null, null);
        int local = 0;
        for (Type argument : Type.getArgumentTypes(descriptor)) {
            adapter.instructions.add(new org.objectweb.asm.tree.VarInsnNode(
                    argument.getOpcode(org.objectweb.asm.Opcodes.ILOAD), local));
            local += argument.getSize();
        }
        adapter.instructions.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                bridge.getOwner(), bridge.getName(), bridge.getDesc(), false));
        adapter.instructions.add(new org.objectweb.asm.tree.InsnNode(
                Type.getReturnType(descriptor).getOpcode(org.objectweb.asm.Opcodes.IRETURN)));
        adapter.maxLocals = local; adapter.maxStack = Math.max(local, Type.getReturnType(descriptor).getSize());
        adapters.add(adapter);
        return new Handle(org.objectweb.asm.Opcodes.H_INVOKESTATIC, node.name, name, descriptor,
                (node.access & org.objectweb.asm.Opcodes.ACC_INTERFACE) != 0);
    }

    public static boolean hasMethod(ClassNode node,
            java.util.List<org.objectweb.asm.tree.MethodNode> adapters, String name, String descriptor) {
        return java.util.stream.Stream.concat(node.methods.stream(), adapters.stream())
                .anyMatch(method -> method.name.equals(name) && method.desc.equals(descriptor));
    }

}
