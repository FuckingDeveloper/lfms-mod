package com.fuckingdeveloper.lms.profile.forge1192;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/** Exact directionless source Block callback -> directional native callback entry. */
public final class Forge1192AnalogOutputRewriter {
    private Forge1192AnalogOutputRewriter() {}
    public static final String SOURCE = "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)I";
    public static final String TARGET = SOURCE.replace(")I", "Lnet/minecraft/core/Direction;)I");

    public static int rewrite(ClassNode node, boolean sourceBlock) {
        if (!sourceBlock) return 0;
        var callbacks = node.methods.stream().filter(m -> m.name.equals("m_6782_") && m.desc.equals(SOURCE)).toList();
        if (callbacks.isEmpty()) return 0;
        if (callbacks.size() != 1 || (callbacks.getFirst().access & (Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_PRIVATE)) != 0
                || node.methods.stream().anyMatch(m -> m.name.equals("getAnalogOutputSignal") && m.desc.equals(TARGET)))
            throw new IllegalStateException("Ambiguous source analog output callback: " + node.name);
        var entry = new MethodNode(Opcodes.ACC_PUBLIC, "getAnalogOutputSignal", TARGET, null, null);
        for (int i = 0; i <= 3; i++) entry.instructions.add(new VarInsnNode(Opcodes.ALOAD, i));
        entry.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, node.name, "m_6782_", SOURCE, false));
        entry.instructions.add(new InsnNode(Opcodes.IRETURN));
        entry.maxStack = 4; entry.maxLocals = 5;
        node.methods.add(entry);
        return 1;
    }
}
