package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.Set;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Static source factories carry explicit actor/world provenance into native types. */
final class Forge1192DamageFactoryRewriter {
    private static final String ROOT = "net/minecraft/world/damagesource/DamageSource";
    private static final String LEVEL = "Lnet/minecraft/world/level/Level;";
    interface Evidence { String identity(String owner, String name, String descriptor); }
    private static String operation(String owner, String name, String desc, Evidence evidence) {
        if (!owner.equals(ROOT)) return null;
        String identity = evidence.identity(owner, name, desc);
        if (identity == null || !identity.startsWith(ROOT + "#")) return null;
        String op = identity.substring(ROOT.length() + 1);
        String arg = switch (op) {
            case "explosion" -> desc.startsWith("(Lnet/minecraft/world/level/Explosion;")
                    ? "net/minecraft/world/level/Explosion" : "net/minecraft/world/entity/LivingEntity";
            case "mobAttack" -> "net/minecraft/world/entity/LivingEntity";
            case "playerAttack" -> "net/minecraft/world/entity/player/Player";
            case "thorns" -> "net/minecraft/world/entity/Entity";
            default -> null;
        };
        return arg != null && desc.equals("(L" + arg + ";)L" + ROOT + ";") ? op : null;
    }
    static int rewrite(ClassNode node, Evidence evidence) {
        String bridge = com.fuckingdeveloper.lms.target.InstalledTargetAdapters.damageFactoriesBridge();
        int count = 0;
        for (var method : node.methods) {
            int levelSlot = -1, slot = (method.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
            boolean ambiguous = false;
            for (var arg : Type.getArgumentTypes(method.desc)) {
                if (arg.getSort() == Type.OBJECT && Set.of("net/minecraft/world/level/Level",
                        "net/minecraft/server/level/ServerLevel", "net/minecraft/client/multiplayer/ClientLevel")
                        .contains(arg.getInternalName())) {
                    if (levelSlot >= 0) ambiguous = true;
                    levelSlot = slot;
                }
                slot += arg.getSize();
            }
            int snapshot = -1;
            for (var insn : method.instructions.toArray()) {
                if (insn instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESTATIC) {
                    String op = operation(call.owner, call.name, call.desc, evidence);
                    if (op == null) continue;
                    if (op.equals("explosion") && levelSlot >= 0 && !ambiguous) {
                        if (snapshot < 0) {
                            snapshot = Math.max(method.maxLocals, slot); method.maxLocals = snapshot + 1;
                            var capture = new InsnList(); capture.add(new VarInsnNode(Opcodes.ALOAD, levelSlot));
                            capture.add(new VarInsnNode(Opcodes.ASTORE, snapshot)); method.instructions.insert(capture);
                        }
                        var context = new InsnList(); context.add(new VarInsnNode(Opcodes.ALOAD, snapshot));
                        context.add(new InsnNode(Opcodes.SWAP)); method.instructions.insertBefore(call, context);
                        call.desc = "(" + LEVEL + call.desc.substring(1);
                    }
                    call.owner = bridge; call.name = op; call.itf = false; count++;
                } else if (insn instanceof LdcInsnNode ldc) {
                    Object next = constant(ldc.cst, evidence, bridge);
                    if (next != ldc.cst) { ldc.cst = next; count++; }
                } else if (insn instanceof InvokeDynamicInsnNode indy) {
                    for (int i=0;i<indy.bsmArgs.length;i++) {
                        Object next=constant(indy.bsmArgs[i], evidence, bridge);
                        if(next!=indy.bsmArgs[i]) {indy.bsmArgs[i]=next;count++;}
                    }
                }
            }
        }
        return count;
    }
    private static Object constant(Object value, Evidence evidence, String bridge) {
        if (value instanceof Handle handle && handle.getTag() == Opcodes.H_INVOKESTATIC) {
            String op=operation(handle.getOwner(),handle.getName(),handle.getDesc(),evidence);
            return op==null?value:new Handle(Opcodes.H_INVOKESTATIC,bridge,op,handle.getDesc(),false);
        }
        if(value instanceof ConstantDynamic dynamic) {
            Object[] args=new Object[dynamic.getBootstrapMethodArgumentCount()]; boolean changed=false;
            for(int i=0;i<args.length;i++) {Object old=dynamic.getBootstrapMethodArgument(i);args[i]=constant(old,evidence,bridge);changed|=old!=args[i];}
            if(changed)return new ConstantDynamic(dynamic.getName(),dynamic.getDescriptor(),dynamic.getBootstrapMethod(),args);
        }
        return value;
    }
}
