package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.transform.SemanticInvocationRewriter;
import java.util.function.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

final class Forge1192LanguageRewriter {
    static final String INFO="net/minecraft/client/resources/language/LanguageInfo";
    static final String VIEW="com/fuckingdeveloper/lms/profile/forge1192/runtime/language/LegacyLanguageInfo";
    private static final String MANAGER="net/minecraft/client/resources/language/LanguageManager";
    private static final java.util.Set<String> QUERY_NAMES=java.util.Set.of("m_118983_", "m_118976_", "m_118984_", "m_118974_",
            "getSelected", "getLanguage", "getLanguages", "setSelected");
    interface Names { String canonical(String owner,String name,String descriptor); }
    static int rewrite(ClassNode node, BiPredicate<String,String> subtype, Names names) {
        if (subtype.test(node.name,MANAGER)) throw new UnsupportedOperationException("Source LanguageManager subclasses require construction/lifecycle adaptation");
        for(var method:node.methods)for(var insn:method.instructions) {
            if(insn instanceof TypeInsnNode type && type.getOpcode()==NEW && type.desc.equals(MANAGER))
                throw new UnsupportedOperationException("Source LanguageManager construction is not adapted");
        }
        int[] count={0};
        if (needsInfoNormalization(node)) {
            var mapped=new ClassNode();
            node.accept(new ClassRemapper(mapped,new Remapper(ASM9) {
                @Override public String mapMethodName(String owner,String name,String desc) {
                    // Pinned joined.tsrg: the only SRG-named LanguageInfo operation.
                    if(name.equals("m_118952_") && desc.equals("()Z") && subtype.test(owner,INFO)) {
                        count[0]++; return "isBidirectional";
                    }
                    return name;
                }
            }));
            node.methods=mapped.methods;node.fields=mapped.fields;
        }
        return count[0]+new SemanticInvocationRewriter(subtype).rewrite(node,(opcode,owner,name,desc)->{
            if(opcode!=INVOKEVIRTUAL || !QUERY_NAMES.contains(name)
                    || !subtype.test(owner,MANAGER))return null;
            String canonical=names.canonical(MANAGER,name,desc);
            boolean known=switch(canonical) {
                case "getSelected" -> desc.equals("()L"+INFO+";");
                case "getLanguage" -> desc.equals("(Ljava/lang/String;)L"+INFO+";");
                case "getLanguages" -> desc.equals("()Ljava/util/SortedSet;");
                case "setSelected" -> desc.equals("(L"+INFO+";)V");
                default -> false;
            };
            return known?new Handle(H_INVOKESTATIC,"com/fuckingdeveloper/lms/target/neoforge263/Target263SourceLanguages",canonical,"(L"+MANAGER+";"+desc.substring(1),false):null;
        });
    }
    private static boolean needsInfoNormalization(ClassNode node) {
        for(var method:node.methods) {
            if(method.name.equals("m_118952_"))return true;
            for(var insn:method.instructions) {
                if(insn instanceof MethodInsnNode call && call.name.equals("m_118952_"))return true;
                if(insn instanceof LdcInsnNode ldc && infoConstant(ldc.cst))return true;
                if(insn instanceof InvokeDynamicInsnNode indy) {
                    if(infoConstant(indy.bsm))return true;
                    for(Object arg:indy.bsmArgs)if(infoConstant(arg))return true;
                }
            }
        }
        return false;
    }
    private static boolean infoConstant(Object value) {
        if(value instanceof Handle handle)return handle.getName().equals("m_118952_");
        if(value instanceof ConstantDynamic dynamic) {
            if(infoConstant(dynamic.getBootstrapMethod()))return true;
            for(int i=0;i<dynamic.getBootstrapMethodArgumentCount();i++)if(infoConstant(dynamic.getBootstrapMethodArgument(i)))return true;
        }
        return false;
    }

}
