package com.fuckingdeveloper.lms.profile;

import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.*;

/** Source-owned callback and exact block-state query identities. */
final class Forge1192ToolActionRewriter {
    private Forge1192ToolActionRewriter() {}
    static int callbacks(ClassNode node, boolean itemSubtype) {
        int rewrites = 0;
        if (itemSubtype) {
            String sourceToolQuery = "(Lnet/minecraft/world/item/ItemStack;Lnet/minecraftforge/common/ToolAction;)Z";
            String targetToolQuery = "(Lnet/minecraft/world/item/ItemInstance;Lnet/neoforged/neoforge/common/ItemAbility;)Z";
            var sourceToolCallback = node.methods.stream().filter(m -> m.name.equals("canPerformAction") && m.desc.equals(sourceToolQuery)).findFirst();
            if (sourceToolCallback.isPresent()) {
                if ((sourceToolCallback.get().access & (org.objectweb.asm.Opcodes.ACC_STATIC | org.objectweb.asm.Opcodes.ACC_PRIVATE)) != 0
                        || node.methods.stream().anyMatch(m -> m.name.equals("canPerformAction") && m.desc.equals(targetToolQuery)))
                    throw new IllegalStateException("Unadapted source tool-action override shape: " + node.name);
                var wrapper = new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC | org.objectweb.asm.Opcodes.ACC_SYNTHETIC,
                        "canPerformAction", targetToolQuery, null, null);
                wrapper.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 0));
                wrapper.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 1));
                wrapper.instructions.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192ToolModificationBridge", "requireStack",
                        "(Lnet/minecraft/world/item/ItemInstance;)Lnet/minecraft/world/item/ItemStack;", false));
                wrapper.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 2));
                wrapper.instructions.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKEVIRTUAL, node.name,
                        "canPerformAction", sourceToolQuery, false));
                wrapper.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.IRETURN));
                node.methods.add(wrapper); rewrites++;
            }
        }
        return rewrites;
    }
    static Handle stateQuery(int opcode, String owner, String name, String desc) {
        return (opcode == org.objectweb.asm.Opcodes.INVOKEVIRTUAL || opcode == org.objectweb.asm.Opcodes.INVOKEINTERFACE)
                && (owner.equals("net/minecraft/world/level/block/state/BlockState")
                    || owner.equals("net/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase")
                    || owner.equals("net/minecraftforge/common/extensions/IForgeBlockState"))
                && name.equals("getToolModifiedState")
                && desc.equals("(Lnet/minecraft/world/item/context/UseOnContext;Lnet/minecraftforge/common/ToolAction;Z)Lnet/minecraft/world/level/block/state/BlockState;")
                ? new Handle(org.objectweb.asm.Opcodes.H_INVOKESTATIC,
                    "com/fuckingdeveloper/lms/runtime/Forge1192ToolModificationBridge", "query",
                    "(Lnet/minecraft/world/level/block/state/BlockState;" + desc.substring(1), false) : null;
    }
}
