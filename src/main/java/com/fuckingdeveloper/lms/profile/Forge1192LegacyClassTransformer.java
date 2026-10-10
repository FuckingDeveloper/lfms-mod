package com.fuckingdeveloper.lms.profile;

import com.fuckingdeveloper.lms.classloading.LegacyClassTransformer;
import com.fuckingdeveloper.lms.mapping.Forge1192MappingLayer;
import com.fuckingdeveloper.lms.mapping.Forge1192SrgRuntimeResolver;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.Handle;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.Type;

/**
 * Forge 1.19.2 legacy-owned class transformation pipeline.
 *
 * <p>The initial implementation is intentionally semantic-identity: it parses
 * every managed class through ASM and emits verified class bytes. Profile
 * rewrite passes are added here, before definition, rather than inside the
 * classloader. This establishes the mandatory transformation boundary without
 * claiming that removed Forge APIs have already been adapted.</p>
 */
public final class Forge1192LegacyClassTransformer implements LegacyClassTransformer {
    private static final Forge1192MappingLayer VANILLA_RELOCATIONS = new Forge1192MappingLayer();
    private static final java.util.Optional<Forge1192SrgRuntimeResolver> SRG_RUNTIME_RESOLVER =
            Forge1192SrgRuntimeResolver.load(java.nio.file.Path.of(System.getProperty("user.dir")));
    private int transformedClasses;
    private int totalRewrites;

    public int transformedClasses() { return transformedClasses; }
    public int totalRewrites() { return totalRewrites; }

