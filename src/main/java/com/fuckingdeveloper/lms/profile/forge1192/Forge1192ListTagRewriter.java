package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.transform.SemanticInvocationRewriter;
import java.util.Map;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;

/** Complete pinned typed-read family, selected before target Optional-return migration. */
final class Forge1192ListTagRewriter {
    private static final String ROOT = "net/minecraft/nbt/ListTag";
    private static final Map<String,String> READS = Map.ofEntries(
            Map.entry("getCompound(I)Lnet/minecraft/nbt/CompoundTag;", "getCompound"),
            Map.entry("getList(I)Lnet/minecraft/nbt/ListTag;", "getList"),
            Map.entry("getShort(I)S", "getShort"), Map.entry("getInt(I)I", "getInt"),
            Map.entry("getDouble(I)D", "getDouble"), Map.entry("getFloat(I)F", "getFloat"),
            Map.entry("getIntArray(I)[I", "getIntArray"), Map.entry("getLongArray(I)[J", "getLongArray"),
            Map.entry("getString(I)Ljava/lang/String;", "getString"), Map.entry("getElementType()B", "getElementType"));
    static int rewrite(ClassNode node, Forge1192RegistryQueryRewriter.Canonical canonical, SemanticInvocationRewriter calls) {
        return calls.rewrite(node, (opcode,owner,name,descriptor) -> {
            if (opcode != Opcodes.INVOKEVIRTUAL || !ROOT.equals(owner)) return null;
            String named = name.startsWith("m_") ? canonical.name(owner,name,descriptor) : name;
            String mapped = READS.get(named + descriptor);
            return mapped == null ? null : new Handle(Opcodes.H_INVOKESTATIC,
                    "com/fuckingdeveloper/lms/profile/forge1192/runtime/data/Forge1192ListTagBridge",
                    mapped, "(L" + ROOT + ";" + descriptor.substring(1), false);
        });
    }
}
