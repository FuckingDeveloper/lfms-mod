package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

/** Synthetic source bytecode is independent of any integration mod identity. */
public final class GameplayFamilyCheck {
    private static final String EFFECT = "Lnet/minecraft/world/effect/MobEffect;";
    private static final String INSTANCE = "Lnet/minecraft/world/effect/MobEffectInstance;";
    private static final String PLAYER = "net/minecraft/world/entity/player/Player";
    private static final String BLOCK = "net/minecraft/world/level/block/Block";
    private static final String USE = "(Lnet/minecraft/world/level/block/state/BlockState;"
            + "Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;"
            + "Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;"
            + "Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;";

    public static void main(String[] args) throws Exception {
        try { InvocationExtractionCheck.run(); }
        catch (Exception failure) { throw failure; }
        catch (Error failure) { throw failure; }
        catch (Throwable failure) { throw new AssertionError(failure); }
        com.fuckingdeveloper.lms.profile.forge1192.BlockDataExtractionCheck.run();
        com.fuckingdeveloper.lms.profile.forge1192.MethodCatalogCheck.run();
        SourceClassDomainCheck.run();
        ChatFormattingFamilyCheck.run();
        CompositionContractCheck.run();
        LoaderEvidenceCheck.run();
        DistributionFilterCheck.run();
        NativeEnergyOperationsCheck.run();
        SourceCanonicalInformationCheck.run();
        var transformer = new Forge1192LegacyClassTransformer();
        var child = node("fixture/PlayerSubclass", PLAYER);
        transformer.bindManagedClassBytes(name -> name.equals(child.name) ? bytes(child) : null);
        String[] names = {"m_21124_", "m_21023_", "m_6234_", "m_21195_", "m_19544_"};
        String[] results = {INSTANCE, "Z", INSTANCE, "Z", EFFECT};
        int queries = 0;
        for (int i = 0; i < names.length; i++) {
            String owner = i == 4 ? "net/minecraft/world/effect/MobEffectInstance" : PLAYER;
            String descriptor = (i == 4 ? "()" : "(" + EFFECT + ")") + results[i];
            for (String receiver : i == 4 ? List.of(owner) : List.of(owner, child.name)) {
                var fixture = callFixture(receiver, names[i], descriptor);
                check(pass(transformer, "rewriteLegacyMobEffectQueries", fixture) == 1, "effect query not rewritten");
                var call = (MethodInsnNode) fixture.methods.getFirst().instructions.get(2 - (i == 4 ? 1 : 0));
                check(call.getOpcode() == Opcodes.INVOKESTATIC && call.owner.endsWith("Forge1192MobEffectBridge"), "effect bridge ABI");
                verify(fixture);
                queries++;
            }
            var unrelated = callFixture("fixture/Unrelated", names[i], descriptor);
            check(pass(transformer, "rewriteLegacyMobEffectQueries", unrelated) == 0, "descriptor-only effect rewrite");
            var constant = node("fixture/Constants" + i, "java/lang/Object");
            var method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "constant", "()V", null, null);
            Handle handle = new Handle(Opcodes.H_INVOKEVIRTUAL, owner, names[i], descriptor, false);
            Handle bootstrap = new Handle(Opcodes.H_INVOKESTATIC, "fixture/Bootstrap", "constant",
                    "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;", false);
            method.instructions.add(new LdcInsnNode(new ConstantDynamic("outer", "Ljava/lang/Object;", bootstrap,
                    new ConstantDynamic("inner", "Ljava/lang/Object;", bootstrap, handle))));
            method.instructions.add(new InsnNode(Opcodes.POP)); method.instructions.add(new InsnNode(Opcodes.RETURN));
            method.maxStack = 1; constant.methods.add(method);
            check(pass(transformer, "rewriteLegacyMobEffectQueries", constant) == 1, "nested effect handle missed");
            var outer = (ConstantDynamic) ((LdcInsnNode) method.instructions.getFirst()).cst;
            var inner = (ConstantDynamic) outer.getBootstrapMethodArgument(0);
            check(((Handle) inner.getBootstrapMethodArgument(0)).getTag() == Opcodes.H_INVOKESTATIC, "nested handle ABI");
            var indy = new InvokeDynamicInsnNode("apply", "()Ljava/lang/Object;", bootstrap, handle);
            method.instructions.insertBefore(method.instructions.getFirst(), indy);
            method.instructions.insert(indy, new InsnNode(Opcodes.POP));
            check(pass(transformer, "rewriteLegacyMobEffectQueries", constant) == 1, "indy effect handle missed");
            check(((Handle) indy.bsmArgs[0]).getTag() == Opcodes.H_INVOKESTATIC, "indy handle ABI");
        }
        for (String suffix : List.of(")V", "I)V", "II)V", "IIZZ)V", "IIZZZ)V")) {
            var fixture = node("fixture/Construction", "java/lang/Object");
            var method = new MethodNode(Opcodes.ACC_STATIC, "construct", "(" + EFFECT + ")" + INSTANCE, null, null);
            method.instructions.add(new TypeInsnNode(Opcodes.NEW, "net/minecraft/world/effect/MobEffectInstance"));
            method.instructions.add(new InsnNode(Opcodes.DUP)); method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            for (Type type : Type.getArgumentTypes("(" + EFFECT + suffix).clone()) {
                if (type.getSort() != Type.OBJECT) method.instructions.add(new InsnNode(Opcodes.ICONST_1));
            }
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/world/effect/MobEffectInstance", "<init>", "(" + EFFECT + suffix, false));
            method.instructions.add(new InsnNode(Opcodes.ARETURN)); method.maxLocals = 1; method.maxStack = 10; fixture.methods.add(method);
            check(pass(transformer, "rewriteLegacyMobEffectInstanceConstruction", fixture) == 1, "effect construction missed"); verify(fixture);
        }
        var block = node("fixture/InteractiveBlock", BLOCK);
        var source = new MethodNode(Opcodes.ACC_PUBLIC, "m_6227_", USE, null, null);
        source.instructions.add(new InsnNode(Opcodes.ACONST_NULL)); source.instructions.add(new InsnNode(Opcodes.ARETURN));
        source.maxLocals = 7; source.maxStack = 1; block.methods.add(source);
        check(pass(transformer, "rewriteLegacyBlockUseCallbacks", block) == 2, "block callback family missing"); verify(block);
        var withItem = block.methods.stream().filter(m -> m.name.equals("useItemOn")).findFirst().orElseThrow();
        check(withItem.instructions.size() == 9, "block callback must return source result directly");
        for (int i = 1; i <= 6; i++) check(((VarInsnNode) withItem.instructions.get(i)).var == i + 1, "block operand order");
        check(pass(transformer, "rewriteLegacyBlockUseCallbacks", node("fixture/Unrelated", "java/lang/Object")) == 0, "non-block rewritten");
        try { pass(transformer, "rewriteLegacyBlockUseCallbacks", block); throw new AssertionError("callback collision accepted"); }
        catch (InvocationTargetException expected) { check(expected.getCause() instanceof IllegalStateException, "wrong collision failure"); }
        auditExternalArtifact(transformer);
        FollowupFamilyCheck.run(transformer);
        HierarchyEvidenceCheck.run();
        CapabilityNetworkFamilyCheck.run();
        LivingFallFamilyCheck.run();
        var map = callFixture(PLAYER, "m_21221_", "()Ljava/util/Map;");
        check(pass(transformer, "rewriteLegacyMobEffectQueries", map) == 1, "raw effect map boundary missed");
        verify(map);
        try {
            com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192MobEffectBridge.getActiveEffectsMap(null);
            throw new AssertionError("holder-key map silently exposed as raw effects");
        } catch (IllegalStateException expected) {}
        if (java.nio.file.Files.exists(java.nio.file.Path.of("legacy-mods/IC2Classic-1.19.2-2.1.3.4.jar"))) {
            var metadata = com.fuckingdeveloper.lms.target.neoforge263.Target263LegacyModListBridge.readMetadata(
                    java.nio.file.Path.of("legacy-mods/IC2Classic-1.19.2-2.1.3.4.jar"), "ic2");
            check(metadata.id().equals("ic2") && !metadata.displayName().getString().isBlank()
                    && !metadata.version().isBlank(), "admitted display metadata lost");
        }
        System.out.println("PASS: " + queries + " effect queries, inherited receivers, negative owners, indy/condy handles, five constructors, block callback operands/collisions, ASM verification");
    }

