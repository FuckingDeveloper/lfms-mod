package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.block.Forge1192BlockDataBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.block.LegacyBlockDataCallbacks;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.capability.LegacyCapability;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.capability.LegacyICapabilityProvider;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.capability.LegacyINBTSerializable;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.capability.LegacyLazyOptional;
import com.fuckingdeveloper.lms.target.neoforge263.Target263DataComponentBindingBridge;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;


import com.fuckingdeveloper.lms.profile.forge1192.runtime.event.LegacyAttachCapabilitiesEvent;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Independent legacy callback bytecode, exercised by actual native storage APIs. */
public final class BlockDataFamilyCheck {
    private static final String BASE = "net/minecraft/world/level/block/entity/BlockEntity";
    private static final String TAG = "net/minecraft/nbt/CompoundTag";
    private static final String TAG_ARG = "(L" + TAG + ";)V";
    private static final List<String> ORDER = new ArrayList<>();
    private static final HolderLookup.Provider LOOKUP = RegistryAccess.EMPTY;
    private static Runnable nested;
    public static net.minecraft.world.level.block.entity.BlockEntityType<?> fixtureType() {
        return net.minecraft.world.level.block.entity.BlockEntityTypes.CHEST;
    }
    public static void observe(BlockEntity entity, CompoundTag tag, String phase) {
        check(Forge1192BlockDataBridge.currentLookup().orElseThrow() == LOOKUP, "native lookup context replaced");
        check(Target263DataComponentBindingBridge.currentRegistries().orElseThrow() == LOOKUP, "source codec cannot see native lookup context");
        ORDER.add(phase);
        if (nested != null) { var action = nested; nested = null; action.run(); }
    }
    static final class Provider implements LegacyICapabilityProvider, LegacyINBTSerializable<CompoundTag> {
        int value = 19, saves, loads;
        public <T> LegacyLazyOptional<T> getCapability(LegacyCapability<T> cap, Direction side) { return LegacyLazyOptional.empty(); }
        public CompoundTag serializeNBT() { saves++; var tag = new CompoundTag(); tag.putInt("value", value); return tag; }
        public void deserializeNBT(CompoundTag tag) { loads++; value = tag.getIntOr("value", -1); }
    }
    public static void run() throws Exception {
        var parent = fixture("fixture/SourceDataParent", BASE, false);
        var child = fixture("fixture/SourceDataChild", parent.name, true);
        var partial = fixture("fixture/SourceDataPartial", BASE, false);
        partial.methods.removeIf(method -> !method.name.equals("<init>") && !method.name.equals("handleUpdateTag") && !method.name.equals("unusedRenderer"));
        var partialChild = fixture("fixture/SourceDataPartialChild", partial.name, true);
        partialChild.methods.removeIf(method -> !method.name.equals("<init>") && !method.name.equals("m_142466_") && !method.name.equals("unusedRenderer"));
        var path = Files.createTempFile("lms-block-data-source", ".jar");
        try (var jar = new JarOutputStream(Files.newOutputStream(path))) {
            for (var node : List.of(parent, child, partial, partialChild)) { jar.putNextEntry(new JarEntry(node.name + ".class")); jar.write(bytes(node)); jar.closeEntry(); }
        }
        byte[] immutable = Files.readAllBytes(path);
        Map<BlockEntity, Provider> providers = new IdentityHashMap<>();
        java.util.function.Consumer<LegacyAttachCapabilitiesEvent> gather = event -> {
            if (!(event.getObject() instanceof BlockEntity receiver) || !receiver.getClass().getName().startsWith("fixture.SourceData")) return;
            var provider = new Provider(); providers.put(receiver, provider);
            event.addCapability(Identifier.fromNamespaceAndPath("source_data_check", "probe"), provider);
        };
        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, LegacyAttachCapabilitiesEvent.class, gather);
        try (var loader = new ManagedLegacyClassLoader(path, BlockDataFamilyCheck.class.getClassLoader(), new Forge1192LegacyClassTransformer())) {
            var type = loader.linkOwnedClass(child.name.replace('/', '.')); var entity = (BlockEntity)type.getConstructor().newInstance();
            check(entity instanceof LegacyBlockDataCallbacks, "source callback marker missing");
            try { type.getDeclaredMethods(); throw new AssertionError("fixture utility signature linked"); }
            catch (NoClassDefFoundError expected) {}
            type.getField("connections").setInt(entity, 37);
            entity.getPersistentData().putInt("persisted", 11);
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, LOOKUP);
            output.putByte("unknown", (byte)4); output.putString("NeoForgeData", "source-owned opaque value");
            ORDER.clear(); entity.saveCustomOnly(output); var saved = output.buildResult();
            check(ORDER.equals(List.of("save-parent", "save-child")), "source save dispatch/super order duplicated");
            check(saved.getIntOr("connections", 0) == 37 && saved.getIntOr("child", 0) == 17, "native save never entered source callbacks");
            check(saved.get("unknown") instanceof ByteTag && saved.getStringOr("NeoForgeData", "").equals("source-owned opaque value"), "unknown/source-owned NBT keys changed");
            check(saved.getCompoundOrEmpty("ForgeData").getIntOr("persisted", 0) == 11 && providers.get(entity).saves == 1, "source base persistence/capabilities not serialized exactly once");
            var loaded = (BlockEntity)type.getConstructor().newInstance(); ORDER.clear();
            loaded.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, LOOKUP, saved));
            check(ORDER.equals(List.of("load-parent", "load-child")) && type.getField("connections").getInt(loaded) == 37, "native load source dispatch/state not restored");
            var sourceTag = (CompoundTag)type.getField("lastTag").get(loaded);
            check(loaded.getPersistentData() == sourceTag.getCompoundOrEmpty("ForgeData"), "source base persistent tag identity lost");
            check(providers.get(loaded).loads == 1 && providers.get(loaded).value == 19, "source capability load duplicated/lost");
            ORDER.clear(); var update = entity.getUpdateTag(LOOKUP);
            check(update.getIntOr("network", 0) == 1037 && update.getIntOr("childPacket", 0) == 23 && !update.contains("connections"), "initial network data replaced with persistence output");
            check(ORDER.equals(List.of("update-parent", "update-child")), "source update tag dispatch duplicated");
            ORDER.clear(); loaded.handleUpdateTag(TagValueInput.create(ProblemReporter.DISCARDING, LOOKUP, update));
            check(type.getField("connections").getInt(loaded) == 37 && ORDER.equals(List.of("load-parent", "load-child", "handle-parent", "handle-child")), "initial update handling lost source super/load/network order");
            check(!Forge1192BlockDataBridge.dataContextActive(), "source data context leaked");
            // Failure must restore the previous scoped registry context, not retain the failed tag.
            type.getField("fail").setBoolean(entity, true);
            try { entity.saveCustomOnly(TagValueOutput.createWithContext(ProblemReporter.DISCARDING, LOOKUP)); throw new AssertionError("source save failure swallowed"); }
            catch (IllegalStateException expected) { check(expected.getMessage().equals("source save failure"), "source error changed"); }
            check(!Forge1192BlockDataBridge.dataContextActive(), "failed callback retained native lookup/output scope");

            var partialType = loader.linkOwnedClass(partialChild.name.replace('/', '.'));
            var partialEntity = (BlockEntity)partialType.getConstructor().newInstance();
            partialEntity.getPersistentData().putInt("persisted", 29);
            var partialOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, LOOKUP);
            partialEntity.saveCustomOnly(partialOutput);
            var partialSaved = partialOutput.buildResult();
            check(partialSaved.getCompoundOrEmpty("ForgeData").getIntOr("persisted", 0) == 29 && providers.get(partialEntity).saves == 1, "inherited source base save lost persistence/capabilities");
            ORDER.clear(); partialEntity.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, LOOKUP, partialSaved));
            check(ORDER.equals(List.of("load-child")) && providers.get(partialEntity).loads == 1, "descendant override above inherited base default dispatched twice");
            check(partialEntity.getUpdateTag(LOOKUP).isEmpty(), "inherited base update tag is not empty");
            var direct = new CompoundTag(); direct.putInt("network", 1012);
            var persisted = new CompoundTag(); persisted.putInt("persisted", 33); direct.put("ForgeData", persisted);
            nested = () -> {
                var components = net.minecraft.core.component.DataComponentMap.builder().set(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE, 9).build();
                partialEntity.setComponents(components);
                ORDER.clear(); Forge1192BlockDataBridge.handleUpdateTagLegacy(partialEntity, direct);
                check(ORDER.equals(List.of("load-child", "handle-parent")) && partialEntity.getPersistentData() == persisted, "partial source handle/load defaults lost direct tag identity/order");
                check(partialEntity.components() == components, "source handle default applied target component replacement");
                var previousPersistent = partialEntity.getPersistentData();
                var opaque = new CompoundTag(); var opaqueData = new CompoundTag(); opaqueData.putInt("unrelated", 1); opaque.put("NeoForgeData", opaqueData);
                Forge1192BlockDataBridge.loadLegacy(partialEntity, opaque);
                check(partialEntity.getPersistentData() == previousPersistent, "opaque source NeoForgeData changed persistent ownership");
            };
            type.getField("fail").setBoolean(entity, false);
            try { entity.getUpdateTag(LOOKUP); } finally { nested = null; }
            check(!Forge1192BlockDataBridge.dataContextActive(), "nested direct source callback leaked registry context");
        } finally { NeoForge.EVENT_BUS.unregister(gather); }
        check(Arrays.equals(immutable, Files.readAllBytes(path)), "legacy input JAR changed");
        var unsupported = fixture("fixture/UnsupportedSourceSuperHandle", BASE, false);
        var handle = new MethodNode(Opcodes.ACC_PUBLIC, "superReference", "()V", null, null);
        handle.instructions.add(new LdcInsnNode(new Handle(Opcodes.H_INVOKESPECIAL, BASE, "m_142466_", TAG_ARG, false)));
        handle.instructions.add(new InsnNode(Opcodes.POP)); handle.instructions.add(new InsnNode(Opcodes.RETURN)); unsupported.methods.add(handle);
        try { new Forge1192LegacyClassTransformer().transform(unsupported.name.replace('/', '.'), bytes(unsupported)); throw new AssertionError("unsupported source-super handle accepted"); }
        catch (IllegalStateException expected) { check(expected.getMessage().contains("source-super block data callable reference"), "source-super handle error is not attributable"); }
        System.out.println("PASS: actual FML native save/load/initial update APIs, source inheritance/super order, unrelated absent utility types, NBT key/type/tag identity, exact lookup context, once-only capability persistence, source network data and failure-scope restoration");
    }
    private static ClassNode fixture(String name, String parent, boolean child) {
        var node = new ClassNode(); node.version = Opcodes.V17; node.access = Opcodes.ACC_PUBLIC; node.name = name; node.superName = parent;
        if (!child) for (var field : List.of(new FieldNode(Opcodes.ACC_PUBLIC, "connections", "I", null, null), new FieldNode(Opcodes.ACC_PUBLIC, "fail", "Z", null, null), new FieldNode(Opcodes.ACC_PUBLIC, "lastTag", "L" + TAG + ";", null, null))) node.fields.add(field);
        var constructor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null); var code = constructor.instructions;
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        if (!child) {
            code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(BlockDataFamilyCheck.class), "fixtureType", "()Lnet/minecraft/world/level/block/entity/BlockEntityType;", false));
            code.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/core/BlockPos", "ZERO", "Lnet/minecraft/core/BlockPos;"));
            code.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/world/level/block/Blocks", "CHEST", "Lnet/minecraft/world/level/block/Block;"));
            code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/level/block/Block", "defaultBlockState", "()Lnet/minecraft/world/level/block/state/BlockState;", false));
        }
        code.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, parent, "<init>", child ? "()V" : "(Lnet/minecraft/world/level/block/entity/BlockEntityType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", false)); code.add(new InsnNode(Opcodes.RETURN)); node.methods.add(constructor);
        for (String callback : List.of("m_142466_", "m_183515_", "handleUpdateTag", "m_5995_")) {
            boolean update = callback.equals("m_5995_"), save = callback.equals("m_183515_");
            var method = new MethodNode(save ? Opcodes.ACC_PROTECTED : Opcodes.ACC_PUBLIC, callback, update ? "()L" + TAG + ";" : TAG_ARG, null, null); var body = method.instructions;
            body.add(new VarInsnNode(Opcodes.ALOAD, 0)); if (!update) body.add(new VarInsnNode(Opcodes.ALOAD, 1));
            body.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, parent, callback, method.desc, false));
            if (update) body.add(new VarInsnNode(Opcodes.ASTORE, 1));
            if (save) {
                if (!child) {
                    body.add(new VarInsnNode(Opcodes.ALOAD, 0)); body.add(new FieldInsnNode(Opcodes.GETFIELD, name, "fail", "Z")); var normal = new LabelNode(); body.add(new JumpInsnNode(Opcodes.IFEQ, normal));
                    body.add(new TypeInsnNode(Opcodes.NEW, "java/lang/IllegalStateException")); body.add(new InsnNode(Opcodes.DUP)); body.add(new LdcInsnNode("source save failure")); body.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;)V", false)); body.add(new InsnNode(Opcodes.ATHROW)); body.add(normal);
                }
                putInt(body, child ? "child" : "connections", child ? 17 : null, name);
            } else if (update) { putInt(body, child ? "childPacket" : "network", child ? 23 : null, name, child ? 0 : 1000); }
            else if (!child) {
                body.add(new VarInsnNode(Opcodes.ALOAD, 0)); body.add(new VarInsnNode(Opcodes.ALOAD, 1)); body.add(new LdcInsnNode(callback.equals("handleUpdateTag") ? "network" : "connections"));
                body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, TAG, "m_128451_", "(Ljava/lang/String;)I", false));
                if (callback.equals("handleUpdateTag")) { body.add(new LdcInsnNode(1000)); body.add(new InsnNode(Opcodes.ISUB)); }
                body.add(new FieldInsnNode(Opcodes.PUTFIELD, name, "connections", "I"));
                body.add(new VarInsnNode(Opcodes.ALOAD, 0)); body.add(new VarInsnNode(Opcodes.ALOAD, 1)); body.add(new FieldInsnNode(Opcodes.PUTFIELD, name, "lastTag", "L" + TAG + ";"));
            }
            String phase = update ? "update" : save ? "save" : callback.equals("handleUpdateTag") ? "handle" : "load";
            body.add(new VarInsnNode(Opcodes.ALOAD, 0)); body.add(new VarInsnNode(Opcodes.ALOAD, 1)); body.add(new LdcInsnNode(phase + (child ? "-child" : "-parent")));
            body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(BlockDataFamilyCheck.class), "observe", "(L" + BASE + ";L" + TAG + ";Ljava/lang/String;)V", false));
            if (update) body.add(new VarInsnNode(Opcodes.ALOAD, 1)); body.add(new InsnNode(update ? Opcodes.ARETURN : Opcodes.RETURN)); node.methods.add(method);
        }
        var unused = new MethodNode(Opcodes.ACC_PUBLIC, "unusedRenderer", "(Lnet/minecraft/client/renderer/entity/ItemRenderer;)V", null, null); unused.instructions.add(new InsnNode(Opcodes.RETURN)); node.methods.add(unused); return node;
    }
    private static void putInt(InsnList code, String key, Integer value, String owner) { putInt(code, key, value, owner, 0); }
    private static void putInt(InsnList code, String key, Integer value, String owner, int add) {
        code.add(new VarInsnNode(Opcodes.ALOAD, 1)); code.add(new LdcInsnNode(key));
        if (value != null) code.add(new LdcInsnNode(value)); else { code.add(new VarInsnNode(Opcodes.ALOAD, 0)); code.add(new FieldInsnNode(Opcodes.GETFIELD, owner, "connections", "I")); if (add != 0) { code.add(new LdcInsnNode(add)); code.add(new InsnNode(Opcodes.IADD)); } }
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, TAG, "m_128405_", "(Ljava/lang/String;I)V", false));
    }
    private static byte[] bytes(ClassNode node) { var writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS); node.accept(writer); return writer.toByteArray(); }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
