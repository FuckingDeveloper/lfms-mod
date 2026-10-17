package com.fuckingdeveloper.lms.profile.forge1192;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import com.fuckingdeveloper.lms.transform.SemanticInvocationRewriter;

/** Source exclusive ranges over target-owned world/accessor implementations. */
final class Forge1192HeightQueryRewriter {
    static final String ROOT = "net/minecraft/world/level/LevelHeightAccessor";
    private static final String MARKER = "com/fuckingdeveloper/lms/profile/forge1192/runtime/world/Forge1192UnadaptedHeightOverrides";
    private static final Map<String, String> METHODS = Map.ofEntries(
            Map.entry("getHeight", "()I"), Map.entry("getMinBuildHeight", "()I"),
            Map.entry("getMaxBuildHeight", "()I"), Map.entry("getSectionsCount", "()I"),
            Map.entry("getMinSection", "()I"), Map.entry("getMaxSection", "()I"),
            Map.entry("isOutsideBuildHeight", "(I)Z"),
            Map.entry("getSectionIndex", "(I)I"), Map.entry("getSectionIndexFromSectionY", "(I)I"),
            Map.entry("getSectionYFromSectionIndex", "(I)I"));
    private static String canonicalName(String name, String desc, BiFunction<String,String,String> canonical) {
        return name.startsWith("m_") ? canonical.apply(name, desc) : name;
    }
    private static boolean operation(String name, String desc) {
        return name != null && (desc.equals(METHODS.get(name)) || name.equals("isOutsideBuildHeight")
                && desc.equals("(Lnet/minecraft/core/BlockPos;)Z"));
    }
    static int rewrite(ClassNode node, BiPredicate<String,String> subtype,
                       BiFunction<String,String,String> canonical, SemanticInvocationRewriter invocations) {
        int count = 0;
        if (subtype.test(node.name, ROOT) && !node.interfaces.contains(MARKER)) {
            for (var method : node.methods) {
                if ((method.access & Opcodes.ACC_STATIC) == 0 && operation(canonicalName(method.name, method.desc, canonical), method.desc)) {
                    node.interfaces.add(MARKER); count++; break;
                }
            }
        }
        String bridge = com.fuckingdeveloper.lms.target.InstalledTargetAdapters.heightQueriesBridge();
        count += invocations.rewrite(node, (opcode, owner, name, desc) -> {
            if ((opcode != Opcodes.INVOKEVIRTUAL && opcode != Opcodes.INVOKEINTERFACE) || !subtype.test(owner, ROOT)) return null;
            String op = canonicalName(name, desc, canonical);
            if (!operation(op, desc)) return null;
            return new Handle(Opcodes.H_INVOKESTATIC, bridge, op, "(L" + ROOT + ";" + desc.substring(1), false);
        });
        for (var method : node.methods) for (var instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESTATIC
                    && factory(call.owner, call.name, call.desc, canonical)) {
                call.owner = bridge; call.name = "create"; call.itf = false; count++;
            } else if (instruction instanceof LdcInsnNode ldc) {
                Object next = constant(ldc.cst, bridge, canonical);
                if (next != ldc.cst) { ldc.cst = next; count++; }
            } else if (instruction instanceof InvokeDynamicInsnNode indy) {
                for (int i=0;i<indy.bsmArgs.length;i++) {
                    Object next=constant(indy.bsmArgs[i],bridge,canonical);
                    if(next!=indy.bsmArgs[i]) {indy.bsmArgs[i]=next;count++;}
                }
            }
        }
        return count;
    }
    private static boolean factory(String owner,String name,String desc,BiFunction<String,String,String> canonical) {
        return owner.equals(ROOT) && desc.equals("(II)L" + ROOT + ";") && "create".equals(canonicalName(name,desc,canonical));
    }
    private static Object constant(Object value,String bridge,BiFunction<String,String,String> canonical) {
        if(value instanceof Handle handle && handle.getTag()==Opcodes.H_INVOKESTATIC && factory(handle.getOwner(),handle.getName(),handle.getDesc(),canonical))
            return new Handle(Opcodes.H_INVOKESTATIC,bridge,"create",handle.getDesc(),false);
        if(value instanceof ConstantDynamic dynamic) {
            Object[] args=new Object[dynamic.getBootstrapMethodArgumentCount()];boolean changed=false;
            for(int i=0;i<args.length;i++){Object old=dynamic.getBootstrapMethodArgument(i);args[i]=constant(old,bridge,canonical);changed|=old!=args[i];}
            if(changed)return new ConstantDynamic(dynamic.getName(),dynamic.getDescriptor(),dynamic.getBootstrapMethod(),args);
        }
        return value;
    }
}
