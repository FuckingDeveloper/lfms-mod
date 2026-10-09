package com.fuckingdeveloper.lms.transform;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Native LMS replacement for IC2 Classic's legacy stepassistfix coremod.
 *
 * <p>Forge 1.19.2 patched Player#getStepHeight() through IForgeEntity. In the
 * current runtime Player#maybeBackOffFromEdge uses maxUpStep() instead. The old
 * coremod's intent was to route the step-height read through
 * ASMHacks#getStepHeight(Player), so LMS rewrites that exact call site without
 * executing the legacy JavaScript coremod.</p>
 */
public final class CoremodTransform6Rewriter {
    public static final String TARGET_OWNER = "net/minecraft/world/entity/player/Player";
    public static final String TARGET_METHOD = "maybeBackOffFromEdge";
    public static final String TARGET_DESCRIPTOR =
            "(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/MoverType;)"
                    + "Lnet/minecraft/world/phys/Vec3;";

    private static final String CURRENT_STEP_METHOD = "maxUpStep";
    private static final String IC2_HOOK_OWNER = "ic2/core/platform/corehacks/ASMHacks";
    private static final String IC2_HOOK_METHOD = "getStepHeight";
    private static final String IC2_HOOK_DESCRIPTOR =
            "(Lnet/minecraft/world/entity/player/Player;)F";

    public record Result(byte[] bytecode, int replacements, boolean targetMethodFound) {
        public boolean changed() {
            return replacements > 0;
        }
    }

    public Result rewrite(byte[] originalBytecode) {
        ClassNode node = new ClassNode();
        new ClassReader(originalBytecode).accept(node, 0);
        if (!TARGET_OWNER.equals(node.name)) {
            return new Result(originalBytecode, 0, false);
        }

        boolean targetFound = false;
        int replacements = 0;
        for (MethodNode method : node.methods) {
            if (!TARGET_METHOD.equals(method.name) || !TARGET_DESCRIPTOR.equals(method.desc)) continue;
            targetFound = true;

            for (var instruction = method.instructions.getFirst();
                 instruction != null;
                 instruction = instruction.getNext()) {
                if (!(instruction instanceof MethodInsnNode call)) continue;
                if (call.getOpcode() != Opcodes.INVOKEVIRTUAL
                        || !TARGET_OWNER.equals(call.owner)
                        || !CURRENT_STEP_METHOD.equals(call.name)
                        || !"()F".equals(call.desc)) {
                    continue;
                }

                // The receiver Player already sits on the operand stack. The static
                // IC2 hook consumes that same Player and returns the replacement float.
                MethodInsnNode replacement = new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        IC2_HOOK_OWNER,
                        IC2_HOOK_METHOD,
                        IC2_HOOK_DESCRIPTOR,
                        false);
                method.instructions.set(call, replacement);
                replacements++;
            }
        }

        if (replacements == 0) {
            return new Result(originalBytecode, 0, targetFound);
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return new Result(writer.toByteArray(), replacements, true);
    }
}
