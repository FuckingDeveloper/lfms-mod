package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.transform.SemanticInvocationRewriter;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;

/** Exact pinned source declarations; no removed-class blanket type projection. */
final class Forge1192EnchantedBookRewriter {
    private static final String BOOK = "net/minecraft/world/item/EnchantedBookItem";
    private static final String ENCHANTMENT = "net/minecraft/world/item/enchantment/Enchantment";
    private static final String BRIDGE = "com/fuckingdeveloper/lms/profile/forge1192/runtime/item/Forge1192EnchantedBookBridge";
    static int rewrite(ClassNode node, SemanticInvocationRewriter calls) {
        if (BOOK.equals(node.superName))
            throw new UnsupportedOperationException("Source enchanted-book subclasses require a construction adapter");
        return calls.rewrite(node, (opcode, owner, name, descriptor) -> {
            if (opcode == Opcodes.INVOKESTATIC && BOOK.equals(owner)
                    && (name.equals("m_41163_") || name.equals("getEnchantments"))
                    && descriptor.equals("(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/nbt/ListTag;"))
                return new Handle(Opcodes.H_INVOKESTATIC, BRIDGE, "getEnchantments", descriptor, false);
            if (opcode == Opcodes.INVOKEVIRTUAL && ENCHANTMENT.equals(owner)
                    && (name.equals("m_44704_") || name.equals("getDescriptionId"))
                    && descriptor.equals("()Ljava/lang/String;"))
                return new Handle(Opcodes.H_INVOKESTATIC, BRIDGE, "descriptionId", "(Ljava/lang/Object;)Ljava/lang/String;", false);
            return null;
        });
    }
}
