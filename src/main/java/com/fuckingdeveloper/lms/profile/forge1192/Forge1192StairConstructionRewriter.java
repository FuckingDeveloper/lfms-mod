package com.fuckingdeveloper.lms.profile.forge1192;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

/** Exact Forge supplier constructor; source subclasses retain their own initialization. */
final class Forge1192StairConstructionRewriter {
    private static final String ROOT="net/minecraft/world/level/block/StairBlock";
    private static final String SUPPLIER="Ljava/util/function/Supplier;",STATE="Lnet/minecraft/world/level/block/state/BlockState;",PROPERTIES="Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;";
    private static final String SOURCE="("+SUPPLIER+PROPERTIES+")V";
    static int rewrite(ClassNode node,String bridge) {
        int count=0;
        for(var method:node.methods)for(var insn:method.instructions.toArray()) {
            if(insn instanceof MethodInsnNode call && call.getOpcode()==INVOKESPECIAL && call.owner.equals(ROOT)
                    && call.name.equals("<init>") && call.desc.equals(SOURCE)) {
                var arguments=new InsnList();arguments.add(new InsnNode(SWAP));
                arguments.add(new MethodInsnNode(INVOKESTATIC,bridge,"resolve","("+SUPPLIER+")"+STATE,false));arguments.add(new InsnNode(SWAP));
                method.instructions.insertBefore(call,arguments);call.desc="("+STATE+PROPERTIES+")V";count++;
            }else if(insn instanceof LdcInsnNode ldc){Object next=constant(ldc.cst,bridge);if(next!=ldc.cst){ldc.cst=next;count++;}}
            else if(insn instanceof InvokeDynamicInsnNode indy)for(int i=0;i<indy.bsmArgs.length;i++){Object next=constant(indy.bsmArgs[i],bridge);if(next!=indy.bsmArgs[i]){indy.bsmArgs[i]=next;count++;}}
        }
        return count;
    }
    private static Object constant(Object value,String bridge) {
        if(value instanceof Handle h && h.getTag()==H_NEWINVOKESPECIAL && h.getOwner().equals(ROOT)&&h.getName().equals("<init>")&&h.getDesc().equals(SOURCE))
            return new Handle(H_INVOKESTATIC,bridge,"create","("+SUPPLIER+PROPERTIES+")L"+ROOT+";",false);
        if(value instanceof ConstantDynamic dynamic){Object[] arguments=new Object[dynamic.getBootstrapMethodArgumentCount()];boolean changed=false;for(int i=0;i<arguments.length;i++){Object old=dynamic.getBootstrapMethodArgument(i);arguments[i]=constant(old,bridge);changed|=old!=arguments[i];}if(changed)return new ConstantDynamic(dynamic.getName(),dynamic.getDescriptor(),dynamic.getBootstrapMethod(),arguments);}
        return value;
    }
}