    private static void auditExternalArtifact(Forge1192LegacyClassTransformer transformer) throws Exception {
        var path = java.nio.file.Path.of("legacy-mods/IC2Classic-1.19.2-2.1.3.4.jar");
        if (!java.nio.file.Files.exists(path)) { System.out.println("External integration artifact absent; synthetic checks only"); return; }
        Map<String, byte[]> classes = new HashMap<>();
        try (var jar = new JarFile(path.toFile())) {
            for (var entry : jar.stream().filter(e -> e.getName().endsWith(".class")).toList())
                try (var input = jar.getInputStream(entry)) { classes.put(entry.getName().replace(".class", ""), input.readAllBytes()); }
        }
        transformer.bindManagedClassBytes(classes::get);
        int effects = 0, blocks = 0, constructors = 0;
        for (byte[] bytes : classes.values()) {
            var fixture = new ClassNode(); new ClassReader(bytes).accept(fixture, 0);
            effects += pass(transformer, "rewriteLegacyMobEffectQueries", fixture);
            constructors += pass(transformer, "rewriteLegacyMobEffectInstanceConstruction", fixture);
            blocks += pass(transformer, "rewriteLegacyBlockUseCallbacks", fixture);
            for (var method : fixture.methods) if ((method.access & Opcodes.ACC_SYNTHETIC) != 0 && method.name.startsWith("use"))
                new Analyzer<>(new BasicVerifier()).analyze(fixture.name, method);
        }
        check(effects > 0 && blocks > 0 && constructors > 0, "external family coverage absent");
        System.out.println("External immutable artifact family coverage: queries=" + effects + ", constructors=" + constructors + ", block callbacks/super=" + blocks);
    }

    private static ClassNode callFixture(String owner, String name, String descriptor) {
        var node = node("fixture/Caller", "java/lang/Object");
        Type[] args = Type.getArgumentTypes(descriptor);
        var method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "call", "(L" + owner + ";" + descriptor.substring(1), null, null);
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        if (args.length > 0) method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, owner, name, descriptor, false));
        method.instructions.add(new InsnNode(Type.getReturnType(descriptor).getOpcode(Opcodes.IRETURN)));
        method.maxLocals = 1 + args.length; method.maxStack = 4; node.methods.add(method); return node;
    }
    private static ClassNode node(String name, String parent) {
        var node = new ClassNode(); node.version = Opcodes.V17; node.access = Opcodes.ACC_PUBLIC;
        node.name = name; node.superName = parent; return node;
    }
    private static byte[] bytes(ClassNode node) { var writer = new ClassWriter(0); node.accept(writer); return writer.toByteArray(); }
    private static int pass(Object transformer, String name, ClassNode node) throws Exception {
        var method = Forge1192LegacyClassTransformer.class.getDeclaredMethod(name, ClassNode.class);
        method.setAccessible(true); return (int) method.invoke(transformer, node);
    }
    private static void verify(ClassNode node) throws Exception {
        for (var method : node.methods) new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
