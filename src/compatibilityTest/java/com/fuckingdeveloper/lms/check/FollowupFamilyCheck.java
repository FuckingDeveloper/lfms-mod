package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.mapping.Forge1192MappingLayer;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.util.List;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

final class FollowupFamilyCheck {
    private static final String LEVEL = "net/minecraft/world/level/Level";
    private static final String PREFIX = "(Lnet/minecraft/world/entity/player/Player;";
    private static final String SOUND = "Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF";

    static void run(Forge1192LegacyClassTransformer transformer) throws Exception {
        var pass = Forge1192LegacyClassTransformer.class.getDeclaredMethod("rewriteLegacyLevelSoundPlayback", ClassNode.class);
        pass.setAccessible(true);
        String[] names = {"m_5594_", "m_6263_", "m_6269_", "m_214150_", "m_213890_"};
        String[] descriptors = {PREFIX + "Lnet/minecraft/core/BlockPos;" + SOUND + ")V",
                PREFIX + "DDD" + SOUND + ")V", PREFIX + "Lnet/minecraft/world/entity/Entity;" + SOUND + ")V",
                PREFIX + "DDD" + SOUND + "J)V", PREFIX + "Lnet/minecraft/world/entity/Entity;" + SOUND + "J)V"};
        int calls = 0;
        for (int i = 0; i < names.length; i++) {
            for (String owner : List.of(LEVEL, "net/minecraft/server/level/ServerLevel", "fixture/Unrelated")) {
                var node = fixture(owner, names[i], descriptors[i]);
                int count = (int) pass.invoke(transformer, node);
                boolean unrelated = owner.equals("fixture/Unrelated");
                check(count == (unrelated ? 0 : 1), "sound receiver/member selection");
                new Analyzer<>(new BasicVerifier()).analyze(node.name, node.methods.getFirst());
                if (!unrelated) {
                    var call = (MethodInsnNode) node.methods.getFirst().instructions.get(node.methods.getFirst().instructions.size() - 2);
                    check(call.owner.endsWith("Forge1192LevelSoundBridge") && call.getOpcode() == Opcodes.INVOKESTATIC,
                            "sound adapter ABI");
                    var bridgeType = java.lang.invoke.MethodType.fromMethodDescriptorString(call.desc,
                            FollowupFamilyCheck.class.getClassLoader());
                    java.lang.invoke.MethodHandles.publicLookup().findStatic(
                            com.fuckingdeveloper.lms.profile.forge1192.runtime.render.Forge1192LevelSoundBridge.class, call.name, bridgeType);
                    linkMethodReference(transformer, pass, owner, owner, names[i], descriptors[i]);
                    if (owner.equals(LEVEL)) linkMethodReference(transformer, pass, owner,
                            "net/minecraft/server/level/ServerLevel", names[i], descriptors[i]);
                    calls++;
                }
            }
            var node = fixture(LEVEL, names[i], descriptors[i]);
            var method = node.methods.getFirst();
            Handle handle = new Handle(Opcodes.H_INVOKEVIRTUAL, LEVEL, names[i], descriptors[i], false);
            Handle bootstrap = new Handle(Opcodes.H_INVOKESTATIC, "fixture/Bootstrap", "constant",
                    "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;", false);
            var constant = new LdcInsnNode(new ConstantDynamic("outer", "Ljava/lang/Object;", bootstrap,
                    new ConstantDynamic("inner", "Ljava/lang/Object;", bootstrap, handle)));
            method.instructions.insertBefore(method.instructions.getFirst(), constant);
            method.instructions.insert(constant, new InsnNode(Opcodes.POP));
            var indy = new InvokeDynamicInsnNode("sound", "()Ljava/lang/Object;", bootstrap, handle);
            method.instructions.insertBefore(method.instructions.getFirst(), indy);
            method.instructions.insert(indy, new InsnNode(Opcodes.POP));
            check((int) pass.invoke(transformer, node) == 3, "sound direct/indy/condy family coverage");
            check(((Handle) indy.bsmArgs[0]).getTag() == Opcodes.H_INVOKESTATIC, "sound indy handle");
            var inner = (ConstantDynamic) ((ConstantDynamic) constant.cst).getBootstrapMethodArgument(0);
            check(((Handle) inner.getBootstrapMethodArgument(0)).getTag() == Opcodes.H_INVOKESTATIC, "sound condy handle");
            new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
        }
        var effects = Forge1192LegacyClassTransformer.class.getDeclaredMethod("rewriteLegacyMobEffectQueries", ClassNode.class);
        effects.setAccessible(true);
        linkMethodReference(transformer, effects, "net/minecraft/world/entity/player/Player",
                "net/minecraft/server/level/ServerPlayer", "m_21124_",
                "(Lnet/minecraft/world/effect/MobEffect;)Lnet/minecraft/world/effect/MobEffectInstance;");
        linkMethodReference(transformer, effects, "net/minecraft/server/level/ServerPlayer",
                "net/minecraft/server/level/ServerPlayer", "m_21023_",
                "(Lnet/minecraft/world/effect/MobEffect;)Z");
        Class<?> player = net.minecraft.server.level.ServerPlayer.class;
        check(java.lang.reflect.Modifier.isPublic(player.getDeclaredField("containerCounter").getModifiers()), "menu counter visibility");
        check(java.lang.reflect.Modifier.isPublic(player.getDeclaredMethod("nextContainerCounter").getModifiers()), "menu allocation visibility");
        check(java.lang.reflect.Modifier.isPublic(player.getDeclaredMethod("initMenu", net.minecraft.world.inventory.AbstractContainerMenu.class).getModifiers()), "menu sync visibility");

        var mapping = new Forge1192MappingLayer();
        var index = mapping.runtimeIndex();
        String owner = "net.minecraft.world.level.Level";
        check(mapping.methodsOf(owner).equals(index.methods().stream().filter(m -> m.owner().equals(owner)).toList()), "owner index changes evidence");
        var expectedFields = index.fields().stream().filter(f -> f.owner().equals("net.minecraft.server.level.ServerPlayer")
                && f.name().equals("containerCounter") && f.descriptor().equals("I")).toList();
        check(mapping.searchExactField("net/minecraft/server/level/ServerPlayer", "containerCounter", "I").equals(expectedFields), "field index changes evidence");
        var expectedSound = index.methods().stream().filter(m -> m.name().equals("playSound"))
                .map(m -> new Forge1192MappingLayer.MethodCandidate(m.owner() + "#" + m.name() + m.descriptor(), m.descriptor())).toList();
        check(mapping.searchRuntimeByName("playSound").candidates().equals(expectedSound), "name index loses ambiguity/order");
        var named = mapping.searchNamedMethods(owner, "playSound");
        check(named == mapping.searchNamedMethods(owner, "playSound") && named.candidates().size() > 1, "named lookup cache loses overload evidence");
        var absent = mapping.searchNamedMethods("fixture.Absent", "noMethod");
        check(absent.candidates().isEmpty() && absent == mapping.searchNamedMethods("fixture.Absent", "noMethod"), "negative lookup caching");
        int soundSites = auditExternalSound(transformer, pass);
        checkBlockedSession();
        System.out.println("PASS: " + calls + " sound overload/inherited receiver checks, negative owners, indy/condy, bridge linkage, three actual target menu accessors, indexed evidence/order/negative caches; external sound sites=" + soundSites);
    }

