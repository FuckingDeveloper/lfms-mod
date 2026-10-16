package com.fuckingdeveloper.lms.profile.forge1192;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Pinned TagKey.create identity, before source namespace projection. */
final class Forge1192CommonTagRewriter {
    private static final String OWNER = "net/minecraft/tags/TagKey";
    private static final String DESC = "(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/tags/TagKey;";
    private static Handle operation(Handle handle) {
        if (handle.getOwner().equals(OWNER)) {
            if (handle.getTag()==Opcodes.H_NEWINVOKESPECIAL && handle.getName().equals("<init>")
                    && handle.getDesc().equals(DESC.substring(0,DESC.indexOf(')')+1)+"V"))
                return new Handle(Opcodes.H_INVOKESTATIC,"com/fuckingdeveloper/lms/target/neoforge263/Target263CommonTagProjection","construct",DESC,false);
            if (handle.getTag()==Opcodes.H_INVOKESTATIC && handle.getDesc().equals("(Lnet/minecraft/resources/ResourceKey;)Lcom/mojang/serialization/Codec;")) {
                String codec=switch(handle.getName()) {case "m_203877_","codec"->"codec";case "m_203886_","hashedCodec"->"hashedCodec";default->null;};
                if(codec!=null)return new Handle(Opcodes.H_INVOKESTATIC,"com/fuckingdeveloper/lms/target/neoforge263/Target263CommonTagProjection",codec,handle.getDesc(),false);
            }
        }
        if (handle.getTag() == Opcodes.H_INVOKESTATIC && handle.getName().equals("create")
                && handle.getDesc().equals("(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/tags/TagKey;")) {
            String factory = switch (handle.getOwner()) {
                case "net/minecraft/tags/ItemTags" -> "item";
                case "net/minecraft/tags/BlockTags" -> "block";
                case "net/minecraft/tags/FluidTags" -> "fluid";
                default -> null;
            };
            if (factory != null) return new Handle(Opcodes.H_INVOKESTATIC,
                    "com/fuckingdeveloper/lms/target/neoforge263/Target263CommonTagProjection", factory, handle.getDesc(), false);
        }
        if (handle.getTag() == Opcodes.H_INVOKESTATIC && handle.getOwner().equals(OWNER)
                && (handle.getName().equals("m_203882_") || handle.getName().equals("create"))
                && handle.getDesc().equals(DESC))
            return new Handle(Opcodes.H_INVOKESTATIC, "com/fuckingdeveloper/lms/target/neoforge263/Target263CommonTagProjection", "create", DESC, false);
        return handle;
    }
    private static Object constant(Object value) {
        if (value instanceof Handle handle) return operation(handle);
        if (value instanceof ConstantDynamic dynamic) {
            Object[] args = new Object[dynamic.getBootstrapMethodArgumentCount()]; boolean changed = false;
            for (int i = 0; i < args.length; i++) { Object old = dynamic.getBootstrapMethodArgument(i); args[i] = constant(old); changed |= args[i] != old; }
            if (changed) return new ConstantDynamic(dynamic.getName(), dynamic.getDescriptor(), dynamic.getBootstrapMethod(), args);
        }
        return value;
    }
    static int rewrite(ClassNode node) {
        int count = 0;
        for (var method : node.methods) for (var instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call && call.getOpcode()==Opcodes.INVOKESPECIAL
                    && call.owner.equals(OWNER) && call.name.equals("<init>")
                    && call.desc.equals(DESC.substring(0,DESC.indexOf(')')+1)+"V")) {
                // Pass only initialized arguments, preserving NEW/uninitialized this and fresh identity.
                var projection=new InsnList(); projection.add(new InsnNode(Opcodes.DUP2));
                projection.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"com/fuckingdeveloper/lms/target/neoforge263/Target263CommonTagProjection",
                        "projectLocation",DESC.substring(0,DESC.indexOf(')')+1)+"Lnet/minecraft/resources/ResourceLocation;",false));
                projection.add(new InsnNode(Opcodes.SWAP));projection.add(new InsnNode(Opcodes.POP));
                method.instructions.insertBefore(call,projection);count++;
            } else if (instruction instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESTATIC) {
                var old = new Handle(Opcodes.H_INVOKESTATIC, call.owner, call.name, call.desc, call.itf);
                var next = operation(old);
                if (!next.equals(old)) { call.owner = next.getOwner(); call.name = next.getName(); call.desc = next.getDesc(); call.itf = false; count++; }
            } else if (instruction instanceof LdcInsnNode ldc) {
                Object next = constant(ldc.cst); if (next != ldc.cst) { ldc.cst = next; count++; }
            } else if (instruction instanceof InvokeDynamicInsnNode indy) {
                for (int i = 0; i < indy.bsmArgs.length; i++) { Object next = constant(indy.bsmArgs[i]); if (next != indy.bsmArgs[i]) { indy.bsmArgs[i] = next; count++; } }
            }
        }
        return count;
    }
}
