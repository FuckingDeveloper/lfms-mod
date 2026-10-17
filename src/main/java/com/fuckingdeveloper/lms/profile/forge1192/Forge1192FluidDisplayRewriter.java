package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.transform.SemanticInvocationRewriter;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import static org.objectweb.asm.Opcodes.*;

final class Forge1192FluidDisplayRewriter {
    private static final String SOURCE="net/minecraftforge/fluids/FluidStack";
    static int rewrite(ClassNode node,SemanticInvocationRewriter calls) {
        if(SOURCE.equals(node.superName))throw new UnsupportedOperationException("Source FluidStack subclasses require a separate adapter; native FluidStack is final");
        return calls.rewrite(node,(opcode,owner,name,desc)->{
            if(opcode!=INVOKEVIRTUAL || !owner.equals(SOURCE))return null;
            boolean known=name.equals("getDisplayName")&&desc.equals("()Lnet/minecraft/network/chat/Component;")
                    ||name.equals("getTranslationKey")&&desc.equals("()Ljava/lang/String;");
            return known?new Handle(H_INVOKESTATIC,"com/fuckingdeveloper/lms/target/neoforge263/Target263FluidDisplayQueries",name,"(L"+SOURCE+";"+desc.substring(1),false):null;
        });
    }
}