    private static void linkMethodReference(Object transformer, java.lang.reflect.Method pass,
            String sourceOwner, String capturedOwner, String name, String descriptor) throws Exception {
        var node = new ClassNode(); node.version = Opcodes.V17; node.access = Opcodes.ACC_PUBLIC;
        node.name = "fixture/BoundReference"; node.superName = "java/lang/Object";
        String bindDescriptor = "(L" + capturedOwner + ";"
                + descriptor.substring(1, descriptor.indexOf(')')) + ")Ljava/lang/Runnable;";
        var method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "bind", bindDescriptor, null, null);
        int local = 0;
        for (Type argument : Type.getArgumentTypes(bindDescriptor)) {
            method.instructions.add(new VarInsnNode(argument.getOpcode(Opcodes.ILOAD), local)); local += argument.getSize();
        }
        var metafactory = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;"
                        + "Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false);
        method.instructions.add(new InvokeDynamicInsnNode("run", bindDescriptor, metafactory,
                Type.getMethodType("()V"), new Handle(Opcodes.H_INVOKEVIRTUAL, sourceOwner, name, descriptor, false),
                Type.getMethodType("()V")));
        method.instructions.add(new InsnNode(Opcodes.ARETURN)); method.maxLocals = local; method.maxStack = local;
        node.methods.add(method);
        check((int) pass.invoke(transformer, node) == 1, "method reference missed");
        var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); node.accept(writer); byte[] bytes = writer.toByteArray();
        class Loader extends ClassLoader {
            Loader() { super(FollowupFamilyCheck.class.getClassLoader()); }
            Class<?> define() { return defineClass(node.name.replace('/', '.'), bytes, 0, bytes.length); }
        }
        var generated = new Loader().define();
        var bindType = java.lang.invoke.MethodType.fromMethodDescriptorString(bindDescriptor, generated.getClassLoader());
        var captured = new java.util.ArrayList<Object>();
        for (Class<?> parameter : bindType.parameterArray()) captured.add(
                parameter == double.class ? (Object) Double.valueOf(1)
                        : parameter == float.class ? (Object) Float.valueOf(1)
                        : parameter == long.class ? (Object) Long.valueOf(1) : null);
        try {
            var bound = java.lang.invoke.MethodHandles.publicLookup().findStatic(generated, "bind", bindType)
                    .invokeWithArguments(captured);
            check(bound instanceof Runnable, "source receiver capture did not link");
        } catch (Throwable failure) { throw new AssertionError("Transformed method-reference capture ABI", failure); }
    }

    private static void checkBlockedSession() throws Exception {
        var root = java.nio.file.Files.createTempDirectory("lms-blocked-session-check");
        var artifact = root.resolve("immutable-fixture.jar");
        var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "fixture/Entrypoint", null, "java/lang/Object", null);
        var constructor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode(); constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitInsn(Opcodes.RETURN); constructor.visitMaxs(0, 0); constructor.visitEnd(); writer.visitEnd();
        try (var zip = new java.util.jar.JarOutputStream(java.nio.file.Files.newOutputStream(artifact))) {
            zip.putNextEntry(new java.util.zip.ZipEntry("fixture/Entrypoint.class")); zip.write(writer.toByteArray()); zip.closeEntry();
            zip.putNextEntry(new java.util.zip.ZipEntry("META-INF/mods.toml"));
            zip.write("license='test'\n[[mods]]\nmodId='fixture'\ndisplayName='Fixture'\nversion='1'\n".getBytes(java.nio.charset.StandardCharsets.UTF_8)); zip.closeEntry();
        }
        byte[] original = java.nio.file.Files.readAllBytes(artifact);
        try (var session = new com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192RuntimeSession("fixture", artifact,
                FollowupFamilyCheck.class.getClassLoader(), List.of("fixture.Entrypoint"))) {
            try {
                com.fuckingdeveloper.lms.target.neoforge263.Target263LegacyModListBridge.displayInfo(session);
                throw new AssertionError("linked candidate presented as loaded");
            } catch (IllegalStateException expected) {}
            session.blockInitialization("required-api-family");
            check(session.state() == com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192RuntimeSession.State.BLOCKED,
                    "gate rejection not recorded in session");
            var display = com.fuckingdeveloper.lms.target.neoforge263.Target263LegacyModListBridge.displayInfo(session);
            check(display.id().equals("fixture") && display.description().getString().contains("required-api-family")
                    && !display.displayName().getString().equals("Fixture"), "blocked status/reason absent from display");
            try { session.initialize(); throw new AssertionError("blocked session initialized"); }
            catch (IllegalStateException expected) {}
        }
        check(java.util.Arrays.equals(original, java.nio.file.Files.readAllBytes(artifact)), "source fixture was modified");
    }

    private static int auditExternalSound(Object transformer, java.lang.reflect.Method pass) throws Exception {
        var path = java.nio.file.Path.of("legacy-mods/IC2Classic-1.19.2-2.1.3.4.jar");
        if (!java.nio.file.Files.exists(path)) return 0;
        int count = 0;
        try (var jar = new java.util.jar.JarFile(path.toFile())) {
            for (var entry : jar.stream().filter(e -> e.getName().endsWith(".class")).toList()) {
                var node = new ClassNode();
                try (var input = jar.getInputStream(entry)) { new ClassReader(input).accept(node, 0); }
                count += (int) pass.invoke(transformer, node);
            }
        }
        check(count > 0, "external sound family missed"); return count;
    }

    private static ClassNode fixture(String owner, String name, String descriptor) {
        var node = new ClassNode(); node.name = "fixture/SoundCaller"; node.superName = "java/lang/Object";
        node.version = Opcodes.V17; node.access = Opcodes.ACC_PUBLIC;
        var method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "call",
                "(L" + owner + ";" + descriptor.substring(1), null, null);
        int local = 0;
        for (Type arg : Type.getArgumentTypes(method.desc)) {
            method.instructions.add(new VarInsnNode(arg.getOpcode(Opcodes.ILOAD), local)); local += arg.getSize();
        }
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, owner, name, descriptor, false));
        method.instructions.add(new InsnNode(Opcodes.RETURN)); method.maxLocals = local; method.maxStack = local + 2;
        node.methods.add(method); return node;
    }
    private static void check(boolean value, String reason) { if (!value) throw new AssertionError(reason); }
}
