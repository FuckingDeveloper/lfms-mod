package com.fuckingdeveloper.lms.profile.forge1192;

import com.fuckingdeveloper.lms.transform.SemanticInvocationRewriter;
import com.fuckingdeveloper.lms.runtime.blockdata.BlockDataTargetLayout;
import com.fuckingdeveloper.lms.runtime.blockdata.BlockDataTargetLayout.Operation;
import java.util.Objects;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Source block data callbacks; target entrypoints and original source defaults remain explicit. */
final class Forge1192BlockDataRewriter {
    interface Evidence {
        byte[] managedBytes(String owner) throws java.io.IOException;
        boolean subtype(ClassNode node, String ancestor);
        boolean subtype(String owner, String ancestor);
        String sourceMethod(ClassNode node, String owner, String name, String descriptor);
    }

    private final Evidence evidence;
    private final SemanticInvocationRewriter invocations;
    private final BlockDataTargetLayout target;
    private record BlockDataHierarchy(String parent, java.util.Set<String> callbacks) {}
    private final java.util.Map<String, java.util.Optional<BlockDataHierarchy>> blockDataHierarchyEvidence =
            new java.util.concurrent.ConcurrentHashMap<>();

    Forge1192BlockDataRewriter(Evidence evidence, SemanticInvocationRewriter invocations, BlockDataTargetLayout target) {
        this.evidence = Objects.requireNonNull(evidence);
        this.invocations = Objects.requireNonNull(invocations);
        this.target = Objects.requireNonNull(target);
    }

    /** Caller holds the transformer's evidence write lock. No loader is stored in this cache. */
    void clearEvidence() { blockDataHierarchyEvidence.clear(); }

