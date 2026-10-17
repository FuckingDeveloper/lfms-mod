package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.transform.SemanticInvocationRewriter;
import java.util.function.BiPredicate;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Forge extension identity and verified Item ancestry, independent of fixture ownership. */
final class Forge1192CreatorMetadataRewriter implements Opcodes {
    private static final String ITEM = "net/minecraft/world/item/Item";
    private static final String EXTENSION = "net/minecraftforge/common/extensions/IForgeItem";
    private static final String VIEW = "com/fuckingdeveloper/lms/profile/forge1192/runtime/item/LegacyIForgeItem";
    private static final String STACK = "Lnet/minecraft/world/item/ItemStack;";
    private static final String DESC = "(" + STACK + ")Ljava/lang/String;";
    private static final String BRIDGE = "com/fuckingdeveloper/lms/profile/forge1192/runtime/item/Forge1192CreatorMetadataBridge";

    static int rewrite(ClassNode node, BiPredicate<String, String> subtype) {
        boolean item = subtype.test(node.name, ITEM);
        boolean override = item && node.methods.stream().anyMatch(m -> m.name.equals("getCreatorModId")
                && m.desc.equals(DESC) && (m.access & ACC_STATIC) == 0);
        int count = new SemanticInvocationRewriter(subtype).rewrite(node, (opcode, owner, name, desc) -> {
            if (opcode == INVOKESTATIC && owner.equals("net/minecraftforge/common/ForgeHooks")
                    && name.equals("getDefaultCreatorModId") && desc.equals(DESC))
                return new Handle(H_INVOKESTATIC, BRIDGE, "defaultCreatorModId", DESC, false);
            if (!name.equals("getCreatorModId") || !desc.equals(DESC)) return null;
            boolean itemOwner = subtype.test(owner, ITEM);
            boolean extension = !itemOwner && (owner.equals(EXTENSION) || owner.equals(VIEW) || subtype.test(owner, EXTENSION));
            boolean virtual = opcode == INVOKEVIRTUAL || opcode == INVOKEINTERFACE;
            boolean base = opcode == INVOKESPECIAL && (owner.equals(ITEM) || owner.equals(EXTENSION) || owner.equals(VIEW));
            if (!virtual && !base) return null; // Explicit managed superclass dispatch remains virtual-source ABI.
            if (!extension && !itemOwner) return null;
            String receiver = extension ? VIEW : ITEM;
            String method = base ? (extension ? "interfaceDefault" : "itemDefault")
                    : (extension ? "interfaceCreatorModId" : "creatorModId");
            return new Handle(H_INVOKESTATIC, BRIDGE, method, "(L" + receiver + ";" + STACK + ")Ljava/lang/String;", false);
        });
        if (override && !node.interfaces.contains(VIEW)) { node.interfaces.add(VIEW); count++; }
        if (item && (override || subtype.test(node.name, EXTENSION) || subtype.test(node.name, VIEW))) {
            String nativeDesc = "(Lnet/minecraft/core/HolderLookup$Provider;" + STACK + ")Ljava/lang/String;";
            if (node.methods.stream().noneMatch(m -> m.name.equals("getCreatorModId") && m.desc.equals(nativeDesc))) {
                var callback = new MethodNode(ACC_PUBLIC | ACC_SYNTHETIC, "getCreatorModId", nativeDesc, null, null);
                callback.instructions.add(new VarInsnNode(ALOAD, 0));
                callback.instructions.add(new VarInsnNode(ALOAD, 2));
                callback.instructions.add(new MethodInsnNode(INVOKESTATIC, BRIDGE, "creatorModId",
                        "(L" + ITEM + ";" + STACK + ")Ljava/lang/String;", false));
                callback.instructions.add(new InsnNode(ARETURN));
                callback.maxStack = 2; callback.maxLocals = 3;
                node.methods.add(callback); count++;
            }
        }
        return count;
    }
}
