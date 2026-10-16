package com.fuckingdeveloper.lms.profile.forge1192;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Exact source operation identities, including values embedded in constructor arguments. */
final class Forge1192AttributeOperationRewriter {
    private static final String OWNER="net/minecraft/world/entity/ai/attributes/AttributeModifier$Operation";
    private static String name(String value) {
        return switch(value) {
            case "ADDITION"->"ADD_VALUE";
            case "MULTIPLY_BASE"->"ADD_MULTIPLIED_BASE";
            case "MULTIPLY_TOTAL"->"ADD_MULTIPLIED_TOTAL";
            default->value;
        };
    }
    private static Object constant(Object value) {
        if(value instanceof Handle h && h.getTag()==Opcodes.H_GETSTATIC && h.getOwner().equals(OWNER)
                && h.getDesc().equals("L"+OWNER+";"))
            return new Handle(h.getTag(),h.getOwner(),name(h.getName()),h.getDesc(),h.isInterface());
        if(value instanceof ConstantDynamic c) {
            var args=new Object[c.getBootstrapMethodArgumentCount()];
            for(int i=0;i<args.length;i++)args[i]=constant(c.getBootstrapMethodArgument(i));
            return new ConstantDynamic(c.getName(),c.getDescriptor(),c.getBootstrapMethod(),args);
        }
        return value;
    }
    static int rewrite(ClassNode node) {
        int count=0;
        for(var method:node.methods)for(var insn:method.instructions) {
            if(insn instanceof FieldInsnNode f && f.getOpcode()==Opcodes.GETSTATIC
                    && f.owner.equals(OWNER)&&f.desc.equals("L"+OWNER+";")) {
                var mapped=name(f.name);if(!mapped.equals(f.name)){f.name=mapped;count++;}
            } else if(insn instanceof LdcInsnNode ldc){var mapped=constant(ldc.cst);if(!mapped.equals(ldc.cst)){ldc.cst=mapped;count++;}}
            else if(insn instanceof InvokeDynamicInsnNode indy)for(int i=0;i<indy.bsmArgs.length;i++) {
                var mapped=constant(indy.bsmArgs[i]);if(!mapped.equals(indy.bsmArgs[i])){indy.bsmArgs[i]=mapped;count++;}
            }
        }
        return count;
    }
}
