package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.ArrayList;
import java.util.function.BiPredicate;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/** Source interface identity plus proven producer shape, never fixture identity. */
final class Forge1192LootModifierRewriter implements Opcodes {
    static int rewrite(ClassNode node, BiPredicate<String, String> subtype,
            BiPredicate<String, String> unitCodec) {
        if (!subtype.test(node.name, "net/minecraftforge/common/loot/IGlobalLootModifier")) return 0;
        int edits = 0;
        for (var method : node.methods) {
            if (method.name.equals("priority") && method.desc.equals("()I")) {
                throw new UnsupportedOperationException("Source loot modifier collides with target priority: " + node.name);
            }
            if (!method.name.equals("codec") || !method.desc.equals("()Lcom/mojang/serialization/Codec;")) continue;
            var body = new ArrayList<AbstractInsnNode>();
            for (var insn : method.instructions) if (insn.getOpcode() >= 0) body.add(insn);
            if (body.size() != 2 || !(body.getFirst() instanceof FieldInsnNode field)
                    || field.getOpcode() != GETSTATIC || body.getLast().getOpcode() != ARETURN
                    || !unitCodec.test(field.owner, field.name)) {
                throw new UnsupportedOperationException("Unproven source global loot codec: " + node.name);
            }
            method.desc = "()Lcom/mojang/serialization/MapCodec;";
            if (method.signature != null) method.signature = method.signature.replace(
                    "Lcom/mojang/serialization/Codec<", "Lcom/mojang/serialization/MapCodec<");
            edits++;
        }
        if ((node.access & (ACC_INTERFACE | ACC_ABSTRACT)) == 0) {
            var priority = new MethodNode(ACC_PUBLIC | ACC_SYNTHETIC, "priority", "()I", null, null);
            priority.instructions.add(new FieldInsnNode(GETSTATIC,
                    "net/neoforged/neoforge/common/loot/IGlobalLootModifier", "DEFAULT_PRIORITY", "I"));
            priority.instructions.add(new InsnNode(IRETURN));
            node.methods.add(priority);
            edits++;
        }
        return edits;
    }
}
