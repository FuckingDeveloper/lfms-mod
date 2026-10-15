package com.fuckingdeveloper.lms.profile;

import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Exact 1.19.2 base-owner damage descriptors, separate from native damage types. */
final class Forge1192DamageSourceRewriter {
    private static final String ROOT = "net/minecraft/world/damagesource/DamageSource";
    private static final String BRIDGE = "com/fuckingdeveloper/lms/runtime/Forge1192DamageSourceBridge";
    private static final Set<String> QUERIES = Set.of("m_19360_", "m_19372_", "m_19376_", "m_146705_",
            "m_19378_", "m_19379_", "m_238340_", "m_19384_", "m_146707_", "m_181121_", "m_19387_");
    private static final String CONSTRUCTOR = "(Ljava/lang/String;)V";
    private static Handle bridgeHandle(Handle handle) {
        if (!handle.getOwner().equals(ROOT)) return handle;
        if (handle.getTag() == Opcodes.H_NEWINVOKESPECIAL && handle.getName().equals("<init>")
                && handle.getDesc().equals(CONSTRUCTOR))
            return new Handle(Opcodes.H_INVOKESTATIC, BRIDGE, "create", "(Ljava/lang/String;)L" + ROOT + ";", false);
        if (QUERIES.contains(handle.getName()) && handle.getDesc().equals("()Z")
                && (handle.getTag() == Opcodes.H_INVOKEVIRTUAL || handle.getTag() == Opcodes.H_INVOKESPECIAL))
            return new Handle(Opcodes.H_INVOKESTATIC, BRIDGE,
                    (handle.getTag() == Opcodes.H_INVOKESPECIAL ? "baseQuery_" : "query_") + handle.getName(),
                    "(L" + ROOT + ";)Z", false);
        return handle;
    }
    private static Object constant(Object value) {
        if (value instanceof Handle handle) return bridgeHandle(handle);
        if (value instanceof ConstantDynamic dynamic) {
            Object[] args = new Object[dynamic.getBootstrapMethodArgumentCount()]; boolean changed = false;
            for (int i = 0; i < args.length; i++) { var old = dynamic.getBootstrapMethodArgument(i); args[i] = constant(old); changed |= args[i] != old; }
            if (changed) return new ConstantDynamic(dynamic.getName(), dynamic.getDescriptor(), dynamic.getBootstrapMethod(), args);
        }
        return value;
    }
    static int rewrite(ClassNode node) {
        int count = 0;
        for (var method : node.methods) for (var instruction : method.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals(ROOT)) {
                if (call.getOpcode() == Opcodes.INVOKESPECIAL && call.name.equals("<init>") && call.desc.equals(CONSTRUCTOR)) {
                    // Only the initialized String argument is replaced; NEW/uninitialized this remains untouched.
                    method.instructions.insertBefore(call, new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "descriptor",
                            "(Ljava/lang/String;)Lnet/minecraft/core/Holder;", false));
                    call.desc = "(Lnet/minecraft/core/Holder;)V"; count++;
                } else if (QUERIES.contains(call.name) && call.desc.equals("()Z")
                        && (call.getOpcode() == Opcodes.INVOKEVIRTUAL || call.getOpcode() == Opcodes.INVOKESPECIAL)) {
                    method.instructions.insertBefore(call, new LdcInsnNode(call.name));
                    call.name = call.getOpcode() == Opcodes.INVOKESPECIAL ? "baseQuery" : "query";
                    call.owner = BRIDGE; call.desc = "(L" + ROOT + ";Ljava/lang/String;)Z";
                    call.setOpcode(Opcodes.INVOKESTATIC); call.itf = false; count++;
                }
            } else if (instruction instanceof LdcInsnNode ldc) {
                var changed = constant(ldc.cst); if (changed != ldc.cst) { ldc.cst = changed; count++; }
            } else if (instruction instanceof InvokeDynamicInsnNode dynamic) {
                for (int i = 0; i < dynamic.bsmArgs.length; i++) {
                    var changed = constant(dynamic.bsmArgs[i]); if (changed != dynamic.bsmArgs[i]) { dynamic.bsmArgs[i] = changed; count++; }
                }
            }
        }
        return count;
    }
}
