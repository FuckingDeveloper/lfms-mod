package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

/** Nominal source identities, never a blanket alias to a native recipe implementation. */
final class Forge1192SpecialRecipeIdentities {
    private static final String SOURCE="net/minecraft/world/item/crafting/";
    private static final String VIEW="com/fuckingdeveloper/lms/profile/forge1192/runtime/recipe/identity/";
    private static final Set<String> REMOVED=Set.of("TippedArrowRecipe","ShulkerBoxColoring","SuspiciousStewRecipe");
    static String type(String owner) {
        return owner!=null && owner.startsWith(SOURCE) && REMOVED.contains(owner.substring(SOURCE.length()))
                ? VIEW+owner.substring(SOURCE.length()) : owner;
    }
    static String descriptor(String descriptor) {
        for(String name:REMOVED) descriptor=descriptor.replace("L"+SOURCE+name+";","L"+VIEW+name+";")
                .replace("L"+SOURCE+name+"<","L"+VIEW+name+"<");
        return descriptor;
    }
    private static boolean removed(String owner) { return !Objects.equals(type(owner),owner); }
    static int verify(ClassNode node) {
        if(removed(node.superName))throw unsupported(node.superName);
        for(var method:node.methods)for(var insn:method.instructions) {
            if(insn instanceof TypeInsnNode type && type.getOpcode()==NEW && removed(type.desc))throw unsupported(type.desc);
            if(insn instanceof MethodInsnNode call && removed(call.owner))throw unsupported(call.owner);
            if(insn instanceof FieldInsnNode field && removed(field.owner))throw unsupported(field.owner);
            if(insn instanceof LdcInsnNode ldc)verifyConstant(ldc.cst);
            if(insn instanceof InvokeDynamicInsnNode indy){verifyConstant(indy.bsm);for(Object arg:indy.bsmArgs)verifyConstant(arg);}
        }
        return 0; // Admission check only; full namespace projection preserves class-file type surfaces.
    }
    private static void verifyConstant(Object value) {
        if(value instanceof Handle h && removed(h.getOwner()))throw unsupported(h.getOwner());
        if(value instanceof ConstantDynamic c){verifyConstant(c.getBootstrapMethod());for(int i=0;i<c.getBootstrapMethodArgumentCount();i++)verifyConstant(c.getBootstrapMethodArgument(i));}
    }
    private static UnsupportedOperationException unsupported(String owner) {
        return new UnsupportedOperationException("Removed source special recipe supports nominal type identity only, not execution/construction: "+owner);
    }
}
