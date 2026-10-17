package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.transform.SemanticInvocationRewriter;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

/** Source member identity and ancestry select native riding and attachment migration. */
final class Forge1192EntityRidingRewriter {
    private static final String ENTITY = "net/minecraft/world/entity/Entity";
    private static final String VEC = "Lnet/minecraft/world/phys/Vec3;";
    private static final String ONE = "(L" + ENTITY + ";)Z", TWO = "(L" + ENTITY + ";Z)Z";
    private static final String POSITION = "(L" + ENTITY + ";)V";
    interface Evidence {
        boolean entity(String owner);
        boolean managed(String owner);
        String method(String name, String descriptor);
    }
    private static String member(String name, String descriptor, Evidence evidence) {
        String canonical = name.startsWith("m_") ? evidence.method(name, descriptor) : name;
        if ((ONE.equals(descriptor) || TWO.equals(descriptor)) && "startRiding".equals(canonical)) return canonical;
        if ("()D".equals(descriptor) && Set.of("getPassengersRidingOffset", "getMyRidingOffset").contains(canonical == null ? "" : canonical)) return canonical;
        if (POSITION.equals(descriptor) && "positionRider".equals(canonical)) return canonical;
        return null;
    }
    static int rewrite(ClassNode node, Evidence evidence, String bridge, SemanticInvocationRewriter invocations) {
        int count = 0;
        if (evidence.entity(node.name)) for (var method : List.copyOf(node.methods)) {
            if ((method.access & (ACC_STATIC | ACC_PRIVATE)) != 0) continue;
            String member = member(method.name, method.desc, evidence);
            if (member == null) continue;
            if (member.equals("startRiding")) throw new UnsupportedOperationException("Unadapted custom source mounting override: " + node.name + method.desc);
            if (!evidence.managed(node.superName) && !node.superName.equals(ENTITY))
                throw new UnsupportedOperationException("Source riding callback over a native specialized vehicle requires attachment review: " + node.superName);
            String callback = switch (member) {
                case "getPassengersRidingOffset" -> "lms$passengerOffset";
                case "getMyRidingOffset" -> "lms$riderOffset";
                default -> "lms$positionRider";
            };
            if (node.methods.stream().anyMatch(m -> m != method && m.name.equals(callback) && m.desc.equals(method.desc)))
                throw new IllegalStateException("Source riding callback collision: " + node.name + callback);
            method.name = callback;
            method.access = (method.access & ~(ACC_PROTECTED | ACC_PRIVATE)) | ACC_PUBLIC;
            String role = member.equals("getPassengersRidingOffset") ? "PassengerOffset" : member.equals("getMyRidingOffset") ? "RiderOffset" : "Position";
            node.interfaces.add(bridge + "$" + role);
            MethodNode adapter = adapter(node, member, callback, bridge);
            if (node.methods.stream().anyMatch(m -> m.name.equals(adapter.name) && m.desc.equals(adapter.desc)))
                throw new UnsupportedOperationException("Conflicting native/source riding callback: " + node.name + adapter.name);
            node.methods.add(adapter); count++;
        }
        for (var method : node.methods) for (var insn : method.instructions) {
            if (insn instanceof LdcInsnNode ldc) ldc.cst = sourceSuperConstant(ldc.cst, evidence);
            if (insn instanceof InvokeDynamicInsnNode indy) for (int i = 0; i < indy.bsmArgs.length; i++)
                indy.bsmArgs[i] = sourceSuperConstant(indy.bsmArgs[i], evidence);
        }
        count += invocations.rewrite(node, (opcode, owner, name, desc) -> {
            if (opcode == INVOKESTATIC || !evidence.entity(owner)) return null;
            String member = member(name, desc, evidence); if (member == null) return null;
            if (member.equals("startRiding")) {
                if (opcode == INVOKESPECIAL) throw new UnsupportedOperationException("Unadapted source mounting super call");
                return new Handle(H_INVOKESTATIC, bridge, "startRiding", "(L" + ENTITY + ";" + desc.substring(1), false);
            }
            if (member.equals("positionRider") && opcode != INVOKESPECIAL) return null; // Retained native final dispatch.
            if (opcode == INVOKESPECIAL && !owner.equals(ENTITY)) {
                if (!evidence.managed(owner)) throw new UnsupportedOperationException("Unadapted specialized native riding super callback: " + owner);
                return null; // Renamed below, preserving exact source-super dispatch.
            }
            String target = switch (member) {
                case "getPassengersRidingOffset" -> opcode == INVOKESPECIAL ? "basePassengerOffset" : "passengerOffset";
                case "getMyRidingOffset" -> opcode == INVOKESPECIAL ? "baseRiderOffset" : "riderOffset";
                default -> "basePositionRider";
            };
            return new Handle(H_INVOKESTATIC, bridge, target, "(L" + ENTITY + ";" + desc.substring(1), false);
        });
        // Source-super calls must retain invokespecial, never redispatch to the subclass.
        for (var method : node.methods) for (var insn : method.instructions) if (insn instanceof MethodInsnNode call
                && call.getOpcode() == INVOKESPECIAL && evidence.managed(call.owner) && evidence.entity(call.owner)) {
            String member = member(call.name, call.desc, evidence);
            if (member != null) {
                call.name = member.equals("getPassengersRidingOffset") ? "lms$passengerOffset" : member.equals("getMyRidingOffset") ? "lms$riderOffset" : "lms$positionRider";
                count++;
            }
        }
        return count;
    }
    private static Object sourceSuperConstant(Object value, Evidence evidence) {
        if (value instanceof Handle handle && handle.getTag() == H_INVOKESPECIAL
                && evidence.managed(handle.getOwner()) && evidence.entity(handle.getOwner())) {
            String member = member(handle.getName(), handle.getDesc(), evidence);
            if (member == null) return value;
            if (member.equals("startRiding")) throw new UnsupportedOperationException("Unadapted source mounting super handle");
            String alias = member.equals("getPassengersRidingOffset") ? "lms$passengerOffset"
                    : member.equals("getMyRidingOffset") ? "lms$riderOffset" : "lms$positionRider";
            return new Handle(handle.getTag(), handle.getOwner(), alias, handle.getDesc(), handle.isInterface());
        }
        if (value instanceof ConstantDynamic dynamic) {
            Object[] arguments = new Object[dynamic.getBootstrapMethodArgumentCount()]; boolean changed = false;
            for (int i = 0; i < arguments.length; i++) {
                Object previous = dynamic.getBootstrapMethodArgument(i);
                arguments[i] = sourceSuperConstant(previous, evidence); changed |= arguments[i] != previous;
            }
            if (changed) return new ConstantDynamic(dynamic.getName(), dynamic.getDescriptor(), dynamic.getBootstrapMethod(), arguments);
        }
        return value;
    }
    private static MethodNode adapter(ClassNode node, String member, String callback, String bridge) {
        boolean passenger = member.equals("getPassengersRidingOffset"), position = member.equals("positionRider");
        String name = position ? "positionRider" : passenger ? "getPassengerAttachmentPoint" : "getVehicleAttachmentPoint";
        String desc = position ? "(L" + ENTITY + ";L" + ENTITY + "$MoveFunction;)V"
                : passenger ? "(L" + ENTITY + ";Lnet/minecraft/world/entity/EntityDimensions;F)" + VEC : "(L" + ENTITY + ";)" + VEC;
        var method = new MethodNode(ACC_PUBLIC | ACC_SYNTHETIC, name, desc, null, null);
        var code = method.instructions;
        code.add(new VarInsnNode(ALOAD, 0)); code.add(new VarInsnNode(ALOAD, 1));
        if (position) {
            code.add(new VarInsnNode(ALOAD, 2));
            code.add(new MethodInsnNode(INVOKESTATIC, bridge, "positionRider", "(L" + ENTITY + ";L" + ENTITY + ";L" + ENTITY + "$MoveFunction;)V", false));
            code.add(new InsnNode(RETURN));
        } else {
            if (passenger) { code.add(new VarInsnNode(ALOAD, 2)); code.add(new VarInsnNode(FLOAD, 3)); }
            code.add(new MethodInsnNode(INVOKESPECIAL, node.superName, name, desc, false));
            code.add(new VarInsnNode(ALOAD, 0));
            code.add(new MethodInsnNode(INVOKEVIRTUAL, node.name, callback, "()D", false));
            code.add(new MethodInsnNode(INVOKESTATIC, bridge, passenger ? "passengerAttachment" : "riderAttachment", "(" + VEC + "D)" + VEC, false));
            code.add(new InsnNode(ARETURN));
        }
        method.maxStack = 5; method.maxLocals = passenger ? 4 : 3; return method;
    }
}
