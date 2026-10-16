package com.fuckingdeveloper.lms.profile.forge1192;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Source BlockItem names delegate to the actual block, for all construction paths. */
final class Forge1192BlockItemNamesRewriter {
    private static final String BLOCK_ITEM = "net/minecraft/world/item/BlockItem";
    private static final String DESC = "(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/item/Item$Properties;)V";
    private static final String BRIDGE = "com/fuckingdeveloper/lms/profile/forge1192/runtime/item/Forge1192VanillaConstructionBridge";
    private static Object constant(Object value) {
        if (value instanceof Handle h && h.getTag() == Opcodes.H_NEWINVOKESPECIAL && h.getOwner().equals(BLOCK_ITEM)
                && h.getName().equals("<init>") && h.getDesc().equals(DESC))
            return new Handle(Opcodes.H_INVOKESTATIC, BRIDGE, "blockItemWithSourceName", DESC.substring(0, DESC.length()-1) + "L" + BLOCK_ITEM + ";", false);
        if (value instanceof ConstantDynamic d) {
            Object[] args = new Object[d.getBootstrapMethodArgumentCount()]; boolean changed = false;
            for (int i=0;i<args.length;i++) { var old=d.getBootstrapMethodArgument(i); args[i]=constant(old); changed |= args[i]!=old; }
            if (changed) return new ConstantDynamic(d.getName(), d.getDescriptor(), d.getBootstrapMethod(), args);
        }
        return value;
    }
    static int rewrite(ClassNode node) {
        int count=0;
        for (var method:node.methods) for (var instruction:method.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode call && call.getOpcode()==Opcodes.INVOKESPECIAL
                    && call.owner.equals(BLOCK_ITEM) && call.name.equals("<init>") && call.desc.equals(DESC)) {
                var previous=call.getPrevious(); while(previous!=null && previous.getOpcode()<0) previous=previous.getPrevious();
                if(previous instanceof MethodInsnNode hook && hook.owner.equals(BRIDGE) && hook.name.equals("withBlockItemDescription")) continue;
                int props=method.maxLocals++, block=method.maxLocals++;
                var code=new InsnList();
                code.add(new VarInsnNode(Opcodes.ASTORE,props)); code.add(new InsnNode(Opcodes.DUP));
                code.add(new VarInsnNode(Opcodes.ASTORE,block)); code.add(new VarInsnNode(Opcodes.ALOAD,props));
                code.add(new VarInsnNode(Opcodes.ALOAD,block));
                code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,BRIDGE,"withBlockItemDescription",
                        "(Lnet/minecraft/world/item/Item$Properties;Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/item/Item$Properties;",false));
                method.instructions.insertBefore(call,code); count++;
            } else if(instruction instanceof LdcInsnNode ldc) {
                var next=constant(ldc.cst); if(next!=ldc.cst){ldc.cst=next;count++;}
            } else if(instruction instanceof InvokeDynamicInsnNode dynamic) for(int i=0;i<dynamic.bsmArgs.length;i++) {
                var next=constant(dynamic.bsmArgs[i]);if(next!=dynamic.bsmArgs[i]){dynamic.bsmArgs[i]=next;count++;}
            }
        }
        return count;
    }
}
