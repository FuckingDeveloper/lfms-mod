package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.runtime.Forge1192DamageSourceBridge;
import java.lang.invoke.MethodHandle;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.*;
import net.minecraft.world.damagesource.*;
import org.objectweb.asm.*;

final class DamageDescriptorFamilyCheck {
    private static final String ROOT = "net/minecraft/world/damagesource/DamageSource";
    static void run() throws Exception {
        var descriptor = Forge1192DamageSourceBridge.create("fixture.damage");
        check(descriptor.getMsgId().equals("fixture.damage") && descriptor.getFoodExhaustion() == 0.1F
                && !descriptor.scalesWithDifficulty() && descriptor.getEntity() == null
                && descriptor.getDirectEntity() == null, "source constructor metadata changed");
        var names = List.of("m_19360_", "m_19372_", "m_19376_", "m_146705_", "m_19378_",
                "m_19379_", "m_238340_", "m_19384_", "m_146707_", "m_181121_", "m_19387_");
        var tags = List.of(DamageTypeTags.IS_PROJECTILE, DamageTypeTags.IS_EXPLOSION,
                DamageTypeTags.BYPASSES_ARMOR, DamageTypeTags.DAMAGES_HELMET,
                DamageTypeTags.BYPASSES_INVULNERABILITY, DamageTypeTags.BYPASSES_EFFECTS,
                DamageTypeTags.BYPASSES_ENCHANTMENTS, DamageTypeTags.IS_FIRE,
                DamageTypeTags.IS_FALL, DamageTypeTags.NO_ANGER, DamageTypeTags.WITCH_RESISTANT_TO);
        var registry = new MappedRegistry<DamageType>(Registries.DAMAGE_TYPE, com.mojang.serialization.Lifecycle.stable());
        var holder = registry.register(DamageTypes.GENERIC, new DamageType("native", 0.3F), RegistrationInfo.BUILT_IN);
        registry.freeze();
        Map<TagKey<DamageType>, List<Holder<DamageType>>> bindings = new HashMap<>();
        var nativeSource = new DamageSource(holder);
        for (int selected = 0; selected < tags.size(); selected++) {
            bindings.clear(); bindings.put(tags.get(selected), List.of(holder));
            registry.prepareTagReload(new TagLoader.LoadResult<>(Registries.DAMAGE_TYPE, bindings)).apply();
            for (int query = 0; query < names.size(); query++) {
                String name = names.get(query);
                check(Forge1192DamageSourceBridge.query(nativeSource, name) == (query == selected), "native damage categories conflated: " + name);
                check(!Forge1192DamageSourceBridge.query(descriptor, name), "source default flag invented: " + name);
            }
        }
        var access = new RegistryAccess.ImmutableRegistryAccess(List.of(registry));
        var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), access);
        try {
            DamageType.STREAM_CODEC.encode(buffer, holder);
            check(DamageType.STREAM_CODEC.decode(buffer) == holder, "genuine registered damage codec changed");
            try {
                DamageType.STREAM_CODEC.encode(buffer, descriptor.typeHolder());
                throw new AssertionError("direct descriptor advertised as registry-synced custom damage");
            } catch (IllegalArgumentException expected) { /* Native registry-only boundary. */ }
        } finally { buffer.release(); }
        Path fixture = Files.createTempFile("lms-damage-descriptor-", ".jar");
        try {
            try (var jar = new JarOutputStream(Files.newOutputStream(fixture))) {
                jar.putNextEntry(new JarEntry("fixture/Damage.class")); jar.write(fixture()); jar.closeEntry();
            }
            try (var loader = new ManagedLegacyClassLoader(fixture, DamageDescriptorFamilyCheck.class.getClassLoader(), new Forge1192LegacyClassTransformer())) {
                var type = loader.loadClass("fixture.Damage");
                var source = (DamageSource) type.getConstructor(String.class).newInstance("subclass.damage");
                check(source.getMsgId().equals("subclass.damage"), "source subclass constructor failed");
                check((boolean) type.getMethod("query", DamageSource.class).invoke(null, source), "source virtual override lost");
                check(!(boolean) type.getMethod("base").invoke(source), "source super query recursed into override");
                var handle = (MethodHandle) type.getMethod("constructor").invoke(null);
                try {
                    var viaHandle = (DamageSource) handle.invoke("handle.damage");
                    check(viaHandle.getMsgId().equals("handle.damage"), "constructor reference lost message identity");
                    var queryHandle = (MethodHandle) type.getMethod("queryHandle").invoke(null);
                    check((boolean) queryHandle.invoke(source), "query handle lost source virtual override");
                } catch (Throwable failure) { throw new AssertionError(failure); }
            }
        } finally { Files.deleteIfExists(fixture); }
        Path ic2 = Path.of("legacy-mods/IC2Classic-1.19.2-2.1.3.4.jar");
        if (Files.isRegularFile(ic2)) try (var loader = new ManagedLegacyClassLoader(ic2, DamageDescriptorFamilyCheck.class.getClassLoader(), new Forge1192LegacyClassTransformer())) {
            inventory(ic2, names);
            var type = loader.loadClass("ic2.api.util.IC2DamageSource");
            for (String name : List.of("ELECTRICITY", "NUKE", "RADIATION"))
                check(type.getField(name).get(null) instanceof DamageSource, "immutable source descriptor initialization failed");
        }
        System.out.println("PASS: actual source subclass/default descriptor initialization, eleven independent native tag queries, virtual/super dispatch, constructor/query handles and native registry-only synchronization boundary");
    }
    private static void inventory(Path artifact, List<String> names) throws Exception {
        var found = new TreeSet<String>();
        try (var jar = new JarFile(artifact.toFile())) {
            for (var entry : jar.stream().filter(e -> e.getName().endsWith(".class")).toList()) {
                var node = new org.objectweb.asm.tree.ClassNode();
                try (var input = jar.getInputStream(entry)) { new ClassReader(input.readAllBytes()).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES); }
                for (var method : node.methods) for (var instruction : method.instructions)
                    if (instruction instanceof org.objectweb.asm.tree.MethodInsnNode call && call.owner.equals(ROOT) && call.desc.equals("()Z")) found.add(call.name);
            }
        }
        var unsupported = new TreeSet<>(found); unsupported.removeAll(names);
        unsupported.remove("m_19390_"); // Existing native isCreativePlayer remains direct.
        check(unsupported.isEmpty(), "uncovered immutable source damage-query family: " + unsupported);
        System.out.println("DAMAGE-QUERY-INVENTORY: " + found);
    }
    private static byte[] fixture() {
        var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "fixture/Damage", null, ROOT, null);
        var ctor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Ljava/lang/String;)V", null, null); ctor.visitCode();
        ctor.visitVarInsn(Opcodes.ALOAD, 0); ctor.visitVarInsn(Opcodes.ALOAD, 1);
        ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, ROOT, "<init>", "(Ljava/lang/String;)V", false);
        ctor.visitInsn(Opcodes.RETURN); ctor.visitMaxs(0, 0); ctor.visitEnd();
        var override = writer.visitMethod(Opcodes.ACC_PUBLIC, "m_19376_", "()Z", null, null); override.visitCode();
        override.visitInsn(Opcodes.ICONST_1); override.visitInsn(Opcodes.IRETURN); override.visitMaxs(0, 0); override.visitEnd();
        var base = writer.visitMethod(Opcodes.ACC_PUBLIC, "base", "()Z", null, null); base.visitCode();
        base.visitVarInsn(Opcodes.ALOAD, 0); base.visitMethodInsn(Opcodes.INVOKESPECIAL, ROOT, "m_19376_", "()Z", false);
        base.visitInsn(Opcodes.IRETURN); base.visitMaxs(0, 0); base.visitEnd();
        var query = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "query", "(L" + ROOT + ";)Z", null, null); query.visitCode();
        query.visitVarInsn(Opcodes.ALOAD, 0); query.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ROOT, "m_19376_", "()Z", false);
        query.visitInsn(Opcodes.IRETURN); query.visitMaxs(0, 0); query.visitEnd();
        for (var name : List.of("constructor", "queryHandle")) {
            var method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, name, "()Ljava/lang/invoke/MethodHandle;", null, null); method.visitCode();
            method.visitLdcInsn(name.equals("constructor")
                    ? new Handle(Opcodes.H_NEWINVOKESPECIAL, ROOT, "<init>", "(Ljava/lang/String;)V", false)
                    : new Handle(Opcodes.H_INVOKEVIRTUAL, ROOT, "m_19376_", "()Z", false));
            method.visitInsn(Opcodes.ARETURN); method.visitMaxs(0, 0); method.visitEnd();
        }
        writer.visitEnd(); return writer.toByteArray();
    }
    private static void check(boolean value, String reason) { if (!value) throw new AssertionError(reason); }
}