    private record BlockDataMember(String sourceName, String descriptor, String alias,
            Operation operation, String bridgeName) {}
    private static BlockDataMember blockDataMember(String name, String desc) {
        if (desc.equals("(Lnet/minecraft/nbt/CompoundTag;)V")) return switch (name) {
            case "m_142466_", "load" -> new BlockDataMember(name, desc, "lms$sourceLoad", Operation.LOAD, "loadLegacy");
            case "m_183515_", "saveAdditional" -> new BlockDataMember(name, desc, "lms$sourceSave", Operation.SAVE, "saveLegacy");
            case "handleUpdateTag" -> new BlockDataMember(name, desc, "lms$sourceHandleUpdateTag", Operation.HANDLE_UPDATE_TAG, "handleUpdateTagLegacy");
            default -> null;
        };
        if ((name.equals("m_5995_") || name.equals("getUpdateTag")) && desc.equals("()Lnet/minecraft/nbt/CompoundTag;"))
            return new BlockDataMember(name, desc, "lms$sourceUpdateTag", Operation.UPDATE_TAG, "updateTagLegacy");
        return null;
    }
    private boolean inheritedSourceDataCallback(String owner, BlockDataMember member) {
        var seen = new java.util.HashSet<String>();
        while (owner != null && seen.add(owner) && !owner.startsWith("net/minecraft/")) {
            var source = blockDataHierarchyEvidence.computeIfAbsent(owner, key -> {
                try {
                    byte[] bytes = evidence.managedBytes(key);
                    if (bytes == null) return java.util.Optional.empty();
                    var node = new ClassNode(); new ClassReader(bytes).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    var callbacks = new java.util.HashSet<String>();
                    for (var method : node.methods) {
                        var candidate = blockDataMember(method.name, method.desc);
                        if (candidate != null) callbacks.add(candidate.alias());
                    }
                    return java.util.Optional.of(new BlockDataHierarchy(node.superName, java.util.Set.copyOf(callbacks)));
                } catch (java.io.IOException failure) { throw new java.io.UncheckedIOException(failure); }
            }).orElse(null);
            if (source == null) return false;
            if (source.callbacks().contains(member.alias())) return true;
            // A managed direct source root receives the verified inherited base defaults.
            if ("net/minecraft/world/level/block/entity/BlockEntity".equals(source.parent())) return true;
            owner = source.parent();
        }
        return false;
    }
    int rewrite(ClassNode node) {
        String base = "net/minecraft/world/level/block/entity/BlockEntity";
        String bridge = "com/fuckingdeveloper/lms/profile/forge1192/runtime/block/Forge1192BlockDataBridge";
        var wrappers = new java.util.ArrayList<org.objectweb.asm.tree.MethodNode>(); int rewrites = 0;
        if (base.equals(node.superName)) {
            for (String name : java.util.List.of("load", "saveAdditional", "getUpdateTag", "handleUpdateTag")) {
                String desc = name.equals("getUpdateTag") ? "()Lnet/minecraft/nbt/CompoundTag;" : "(Lnet/minecraft/nbt/CompoundTag;)V";
                var member = blockDataMember(name, desc);
                if (node.methods.stream().anyMatch(method -> {
                    var candidate = blockDataMember(method.name, method.desc);
                    return candidate != null && candidate.alias().equals(member.alias());
                })) continue;
                var inherited = new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC | org.objectweb.asm.Opcodes.ACC_SYNTHETIC, name, desc, null, null);
                inherited.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 0));
                if (!name.equals("getUpdateTag")) inherited.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 1));
                inherited.instructions.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESPECIAL, base, name, desc, false));
                inherited.instructions.add(new org.objectweb.asm.tree.InsnNode(name.equals("getUpdateTag") ? org.objectweb.asm.Opcodes.ARETURN : org.objectweb.asm.Opcodes.RETURN));
                node.methods.add(inherited);
            }
        }
        if (evidence.subtype(node, base)) for (var method : node.methods) {
            var member = blockDataMember(method.name, method.desc); if (member == null) continue;
            if (node.methods.stream().anyMatch(other -> other != method && other.name.equals(member.alias()) && other.desc.equals(method.desc)))
                throw new IllegalStateException("Source block data alias collision: " + node.name + "#" + member.alias());
            if ((method.access & org.objectweb.asm.Opcodes.ACC_STATIC) != 0)
                throw new IllegalStateException("Unadapted source block data callback shape: " + node.name + "#" + method.name);
            if (node.methods.stream().anyMatch(other -> other != method && other.name.equals(target.callback(member.operation()).name()) && other.desc.equals(target.callback(member.operation()).descriptor())))
                throw new IllegalStateException("Native/source block data callback collision: " + node.name + "#" + method.name);
            method.name = member.alias(); rewrites++;
            if (!node.interfaces.contains("com/fuckingdeveloper/lms/profile/forge1192/runtime/block/LegacyBlockDataCallbacks")) node.interfaces.add("com/fuckingdeveloper/lms/profile/forge1192/runtime/block/LegacyBlockDataCallbacks");
            if (inheritedSourceDataCallback(node.superName, member)) continue;
            String operation = switch (member.alias()) {
                case "lms$sourceLoad" -> "load"; case "lms$sourceSave" -> "save";
                case "lms$sourceUpdateTag" -> "updateTag"; default -> "handleUpdateTag";
            };
            var wrapper = new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC | org.objectweb.asm.Opcodes.ACC_SYNTHETIC,
                    target.callback(member.operation()).name(), target.callback(member.operation()).descriptor(), null, null);
            wrapper.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 0));
            wrapper.instructions.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 1));
            wrapper.instructions.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC, bridge, operation,
                    "(L" + target.baseOwner() + ";" + target.callback(member.operation()).descriptor().substring(1), false));
            wrapper.instructions.add(new org.objectweb.asm.tree.InsnNode(member.alias().equals("lms$sourceUpdateTag") ? org.objectweb.asm.Opcodes.ARETURN : org.objectweb.asm.Opcodes.RETURN));
            wrappers.add(wrapper); rewrites++;
        }
        for (var method : node.methods) for (var instruction : method.instructions) {
            if (!(instruction instanceof MethodInsnNode call) || call.getOpcode()!=org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                    || !call.desc.equals("(Lnet/minecraft/nbt/CompoundTag;)Z")
                    || !(call.name.equals("m_59631_") || call.name.equals("m_59634_"))
                    || !evidence.subtype(call.owner,"net/minecraft/world/level/block/entity/RandomizableContainerBlockEntity")) continue;
            String sourceIdentity=evidence.sourceMethod(node,call.owner,call.name,call.desc);
            String expected="net/minecraft/world/level/block/entity/RandomizableContainerBlockEntity#"+(call.name.equals("m_59631_")?"tryLoadLootTable":"trySaveLootTable");
            if(!expected.equals(sourceIdentity))throw new IllegalStateException("Unadapted source loot helper override: "+call.owner+"#"+call.name);
            call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
            call.owner="com/fuckingdeveloper/lms/profile/forge1192/runtime/container/Forge1192ContainerDataBridge";
            call.name=call.name.equals("m_59631_")?"tryLoadLootTable":"trySaveLootTable";
            call.desc="(Lnet/minecraft/world/level/block/entity/RandomizableContainerBlockEntity;Lnet/minecraft/nbt/CompoundTag;)Z";
            call.itf=false;rewrites++;
        }
        for (var method : node.methods) for (var instruction : method.instructions) {
            if (!(instruction instanceof MethodInsnNode call) || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL) continue;
            var member = blockDataMember(call.name, call.desc);
            if (member == null || !evidence.subtype(call.owner, base)) continue;
            if (!method.name.startsWith("lms$source")) throw new IllegalStateException("Source-super block data call lacks an established callback context: " + node.name + "#" + method.name);
            if (inheritedSourceDataCallback(call.owner, member)) { call.name = member.alias(); rewrites++; continue; }
            boolean containerBase = (call.owner.equals("net/minecraft/world/level/block/entity/BaseContainerBlockEntity")
                    || call.owner.equals("net/minecraft/world/level/block/entity/RandomizableContainerBlockEntity"))
                    && (member.alias().equals("lms$sourceLoad") || member.alias().equals("lms$sourceSave"));
            if (!call.owner.equals(base) && !containerBase) throw new IllegalStateException("Unadapted native subclass block data semantics: " + call.owner + "#" + call.name);
            var thunk = sourceBaseDataThunk(node, member, wrappers.size(), containerBase); wrappers.add(thunk);
            call.owner = node.name; call.name = thunk.name; call.itf = false; rewrites++;
        }
        rewrites += invocations.rewrite(node, (opcode, owner, name, desc) -> {
            var member = blockDataMember(name, desc);
            if (opcode == org.objectweb.asm.Opcodes.INVOKESPECIAL && member != null && evidence.subtype(owner, base))
                throw new IllegalStateException("Unadapted source-super block data callable reference: " + node.name + " -> " + owner + "#" + name + desc);
            return opcode == org.objectweb.asm.Opcodes.INVOKEVIRTUAL && member != null && evidence.subtype(owner, base)
                    ? new Handle(org.objectweb.asm.Opcodes.H_INVOKESTATIC, bridge, member.bridgeName(), "(L" + base + ";" + desc.substring(1), false) : null;
        });
        node.methods.addAll(wrappers); return rewrites;
    }
    private org.objectweb.asm.tree.MethodNode sourceBaseDataThunk(ClassNode node, BlockDataMember member, int ordinal, boolean containerBase) {
        String base = "net/minecraft/world/level/block/entity/BlockEntity", bridge = "com/fuckingdeveloper/lms/profile/forge1192/runtime/block/Forge1192BlockDataBridge";
        String name = "lms$baseData$" + ordinal;
        if (node.methods.stream().anyMatch(method -> method.name.equals(name))) throw new IllegalStateException("Source block data thunk collision: " + node.name);
        var thunk = new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PRIVATE | org.objectweb.asm.Opcodes.ACC_SYNTHETIC, name, member.descriptor(), null, null);
        var code = thunk.instructions;
        if (member.alias().equals("lms$sourceUpdateTag")) {
            code.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 0));
            code.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC, bridge, "baseUpdateTag", "(L" + base + ";)Lnet/minecraft/nbt/CompoundTag;", false));
            code.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ARETURN)); return thunk;
        }
        if (member.alias().equals("lms$sourceHandleUpdateTag")) {
            // Source Forge default dispatches load(tag); target default additionally replaces components.
            code.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 0));
            code.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 1));
            code.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC, bridge, "loadLegacy", "(L" + base + ";Lnet/minecraft/nbt/CompoundTag;)V", false));
            code.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.RETURN)); return thunk;
        }
        code.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 0));
        code.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 0));
        code.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 1));
        boolean save = member.alias().equals("lms$sourceSave");
        code.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC, bridge, save ? "sourceOutput" : "sourceInput",
                "(L" + target.baseOwner() + ";Lnet/minecraft/nbt/CompoundTag;)" + target.callback(member.operation()).argumentDescriptor(), false));
        code.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESPECIAL, target.baseOwner(), target.callback(member.operation()).name(), target.callback(member.operation()).descriptor(), false));
        if (!member.alias().equals("lms$sourceHandleUpdateTag")) {
            code.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 0));
            code.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 1));
            code.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC, bridge, save ? "finishSourceSave" : "finishSourceLoad", "(L" + base + ";Lnet/minecraft/nbt/CompoundTag;)V", false));
        }
        if (containerBase) {
            code.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 0));
            code.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, 1));
            code.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                    "com/fuckingdeveloper/lms/profile/forge1192/runtime/container/Forge1192ContainerDataBridge", save ? "saveBase" : "loadBase",
                    "(Lnet/minecraft/world/level/block/entity/BaseContainerBlockEntity;Lnet/minecraft/nbt/CompoundTag;)V", false));
        }
        code.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.RETURN)); return thunk;
    }

}
