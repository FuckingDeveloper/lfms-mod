package com.fuckingdeveloper.lms.profile.forge1192;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Exact source custom-renderer and dispatcher family, before type relocation. */
final class Forge1192CustomItemRewriter {
    private static final String BASE = "net/minecraft/client/renderer/BlockEntityWithoutLevelRenderer";
    private static final String DISPATCHER = "net/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher";
    private static final String DESC = "(Lnet/minecraft/world/level/block/entity/BlockEntity;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)Z";
    private static final String BRIDGE = "com/fuckingdeveloper/lms/profile/forge1192/runtime/render/Forge1192CustomItemBridge";

    private static boolean selected(String owner, String name, String desc) {
        return owner.equals(DISPATCHER) && name.equals("m_112272_") && desc.equals(DESC);
    }
    private static Object constant(Object value) {
        if (value instanceof Handle h && h.getTag() == Opcodes.H_INVOKEVIRTUAL && selected(h.getOwner(), h.getName(), h.getDesc()))
            return new Handle(Opcodes.H_INVOKESTATIC, BRIDGE, "renderItem", "(L" + DISPATCHER + ";" + DESC.substring(1), false);
        if (value instanceof ConstantDynamic d) {
            Object[] args = new Object[d.getBootstrapMethodArgumentCount()]; boolean changed = false;
            for (int i = 0; i < args.length; i++) { var old = d.getBootstrapMethodArgument(i); args[i] = constant(old); changed |= args[i] != old; }
            if (changed) return new ConstantDynamic(d.getName(), d.getDescriptor(), d.getBootstrapMethod(), args);
        }
        return value;
    }
    static int rewrite(ClassNode node) {
        boolean direct = BASE.equals(node.superName);
        if (direct && node.methods.stream().noneMatch(m -> m.name.equals("m_108829_") && m.desc.equals(
                "(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V")))
            throw new UnsupportedOperationException("Source custom renderer requires a complete renderByItem override: " + node.name);
        int count = 0;
        for (var method : node.methods) for (var instruction : method.instructions.toArray()) {
            if (instruction instanceof FieldInsnNode field && field.getOpcode() == Opcodes.GETFIELD
                    && field.owner.equals(DISPATCHER) && field.name.equals("f_112249_")
                    && field.desc.equals("Lnet/minecraft/client/Camera;")) {
                var next = field.getNext(); while (next != null && next.getOpcode() < 0) next = next.getNext();
                if (!(next instanceof MethodInsnNode position) || position.getOpcode() != Opcodes.INVOKEVIRTUAL
                        || !position.owner.equals("net/minecraft/client/Camera") || !position.name.equals("m_90583_")
                        || !position.desc.equals("()Lnet/minecraft/world/phys/Vec3;"))
                    throw new UnsupportedOperationException("Source dispatcher camera use requires an established target camera operation");
                method.instructions.set(field, new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "cameraPosition",
                        "(L" + DISPATCHER + ";)Lnet/minecraft/world/phys/Vec3;", false));
                method.instructions.remove(position); count++;
                continue;
            }
            if (instruction instanceof MethodInsnNode call) {
                if (call.getOpcode() == Opcodes.INVOKESPECIAL && call.name.equals("initializeClient")
                        && call.desc.equals("(Ljava/util/function/Consumer;)V")
                        && (call.owner.equals("net/minecraft/world/item/Item") || call.owner.equals("net/minecraft/world/item/BlockItem"))) {
                    call.owner = BRIDGE; call.name = "sourceBaseInitialize";
                    call.desc = "(Lnet/minecraft/world/item/Item;Ljava/util/function/Consumer;)V";
                    call.setOpcode(Opcodes.INVOKESTATIC); call.itf = false; count++;
                }
                if (direct && call.owner.equals(BASE) && !(call.name.equals("<init>") && call.desc.equals(
                        "(L" + DISPATCHER + ";Lnet/minecraft/client/model/geom/EntityModelSet;)V")))
                    throw new UnsupportedOperationException("Source vanilla custom-renderer base behavior is unsupported: " + call.name);
                if (call.getOpcode() == Opcodes.INVOKEVIRTUAL && selected(call.owner, call.name, call.desc)) {
                    call.owner = BRIDGE; call.name = "renderItem"; call.desc = "(L" + DISPATCHER + ";" + DESC.substring(1);
                    call.setOpcode(Opcodes.INVOKESTATIC); call.itf = false; count++;
                }
            } else if (direct && instruction instanceof FieldInsnNode f && f.owner.equals(BASE)) {
                throw new UnsupportedOperationException("Source vanilla custom-renderer base state is unsupported: " + f.name);
            } else if (instruction instanceof LdcInsnNode ldc) {
                var next = constant(ldc.cst); if (next != ldc.cst) { ldc.cst = next; count++; }
            } else if (instruction instanceof InvokeDynamicInsnNode dynamic) {
                for (int i = 0; i < dynamic.bsmArgs.length; i++) {
                    var next = constant(dynamic.bsmArgs[i]); if (next != dynamic.bsmArgs[i]) { dynamic.bsmArgs[i] = next; count++; }
                }
            }
        }
        return count;
    }
}