    @Override
    public Result transform(String binaryName, byte[] original) {
        ClassReader reader = new ClassReader(original);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        String expected = binaryName.replace('.', '/');
        if (!expected.equals(node.name)) {
            throw new IllegalArgumentException(
                    "Class identity mismatch: requested=" + expected + " bytecode=" + node.name);
        }

        int rewrites = rewriteAllNamespaceReferences(node);
        rewrites += rewriteClassStructureNamespaceMigrations(node);
        rewrites += rewriteVerifiedSrgMethodCalls(node);
        rewrites += rewriteLegacyEnvironmentFieldAccess(node);
        rewrites += rewriteSemanticAdapters(node);
        rewrites += rewriteLegacyColorCallbackDescriptors(node);
        verifyNoEscapingLegacyRegistryFacade(node);
        verifyNoUnadaptedLifecycleCalls(node);
        verifyNoEscapingLegacyNetworkFacade(node);
        if (rewrites > 0) {
            transformedClasses++;
            totalRewrites += rewrites;
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        byte[] verified = writer.toByteArray();
        return new Result(verified, rewrites > 0,
                "forge-1.19.2 exact namespace migrations=" + rewrites);
    }
    /**
     * ASM's ClassRemapper covers the complete class-file surface: descriptors,
     * signatures, annotations/type annotations, frames, handles, indy/condy,
     * record components, nest/inner metadata and instruction operands.
     * Semantic adapters still run afterwards on the migrated tree.
     */
    private static int rewriteAllNamespaceReferences(ClassNode node) {
        ClassNode migrated = new ClassNode();
        Remapper remapper = new Remapper() {
            @Override public String map(String internalName) {
                return migrateInternalName(internalName);
            }
        };
        node.accept(new ClassRemapper(migrated, remapper));
        // Keep a simple deterministic signal for diagnostics. Detailed rewrite
        // counts are not semantic evidence; transformation coverage is.
        int changed = java.util.Arrays.equals(writeNode(node), writeNode(migrated)) ? 0 : 1;
        node.version = migrated.version;
        node.access = migrated.access;
        node.name = migrated.name;
        node.signature = migrated.signature;
        node.superName = migrated.superName;
        node.interfaces = migrated.interfaces;
        node.sourceFile = migrated.sourceFile;
        node.sourceDebug = migrated.sourceDebug;
        node.module = migrated.module;
        node.outerClass = migrated.outerClass;
        node.outerMethod = migrated.outerMethod;
        node.outerMethodDesc = migrated.outerMethodDesc;
        node.visibleAnnotations = migrated.visibleAnnotations;
        node.invisibleAnnotations = migrated.invisibleAnnotations;
        node.visibleTypeAnnotations = migrated.visibleTypeAnnotations;
        node.invisibleTypeAnnotations = migrated.invisibleTypeAnnotations;
        node.attrs = migrated.attrs;
        node.innerClasses = migrated.innerClasses;
        node.nestHostClass = migrated.nestHostClass;
        node.nestMembers = migrated.nestMembers;
        node.permittedSubclasses = migrated.permittedSubclasses;
        node.recordComponents = migrated.recordComponents;
        node.fields = migrated.fields;
        node.methods = migrated.methods;
        return changed;
    }

    private static byte[] writeNode(ClassNode node) {
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    /**
     * Conservative first migration pass. Only namespaces whose classes are
     * supplied by current NeoForge are rewritten here. Removed APIs are left
     * untouched so linkage remains fail-closed until a semantic adapter exists.
     */
    private static int rewriteExactNamespaceMigrations(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call) {
                    String owner = migrateInternalName(call.owner);
                    String desc = migrateDescriptor(call.desc);
                    if (!owner.equals(call.owner) || !desc.equals(call.desc)) {
                        call.owner = owner;
                        call.desc = desc;
                        rewrites++;
                    }
                } else if (insn instanceof FieldInsnNode field) {
                    String owner = migrateInternalName(field.owner);
                    String desc = migrateDescriptor(field.desc);
                    if (!owner.equals(field.owner) || !desc.equals(field.desc)) {
                        field.owner = owner;
                        field.desc = desc;
                        rewrites++;
                    }
                } else if (insn instanceof TypeInsnNode type) {
                    String migrated = migrateInternalName(type.desc);
                    if (!migrated.equals(type.desc)) {
                        type.desc = migrated;
                        rewrites++;
                    }
                } else if (insn instanceof LdcInsnNode ldc) {
                    Object migrated = migrateConstant(ldc.cst);
                    if (migrated != ldc.cst) {
                        ldc.cst = migrated;
                        rewrites++;
                    }
                } else if (insn instanceof InvokeDynamicInsnNode indy) {
                    String descriptor = migrateDescriptor(indy.desc);
                    if (!descriptor.equals(indy.desc)) { indy.desc = descriptor; rewrites++; }
                    Handle bootstrap = migrateHandle(indy.bsm);
                    if (bootstrap != indy.bsm) { indy.bsm = bootstrap; rewrites++; }
                    for (int i = 0; i < indy.bsmArgs.length; i++) {
                        Object migrated = migrateConstant(indy.bsmArgs[i]);
                        if (migrated != indy.bsmArgs[i]) { indy.bsmArgs[i] = migrated; rewrites++; }
                    }
                }
            }
            String migratedMethodDesc = migrateDescriptor(method.desc);
            if (!migratedMethodDesc.equals(method.desc)) {
                method.desc = migratedMethodDesc;
                rewrites++;
            }
        }
        return rewrites;
    }


    /**
     * Migrate class-structure references as well as executable instructions.
     * JVM linkage can resolve field descriptors, signatures, exceptions and
     * annotations before the corresponding code path is ever executed.
     */
    private static int rewriteClassStructureNamespaceMigrations(ClassNode node) {
        int rewrites = 0;
        String superName = migrateInternalName(node.superName);
        if (!superName.equals(node.superName)) { node.superName = superName; rewrites++; }
        for (int i = 0; i < node.interfaces.size(); i++) {
            String old = node.interfaces.get(i), migrated = migrateInternalName(old);
            if (!migrated.equals(old)) { node.interfaces.set(i, migrated); rewrites++; }
        }
        if (node.signature != null) {
            String migrated = migrateDescriptor(node.signature);
            if (!migrated.equals(node.signature)) { node.signature = migrated; rewrites++; }
        }
        for (var field : node.fields) {
            String migrated = migrateDescriptor(field.desc);
            if (!migrated.equals(field.desc)) { field.desc = migrated; rewrites++; }
            if (field.signature != null) {
                migrated = migrateDescriptor(field.signature);
                if (!migrated.equals(field.signature)) { field.signature = migrated; rewrites++; }
            }
        }
        for (var method : node.methods) {
            if (method.signature != null) {
                String migrated = migrateDescriptor(method.signature);
                if (!migrated.equals(method.signature)) { method.signature = migrated; rewrites++; }
            }
            if (method.exceptions != null) {
                for (int i = 0; i < method.exceptions.size(); i++) {
                    String old = method.exceptions.get(i), migrated = migrateInternalName(old);
                    if (!migrated.equals(old)) { method.exceptions.set(i, migrated); rewrites++; }
                }
            }
            for (var local : method.localVariables == null ? java.util.List.<org.objectweb.asm.tree.LocalVariableNode>of() : method.localVariables) {
                String migrated = migrateDescriptor(local.desc);
                if (!migrated.equals(local.desc)) { local.desc = migrated; rewrites++; }
                if (local.signature != null) {
                    migrated = migrateDescriptor(local.signature);
                    if (!migrated.equals(local.signature)) { local.signature = migrated; rewrites++; }
                }
            }
        }
        return rewrites;
    }

    private static int rewriteLegacyColorCallbackDescriptors(ClassNode node) {
        int rewrites = 0;
        String legacy = "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;I)I";
        String erased = "(Lnet/minecraft/world/level/block/state/BlockState;Ljava/lang/Object;Lnet/minecraft/core/BlockPos;I)I";
        for (var method : node.methods) {
            if (method.name.equals("m_92566_") && method.desc.equals(legacy)) {
                method.desc = erased;
                rewrites++;
            }
        }
        return rewrites;
    }

    private static void verifyNoEscapingLegacyRegistryFacade(ClassNode node) {
        String legacyRegistry = "net/neoforged/neoforge/registries/IForgeRegistry";
        for (var method : node.methods) {
            if (method.desc.contains(legacyRegistry)) {
                throw new IllegalStateException("Legacy IForgeRegistry escapes through method descriptor: "
                        + node.name + "#" + method.name + method.desc);
            }
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call
                        && (call.owner.contains(legacyRegistry) || call.desc.contains(legacyRegistry))) {
                    throw new IllegalStateException("Unadapted legacy IForgeRegistry method boundary: "
                            + node.name + "#" + method.name + " -> " + call.owner + "#" + call.name + call.desc);
                }
                // A field access may retain IForgeRegistry as a storage type after
                // namespace migration. This is safe when the value never crosses an
                // executable registry API boundary: all IForgeRegistry method calls
                // are independently rewritten above and verified below. Rejecting the
                // field descriptor itself incorrectly blocks legacy registry handles.
                if (insn instanceof TypeInsnNode type && type.desc.contains(legacyRegistry)) {
                    throw new IllegalStateException("Legacy IForgeRegistry escapes through type instruction: "
                            + node.name + "#" + method.name);
                }
            }
        }
    }

    private static int rewriteVerifiedSrgMethodCalls(ClassNode node) {
        if (SRG_RUNTIME_RESOLVER.isEmpty()) return 0;
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)) continue;
                var resolved = SRG_RUNTIME_RESOLVER.get().resolve(call.owner, call.name, call.desc);
                if (!resolved.resolved()) continue;
                if (!call.owner.equals(resolved.owner()) || !call.name.equals(resolved.name())
                        || !call.desc.equals(resolved.descriptor())) {
                    call.owner = resolved.owner();
                    call.name = resolved.name();
                    call.desc = resolved.descriptor();
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    private static int rewriteLegacyEnvironmentFieldAccess(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof FieldInsnNode field)) continue;
                // Forge 1.19.2 exposed the physical distribution as the static
                // FMLEnvironment.dist field. Current FML exposes it through
                // getDist(); preserve the legacy read contract without inventing
                // mutable field state.
                if (field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals("net/neoforged/fml/loading/FMLEnvironment")
                        && field.name.equals("dist")
                        && (field.desc.equals("Lnet/neoforged/api/distmarker/Dist;")
                            || field.desc.equals("Lnet/neoforged/neoforge/api/distmarker/Dist;"))) {
                    MethodInsnNode getter = new MethodInsnNode(
                            org.objectweb.asm.Opcodes.INVOKESTATIC,
                            "net/neoforged/fml/loading/FMLEnvironment",
                            "getDist",
                            "()Lnet/neoforged/api/distmarker/Dist;",
                            false);
                    method.instructions.set(field, getter);
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    private static int rewriteSemanticAdapters(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)) continue;

                // Removed Forge 1.19.2 static lifecycle contexts are represented by
                // scoped LMS tokens. Instance calls consume and validate those tokens.
                if (call.owner.equals("net/neoforged/fml/javafmlmod/FMLJavaModLoadingContext")
                        && call.name.equals("get")
                        && call.desc.equals("()Lnet/neoforged/fml/javafmlmod/FMLJavaModLoadingContext;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192LifecycleBridge";
                    call.name = "getJavaModLoadingContext";
                    call.desc = "()Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/fml/javafmlmod/FMLJavaModLoadingContext")
                        && call.name.equals("getModEventBus")
                        && call.desc.equals("()Lnet/neoforged/bus/api/IEventBus;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192LifecycleBridge";
                    call.name = "getModEventBus";
                    call.desc = "(Ljava/lang/Object;)Lnet/neoforged/bus/api/IEventBus;";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/fml/ModLoadingContext")
                        && call.name.equals("get")
                        && call.desc.equals("()Lnet/neoforged/fml/ModLoadingContext;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192LifecycleBridge";
                    call.name = "getModLoadingContext";
                    call.desc = "()Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/fml/ModLoadingContext")
                        && call.name.equals("getActiveContainer")
                        && call.desc.equals("()Lnet/neoforged/fml/ModContainer;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192LifecycleBridge";
                    call.name = "getActiveContainer";
                    call.desc = "(Ljava/lang/Object;)Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/fml/ModLoadingContext")
                        && call.name.equals("setActiveContainer")
                        && call.desc.equals("(Lnet/neoforged/fml/ModContainer;)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192LifecycleBridge";
                    call.name = "setActiveContainer";
                    call.desc = "(Ljava/lang/Object;Ljava/lang/Object;)V";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/fml/ModLoadingContext")
                        && call.name.equals("getActiveNamespace")
                        && call.desc.equals("()Ljava/lang/String;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192LifecycleBridge";
                    call.name = "getActiveNamespace";
                    call.desc = "(Ljava/lang/Object;)Ljava/lang/String;";
                    call.itf = false;
                    rewrites++;
                }

                // Constructor-only adaptation for legacy subclasses. The
                // builder supplies the modern superclass state, while the
                // original subclass and its virtual icon method remain intact.
                // Creative inventory registration/content still require a
                // separate, verified lifecycle migration.
                if (call.owner.equals("net/minecraft/world/item/CreativeModeTab")
                        && call.name.equals("<init>")
                        && call.desc.equals("(Ljava/lang/String;)V")) {
                    if (!node.superName.equals("net/minecraft/world/item/CreativeModeTab")
                            || !method.name.equals("<init>")) {
                        throw new IllegalStateException(
                                "Unsupported legacy CreativeModeTab constructor context: "
                                        + node.name + "#" + method.name);
                    }
                    method.instructions.insertBefore(call, new MethodInsnNode(
                            org.objectweb.asm.Opcodes.INVOKESTATIC,
                            "com/fuckingdeveloper/lms/runtime/Forge1192CreativeTabBridge",
                            "builder",
                            "(Ljava/lang/String;)Lnet/minecraft/world/item/CreativeModeTab$Builder;",
                            false));
                    call.desc = "(Lnet/minecraft/world/item/CreativeModeTab$Builder;)V";
                    rewrites++;
                }

                // Forge 1.19.2 Dist exposed convenience predicates that are
                // absent from the current enum. Preserve physical-side semantics.
                if (call.owner.equals("net/neoforged/api/distmarker/Dist")
                        && call.name.equals("isClient") && call.desc.equals("()Z")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192SideBridge";
                    call.name = "isClient";
                    call.desc = "(Lnet/neoforged/api/distmarker/Dist;)Z";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/api/distmarker/Dist")
                        && call.name.equals("isDedicatedServer") && call.desc.equals("()Z")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192SideBridge";
                    call.name = "isDedicatedServer";
                    call.desc = "(Lnet/neoforged/api/distmarker/Dist;)Z";
                    call.itf = false;
                    rewrites++;
                }

                // EffectiveSide is a logical-side query. Keep it distinct from
                // physical Dist and route through a profile-owned semantic boundary.
                if (call.owner.equals("net/neoforged/fml/util/thread/EffectiveSide")
                        && call.name.equals("get")
                        && call.desc.equals("()Lnet/neoforged/fml/LogicalSide;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192SideBridge";
                    call.name = "getEffectiveSide";
                    call.desc = "()Lnet/neoforged/fml/LogicalSide;";
                    call.itf = false;
                    rewrites++;
                }
                // RegisterEvent no longer exposes Forge 1.19.2's
                // IForgeRegistry facade. Preserve the registry key directly;
                // mutable registry operations are handled by scoped adapters.
                if (call.owner.equals("net/neoforged/neoforge/registries/RegisterEvent")
                        && call.name.equals("getRegistryKey")
                        && call.desc.equals("()Lnet/minecraft/resources/ResourceKey;")) {
                    // Exact modern API after ResourceLocation -> Identifier migration.
                }
                // Forge 1.19.2 exposed IForgeRegistry from RegisterEvent.
                // NeoForge exposes the concrete Registry instead. Rewrite the
                // producer and all known legacy consumer calls as one contract.
                if (call.owner.equals("net/neoforged/neoforge/registries/RegisterEvent")
                        && call.name.equals("getForgeRegistry")
                        && call.desc.equals("()Lnet/neoforged/neoforge/registries/IForgeRegistry;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RegistrationContext";
                    call.name = "activeRegistryObject";
                    call.desc = "(Lnet/neoforged/neoforge/registries/RegisterEvent;)Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/neoforge/registries/IForgeRegistry")) {
                    String bridgeDesc = switch (call.name) {
                        case "containsKey" -> call.desc.equals("(Lnet/minecraft/resources/Identifier;)Z")
                                ? "(Ljava/lang/Object;Lnet/minecraft/resources/Identifier;)Z" : null;
                        case "getValue" -> call.desc.equals("(Lnet/minecraft/resources/Identifier;)Ljava/lang/Object;")
                                ? "(Ljava/lang/Object;Lnet/minecraft/resources/Identifier;)Ljava/lang/Object;" : null;
                        case "getKey" -> call.desc.equals("(Ljava/lang/Object;)Lnet/minecraft/resources/Identifier;")
                                ? "(Ljava/lang/Object;Ljava/lang/Object;)Lnet/minecraft/resources/Identifier;" : null;
                        case "iterator" -> call.desc.equals("()Ljava/util/Iterator;")
                                ? "(Ljava/lang/Object;)Ljava/util/Iterator;" : null;
                        default -> null;
                    };
                    if (bridgeDesc != null) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RegistrationContext";
                        call.desc = bridgeDesc;
                        call.itf = false;
                        rewrites++;
                    }
                }
                // Legacy IForgeRegistry#register is no longer a mutable
                // registry call. Route it through the active RegisterEvent scope.
                if (call.owner.equals("net/neoforged/neoforge/registries/IForgeRegistry")
                        && call.name.equals("register")
                        && call.desc.equals("(Ljava/lang/String;Ljava/lang/Object;)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RegistrationContext";
                    call.desc = "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/neoforge/registries/IForgeRegistry")
                        && call.name.equals("register")
                        && call.desc.equals("(Lnet/minecraft/resources/Identifier;Ljava/lang/Object;)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RegistrationContext";
                    call.desc = "(Ljava/lang/Object;Lnet/minecraft/resources/Identifier;Ljava/lang/Object;)V";
                    call.itf = false;
                    rewrites++;
                }
                // The old GameData helper relied on implicit active mod
                // registration context. Redirect to an explicit scoped bridge.
                if (call.owner.equals("net/neoforged/neoforge/registries/GameData")
                        && call.name.equals("checkPrefix")
                        && call.desc.equals("(Ljava/lang/String;Z)Lnet/minecraft/resources/Identifier;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RegistrationContext";
                    call.desc = "(Ljava/lang/String;Z)Lnet/minecraft/resources/Identifier;";
                    call.itf = false;
                    rewrites++;
                }
                // Forge 1.19.2 SimpleChannel is a protocol object, not a class
                // that can be namespace-migrated into current NeoForge. Replace
                // construction with an LMS-owned token; message registration stays
                // fail-closed until codec/payload semantics are adapted.
                if (call.owner.equals("net/neoforged/neoforge/network/NetworkRegistry")
                        && call.name.equals("newSimpleChannel")
                        && call.desc.equals("(Lnet/minecraft/resources/Identifier;Ljava/util/function/Supplier;Ljava/util/function/Predicate;Ljava/util/function/Predicate;)Lnet/neoforged/neoforge/network/simple/SimpleChannel;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge";
                    call.name = "newSimpleChannel";
                    call.desc = "(Lnet/minecraft/resources/Identifier;Ljava/util/function/Supplier;Ljava/util/function/Predicate;Ljava/util/function/Predicate;)Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/neoforge/network/simple/SimpleChannel")
                        && call.name.equals("registerMessage")
                        && call.desc.equals("(ILjava/lang/Class;Ljava/util/function/BiConsumer;Ljava/util/function/Function;Ljava/util/function/BiConsumer;)Lnet/neoforged/neoforge/network/simple/IndexedMessageCodec$MessageHandler;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge";
                    call.name = "registerMessage";
                    call.desc = "(Ljava/lang/Object;ILjava/lang/Class;Ljava/util/function/BiConsumer;Ljava/util/function/Function;Ljava/util/function/BiConsumer;)Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                }

                if (call.owner.equals("net/neoforged/neoforge/network/NetworkEvent$Context")) {
                    String bridgeDesc = switch (call.name + call.desc) {
                        case "enqueueWork(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;" ->
                                "(Ljava/lang/Object;Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;";
                        case "getSender()Lnet/minecraft/server/level/ServerPlayer;" ->
                                "(Ljava/lang/Object;)Lnet/minecraft/server/level/ServerPlayer;";
                        case "setPacketHandled(Z)V" -> "(Ljava/lang/Object;Z)V";
                        default -> null;
                    };
                    if (bridgeDesc != null) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge";
                        call.desc = bridgeDesc;
                        call.itf = false;
                        rewrites++;
                    }
                }

                // Current NeoForge EventHooks posts the same NeighborNotifyEvent
                // and returns it; the old ForgeEventFactory owner was removed.
                if (call.owner.equals("net/neoforged/neoforge/event/ForgeEventFactory")
                        && call.name.equals("onNeighborNotify")
                        && call.desc.equals("(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Ljava/util/EnumSet;Z)Lnet/neoforged/neoforge/event/level/BlockEvent$NeighborNotifyEvent;")) {
                    call.owner = "net/neoforged/neoforge/event/EventHooks";
                    rewrites++;
                }

                // Forge 1.19.2 capability type declarations no longer map
                // to a mutable modern capability registry. Validate/capture the
                // declaration without inventing a global registration side effect.
                if (call.owner.equals("net/neoforged/neoforge/capabilities/RegisterCapabilitiesEvent")
                        && call.name.equals("register")
                        && call.desc.equals("(Ljava/lang/Class;)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192CapabilityBridge";
                    call.name = "register";
                    call.desc = "(Lnet/neoforged/neoforge/capabilities/RegisterCapabilitiesEvent;Ljava/lang/Class;)V";
                    call.itf = false;
                    rewrites++;
                }

                // Legacy ForgeMod milk-fluid opt-in is an imperative profile
                // request. Capture it explicitly rather than invoking a removed API.
                if (call.owner.equals("net/neoforged/neoforge/common/ForgeMod")
                        && call.name.equals("enableMilkFluid")
                        && call.desc.equals("()V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192CommonBridge";
                    call.name = "enableMilkFluid";
                    call.desc = "()V";
                    call.itf = false;
                    rewrites++;
                }

                // Removed LazyOptional operations retain an explicit LMS
                // invalidation token. Do not silently substitute java.util.Optional.
                if (call.owner.equals("net/neoforged/neoforge/common/util/LazyOptional")
                        && call.name.equals("empty")
                        && call.desc.equals("()Lnet/neoforged/neoforge/common/util/LazyOptional;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192LazyOptionalBridge";
                    call.name = "empty";
                    call.desc = "()Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/neoforge/common/util/LazyOptional")
                        && call.name.equals("invalidate")
                        && call.desc.equals("()V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192LazyOptionalBridge";
                    call.name = "invalidate";
                    call.desc = "(Ljava/lang/Object;)V";
                    call.itf = false;
                    rewrites++;
                }

                // Forge 1.19.2 FluidStack#isFluidEqual compares fluid identity,
                // not amount or data components. Keep the old contract explicit.
                if (call.owner.equals("net/neoforged/neoforge/fluids/FluidStack")
                        && call.name.equals("isFluidEqual")
                        && call.desc.equals("(Lnet/neoforged/neoforge/fluids/FluidStack;)Z")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192FluidBridge";
                    call.name = "isFluidEqual";
                    call.desc = "(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/FluidStack;)Z";
                    call.itf = false;
                    rewrites++;
                }
                // Legacy listeners capture the active legacy mod identity when
                // registered. RegisterEvent callbacks additionally receive a scoped
                // registration context, so old registry facade adapters execute only
                // during the legal NeoForge registration phase.
                if (call.owner.equals("net/neoforged/bus/api/IEventBus")
                        && call.name.equals("addListener")
                        && call.desc.equals("(Ljava/util/function/Consumer;)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192EventBusBridge";
                    call.name = "addListener";
                    call.desc = "(Lnet/neoforged/bus/api/IEventBus;Ljava/util/function/Consumer;)V";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/bus/api/IEventBus")
                        && call.name.equals("addListener")
                        && call.desc.equals("(Lnet/neoforged/bus/api/EventPriority;Ljava/util/function/Consumer;)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192EventBusBridge";
                    call.name = "addListener";
                    call.desc = "(Lnet/neoforged/bus/api/IEventBus;Lnet/neoforged/bus/api/EventPriority;Ljava/util/function/Consumer;)V";
                    call.itf = false;
                    rewrites++;
                }

                if (call.owner.equals("net/neoforged/bus/api/IEventBus")
                        && call.name.equals("register")
                        && call.desc.equals("(Ljava/lang/Object;)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192EventBusBridge";
                    call.name = "register";
                    call.desc = "(Lnet/neoforged/bus/api/IEventBus;Ljava/lang/Object;)V";
                    call.itf = false;
                    rewrites++;
                }

                // Forge 1.19.2 IEventBus#post returned cancellation state.
                // NeoForge's EventBus returns the posted Event. Preserve the
                // legacy boolean contract through an LMS runtime adapter.
                if (call.owner.equals("net/neoforged/bus/api/IEventBus")
                        && call.name.equals("post")
                        && call.desc.equals("(Lnet/neoforged/bus/api/Event;)Z")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192EventBusBridge";
                    call.name = "post";
                    call.desc = "(Lnet/neoforged/bus/api/IEventBus;Lnet/neoforged/bus/api/Event;)Z";
                    call.itf = false;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    private static void verifyNoEscapingLegacyNetworkFacade(ClassNode node) {
        String simpleChannel = "net/neoforged/neoforge/network/simple/SimpleChannel";
        String networkContext = "net/neoforged/neoforge/network/NetworkEvent$Context";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)) continue;
                if (call.owner.equals("net/neoforged/neoforge/network/NetworkRegistry")
                        && call.name.equals("newSimpleChannel"))
                    throw new IllegalStateException("Unadapted legacy SimpleChannel construction: "
                            + node.name + "#" + method.name + call.desc);
                if (call.owner.equals(simpleChannel) && !call.name.equals("registerMessage"))
                    throw new IllegalStateException("Unsupported legacy SimpleChannel operation: "
                            + node.name + "#" + method.name + " -> " + call.name + call.desc);
                if (call.owner.equals(networkContext))
                    throw new IllegalStateException("Unadapted legacy NetworkEvent.Context operation: "
                            + node.name + "#" + method.name + " -> " + call.name + call.desc);
            }
        }
    }

    private static void verifyNoUnadaptedLifecycleCalls(ClassNode node) {
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)) continue;
                if (call.owner.equals("net/neoforged/fml/ModLoadingContext")
                        && (call.name.equals("getActiveContainer") || call.name.equals("setActiveContainer"))) {
                    throw new IllegalStateException("Unadapted legacy ModLoadingContext boundary: "
                            + node.name + "#" + method.name + " -> " + call.name + call.desc);
                }
            }
        }
    }

    private static Handle migrateHandle(Handle handle) {
        String owner = migrateInternalName(handle.getOwner());
        String desc = migrateDescriptor(handle.getDesc());
        if (owner.equals(handle.getOwner()) && desc.equals(handle.getDesc())) return handle;
        return new Handle(handle.getTag(), owner, handle.getName(), desc, handle.isInterface());
    }

    private static Object migrateConstant(Object value) {
        if (value instanceof Type type) {
            String descriptor = migrateDescriptor(type.getDescriptor());
            return descriptor.equals(type.getDescriptor()) ? value : Type.getType(descriptor);
        }
        if (value instanceof Handle handle) return migrateHandle(handle);
        if (value instanceof ConstantDynamic dynamic) {
            String descriptor = migrateDescriptor(dynamic.getDescriptor());
            Handle bootstrap = migrateHandle(dynamic.getBootstrapMethod());
            Object[] args = new Object[dynamic.getBootstrapMethodArgumentCount()];
            boolean changed = !descriptor.equals(dynamic.getDescriptor()) || bootstrap != dynamic.getBootstrapMethod();
            for (int i = 0; i < args.length; i++) {
                Object old = dynamic.getBootstrapMethodArgument(i);
                args[i] = migrateConstant(old);
                changed |= args[i] != old;
            }
            return changed ? new ConstantDynamic(dynamic.getName(), descriptor, bootstrap, args) : value;
        }
        return value;
    }

    private static String migrateInternalName(String value) {
        // DistExecutor was removed from current NeoForge. Keep the Forge 1.19.2
        // physical-side execution contract behind an LMS-owned runtime boundary
        // instead of migrating it into a non-existent NeoForge class.
        if (value.equals("net/minecraftforge/fml/DistExecutor"))
            return "com/fuckingdeveloper/lms/runtime/Forge1192DistExecutorBridge";
        if (value.equals("net/minecraft/client/color/block/BlockColor"))
            return "com/fuckingdeveloper/lms/runtime/color/LegacyBlockColor";
        if (value.equals("net/minecraft/client/color/item/ItemColor"))
            return "com/fuckingdeveloper/lms/runtime/color/LegacyItemColor";
        if (value.startsWith("net/minecraftforge/client/model/data/"))
            return value.replace("net/minecraftforge/client/model/data/", "net/neoforged/neoforge/model/data/");
        if (value.equals("net/minecraft/resources/ResourceLocation"))
            return "net/minecraft/resources/Identifier";
        if (value.startsWith("net/minecraft/")) {
            var relocation = VANILLA_RELOCATIONS.resolveRelocatedClass(value);
            if (relocation.status() == Forge1192MappingLayer.Status.VERIFIED_IDENTITY
                    && !relocation.targetInternalName().isEmpty()) {
                return relocation.targetInternalName();
            }
        }
        if (value.startsWith("net/minecraftforge/eventbus/"))
            return value.replace("net/minecraftforge/eventbus/", "net/neoforged/bus/");
        if (value.startsWith("net/minecraftforge/fml/"))
            return value.replace("net/minecraftforge/fml/", "net/neoforged/fml/");
        if (value.startsWith("net/minecraftforge/forgespi/"))
            return value.replace("net/minecraftforge/forgespi/", "net/neoforged/neoforgespi/");
        // Main Forge API moved below NeoForge's neoforge namespace.
        if (value.startsWith("net/minecraftforge/"))
            return value.replace("net/minecraftforge/", "net/neoforged/neoforge/");
        return value;
    }

    private static String migrateDescriptor(String descriptor) {
        return descriptor
                .replace("net/minecraftforge/fml/DistExecutor", "com/fuckingdeveloper/lms/runtime/Forge1192DistExecutorBridge")
                .replace("net/minecraft/client/color/block/BlockColor", "com/fuckingdeveloper/lms/runtime/color/LegacyBlockColor")
                .replace("net/minecraft/client/color/item/ItemColor", "com/fuckingdeveloper/lms/runtime/color/LegacyItemColor")
                .replace("net/minecraftforge/client/model/data/", "net/neoforged/neoforge/model/data/")
                .replace("net/minecraft/resources/ResourceLocation", "net/minecraft/resources/Identifier")
                .replace("net/minecraftforge/eventbus/", "net/neoforged/bus/")
                .replace("net/minecraftforge/fml/", "net/neoforged/fml/")
                .replace("net/minecraftforge/forgespi/", "net/neoforged/neoforgespi/")
                .replace("net/minecraftforge/", "net/neoforged/neoforge/");
    }
}
