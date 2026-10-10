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
    private static final java.util.Map<String, String> EXPLICIT_VANILLA_CLASS_RELOCATIONS = java.util.Map.ofEntries(
            java.util.Map.entry("net/minecraft/client/model/ArmorStandArmorModel", "net/minecraft/client/model/object/armorstand/ArmorStandArmorModel"),
            java.util.Map.entry("net/minecraft/client/model/BoatModel", "net/minecraft/client/model/object/boat/BoatModel"),
            java.util.Map.entry("net/minecraft/client/model/BookModel", "net/minecraft/client/model/object/book/BookModel"),
            java.util.Map.entry("net/minecraft/client/model/DrownedModel", "net/minecraft/client/model/monster/zombie/DrownedModel"),
            java.util.Map.entry("net/minecraft/client/model/GiantZombieModel", "net/minecraft/client/model/monster/zombie/GiantZombieModel"),
            java.util.Map.entry("net/minecraft/client/model/PlayerModel", "net/minecraft/client/model/player/PlayerModel"),
            java.util.Map.entry("net/minecraft/client/model/SkeletonModel", "net/minecraft/client/model/monster/skeleton/SkeletonModel"),
            java.util.Map.entry("net/minecraft/client/model/VillagerModel", "net/minecraft/client/model/npc/VillagerModel"),
            java.util.Map.entry("net/minecraft/client/model/ZombieModel", "net/minecraft/client/model/monster/zombie/ZombieModel"),
            java.util.Map.entry("net/minecraft/client/model/ZombieVillagerModel", "net/minecraft/client/model/monster/zombie/ZombieVillagerModel"),
            java.util.Map.entry("net/minecraft/client/renderer/RenderType", "net/minecraft/client/renderer/rendertype/RenderType"),
            java.util.Map.entry("net/minecraft/world/level/BlockAndTintGetter", "net/minecraft/client/renderer/block/BlockAndTintGetter"),
            java.util.Map.entry("net/minecraft/core/BlockSource", "net/minecraft/core/dispenser/BlockSource"),
            java.util.Map.entry("net/minecraft/world/entity/animal/Sheep", "net/minecraft/world/entity/animal/sheep/Sheep"),
            java.util.Map.entry("net/minecraft/world/entity/boss/EnderDragonPart", "net/minecraft/world/entity/boss/enderdragon/EnderDragonPart"),
            java.util.Map.entry("net/minecraft/world/entity/decoration/PaintingVariant", "net/minecraft/world/entity/decoration/painting/PaintingVariant"),
            java.util.Map.entry("net/minecraft/world/entity/npc/Villager", "net/minecraft/world/entity/npc/villager/Villager"),
            java.util.Map.entry("net/minecraft/world/entity/npc/VillagerData", "net/minecraft/world/entity/npc/villager/VillagerData"),
            java.util.Map.entry("net/minecraft/world/entity/npc/VillagerProfession", "net/minecraft/world/entity/npc/villager/VillagerProfession"),
            java.util.Map.entry("net/minecraft/world/entity/npc/VillagerType", "net/minecraft/world/entity/npc/villager/VillagerType"),
            java.util.Map.entry("net/minecraft/world/entity/vehicle/AbstractMinecart", "net/minecraft/world/entity/vehicle/minecart/AbstractMinecart"),
            java.util.Map.entry("net/minecraft/world/entity/vehicle/Boat", "net/minecraft/world/entity/vehicle/boat/Boat"),
            java.util.Map.entry("net/minecraft/world/entity/vehicle/Minecart", "net/minecraft/world/entity/vehicle/minecart/Minecart"),
            java.util.Map.entry("net/minecraft/world/item/ArmorMaterial", "net/minecraft/world/item/equipment/ArmorMaterial"),
            java.util.Map.entry("net/minecraft/world/item/ArmorMaterials", "net/minecraft/world/item/equipment/ArmorMaterials"),
            java.util.Map.entry("net/minecraft/world/level/GameRules", "net/minecraft/world/level/gamerules/GameRules"),
            java.util.Map.entry("net/minecraft/world/level/chunk/ChunkStatus", "net/minecraft/world/level/chunk/status/ChunkStatus"));
    private static final java.util.Optional<Forge1192SrgRuntimeResolver> SRG_RUNTIME_RESOLVER =
            Forge1192SrgRuntimeResolver.load(java.nio.file.Path.of(System.getProperty("user.dir")));
    private int transformedClasses;
    private int totalRewrites;
    private volatile LegacyClassTransformer.ClassBytesLookup managedClassBytes;

    @Override
    public void bindManagedClassBytes(LegacyClassTransformer.ClassBytesLookup lookup) {
        this.managedClassBytes = java.util.Objects.requireNonNull(lookup, "lookup");
    }

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

        // Removed Forge APIs must be adapted while their original identity
        // is still available. Namespace projection is intentionally second:
        // otherwise a heuristic relocation can destroy the semantic evidence
        // required by a removed-API adapter.
        int rewrites = rewriteRemovedOptionalClientScreenConstructors(node);
        rewrites += rewriteLegacySignItemConstruction(node);
        rewrites += rewriteLegacyWoodButtonConstruction(node);
        rewrites += rewriteLegacyVillagerProfessionConstruction(node);
        rewrites += rewriteLegacyDirectionPropertyFactories(node);
        rewrites += rewriteLegacyMetalBlockProperties(node);
        rewrites += rewriteLegacyNamedBlockConstruction(node);
        rewrites += rewriteLegacyCableStateDefinitionOverride(node);
        rewrites += rewriteLegacyCreativeTabAssignment(node);
        rewrites += rewriteLegacyBlockItemConstruction(node);
        rewrites += rewriteLegacyNamedItemConstruction(node);
        rewrites += rewriteLegacyNamedSimpleToolConstruction(node);
        rewrites += rewriteLegacyAttributeModifiers(node);
        rewrites += rewriteAllNamespaceReferences(node);
        rewrites += rewriteClassStructureNamespaceMigrations(node);
        rewrites += rewriteLegacyStateOnlyEmissivePredicates(node);
        rewrites += rewriteVerifiedSrgMethodCalls(node);
        rewrites += rewriteVerifiedSrgFieldAccesses(node);
        rewrites += rewriteLegacyMobEffectInstanceConstruction(node);
        rewrites += rewriteLegacyVanillaRegistryFields(node);
        rewrites += rewriteLegacyForgeBuiltinFields(node);
        rewrites += rewriteLegacyForgeRegistries(node);
        rewrites += rewriteLegacyForgeRegistryKeys(node);
        rewrites += rewriteLegacyEnvironmentFieldAccess(node);
        rewrites += rewriteLegacyTickEventFields(node);
        rewrites += rewriteLegacySpawnCheckSemantics(node);
        rewrites += rewriteLegacyIdentifierConstruction(node);
        rewrites += rewriteLegacyBlockEntityTypeConstruction(node);
        rewrites += rewriteLegacyCraftingContainerConstruction(node);
        rewrites += rewriteLegacyArmorConstructorDescriptors(node);
        rewrites += rewriteSemanticAdapters(node);
        rewrites += rewriteLegacyColorCallbackDescriptors(node);
        verifyNoRemovedForgeClientTypes(node);
        verifyUnresolvedForgeModAccesses(node);
        verifyNoEscapingLegacyRegistryFacade(node);
        verifyNoUnadaptedLifecycleCalls(node);
        verifyNoEscapingLegacyNetworkFacade(node);
        if (rewrites > 0) {
            transformedClasses++;
            totalRewrites += rewrites;
        }

        // Semantic adapters may change descriptors and local/stack types.
        // Frames from the legacy class are therefore no longer authoritative.
        // Recompute both frames and maxs before definition.
        ClassWriter writer = new SafeFrameClassWriter(
                ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, managedClassBytes);
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
     * Frame computation must not ask the application/system classloader to
     * resolve managed legacy classes. They intentionally live behind
     * ManagedLegacyClassLoader and may still contain pre-adaptation names.
     *
     * Object is always a verifier-safe common supertype for unrelated
     * reference values. Preserve identical types and the few array cases ASM
     * can safely reason about without class loading; otherwise merge to Object.
     */
    private static final class SafeFrameClassWriter extends ClassWriter {
        private static final String OBJECT = "java/lang/Object";
        private final LegacyClassTransformer.ClassBytesLookup managedClassBytes;
        private final java.util.Map<String, TypeInfo> cache = new java.util.HashMap<>();

        private SafeFrameClassWriter(int flags, LegacyClassTransformer.ClassBytesLookup managedClassBytes) {
            super(flags);
            this.managedClassBytes = managedClassBytes;
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            if (type1.equals(type2)) return type1;
            if (type1.startsWith("[") || type2.startsWith("[")) {
                return commonArrayType(type1, type2);
            }
            if (isAssignableFrom(type1, type2)) return type1;
            if (isAssignableFrom(type2, type1)) return type2;

            TypeInfo first = info(type1);
            TypeInfo second = info(type2);
            if (first == null || second == null || first.isInterface || second.isInterface) return OBJECT;

            String cursor = first.superName;
            java.util.HashSet<String> seen = new java.util.HashSet<>();
            while (cursor != null && seen.add(cursor)) {
                if (isAssignableFrom(cursor, type2)) return cursor;
                TypeInfo parent = info(cursor);
                cursor = parent == null ? null : parent.superName;
            }
            return OBJECT;
        }

        private String commonArrayType(String first, String second) {
            if (!first.startsWith("[") || !second.startsWith("[")) return OBJECT;
            Type a = Type.getType(first);
            Type b = Type.getType(second);
            if (a.getDimensions() != b.getDimensions()) return OBJECT;
            Type ae = a.getElementType();
            Type be = b.getElementType();
            if (ae.getSort() != Type.OBJECT || be.getSort() != Type.OBJECT) return OBJECT;
            String common = getCommonSuperClass(ae.getInternalName(), be.getInternalName());
            return "[".repeat(a.getDimensions()) + "L" + common + ";";
        }

        private boolean isAssignableFrom(String target, String candidate) {
            if (target.equals(candidate) || target.equals(OBJECT)) return true;
            java.util.ArrayDeque<String> pending = new java.util.ArrayDeque<>();
            java.util.HashSet<String> seen = new java.util.HashSet<>();
            pending.add(candidate);
            while (!pending.isEmpty()) {
                String current = pending.removeFirst();
                if (!seen.add(current)) continue;
                if (target.equals(current)) return true;
                TypeInfo type = info(current);
                if (type == null) continue;
                if (type.superName != null) pending.addLast(type.superName);
                pending.addAll(type.interfaces);
            }
            return false;
        }

        private TypeInfo info(String internalName) {
            if (internalName == null) return null;
            if (cache.containsKey(internalName)) return cache.get(internalName);
            TypeInfo resolved = readManagedInfo(internalName);
            if (resolved == null) resolved = readRuntimeInfo(internalName);
            cache.put(internalName, resolved);
            return resolved;
        }

        private TypeInfo readManagedInfo(String internalName) {
            if (managedClassBytes == null) return null;
            try {
                byte[] bytes = managedClassBytes.find(internalName);
                if (bytes == null) return null;
                ClassReader reader = new ClassReader(bytes);
                String superName = projectHierarchyName(reader.getSuperName());
                String[] rawInterfaces = reader.getInterfaces();
                java.util.ArrayList<String> interfaces = new java.util.ArrayList<>(rawInterfaces.length);
                for (String iface : rawInterfaces) interfaces.add(projectHierarchyName(iface));
                return new TypeInfo(
                        projectHierarchyName(reader.getClassName()),
                        superName,
                        java.util.List.copyOf(interfaces),
                        (reader.getAccess() & org.objectweb.asm.Opcodes.ACC_INTERFACE) != 0);
            } catch (java.io.IOException | RuntimeException ignored) {
                return null;
            }
        }

        private TypeInfo readRuntimeInfo(String internalName) {
            if (!isParentRuntimeType(internalName)) return null;
            try {
                ClassLoader loader = Forge1192LegacyClassTransformer.class.getClassLoader();
                Class<?> type = Class.forName(internalName.replace('/', '.'), false, loader);
                Class<?> parent = type.getSuperclass();
                java.util.ArrayList<String> interfaces = new java.util.ArrayList<>();
                for (Class<?> iface : type.getInterfaces()) {
                    interfaces.add(iface.getName().replace('.', '/'));
                }
                return new TypeInfo(
                        internalName,
                        parent == null ? null : parent.getName().replace('.', '/'),
                        java.util.List.copyOf(interfaces),
                        type.isInterface());
            } catch (LinkageError | ClassNotFoundException ignored) {
                return null;
            }
        }

        private static String projectHierarchyName(String internalName) {
            return internalName == null ? null : migrateInternalName(internalName);
        }

        private static boolean isParentRuntimeType(String type) {
            return type.startsWith("java/")
                    || type.startsWith("javax/")
                    || type.startsWith("jdk/")
                    || type.startsWith("net/minecraft/")
                    || type.startsWith("net/neoforged/")
                    || type.startsWith("com/mojang/")
                    || type.startsWith("com/fuckingdeveloper/lms/");
        }

        private record TypeInfo(String name, String superName, java.util.List<String> interfaces,
                                boolean isInterface) {}
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

    /**
     * Forge 1.19.2 exposed a built-in mod list screen that no longer exists
     * in the target runtime. An old GUI may only use it as an optional
     * navigation destination. In that case the original parent Screen is a
     * safe fallback, while manufacturing a fake screen class is not.
     *
     * <p>Recognize only the proven constructor stack pattern:
     * NEW ModListScreen; DUP; [code producing parent Screen];
     * INVOKESPECIAL ModListScreen(Screen). Replace it with the already
     * evaluated parent Screen. This deliberately does not touch any other
     * constructor or any other use of the removed type.</p>
     */
    private static int rewriteRemovedOptionalClientScreenConstructors(ClassNode node) {
        final String removed = "net/minecraftforge/client/gui/ModListScreen";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; ) {
                var next = insn.getNext();
                if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                        && call.owner.equals(removed)
                        && call.name.equals("<init>")
                        && call.desc.equals("(Lnet/minecraft/client/gui/screens/Screen;)V")) {
                    // The NEW and DUP must be adjacent real instructions. Do
                    // not rewrite arbitrary constructors with unknown stack
                    // shape, and never suppress a required gameplay class.
                    TypeInsnNode allocation = null;
                    org.objectweb.asm.tree.AbstractInsnNode duplicate = null;
                    int steps = 0;
                    for (var previous = call.getPrevious(); previous != null && steps++ < 12;
                         previous = previous.getPrevious()) {
                        if (previous instanceof TypeInsnNode type
                                && type.getOpcode() == org.objectweb.asm.Opcodes.NEW
                                && type.desc.equals(removed)) {
                            var after = type.getNext();
                            while (after != null && after.getOpcode() < 0) after = after.getNext();
                            if (after != null && after.getOpcode() == org.objectweb.asm.Opcodes.DUP) {
                                allocation = type;
                                duplicate = after;
                            }
                            break;
                        }
                    }
                    if (allocation != null) {
                        method.instructions.remove(call);
                        method.instructions.remove(duplicate);
                        method.instructions.remove(allocation);
                        rewrites++;
                    }
                }
                insn = next;
            }
        }
        return rewrites;
    }

    /**
     * Normal signs no longer have a dedicated SignItem subclass. They use
     * StandingAndWallBlockItem with a downward attachment direction and the
     * sign-text item component. Reorder the legacy constructor operands and
     * apply both pieces of the modern contract before namespace migration.
     */
    private static int rewriteLegacySignItemConstruction(ClassNode node) {
        int rewrites = 0;
        final String legacyOwner = "net/minecraft/world/item/SignItem";
        final String legacyDesc = "(Lnet/minecraft/world/item/Item$Properties;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/block/Block;)V";
        for (var method : node.methods) {
            RegistryNameEvidence registryName = method.name.equals("<init>")
                    ? legacyRegistryNameEvidence(node, method) : null;
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !call.owner.equals(legacyOwner)
                        || !call.name.equals("<init>")
                        || !call.desc.equals(legacyDesc)) continue;

                var adaptation = new org.objectweb.asm.tree.InsnList();
                if (registryName != null) {
                    // Keep uninitialized this untouched on the operand stack;
                    // spill only the constructor arguments while attaching the
                    // bytecode-proven item id to Properties.
                    int wallLocal = method.maxLocals++;
                    int standingLocal = method.maxLocals++;
                    int propertiesLocal = method.maxLocals++;
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ASTORE, wallLocal));
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ASTORE, standingLocal));
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ASTORE, propertiesLocal));
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, standingLocal));
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, wallLocal));
                    adaptation.add(new FieldInsnNode(org.objectweb.asm.Opcodes.GETSTATIC,
                            "net/minecraft/core/Direction", "DOWN", "Lnet/minecraft/core/Direction;"));
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, propertiesLocal));
                    appendRegistryNameLoad(adaptation, registryName);
                    adaptation.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                            "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                            "withItemId",
                            "(Lnet/minecraft/world/item/Item$Properties;Ljava/lang/String;)"
                                    + "Lnet/minecraft/world/item/Item$Properties;", false));
                } else {
                    // this, properties, standing, wall -> this, standing, wall, properties
                    adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.DUP2_X1));
                    adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.POP2));
                    adaptation.add(new FieldInsnNode(org.objectweb.asm.Opcodes.GETSTATIC,
                            "net/minecraft/core/Direction", "DOWN", "Lnet/minecraft/core/Direction;"));
                    adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.SWAP));
                }
                adaptation.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKEVIRTUAL,
                        "net/minecraft/world/item/Item$Properties", "signText",
                        "()Lnet/minecraft/world/item/Item$Properties;", false));
                method.instructions.insertBefore(call, adaptation);
                call.owner = "net/minecraft/world/item/StandingAndWallBlockItem";
                call.desc = "(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/Direction;Lnet/minecraft/world/item/Item$Properties;)V";
                rewrites++;
            }
        }
        return rewrites;
    }

    /**
     * WoodButtonBlock was folded into ButtonBlock. The old subclass constructor
     * is exactly the modern oak button configuration: arrow-sensitive with a
     * thirty-tick press duration.
     */
    private static int rewriteLegacyWoodButtonConstruction(ClassNode node) {
        int rewrites = 0;
        final String legacyOwner = "net/minecraft/world/level/block/WoodButtonBlock";
        final String properties = "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !call.owner.equals(legacyOwner)
                        || !call.name.equals("<init>")
                        || !call.desc.equals("(" + properties + ")V")) continue;

                var adaptation = new org.objectweb.asm.tree.InsnList();
                // this, properties -> this, BlockSetType.OAK, 30, properties
                adaptation.add(new FieldInsnNode(org.objectweb.asm.Opcodes.GETSTATIC,
                        "net/minecraft/world/level/block/state/properties/BlockSetType", "OAK",
                        "Lnet/minecraft/world/level/block/state/properties/BlockSetType;"));
                adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.SWAP));
                adaptation.add(new org.objectweb.asm.tree.IntInsnNode(org.objectweb.asm.Opcodes.BIPUSH, 30));
                adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.SWAP));
                method.instructions.insertBefore(call, adaptation);
                call.owner = "net/minecraft/world/level/block/ButtonBlock";
                call.desc = "(Lnet/minecraft/world/level/block/state/properties/BlockSetType;I" + properties + ")V";
                rewrites++;
            }
        }
        return rewrites;
    }

    /**
     * DirectionProperty was folded into the generic EnumProperty. Keep the
     * legacy factories behind a small bridge because the collection overload
     * also narrowed from Collection to List in the target runtime.
     */
    private static int rewriteLegacyDirectionPropertyFactories(ClassNode node) {
        int rewrites = 0;
        final String owner = "net/minecraft/world/level/block/state/properties/DirectionProperty";
        final String predicateDesc = "(Ljava/lang/String;Ljava/util/function/Predicate;)L" + owner + ";";
        final String collectionDesc = "(Ljava/lang/String;Ljava/util/Collection;)L" + owner + ";";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESTATIC
                        || !call.owner.equals(owner)) continue;
                if (call.desc.equals(predicateDesc)) {
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192DirectionPropertyBridge";
                    call.name = "create";
                    call.desc = "(Ljava/lang/String;Ljava/util/function/Predicate;)"
                            + "Lnet/minecraft/world/level/block/state/properties/EnumProperty;";
                    call.itf = false;
                    rewrites++;
                } else if (call.desc.equals(collectionDesc)) {
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192DirectionPropertyBridge";
                    call.name = "create";
                    call.desc = "(Ljava/lang/String;Ljava/util/Collection;)"
                            + "Lnet/minecraft/world/level/block/state/properties/EnumProperty;";
                    call.itf = false;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /**
     * The gameplay Material class was removed. For the verified METAL ->
     * Properties.of call shape, retain its surviving map-color semantics and
     * avoid relocating it to the unrelated client sprite Material record.
     */
    private static int rewriteLegacyMetalBlockProperties(ClassNode node) {
        int rewrites = 0;
        final String material = "net/minecraft/world/level/material/Material";
        final String properties = "net/minecraft/world/level/block/state/BlockBehaviour$Properties";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESTATIC
                        || !call.owner.equals(properties)
                        || !call.name.equals("m_60939_")
                        || !call.desc.equals("(L" + material + ";)L" + properties + ";")) continue;
                var previous = call.getPrevious();
                if (!(previous instanceof FieldInsnNode field)
                        || field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                        || !field.owner.equals(material)
                        || !field.desc.equals("L" + material + ";")) continue;
                String bridge = switch (field.name) {
                    case "f_76279_" -> "metalBlockProperties";
                    case "f_76275_" -> "glassBlockProperties";
                    default -> null;
                };
                if (bridge == null) continue;
                method.instructions.remove(field);
                call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge";
                call.name = bridge;
                call.desc = "()L" + properties + ";";
                call.itf = false;
                rewrites++;
            }
        }
        return rewrites;
    }

    /**
     * Modern blocks require their registry key in Properties before the Block
     * superclass constructor runs. Adapt only constructors whose own bytecode
     * proves which String parameter becomes the legacy registry name.
     */
    private static int rewriteLegacyNamedBlockConstruction(ClassNode node) {
        final String properties = "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;";
        int rewrites = 0;
        for (var method : node.methods) {
            if (!method.name.equals("<init>")) continue;
            RegistryNameEvidence registryName = legacyRegistryNameEvidence(node, method);
            if (registryName == null) continue;
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !(call.owner.equals("net/minecraft/world/level/block/Block")
                            || call.owner.equals("net/minecraft/world/level/block/BaseEntityBlock"))
                        || !call.name.equals("<init>")
                        || !call.desc.equals("(" + properties + ")V")) continue;
                var adaptation = new org.objectweb.asm.tree.InsnList();
                appendRegistryNameLoad(adaptation, registryName);
                adaptation.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                        "withBlockId", "(" + properties + "Ljava/lang/String;)" + properties, false));
                method.instructions.insertBefore(call, adaptation);
                rewrites++;
            }
        }
        return rewrites;
    }

    /** Attach a bytecode-proven legacy item name before Item reads Properties.id. */
    private static int rewriteLegacyNamedItemConstruction(ClassNode node) {
        final String properties = "Lnet/minecraft/world/item/Item$Properties;";
        int rewrites = 0;
        for (var method : node.methods) {
            if (!method.name.equals("<init>")) continue;
            RegistryNameEvidence registryName = legacyRegistryNameEvidence(node, method);
            if (registryName == null) continue;
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !call.owner.equals("net/minecraft/world/item/Item")
                        || !call.name.equals("<init>")
                        || !call.desc.equals("(" + properties + ")V")) continue;
                var adaptation = new org.objectweb.asm.tree.InsnList();
                appendRegistryNameLoad(adaptation, registryName);
                adaptation.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                        "withItemId", "(" + properties + "Ljava/lang/String;)" + properties, false));
                method.instructions.insertBefore(call, adaptation);
                rewrites++;
            }
        }
        return rewrites;
    }

    /** Restore legacy SRG-named overrides of Block#createBlockStateDefinition. */
    private int rewriteLegacyCableStateDefinitionOverride(ClassNode node) {
        if (!isManagedSubtypeOf(node, "net/minecraft/world/level/block/Block")) return 0;
        final String desc = "(Lnet/minecraft/world/level/block/state/StateDefinition$Builder;)V";
        for (var method : node.methods) {
            if (!method.name.equals("m_7926_") || !method.desc.equals(desc)) continue;
            var resolved = resolveCanonicalBlockMethod(method.name, method.desc);
            if (resolved == null
                    || !resolved.name().equals("createBlockStateDefinition")
                    || !resolved.descriptor().equals(desc)) continue;
            method.name = resolved.name();
            return 1;
        }
        return 0;
    }

    /**
     * Creative tabs are no longer an Item.Properties attribute. Preserve the
     * properties chain while leaving tab population to the modern tab event.
     */
    private static int rewriteLegacyCreativeTabAssignment(ClassNode node) {
        int rewrites = 0;
        final String owner = "net/minecraft/world/item/Item$Properties";
        final String desc = "(Lnet/minecraft/world/item/CreativeModeTab;)L" + owner + ";";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !call.owner.equals(owner)
                        || !call.name.equals("m_41491_")
                        || !call.desc.equals(desc)) continue;
                call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge";
                call.name = "recordLegacyCreativeTab";
                call.desc = "(L" + owner + ";Lnet/minecraft/world/item/CreativeModeTab;)L" + owner + ";";
                call.itf = false;
                rewrites++;
            }
        }
        return rewrites;
    }

    /** Supply paired block-item ids before the modern Item constructor requires them. */
    private static int rewriteLegacyBlockItemConstruction(ClassNode node) {
        if (!declaresLegacyRegistryNameAccessor(node)) return 0;
        final String desc = "(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/item/Item$Properties;)V";
        int rewrites = 0;
        for (var method : node.methods) {
            if (!method.name.equals("<init>")) continue;
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !call.owner.equals("net/minecraft/world/item/BlockItem")
                        || !call.name.equals("<init>")
                        || !call.desc.equals(desc)) continue;
                int propertiesLocal = method.maxLocals++;
                var adaptation = new org.objectweb.asm.tree.InsnList();
                adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ASTORE, propertiesLocal));
                adaptation.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.DUP));
                adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, propertiesLocal));
                adaptation.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                        "withBlockItemId",
                        "(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/item/Item$Properties;)"
                                + "Lnet/minecraft/world/item/Item$Properties;", false));
                method.instructions.insertBefore(call, adaptation);
                rewrites++;
            }
        }
        return rewrites;
    }

    /** Attach an explicit legacy registry name before a removed tool superclass runs. */
    private static int rewriteLegacyNamedSimpleToolConstruction(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            if (!method.name.equals("<init>")) continue;
            RegistryNameEvidence registryName = legacyRegistryNameEvidence(node, method);
            if (registryName == null) continue;
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !call.name.equals("<init>")
                        || !isLegacyToolConstructor(call.owner, call.desc)) continue;
                int propertiesLocal = method.maxLocals++;
                var adaptation = new org.objectweb.asm.tree.InsnList();
                adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ASTORE, propertiesLocal));
                adaptation.add(new org.objectweb.asm.tree.VarInsnNode(org.objectweb.asm.Opcodes.ALOAD, propertiesLocal));
                appendRegistryNameLoad(adaptation, registryName);
                adaptation.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                        "withItemId",
                        "(Lnet/minecraft/world/item/Item$Properties;Ljava/lang/String;)"
                                + "Lnet/minecraft/world/item/Item$Properties;", false));
                method.instructions.insertBefore(call, adaptation);
                rewrites++;
            }
        }
        return rewrites;
    }

    private static boolean isLegacyToolConstructor(String owner, String desc) {
        return switch (owner) {
            case "net/minecraft/world/item/AxeItem", "net/minecraft/world/item/ShovelItem" ->
                    desc.equals("(Lnet/minecraft/world/item/Tier;FFLnet/minecraft/world/item/Item$Properties;)V");
            case "net/minecraft/world/item/HoeItem", "net/minecraft/world/item/PickaxeItem",
                 "net/minecraft/world/item/SwordItem" ->
                    desc.equals("(Lnet/minecraft/world/item/Tier;IFLnet/minecraft/world/item/Item$Properties;)V");
            default -> false;
        };
    }

    /**
     * Locate a String parameter that the legacy constructor itself proves is
     * its registry name by passing it through Forge GameData#checkPrefix and
     * storing the resulting ResourceLocation on the constructed object.
     */
    private record RegistryNameEvidence(int local, boolean lowercase) {}

    private static RegistryNameEvidence legacyRegistryNameEvidence(
            ClassNode node, org.objectweb.asm.tree.MethodNode method) {
        RegistryNameEvidence match = null;
        for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof MethodInsnNode call)
                    || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESTATIC
                    || !call.owner.equals("net/minecraftforge/registries/GameData")
                    || !call.name.equals("checkPrefix")
                    || !call.desc.equals("(Ljava/lang/String;Z)Lnet/minecraft/resources/ResourceLocation;")) continue;
            var warn = previousExecutable(call);
            var producer = warn == null ? null : previousExecutable(warn);
            var store = nextExecutable(call);
            boolean lowercase = producer instanceof MethodInsnNode normalization
                    && normalization.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                    && normalization.owner.equals("java/lang/String")
                    && normalization.name.equals("toLowerCase")
                    && normalization.desc.equals("()Ljava/lang/String;");
            var name = lowercase ? previousExecutable(producer) : producer;
            if (warn == null || warn.getOpcode() != org.objectweb.asm.Opcodes.ICONST_0
                    || !(name instanceof org.objectweb.asm.tree.VarInsnNode load)
                    || load.getOpcode() != org.objectweb.asm.Opcodes.ALOAD
                    || !(store instanceof FieldInsnNode field)
                    || store.getOpcode() != org.objectweb.asm.Opcodes.PUTFIELD
                    || !field.owner.equals(node.name)
                    || !field.desc.equals("Lnet/minecraft/resources/ResourceLocation;")
                    || !isStringArgumentLocal(method, load.var)) continue;
            RegistryNameEvidence evidence = new RegistryNameEvidence(load.var, lowercase);
            if (match != null && !match.equals(evidence)) return null;
            match = evidence;
        }
        return match;
    }

    private static void appendRegistryNameLoad(org.objectweb.asm.tree.InsnList instructions,
                                               RegistryNameEvidence evidence) {
        instructions.add(new org.objectweb.asm.tree.VarInsnNode(
                org.objectweb.asm.Opcodes.ALOAD, evidence.local()));
        if (evidence.lowercase()) {
            instructions.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKEVIRTUAL,
                    "java/lang/String", "toLowerCase", "()Ljava/lang/String;", false));
        }
    }

    private static boolean declaresLegacyRegistryNameAccessor(ClassNode node) {
        return node.methods.stream().anyMatch(method -> method.name.equals("getRegistryName")
                && method.desc.equals("()Lnet/minecraft/resources/ResourceLocation;")
                && (method.access & org.objectweb.asm.Opcodes.ACC_STATIC) == 0);
    }

    private static boolean isStringArgumentLocal(org.objectweb.asm.tree.MethodNode method, int local) {
        int cursor = (method.access & org.objectweb.asm.Opcodes.ACC_STATIC) == 0 ? 1 : 0;
        for (Type argument : Type.getArgumentTypes(method.desc)) {
            if (cursor == local) return argument.equals(Type.getType(String.class));
            cursor += argument.getSize();
        }
        return false;
    }

    private static org.objectweb.asm.tree.AbstractInsnNode previousExecutable(
            org.objectweb.asm.tree.AbstractInsnNode instruction) {
        var cursor = instruction.getPrevious();
        while (cursor != null && cursor.getOpcode() < 0) cursor = cursor.getPrevious();
        return cursor;
    }

    private static org.objectweb.asm.tree.AbstractInsnNode nextExecutable(
            org.objectweb.asm.tree.AbstractInsnNode instruction) {
        var cursor = instruction.getNext();
        while (cursor != null && cursor.getOpcode() < 0) cursor = cursor.getNext();
        return cursor;
    }

    /**
     * VillagerProfession changed from a six-field record with a String name to
     * a seven-field record with a Component name and data-driven trade sets.
     * Legacy constructors had no trade-set mapping, so preserve the visible
     * literal name and supply an empty mapping without guessing a namespace.
     */
    private static int rewriteLegacyVillagerProfessionConstruction(ClassNode node) {
        int rewrites = 0;
        final String owner = "net/minecraft/world/entity/npc/VillagerProfession";
        final String desc = "(Ljava/lang/String;Ljava/util/function/Predicate;Ljava/util/function/Predicate;"
                + "Lcom/google/common/collect/ImmutableSet;Lcom/google/common/collect/ImmutableSet;"
                + "Lnet/minecraft/sounds/SoundEvent;)V";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !call.owner.equals(owner)
                        || !call.name.equals("<init>")
                        || !call.desc.equals(desc)) continue;

                TypeInsnNode allocation = null;
                org.objectweb.asm.tree.AbstractInsnNode duplicate = null;
                for (var candidate = call.getPrevious(); candidate != null; candidate = candidate.getPrevious()) {
                    if (candidate instanceof TypeInsnNode type
                            && type.getOpcode() == org.objectweb.asm.Opcodes.NEW
                            && type.desc.equals(owner)) {
                        var after = candidate.getNext();
                        if (after != null && after.getOpcode() == org.objectweb.asm.Opcodes.DUP) {
                            allocation = type;
                            duplicate = after;
                        }
                        break;
                    }
                }
                if (allocation == null) {
                    throw new IllegalStateException("Cannot pair legacy VillagerProfession allocation with constructor: "
                            + node.name + "#" + method.name + method.desc);
                }
                method.instructions.remove(allocation);
                method.instructions.remove(duplicate);
                call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192VillagerBridge";
                call.name = "createProfession";
                call.desc = desc.substring(0, desc.length() - 1)
                        + "Lnet/minecraft/world/entity/npc/villager/VillagerProfession;";
                call.itf = false;
                rewrites++;
            }
        }
        return rewrites;
    }

    private static void verifyNoRemovedForgeClientTypes(ClassNode node) {
        final String removed = "net/minecraftforge/client/gui/ModListScreen";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof TypeInsnNode type && type.desc.equals(removed)) {
                    throw new IllegalStateException("Removed Forge client type escaped adaptation: "
                            + node.name + "#" + method.name + method.desc + " -> " + removed);
                }
                if (insn instanceof MethodInsnNode call && call.owner.equals(removed)) {
                    throw new IllegalStateException("Removed Forge client call escaped adaptation: "
                            + node.name + "#" + method.name + method.desc + " -> "
                            + call.owner + "#" + call.name + call.desc);
                }
                if (insn instanceof FieldInsnNode field && field.owner.equals(removed)) {
                    throw new IllegalStateException("Removed Forge client field escaped adaptation: "
                            + node.name + "#" + method.name + method.desc + " -> "
                            + field.owner + "#" + field.name);
                }
            }
        }
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
            // A legacy-owned method may retain IForgeRegistry in its internal
            // ABI. The type itself is an opaque handle; this is equivalent to
            // retaining it in a field. Safety is enforced at executable API
            // boundaries below, where registry operations must be adapted.
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

    private int rewriteVerifiedSrgMethodCalls(ClassNode node) {
        if (SRG_RUNTIME_RESOLVER.isEmpty()) return 0;
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof InvokeDynamicInsnNode indy) {
                    for (int i = 0; i < indy.bsmArgs.length; i++) {
                        if (!(indy.bsmArgs[i] instanceof Handle handle)
                                || !handle.getName().startsWith("m_")) continue;
                        var resolved = handle.getOwner().startsWith("net/minecraft/")
                                ? SRG_RUNTIME_RESOLVER.get().resolve(
                                        handle.getOwner(), handle.getName(), handle.getDesc())
                                : resolveInheritedVanillaMethod(
                                        handle.getOwner(), handle.getName(), handle.getDesc());
                        if (resolved == null || !resolved.resolved()) continue;
                        String owner = handle.getOwner().startsWith("net/minecraft/")
                                ? resolved.owner() : handle.getOwner();
                        indy.bsmArgs[i] = new Handle(handle.getTag(), owner,
                                resolved.name(), resolved.descriptor(), handle.isInterface());
                        rewrites++;
                    }
                }
                if (!(insn instanceof MethodInsnNode call)) continue;

                // The 1.19.2 record accessor's SRG name is field-shaped and
                // therefore outside the normal m_* resolver. Its exact owner
                // and return type identify TagKey#location unambiguously.
                if (call.owner.equals("net/minecraft/tags/TagKey")
                        && call.name.equals("f_203868_")
                        && call.desc.equals("()Lnet/minecraft/resources/Identifier;")) {
                    call.name = "location";
                    rewrites++;
                }

                // ResourceLocation#getPath was projected to Identifier in the
                // target namespace; retain the exact 1.19.2 SRG member identity.
                if (call.owner.equals("net/minecraft/resources/Identifier")
                        && call.name.equals("m_135827_")
                        && call.desc.equals("()Ljava/lang/String;")) {
                    call.name = "getNamespace";
                    rewrites++;
                }

                if (call.owner.equals("net/minecraft/resources/Identifier")
                        && call.name.equals("m_135815_")
                        && call.desc.equals("()Ljava/lang/String;")) {
                    call.name = "getPath";
                    rewrites++;
                }

                // The legacy flag disabled anvil combination repair. That
                // surviving semantic is named setNoCombineRepair in 26.3.
                if (call.owner.equals("net/minecraft/world/item/Item$Properties")
                        && call.name.equals("setNoRepair")
                        && call.desc.equals("()Lnet/minecraft/world/item/Item$Properties;")) {
                    call.name = "setNoCombineRepair";
                    rewrites++;
                }

                if (call.owner.equals("net/minecraft/world/food/FoodProperties$Builder")) {
                    String bridgeName = null;
                    String bridgeDesc = null;
                    if (call.name.equals("m_38766_")
                            && call.desc.equals("()Lnet/minecraft/world/food/FoodProperties$Builder;")) {
                        bridgeName = "fastFood";
                        bridgeDesc = "(Lnet/minecraft/world/food/FoodProperties$Builder;)"
                                + "Lnet/minecraft/world/food/FoodProperties$Builder;";
                    } else if (call.name.equals("m_38765_")
                            && call.desc.equals("()Lnet/minecraft/world/food/FoodProperties$Builder;")) {
                        call.name = "alwaysEdible";
                        rewrites++;
                    } else if (call.name.equals("m_38758_")
                            && call.desc.equals("(F)Lnet/minecraft/world/food/FoodProperties$Builder;")) {
                        call.name = "saturationModifier";
                        rewrites++;
                    } else if (call.name.equals("effect")
                            && call.desc.equals("(Ljava/util/function/Supplier;F)"
                                    + "Lnet/minecraft/world/food/FoodProperties$Builder;")) {
                        bridgeName = "foodEffect";
                        bridgeDesc = "(Lnet/minecraft/world/food/FoodProperties$Builder;"
                                + "Ljava/util/function/Supplier;F)Lnet/minecraft/world/food/FoodProperties$Builder;";
                    } else if (call.name.equals("m_38767_")
                            && call.desc.equals("()Lnet/minecraft/world/food/FoodProperties;")) {
                        bridgeName = "buildFood";
                        bridgeDesc = "(Lnet/minecraft/world/food/FoodProperties$Builder;)"
                                + "Lnet/minecraft/world/food/FoodProperties;";
                    }
                    if (bridgeName != null) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge";
                        call.name = bridgeName;
                        call.desc = bridgeDesc;
                        call.itf = false;
                        rewrites++;
                    }
                }

                if (call.owner.equals("net/minecraft/world/item/Item$Properties")
                        && call.name.equals("food")
                        && call.desc.equals("(Lnet/minecraft/world/food/FoodProperties;)"
                                + "Lnet/minecraft/world/item/Item$Properties;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge";
                    call.desc = "(Lnet/minecraft/world/item/Item$Properties;Lnet/minecraft/world/food/FoodProperties;)"
                            + "Lnet/minecraft/world/item/Item$Properties;";
                    call.itf = false;
                    rewrites++;
                }

                // Builder#build migrated from an untyped legacy DFU name to
                // the corresponding EntityType ResourceKey. Match the exact
                // 1.19.2 SRG member, not merely its descriptor.
                if (call.owner.equals("net/minecraft/world/entity/EntityType$Builder")
                        && call.name.equals("m_20712_")
                        && call.desc.equals("(Ljava/lang/String;)Lnet/minecraft/world/entity/EntityType;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge";
                    call.name = "entityType";
                    call.desc = "(Lnet/minecraft/world/entity/EntityType$Builder;Ljava/lang/String;)Lnet/minecraft/world/entity/EntityType;";
                    call.itf = false;
                    rewrites++;
                }

                // javac records the concrete receiver class for inherited
                // calls. Walk the managed hierarchy to the declaring vanilla
                // ancestor, resolve the exact SRG identity there, and retain
                // the verified managed receiver for virtual dispatch.
                if (!call.owner.startsWith("net/minecraft/") && call.name.startsWith("m_")) {
                    var inherited = resolveInheritedVanillaMethod(call.owner, call.name, call.desc);
                    if (inherited != null) {
                        call.name = inherited.name();
                        call.desc = inherited.descriptor();
                        rewrites++;
                    }
                }
                if (call.owner.equals("net/minecraft/world/level/block/state/BlockState")) {
                    String inheritedName = switch (call.name) {
                        case "m_61122_" -> "cycle";
                        case "m_61147_" -> "getProperties";
                        case "m_61138_" -> "hasProperty";
                        case "m_61143_" -> "getValue";
                        case "m_61145_" -> "getOptionalValue";
                        case "m_61124_" -> "setValue";
                        default -> null;
                    };
                    if (inheritedName != null) {
                        call.name = inheritedName;
                        rewrites++;
                    }
                }

                if (call.owner.equals("net/minecraft/world/level/block/state/BlockState")
                        && call.name.equals("canSustainPlant")
                        && call.desc.equals("(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lcom/fuckingdeveloper/lms/runtime/block/LegacyIPlantable;)Z")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/block/LegacyIPlantable";
                    call.desc = "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lcom/fuckingdeveloper/lms/runtime/block/LegacyIPlantable;)Z";
                    call.itf = true;
                    rewrites++;
                }
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

    /**
     * The modern emissive predicate receives only BlockState. Adapt a legacy
     * three-argument StatePredicate only when its implementation bytecode
     * proves that BlockGetter and BlockPos are unused.
     */
    private static int rewriteLegacyStateOnlyEmissivePredicates(ClassNode node) {
        final String legacyPredicate = "(Lnet/minecraft/world/level/block/state/BlockState;"
                + "Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z";
        final String oldCall = "(Lnet/minecraft/world/level/block/state/BlockBehaviour$StatePredicate;)"
                + "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;";
        int rewrites = 0;
        java.util.List<org.objectweb.asm.tree.MethodNode> adapters = new java.util.ArrayList<>();
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        || !call.owner.equals("net/minecraft/world/level/block/state/BlockBehaviour$Properties")
                        || !call.name.equals("m_60991_") || !call.desc.equals(oldCall)) continue;
                var producer = previousExecutable(call);
                if (!(producer instanceof InvokeDynamicInsnNode indy)
                        || !indy.desc.equals("()Lnet/minecraft/world/level/block/state/BlockBehaviour$StatePredicate;")
                        || indy.bsmArgs.length < 3
                        || !(indy.bsmArgs[1] instanceof Handle implementation)
                        || implementation.getTag() != org.objectweb.asm.Opcodes.H_INVOKESTATIC
                        || !implementation.getOwner().equals(node.name)
                        || !implementation.getDesc().equals(legacyPredicate)) continue;
                var target = node.methods.stream()
                        .filter(candidate -> candidate.name.equals(implementation.getName())
                                && candidate.desc.equals(legacyPredicate)
                                && (candidate.access & org.objectweb.asm.Opcodes.ACC_STATIC) != 0)
                        .findFirst().orElse(null);
                if (target == null || usesPredicateContextArguments(target)) continue;

                String adapterName = "$lms$emissivePredicate$" + adapters.size();
                var adapter = new org.objectweb.asm.tree.MethodNode(
                        org.objectweb.asm.Opcodes.ACC_PRIVATE | org.objectweb.asm.Opcodes.ACC_STATIC
                                | org.objectweb.asm.Opcodes.ACC_SYNTHETIC,
                        adapterName, "(Lnet/minecraft/world/level/block/state/BlockState;)Z", null, null);
                adapter.instructions.add(new org.objectweb.asm.tree.VarInsnNode(
                        org.objectweb.asm.Opcodes.ALOAD, 0));
                adapter.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ACONST_NULL));
                adapter.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ACONST_NULL));
                adapter.instructions.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                        node.name, implementation.getName(), legacyPredicate, false));
                adapter.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.IRETURN));
                adapter.maxStack = 3;
                adapter.maxLocals = 1;
                adapters.add(adapter);

                indy.desc = "()Ljava/util/function/Predicate;";
                indy.bsmArgs[0] = Type.getMethodType("(Ljava/lang/Object;)Z");
                indy.bsmArgs[1] = new Handle(org.objectweb.asm.Opcodes.H_INVOKESTATIC,
                        node.name, adapterName,
                        "(Lnet/minecraft/world/level/block/state/BlockState;)Z", false);
                indy.bsmArgs[2] = Type.getMethodType(
                        "(Lnet/minecraft/world/level/block/state/BlockState;)Z");
                call.name = "emissiveRendering";
                call.desc = "(Ljava/util/function/Predicate;)"
                        + "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;";
                rewrites++;
            }
        }
        node.methods.addAll(adapters);
        return rewrites;
    }

    private static boolean usesPredicateContextArguments(org.objectweb.asm.tree.MethodNode method) {
        for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof org.objectweb.asm.tree.VarInsnNode variable
                    && (variable.var == 1 || variable.var == 2)) return true;
            if (insn instanceof org.objectweb.asm.tree.IincInsnNode increment
                    && (increment.var == 1 || increment.var == 2)) return true;
        }
        return false;
    }

    private Forge1192SrgRuntimeResolver.Resolution resolveCanonicalBlockMethod(String name, String desc) {
        if (!name.startsWith("m_") || SRG_RUNTIME_RESOLVER.isEmpty()) return null;
        var resolved = SRG_RUNTIME_RESOLVER.get().resolve(
                "net/minecraft/world/level/block/Block", name, desc);
        return resolved.resolved()
                && resolved.owner().equals("net/minecraft/world/level/block/Block")
                ? resolved : null;
    }

    private Forge1192SrgRuntimeResolver.Resolution resolveInheritedVanillaMethod(
            String receiver, String name, String desc) {
        if (SRG_RUNTIME_RESOLVER.isEmpty()) return null;
        java.util.ArrayDeque<String> pending = new java.util.ArrayDeque<>();
        java.util.HashSet<String> seen = new java.util.HashSet<>();
        pending.add(receiver);
        while (!pending.isEmpty()) {
            String current = pending.removeFirst();
            if (!seen.add(current)) continue;
            if (current.startsWith("net/minecraft/")) {
                var resolved = SRG_RUNTIME_RESOLVER.get().resolve(current, name, desc);
                if (resolved.resolved()) return resolved;
            }
            try {
                byte[] bytes = hierarchyBytes(current);
                if (bytes == null) continue;
                ClassReader reader = new ClassReader(bytes);
                if (reader.getSuperName() != null) pending.addLast(reader.getSuperName());
                java.util.Collections.addAll(pending, reader.getInterfaces());
            } catch (java.io.IOException | RuntimeException ignored) {
                // Incomplete hierarchy evidence fails closed.
            }
        }
        return null;
    }

    private byte[] hierarchyBytes(String internalName) throws java.io.IOException {
        byte[] bytes = managedClassBytes == null ? null : managedClassBytes.find(internalName);
        if (bytes != null) return bytes;
        if (!SafeFrameClassWriter.isParentRuntimeType(internalName)) return null;
        try (var stream = Forge1192LegacyClassTransformer.class.getClassLoader()
                .getResourceAsStream(internalName + ".class")) {
            return stream == null ? null : stream.readAllBytes();
        }
    }

    private boolean isManagedSubtypeOf(String candidate, String target) {
        java.util.ArrayDeque<String> pending = new java.util.ArrayDeque<>();
        pending.add(candidate);
        return hierarchyContains(pending, target);
    }

    private boolean isManagedSubtypeOf(ClassNode candidate, String target) {
        if (candidate.name.equals(target)) return true;
        java.util.ArrayDeque<String> pending = new java.util.ArrayDeque<>();
        if (candidate.superName != null) pending.add(candidate.superName);
        pending.addAll(candidate.interfaces);
        return hierarchyContains(pending, target);
    }

    private boolean hierarchyContains(java.util.ArrayDeque<String> pending, String target) {
        java.util.HashSet<String> seen = new java.util.HashSet<>();
        while (!pending.isEmpty()) {
            String current = pending.removeFirst();
            if (!seen.add(current)) continue;
            if (current.equals(target)) return true;
            try {
                byte[] bytes = hierarchyBytes(current);
                if (bytes == null) continue;
                ClassReader reader = new ClassReader(bytes);
                if (reader.getSuperName() != null) pending.addLast(reader.getSuperName());
                java.util.Collections.addAll(pending, reader.getInterfaces());
            } catch (java.io.IOException | LinkageError | RuntimeException ignored) {
                // Missing/corrupt hierarchy evidence must fail closed.
            }
        }
        return false;
    }

    private static int rewriteVerifiedSrgFieldAccesses(ClassNode node) {
        if (SRG_RUNTIME_RESOLVER.isEmpty()) return 0;
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof FieldInsnNode field)) continue;
                String mobEffect = field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals("net/minecraft/world/effect/MobEffects")
                        && field.desc.equals("Lnet/minecraft/world/effect/MobEffect;")
                        ? modernMobEffectField(field.name) : null;
                if (mobEffect != null) {
                    field.name = mobEffect;
                    field.desc = "Lnet/minecraft/core/Holder;";
                    var unwrap = new org.objectweb.asm.tree.InsnList();
                    unwrap.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKEINTERFACE,
                            "net/minecraft/core/Holder", "value", "()Ljava/lang/Object;", true));
                    unwrap.add(new TypeInsnNode(org.objectweb.asm.Opcodes.CHECKCAST,
                            "net/minecraft/world/effect/MobEffect"));
                    method.instructions.insert(field, unwrap);
                    rewrites++;
                    continue;
                }
                var resolved = SRG_RUNTIME_RESOLVER.get().resolveField(field.owner, field.name, field.desc);
                if (!resolved.resolved()) continue;
                if (!field.owner.equals(resolved.owner()) || !field.name.equals(resolved.name())
                        || !field.desc.equals(resolved.descriptor())) {
                    field.owner = resolved.owner();
                    field.name = resolved.name();
                    field.desc = resolved.descriptor();
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /** Exact 1.19.2 SRG identities for MobEffects' migration to Holder fields. */
    private static String modernMobEffectField(String srgName) {
        return switch (srgName) {
            case "f_19596_" -> "SPEED";
            case "f_19597_" -> "SLOWNESS";
            case "f_19598_" -> "HASTE";
            case "f_19599_" -> "MINING_FATIGUE";
            case "f_19600_" -> "STRENGTH";
            case "f_19601_" -> "INSTANT_HEALTH";
            case "f_19602_" -> "INSTANT_DAMAGE";
            case "f_19603_" -> "JUMP_BOOST";
            case "f_19604_" -> "NAUSEA";
            case "f_19605_" -> "REGENERATION";
            case "f_19606_" -> "RESISTANCE";
            case "f_19607_" -> "FIRE_RESISTANCE";
            case "f_19608_" -> "WATER_BREATHING";
            case "f_19609_" -> "INVISIBILITY";
            case "f_19610_" -> "BLINDNESS";
            case "f_19611_" -> "NIGHT_VISION";
            case "f_19612_" -> "HUNGER";
            case "f_19613_" -> "WEAKNESS";
            case "f_19614_" -> "POISON";
            case "f_19615_" -> "WITHER";
            case "f_19616_" -> "HEALTH_BOOST";
            case "f_19617_" -> "ABSORPTION";
            case "f_19618_" -> "SATURATION";
            case "f_19619_" -> "GLOWING";
            case "f_19620_" -> "LEVITATION";
            case "f_19621_" -> "LUCK";
            case "f_19590_" -> "UNLUCK";
            case "f_19591_" -> "SLOW_FALLING";
            case "f_19592_" -> "CONDUIT_POWER";
            case "f_19593_" -> "DOLPHINS_GRACE";
            case "f_19594_" -> "BAD_OMEN";
            case "f_19595_" -> "HERO_OF_THE_VILLAGE";
            case "f_216964_" -> "DARKNESS";
            default -> null;
        };
    }

    /**
     * MobEffectInstance constructors now accept Holder<MobEffect>. Preserve
     * legacy raw-effect construction through the built-in registry holder.
     */
    private static int rewriteLegacyMobEffectInstanceConstruction(ClassNode node) {
        int rewrites = 0;
        String owner = "net/minecraft/world/effect/MobEffectInstance";
        String legacyPrefix = "(Lnet/minecraft/world/effect/MobEffect;";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)
                        || call.getOpcode() != org.objectweb.asm.Opcodes.INVOKESPECIAL
                        || !call.owner.equals(owner)
                        || !call.name.equals("<init>")
                        || !call.desc.startsWith(legacyPrefix)) continue;
                Type[] arguments = Type.getArgumentTypes(call.desc);
                int[] locals = new int[arguments.length];
                var adaptation = new org.objectweb.asm.tree.InsnList();
                for (int i = arguments.length - 1; i >= 1; i--) {
                    locals[i] = method.maxLocals;
                    method.maxLocals += arguments[i].getSize();
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(
                            arguments[i].getOpcode(org.objectweb.asm.Opcodes.ISTORE), locals[i]));
                }
                adaptation.add(new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                        "mobEffectHolder",
                        "(Lnet/minecraft/world/effect/MobEffect;)Lnet/minecraft/core/Holder;", false));
                for (int i = 1; i < arguments.length; i++) {
                    adaptation.add(new org.objectweb.asm.tree.VarInsnNode(
                            arguments[i].getOpcode(org.objectweb.asm.Opcodes.ILOAD), locals[i]));
                }
                method.instructions.insertBefore(call, adaptation);
                Type[] modern = arguments.clone();
                modern[0] = Type.getObjectType("net/minecraft/core/Holder");
                call.desc = Type.getMethodDescriptor(Type.VOID_TYPE, modern);
                rewrites++;
            }
        }
        return rewrites;
    }

    private static int rewriteLegacyForgeBuiltinFields(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof FieldInsnNode field)
                        || field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                        || !field.owner.equals("net/minecraftforge/common/ForgeMod")) continue;
                if (field.name.equals("MILK")
                        && (field.desc.equals("Lnet/minecraftforge/registries/RegistryObject;")
                        || field.desc.equals("Lcom/fuckingdeveloper/lms/runtime/registry/LegacyRegistryObject;"))) {
                    MethodInsnNode replacement = new MethodInsnNode(
                            org.objectweb.asm.Opcodes.INVOKESTATIC,
                            "com/fuckingdeveloper/lms/runtime/Forge1192BuiltinRegistryBridge",
                            "milk",
                            "()Lcom/fuckingdeveloper/lms/runtime/registry/LegacyRegistryObject;",
                            false);
                    method.instructions.set(field, replacement);
                    rewrites++;
                } else if (field.desc.equals("Lcom/fuckingdeveloper/lms/runtime/registry/LegacyRegistryObject;")) {
                    String bridgeMethod = switch (field.name) {
                        case "REACH_DISTANCE" -> "reachDistance";
                        case "ATTACK_RANGE" -> "attackRange";
                        case "ENTITY_GRAVITY" -> "entityGravity";
                        case "STEP_HEIGHT_ADDITION" -> "stepHeightAddition";
                        default -> null;
                    };
                    if (bridgeMethod != null) {
                        method.instructions.set(field, new MethodInsnNode(
                                org.objectweb.asm.Opcodes.INVOKESTATIC,
                                "com/fuckingdeveloper/lms/runtime/Forge1192BuiltinRegistryBridge",
                                bridgeMethod,
                                "()Lcom/fuckingdeveloper/lms/runtime/registry/LegacyRegistryObject;",
                                false));
                        rewrites++;
                    }
                }
            }
        }
        return rewrites;
    }

    /**
     * Attribute modifiers moved from UUID/name identity to Identifier identity.
     * Preserve the UUID as the authoritative stable key and adapt the complete
     * construction/query/removal family together.
     */
    private static int rewriteLegacyAttributeModifiers(ClassNode node) {
        final String modifier = "net/minecraft/world/entity/ai/attributes/AttributeModifier";
        final String operation = modifier + "$Operation";
        final String legacyConstructor = "(Ljava/util/UUID;Ljava/lang/String;DL" + operation + ";)V";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; ) {
                var next = insn.getNext();
                if (insn instanceof TypeInsnNode allocation
                        && allocation.getOpcode() == org.objectweb.asm.Opcodes.NEW
                        && allocation.desc.equals(modifier)) {
                    var duplicate = allocation.getNext();
                    MethodInsnNode constructor = null;
                    int nested = 0;
                    if (duplicate != null && duplicate.getOpcode() == org.objectweb.asm.Opcodes.DUP) {
                        for (var cursor = duplicate.getNext(); cursor != null; cursor = cursor.getNext()) {
                            if (cursor instanceof TypeInsnNode nestedType
                                    && nestedType.getOpcode() == org.objectweb.asm.Opcodes.NEW) nested++;
                            else if (cursor instanceof MethodInsnNode call
                                    && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                                    && call.name.equals("<init>")) {
                                if (nested > 0) nested--;
                                else if (call.owner.equals(modifier)) { constructor = call; break; }
                            }
                        }
                    }
                    if (constructor != null && constructor.desc.equals(legacyConstructor)) {
                        method.instructions.remove(allocation);
                        method.instructions.remove(duplicate);
                        var factory = new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC,
                                "com/fuckingdeveloper/lms/runtime/Forge1192AttributeBridge",
                                "modifier", "(Ljava/util/UUID;Ljava/lang/String;DL" + operation + ";)L"
                                        + modifier + ";", false);
                        method.instructions.set(constructor, factory);
                        next = factory.getNext();
                        rewrites++;
                    }
                } else if (insn instanceof FieldInsnNode field
                        && field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals(operation)) {
                    String modern = switch (field.name) {
                        case "ADDITION" -> "ADD_VALUE";
                        case "MULTIPLY_BASE" -> "ADD_MULTIPLIED_BASE";
                        case "MULTIPLY_TOTAL" -> "ADD_MULTIPLIED_TOTAL";
                        default -> null;
                    };
                    if (modern != null) {
                        field.name = modern;
                        rewrites++;
                    }
                } else if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKEVIRTUAL
                        && call.owner.equals("net/minecraft/world/entity/ai/attributes/AttributeInstance")) {
                    if (call.name.equals("m_22111_")
                            && call.desc.equals("(Ljava/util/UUID;)L" + modifier + ";")) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192AttributeBridge";
                        call.name = "getModifier";
                        call.desc = "(Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;Ljava/util/UUID;)L"
                                + modifier + ";";
                        call.itf = false;
                        rewrites++;
                    } else if (call.name.equals("m_22120_")
                            && call.desc.equals("(Ljava/util/UUID;)V")) {
                        call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                        call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192AttributeBridge";
                        call.name = "removeModifier";
                        call.desc = "(Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;Ljava/util/UUID;)V";
                        call.itf = false;
                        rewrites++;
                    }
                }
                insn = next;
            }
        }
        return rewrites;
    }

    /** Registry static fields moved to BuiltInRegistries after 1.19.2. */
    private static int rewriteLegacyVanillaRegistryFields(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof FieldInsnNode field
                        && field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals("net/minecraft/core/Registry")
                        && field.name.equals("f_122832_")
                        && field.desc.equals("Lnet/minecraft/core/Registry;")) {
                    field.owner = "net/minecraft/core/registries/BuiltInRegistries";
                    field.name = "CUSTOM_STAT";
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    /**
     * Forge 1.19.2 exposed process-global registry objects through
     * ForgeRegistries. Current built-in registries are the same concrete
     * Registry instances under BuiltInRegistries/NeoForgeRegistries. Replace
     * only fields with a verified process-global target; data-driven registries
     * deliberately remain migration boundaries because they require a
     * RegistryAccess rather than a fabricated global facade.
     */
    private static int rewriteLegacyForgeRegistries(ClassNode node) {
        int rewrites = 0;
        final String legacyOwner = "net/minecraftforge/registries/ForgeRegistries";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; ) {
                var next = insn.getNext();
                if (!(insn instanceof FieldInsnNode field)
                        || !field.owner.equals(legacyOwner)) {
                    insn = next;
                    continue;
                }
                if (field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC) {
                    throw new IllegalStateException("Unsupported ForgeRegistries field write: "
                            + node.name + "#" + method.name + " " + field.name);
                }

                if (field.name.equals("FLUID_TYPES")
                        && field.desc.equals("Ljava/util/function/Supplier;")) {
                    var supplierGet = nextRealInstruction(field.getNext());
                    var cast = nextRealInstruction(supplierGet == null ? null : supplierGet.getNext());
                    if (!(supplierGet instanceof MethodInsnNode get)
                            || get.getOpcode() != org.objectweb.asm.Opcodes.INVOKEINTERFACE
                            || !get.owner.equals("java/util/function/Supplier")
                            || !get.name.equals("get")
                            || !get.desc.equals("()Ljava/lang/Object;")
                            || !(cast instanceof TypeInsnNode type)
                            || type.getOpcode() != org.objectweb.asm.Opcodes.CHECKCAST
                            || !type.desc.equals("com/fuckingdeveloper/lms/runtime/registry/LegacyForgeRegistry")) {
                        throw new IllegalStateException("Unsupported ForgeRegistries.FLUID_TYPES access pattern: "
                                + node.name + "#" + method.name);
                    }
                    field.owner = "net/neoforged/neoforge/registries/NeoForgeRegistries";
                    field.desc = "Lnet/minecraft/core/Registry;";
                    next = cast.getNext();
                    method.instructions.remove(supplierGet);
                    method.instructions.remove(cast);
                    rewrites++;
                    insn = next;
                    continue;
                }

                String targetName = switch (field.name) {
                    case "BLOCKS" -> "BLOCK";
                    case "BLOCK_ENTITY_TYPES" -> "BLOCK_ENTITY_TYPE";
                    case "ENTITY_TYPES" -> "ENTITY_TYPE";
                    case "FLUIDS" -> "FLUID";
                    case "FOLIAGE_PLACER_TYPES" -> "FOLIAGE_PLACER_TYPE";
                    case "ITEMS" -> "ITEM";
                    case "MOB_EFFECTS" -> "MOB_EFFECT";
                    case "POI_TYPES" -> "POINT_OF_INTEREST_TYPE";
                    case "RECIPE_SERIALIZERS" -> "RECIPE_SERIALIZER";
                    case "VILLAGER_PROFESSIONS" -> "VILLAGER_PROFESSION";
                    default -> null;
                };
                if (targetName == null) {
                    String bridgeMethod = switch (field.name) {
                        case "ENCHANTMENTS" -> "enchantments";
                        case "PAINTING_VARIANTS" -> "paintingVariants";
                        default -> null;
                    };
                    if (bridgeMethod != null) {
                        method.instructions.set(field, new MethodInsnNode(
                                org.objectweb.asm.Opcodes.INVOKESTATIC,
                                "com/fuckingdeveloper/lms/runtime/Forge1192DynamicRegistryBridge",
                                bridgeMethod,
                                "()Lnet/minecraft/core/Registry;",
                                false));
                        rewrites++;
                        insn = next;
                        continue;
                    }
                    throw new IllegalStateException("Forge 1.19.2 registry has no verified process-global target: "
                            + node.name + "#" + method.name + " ForgeRegistries." + field.name);
                }
                field.owner = "net/minecraft/core/registries/BuiltInRegistries";
                field.name = targetName;
                field.desc = switch (targetName) {
                    case "BLOCK", "ENTITY_TYPE", "FLUID", "ITEM", "VILLAGER_PROFESSION" ->
                            "Lnet/minecraft/core/DefaultedRegistry;";
                    default -> "Lnet/minecraft/core/Registry;";
                };
                rewrites++;
                insn = next;
            }
        }
        return rewrites;
    }

    private static org.objectweb.asm.tree.AbstractInsnNode nextRealInstruction(
            org.objectweb.asm.tree.AbstractInsnNode instruction) {
        while (instruction != null && instruction.getOpcode() < 0) instruction = instruction.getNext();
        return instruction;
    }

    /**
     * ForgeRegistries.Keys held registry ResourceKey constants in 1.19.2.
     * The modern vanilla keys live in Registries (mostly singular names);
     * NeoForge-only keys live in NeoForgeRegistries.Keys. Resolve actual
     * runtime fields instead of defining a counterfeit ForgeRegistries.Keys.
     */
    private static int rewriteLegacyForgeRegistryKeys(ClassNode node) {
        int rewrites = 0;
        String legacy = "net/minecraftforge/registries/ForgeRegistries$Keys";
        String vanilla = "net/minecraft/core/registries/Registries";
        String neoforge = "net/neoforged/neoforge/registries/NeoForgeRegistries$Keys";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof FieldInsnNode field) || !field.owner.equals(legacy)) continue;
                if (field.getOpcode() != org.objectweb.asm.Opcodes.GETSTATIC
                        || !field.desc.equals("Lnet/minecraft/resources/ResourceKey;"))
                    throw new IllegalStateException("Unsupported legacy registry key access: "
                            + node.name + "#" + method.name + " " + field.name + ":" + field.desc);
                String singular = switch (field.name) {
                    case "BLOCKS" -> "BLOCK";
                    case "ITEMS" -> "ITEM";
                    case "FLUIDS" -> "FLUID";
                    case "ENTITY_TYPES" -> "ENTITY_TYPE";
                    case "MOB_EFFECTS" -> "MOB_EFFECT";
                    case "SOUND_EVENTS" -> "SOUND_EVENT";
                    case "POTIONS" -> "POTION";
                    case "ENCHANTMENTS" -> "ENCHANTMENT";
                    case "BLOCK_ENTITY_TYPES" -> "BLOCK_ENTITY_TYPE";
                    case "PARTICLE_TYPES" -> "PARTICLE_TYPE";
                    case "MENU_TYPES" -> "MENU";
                    case "RECIPE_TYPES" -> "RECIPE_TYPE";
                    case "RECIPE_SERIALIZERS" -> "RECIPE_SERIALIZER";
                    case "ATTRIBUTES" -> "ATTRIBUTE";
                    case "STAT_TYPES" -> "STAT_TYPE";
                    case "COMMAND_ARGUMENT_TYPES" -> "COMMAND_ARGUMENT_TYPE";
                    case "VILLAGER_PROFESSIONS" -> "VILLAGER_PROFESSION";
                    case "POI_TYPES" -> "POINT_OF_INTEREST_TYPE";
                    case "MEMORY_MODULE_TYPES" -> "MEMORY_MODULE_TYPE";
                    case "SENSOR_TYPES" -> "SENSOR_TYPE";
                    case "SCHEDULES" -> "SCHEDULE";
                    case "ACTIVITIES" -> "ACTIVITY";
                    case "LOOT_POOL_ENTRY_TYPES" -> "LOOT_POOL_ENTRY_TYPE";
                    case "LOOT_FUNCTION_TYPES" -> "LOOT_FUNCTION_TYPE";
                    case "LOOT_CONDITION_TYPES" -> "LOOT_CONDITION_TYPE";
                    case "LOOT_NUMBER_PROVIDER_TYPES" -> "LOOT_NUMBER_PROVIDER_TYPE";
                    case "LOOT_NBT_PROVIDER_TYPES" -> "LOOT_NBT_PROVIDER_TYPE";
                    case "LOOT_SCORE_PROVIDER_TYPES" -> "LOOT_SCORE_PROVIDER_TYPE";
                    case "FLOAT_PROVIDER_TYPES" -> "FLOAT_PROVIDER_TYPE";
                    case "INT_PROVIDER_TYPES" -> "INT_PROVIDER_TYPE";
                    case "HEIGHT_PROVIDER_TYPES" -> "HEIGHT_PROVIDER_TYPE";
                    case "BLOCK_STATE_PROVIDER_TYPES" -> "BLOCK_STATE_PROVIDER_TYPE";
                    case "FOLIAGE_PLACER_TYPES" -> "FOLIAGE_PLACER_TYPE";
                    case "TRUNK_PLACER_TYPES" -> "TRUNK_PLACER_TYPE";
                    case "TREE_DECORATOR_TYPES" -> "TREE_DECORATOR_TYPE";
                    case "FEATURE_SIZE_TYPES" -> "FEATURE_SIZE_TYPE";
                    case "PLACEMENT_MODIFIER_TYPES" -> "PLACEMENT_MODIFIER_TYPE";
                    case "CARVER_TYPES" -> "CARVER";
                    case "FEATURES" -> "FEATURE";
                    case "STRUCTURE_PIECE_TYPES" -> "STRUCTURE_PIECE";
                    case "STRUCTURE_TYPES" -> "STRUCTURE_TYPE";
                    case "STRUCTURE_PLACEMENT_TYPES" -> "STRUCTURE_PLACEMENT";
                    case "STRUCTURE_PROCESSOR_TYPES" -> "STRUCTURE_PROCESSOR";
                    case "STRUCTURE_POOL_ELEMENT_TYPES" -> "STRUCTURE_POOL_ELEMENT";
                    case "CHUNK_STATUS" -> "CHUNK_STATUS";
                    case "RULE_TEST_TYPES" -> "RULE_TEST";
                    case "POS_RULE_TEST_TYPES" -> "POS_RULE_TEST";
                    case "PAINTING_VARIANTS" -> "PAINTING_VARIANT";
                    default -> field.name;
                };
                if (runtimeHasRegistryKey(vanilla, singular)) {
                    field.owner = vanilla;
                    field.name = singular;
                } else if (runtimeHasRegistryKey(neoforge, field.name)) {
                    field.owner = neoforge;
                } else {
                    throw new IllegalStateException("Unresolved ForgeRegistries.Keys mapping: "
                            + node.name + "#" + method.name + " key=" + field.name
                            + " candidate=" + singular);
                }
                rewrites++;
            }
        }
        return rewrites;
    }

    private static boolean runtimeHasRegistryKey(String owner, String name) {
        try {
            Class<?> type = Class.forName(owner.replace('/', '.'), false,
                    Forge1192LegacyClassTransformer.class.getClassLoader());
            return type.getField(name).getType().getName()
                    .equals("net.minecraft.resources.ResourceKey");
        } catch (ClassNotFoundException | NoSuchFieldException | LinkageError ignored) {
            return false;
        }
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

    private static int rewriteLegacyTickEventFields(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null;) {
                var next = insn.getNext();
                if (insn instanceof FieldInsnNode field
                        && field.getOpcode() == org.objectweb.asm.Opcodes.GETFIELD) {
                    MethodInsnNode replacement = legacyTickFieldAdapter(field);
                    if (replacement != null) {
                        method.instructions.set(field, replacement);
                        rewrites++;
                    }
                }
                insn = next;
            }
        }
        return rewrites;
    }

    private static MethodInsnNode legacyTickFieldAdapter(FieldInsnNode field) {
        String bridge = "com/fuckingdeveloper/lms/runtime/Forge1192TickEventBridge";
        boolean tickOwner = field.owner.equals("net/neoforged/neoforge/event/tick/ServerTickEvent")
                || field.owner.equals("net/neoforged/neoforge/event/tick/LevelTickEvent")
                || field.owner.equals("net/neoforged/neoforge/event/tick/PlayerTickEvent")
                || field.owner.equals("net/neoforged/neoforge/client/event/ClientTickEvent");
        if (tickOwner && field.name.equals("phase")) {
            return new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC, bridge, "phase",
                    "(Ljava/lang/Object;)Lcom/fuckingdeveloper/lms/runtime/Forge1192TickEventBridge$Phase;",
                    false);
        }
        if (field.owner.equals("net/neoforged/neoforge/event/tick/PlayerTickEvent")) {
            if (field.name.equals("side")) {
                return new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC, bridge, "side",
                        "(Lnet/neoforged/neoforge/event/tick/PlayerTickEvent;)Lnet/neoforged/fml/LogicalSide;",
                        false);
            }
            if (field.name.equals("player")) {
                return new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC, bridge, "player",
                        "(Lnet/neoforged/neoforge/event/tick/PlayerTickEvent;)Lnet/minecraft/world/entity/player/Player;",
                        false);
            }
        }
        if (field.owner.equals("net/neoforged/neoforge/event/tick/LevelTickEvent")) {
            if (field.name.equals("side")) {
                return new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC, bridge, "side",
                        "(Lnet/neoforged/neoforge/event/tick/LevelTickEvent;)Lnet/neoforged/fml/LogicalSide;",
                        false);
            }
            if (field.name.equals("level")) {
                return new MethodInsnNode(org.objectweb.asm.Opcodes.INVOKESTATIC, bridge, "level",
                        "(Lnet/neoforged/neoforge/event/tick/LevelTickEvent;)Lnet/minecraft/world/level/Level;",
                        false);
            }
        }
        return null;
    }

    private static int rewriteLegacySpawnCheckSemantics(ClassNode node) {
        int rewrites = 0;
        String eventOwner = "net/neoforged/neoforge/event/entity/living/MobSpawnEvent$PositionCheck";
        String resultOwner = eventOwner + "$Result";
        for (var method : node.methods) {
            boolean hasPositionResult = false;
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call && call.owner.equals(eventOwner)
                        && call.name.equals("setResult")) {
                    hasPositionResult = true;
                    break;
                }
            }
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call && call.owner.equals(eventOwner)) {
                    if (call.name.equals("getSpawnReason")
                            && call.desc.equals("()Lnet/minecraft/world/entity/EntitySpawnReason;")) {
                        call.name = "getSpawnType";
                        rewrites++;
                    } else if (call.name.equals("setResult")
                            && call.desc.equals("(Lnet/neoforged/bus/api/Event$Result;)V")) {
                        call.desc = "(L" + resultOwner + ";)V";
                        rewrites++;
                    }
                } else if (hasPositionResult && insn instanceof FieldInsnNode field
                        && field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                        && field.owner.equals("net/neoforged/bus/api/Event$Result")
                        && field.name.equals("DENY")) {
                    field.owner = resultOwner;
                    field.name = "FAIL";
                    field.desc = "L" + resultOwner + ";";
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    private static int rewriteLegacyIdentifierConstruction(ClassNode node) {
        int rewrites = 0;
        final String identifier = "net/minecraft/resources/Identifier";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; ) {
                var next = insn.getNext();
                if (!(insn instanceof TypeInsnNode allocation)
                        || allocation.getOpcode() != org.objectweb.asm.Opcodes.NEW
                        || !allocation.desc.equals(identifier)) {
                    insn = next;
                    continue;
                }
                var dup = allocation.getNext();
                if (dup == null || dup.getOpcode() != org.objectweb.asm.Opcodes.DUP) {
                    throw new IllegalStateException("Unsupported legacy Identifier allocation: "
                            + node.name + "#" + method.name);
                }
                MethodInsnNode init = null;
                int nested = 0;
                for (var cursor = dup.getNext(); cursor != null; cursor = cursor.getNext()) {
                    if (cursor instanceof TypeInsnNode type
                            && type.getOpcode() == org.objectweb.asm.Opcodes.NEW) {
                        nested++;
                    } else if (cursor instanceof MethodInsnNode call
                            && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                            && call.name.equals("<init>")) {
                        if (nested > 0) {
                            nested--;
                        } else if (call.owner.equals(identifier)) {
                            init = call;
                            break;
                        }
                    }
                }
                if (init == null) {
                    throw new IllegalStateException("Cannot match legacy Identifier constructor: "
                            + node.name + "#" + method.name);
                }
                String factoryDesc = switch (init.desc) {
                    case "(Ljava/lang/String;)V" ->
                            "(Ljava/lang/String;)Lnet/minecraft/resources/Identifier;";
                    case "(Ljava/lang/String;Ljava/lang/String;)V" ->
                            "(Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/resources/Identifier;";
                    default -> throw new IllegalStateException("Unsupported legacy Identifier constructor: "
                            + node.name + "#" + method.name + init.desc);
                };
                method.instructions.remove(allocation);
                method.instructions.remove(dup);
                MethodInsnNode factory = new MethodInsnNode(
                        org.objectweb.asm.Opcodes.INVOKESTATIC,
                        "com/fuckingdeveloper/lms/runtime/Forge1192IdentifierBridge",
                        "create", factoryDesc, false);
                method.instructions.set(init, factory);
                rewrites++;
                // 'next' was the DUP we just removed. Continuing from that
                // detached node terminates ASM traversal after the first
                // ResourceLocation in a method. Resume after the replacement
                // factory so every allocation in large <clinit> methods is adapted.
                insn = factory.getNext();
            }
        }
        return rewrites;
    }

    /**
     * CraftingContainer changed from a concrete 1.19.2 inventory to an interface.
     * TransientCraftingContainer is its vanilla concrete successor. Only migrate
     * construction when the target runtime proves an identical public constructor.
     */
    /**
     * ArmorMaterial no longer exists in the target runtime. After ArmorItem is
     * structurally redirected to LegacyArmorItem, erase only that constructor
     * parameter to Object. The runtime value remains an opaque legacy token.
     */
    private static int rewriteLegacyArmorConstructorDescriptors(ClassNode node) {
        final String owner = "com/fuckingdeveloper/lms/runtime/item/LegacyArmorItem";
        final String legacy = "(Lnet/minecraft/world/item/ArmorMaterial;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/Item$Properties;)V";
        final String erased = "(Ljava/lang/Object;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/Item$Properties;)V";
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                        && call.owner.equals(owner)
                        && call.name.equals("<init>")
                        && call.desc.equals(legacy)) {
                    call.desc = erased;
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    private static int rewriteLegacyBlockEntityTypeConstruction(ClassNode node) {
        int rewrites = 0;
        final String owner = "net/minecraft/world/level/block/entity/BlockEntityType";
        final String legacyDesc = "(Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;Ljava/util/Set;Lcom/mojang/datafixers/types/Type;)V";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; ) {
                var next = insn.getNext();
                if (insn instanceof TypeInsnNode allocation
                        && allocation.getOpcode() == org.objectweb.asm.Opcodes.NEW
                        && allocation.desc.equals(owner)) {
                    var dup = allocation.getNext();
                    MethodInsnNode init = null;
                    int nested = 0;
                    if (dup != null && dup.getOpcode() == org.objectweb.asm.Opcodes.DUP) {
                        for (var cursor = dup.getNext(); cursor != null; cursor = cursor.getNext()) {
                            if (cursor instanceof TypeInsnNode nestedType
                                    && nestedType.getOpcode() == org.objectweb.asm.Opcodes.NEW) nested++;
                            else if (cursor instanceof MethodInsnNode call
                                    && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                                    && call.name.equals("<init>")) {
                                if (nested > 0) nested--;
                                else if (call.owner.equals(owner)) { init = call; break; }
                            }
                        }
                    }
                    if (init != null && init.desc.equals(legacyDesc)) {
                        method.instructions.remove(allocation);
                        method.instructions.remove(dup);
                        MethodInsnNode factory = new MethodInsnNode(
                                org.objectweb.asm.Opcodes.INVOKESTATIC,
                                "com/fuckingdeveloper/lms/runtime/Forge1192VanillaConstructionBridge",
                                "blockEntityType",
                                "(Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;Ljava/util/Set;Lcom/mojang/datafixers/types/Type;)Lnet/minecraft/world/level/block/entity/BlockEntityType;",
                                false);
                        method.instructions.set(init, factory);
                        next = factory.getNext();
                        rewrites++;
                    }
                }
                insn = next;
            }
            // Legacy subclasses invoke the removed three-argument constructor
            // directly on super and therefore have no NEW/DUP pair to replace.
            // Discard the obsolete DFU Type value and bind to the verified
            // 26.3 direct constructor while preserving supplier + valid blocks.
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                        && call.owner.equals(owner)
                        && call.name.equals("<init>")
                        && call.desc.equals(legacyDesc)) {
                    method.instructions.insertBefore(call,
                            new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.POP));
                    call.desc = "(Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;Ljava/util/Set;)V";
                    rewrites++;
                }
            }
        }
        return rewrites;
    }

    private static int rewriteLegacyCraftingContainerConstruction(ClassNode node) {
        final String legacy = "net/minecraft/world/inventory/CraftingContainer";
        final String target = "net/minecraft/world/inventory/TransientCraftingContainer";
        int rewrites = 0;
        var constructors = VANILLA_RELOCATIONS.constructorsOf(target);
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof TypeInsnNode allocation)
                        || allocation.getOpcode() != org.objectweb.asm.Opcodes.NEW
                        || !allocation.desc.equals(legacy)) continue;
                // Only pair a standard NEW/DUP with its matching constructor.
                var dup = allocation.getNext();
                if (dup == null || dup.getOpcode() != org.objectweb.asm.Opcodes.DUP) {
                    throw new IllegalStateException("Unsupported legacy CraftingContainer allocation pattern: "
                            + node.name + "#" + method.name);
                }
                MethodInsnNode init = null;
                int nestedAllocations = 0;
                for (var cursor = dup.getNext(); cursor != null; cursor = cursor.getNext()) {
                    if (cursor instanceof TypeInsnNode nested
                            && nested.getOpcode() == org.objectweb.asm.Opcodes.NEW) {
                        nestedAllocations++;
                        continue;
                    }
                    if (cursor instanceof MethodInsnNode candidate
                            && candidate.getOpcode() == org.objectweb.asm.Opcodes.INVOKESPECIAL
                            && candidate.name.equals("<init>")) {
                        if (nestedAllocations > 0) {
                            nestedAllocations--;
                            continue;
                        }
                        if (candidate.owner.equals(legacy)) {
                            init = candidate;
                        }
                        break;
                    }
                }
                if (init == null) {
                    throw new IllegalStateException("Cannot pair legacy CraftingContainer allocation with constructor: "
                            + node.name + "#" + method.name);
                }
                if (!constructors.contains(init.desc)) {
                    throw new IllegalStateException("CraftingContainer constructor requires semantic adapter: "
                            + node.name + "#" + method.name + init.desc
                            + "; target public constructors=" + constructors);
                }
                allocation.desc = target;
                init.owner = target;
                rewrites++;
            }
        }
        return rewrites;
    }

    private static int rewriteSemanticAdapters(ClassNode node) {
        int rewrites = 0;
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode call)) continue;

                // Authlib's legacy UUIDTypeAdapter helper is not ABI-stable across
                // Minecraft generations. Preserve the Forge 1.19.2 executable
                // contract behind an LMS boundary instead of linking against the
                // target Authlib implementation by name.
                if (call.owner.equals("com/mojang/util/UUIDTypeAdapter")
                        && call.name.equals("fromString")
                        && call.desc.equals("(Ljava/lang/String;)Ljava/util/UUID;")
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC) {
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192UuidBridge";
                    call.name = "fromString";
                    call.desc = "(Ljava/lang/String;)Ljava/util/UUID;";
                    call.itf = false;
                    rewrites++;
                }

                // Removed ForgeMod RegistryObject fields are converted to
                // explicit LMS builtin handles. Their RegistryObject operations are
                // adapted below, so the removed Forge holder never reaches linkage.
                if ((call.owner.equals("net/minecraftforge/registries/RegistryObject")
                        || call.owner.equals("net/neoforged/neoforge/registries/RegistryObject")
                        || call.owner.equals("com/fuckingdeveloper/lms/runtime/registry/LegacyRegistryObject"))
                        && call.name.equals("get")
                        && call.desc.equals("()Ljava/lang/Object;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192BuiltinRegistryBridge";
                    call.name = "get";
                    call.desc = "(Lcom/fuckingdeveloper/lms/runtime/registry/LegacyRegistryObject;)Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                }

                // Forge 1.19.2 exposed enableMilkFluid() as an opt-in bootstrap
                // request. The removed ForgeMod holder must never escape into the
                // target loader; preserve the operation as an explicit LMS boundary.
                if (call.owner.equals("net/minecraftforge/common/ForgeMod")
                        && call.name.equals("enableMilkFluid")
                        && call.desc.equals("()V")
                        && call.getOpcode() == org.objectweb.asm.Opcodes.INVOKESTATIC) {
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192BuiltinRegistryBridge";
                    call.name = "enableMilkFluid";
                    call.desc = "()V";
                    call.itf = false;
                    rewrites++;
                }

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
                    call.desc = "(Ljava/lang/Object;)Lnet/neoforged/fml/ModContainer;";
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
                if (call.owner.equals("net/neoforged/neoforge/event/entity/living/LivingShieldBlockEvent")
                        && call.name.equals("setShieldTakesDamage") && call.desc.equals("(Z)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192LivingEventBridge";
                    call.desc = "(Lnet/neoforged/neoforge/event/entity/living/LivingShieldBlockEvent;Z)V";
                    call.itf = false;
                    rewrites++;
                }
                // DFU renamed the value-preserving Supplier overload while
                // keeping its Codec return contract unchanged.
                if (call.owner.equals("com/mojang/serialization/Codec")
                        && call.name.equals("unit")
                        && call.desc.equals("(Ljava/util/function/Supplier;)Lcom/mojang/serialization/Codec;")) {
                    call.owner = "com/mojang/serialization/MapCodec";
                    call.name = "unitCodec";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/minecraft/core/Registry")
                        && call.name.equals("m_122965_")
                        && call.desc.equals("(Lnet/minecraft/core/Registry;Lnet/minecraft/resources/Identifier;Ljava/lang/Object;)Ljava/lang/Object;")) {
                    call.name = "register";
                    call.itf = true;
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
                        && (call.desc.equals("()Lnet/neoforged/neoforge/registries/IForgeRegistry;")
                            || call.desc.equals("()Lnet/minecraftforge/registries/IForgeRegistry;")
                            || call.desc.equals("()Lcom/fuckingdeveloper/lms/runtime/registry/LegacyForgeRegistry;"))) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RegistrationContext";
                    call.name = "activeRegistryObject";
                    call.desc = "(Lnet/neoforged/neoforge/registries/RegisterEvent;)Ljava/lang/Object;";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/neoforge/registries/IForgeRegistry")
                        || call.owner.equals("com/fuckingdeveloper/lms/runtime/registry/LegacyForgeRegistry")) {
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
                if ((call.owner.equals("net/neoforged/neoforge/registries/IForgeRegistry")
                        || call.owner.equals("com/fuckingdeveloper/lms/runtime/registry/LegacyForgeRegistry"))
                        && call.name.equals("register")
                        && call.desc.equals("(Ljava/lang/String;Ljava/lang/Object;)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RegistrationContext";
                    call.desc = "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V";
                    call.itf = false;
                    rewrites++;
                }
                if ((call.owner.equals("net/neoforged/neoforge/registries/IForgeRegistry")
                        || call.owner.equals("com/fuckingdeveloper/lms/runtime/registry/LegacyForgeRegistry"))
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
                if ((call.owner.equals("net/neoforged/neoforge/registries/GameData")
                        || call.owner.equals("net/minecraftforge/registries/GameData"))
                        && call.name.equals("checkPrefix")
                        && call.desc.equals("(Ljava/lang/String;Z)Lnet/minecraft/resources/Identifier;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192RegistrationContext";
                    call.desc = "(Ljava/lang/String;Z)Lnet/minecraft/resources/Identifier;";
                    call.itf = false;
                    rewrites++;
                }
                // Forge 1.19.2 FriendlyByteBuf registry-id serialization used
                // IForgeRegistry as an explicit registry token. LMS represents that
                // token as the concrete vanilla Registry and preserves the raw-id
                // varint wire contract in the network bridge.
                if (call.owner.equals("net/minecraft/network/FriendlyByteBuf")
                        && call.name.equals("writeRegistryIdUnsafe")
                        && call.desc.equals("(Lnet/neoforged/neoforge/registries/IForgeRegistry;Ljava/lang/Object;)V")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge";
                    call.name = "writeRegistryIdUnsafe";
                    call.desc = "(Lnet/minecraft/network/FriendlyByteBuf;Ljava/lang/Object;Ljava/lang/Object;)V";
                    call.itf = false;
                    rewrites++;
                }

                if (call.owner.equals("net/minecraft/network/FriendlyByteBuf")
                        && call.name.equals("readRegistryIdUnsafe")
                        && call.desc.equals("(Lnet/neoforged/neoforge/registries/IForgeRegistry;)Ljava/lang/Object;")) {
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192NetworkBridge";
                    call.name = "readRegistryIdUnsafe";
                    call.desc = "(Lnet/minecraft/network/FriendlyByteBuf;Ljava/lang/Object;)Ljava/lang/Object;";
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
                    Type eventType = inferLambdaEventType(call);
                    if (eventType == null)
                        throw new IllegalStateException("Cannot infer legacy lambda event type: "
                                + node.name + "#" + method.name + method.desc);
                    method.instructions.insertBefore(call, new LdcInsnNode(eventType));
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192EventBusBridge";
                    call.name = "addListener";
                    call.desc = "(Lnet/neoforged/bus/api/IEventBus;Ljava/util/function/Consumer;Ljava/lang/Class;)V";
                    call.itf = false;
                    rewrites++;
                }
                if (call.owner.equals("net/neoforged/bus/api/IEventBus")
                        && call.name.equals("addListener")
                        && call.desc.equals("(Lnet/neoforged/bus/api/EventPriority;Ljava/util/function/Consumer;)V")) {
                    Type eventType = inferLambdaEventType(call);
                    if (eventType == null)
                        throw new IllegalStateException("Cannot infer prioritized legacy lambda event type: "
                                + node.name + "#" + method.name + method.desc);
                    method.instructions.insertBefore(call, new LdcInsnNode(eventType));
                    call.setOpcode(org.objectweb.asm.Opcodes.INVOKESTATIC);
                    call.owner = "com/fuckingdeveloper/lms/runtime/Forge1192EventBusBridge";
                    call.name = "addListener";
                    call.desc = "(Lnet/neoforged/bus/api/IEventBus;Lnet/neoforged/bus/api/EventPriority;Ljava/util/function/Consumer;Ljava/lang/Class;)V";
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

    private static Type inferLambdaEventType(MethodInsnNode addListenerCall) {
        int realInstructions = 0;
        for (var previous = addListenerCall.getPrevious();
             previous != null && realInstructions < 24; previous = previous.getPrevious()) {
            if (previous.getOpcode() >= 0) realInstructions++;
            if (!(previous instanceof InvokeDynamicInsnNode indy)) continue;
            if (!indy.desc.endsWith(")Ljava/util/function/Consumer;")) continue;

            // LambdaMetafactory's instantiated method type is conventionally
            // bootstrap arg #2. It preserves Consumer<ConcreteEvent> even
            // though the runtime Consumer object itself is erased.
            if (indy.bsmArgs.length >= 3 && indy.bsmArgs[2] instanceof Type instantiated
                    && instantiated.getSort() == Type.METHOD) {
                Type[] args = instantiated.getArgumentTypes();
                if (args.length == 1 && args[0].getSort() == Type.OBJECT)
                    return Type.getObjectType(migrateInternalName(args[0].getInternalName()));
            }

            // Method-reference/lambda fallback: use the final object argument
            // of the implementation handle after captured arguments.
            for (Object bootstrapArg : indy.bsmArgs) {
                if (!(bootstrapArg instanceof Handle handle)) continue;
                Type methodType = Type.getMethodType(handle.getDesc());
                Type[] args = methodType.getArgumentTypes();
                if (args.length == 0) continue;
                Type candidate = args[args.length - 1];
                if (candidate.getSort() == Type.OBJECT)
                    return Type.getObjectType(migrateInternalName(candidate.getInternalName()));
            }
        }
        return null;
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

    /**
     * ForgeMod is a built-in Forge registry holder, not a portable class alias.
     * Report the exact static field/method contract before JVM linkage, so
     * migration rules can be proven against the target registry rather than
     * inventing an empty compatibility class.
     */
    private static void verifyUnresolvedForgeModAccesses(ClassNode node) {
        final String legacyOwner = "net/minecraftforge/common/ForgeMod";
        for (var method : node.methods) {
            for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof FieldInsnNode field && field.owner.equals(legacyOwner)) {
                    throw new IllegalStateException("UNRESOLVED_FORGE_BUILTIN_REGISTRY_FIELD "
                            + node.name + "#" + method.name + method.desc
                            + " opcode=" + field.getOpcode()
                            + " field=" + field.owner + "." + field.name + ":" + field.desc);
                }
                if (insn instanceof MethodInsnNode call && call.owner.equals(legacyOwner)) {
                    throw new IllegalStateException("UNRESOLVED_FORGE_BUILTIN_REGISTRY_CALL "
                            + node.name + "#" + method.name + method.desc
                            + " opcode=" + call.getOpcode()
                            + " call=" + call.owner + "." + call.name + call.desc);
                }
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

    /** Static namespace projection used by non-executing migration planning. */
    public static String projectInternalName(String value) {
        return migrateInternalName(value);
    }

    /** Descriptor projection must use the exact same namespace contract as runtime transformation. */
    public static String projectDescriptor(String descriptor) {
        return migrateDescriptor(descriptor);
    }

    private static String migrateInternalName(String value) {
        // DistExecutor was removed from current NeoForge. Keep the Forge 1.19.2
        // physical-side execution contract behind an LMS-owned runtime boundary
        // instead of migrating it into a non-existent NeoForge class.
        if (value.equals("net/minecraftforge/fml/DistExecutor"))
            return "com/fuckingdeveloper/lms/runtime/Forge1192DistExecutorBridge";
        if (value.equals("net/minecraftforge/api/distmarker/Dist"))
            return "net/neoforged/api/distmarker/Dist";
        if (value.equals("net/minecraft/client/color/block/BlockColor"))
            return "com/fuckingdeveloper/lms/runtime/color/LegacyBlockColor";
        if (value.equals("net/minecraft/client/color/item/ItemColor"))
            return "com/fuckingdeveloper/lms/runtime/color/LegacyItemColor";
        String explicitVanillaRelocation = EXPLICIT_VANILLA_CLASS_RELOCATIONS.get(value);
        if (explicitVanillaRelocation != null) return explicitVanillaRelocation;
        // IForgeItem was a Forge extension interface mixed into Item in 1.19.2.
        // It has no target class in 26.3; references must remain on Item and its
        // operations are resolved/adapted separately rather than namespace-guessed.
        // Forge's global gameplay event bus survived conceptually as NeoForge.EVENT_BUS.
        // This is an exact static-singleton migration; keeping MinecraftForge around
        // would force the managed loader to resolve a removed framework class.
        if (value.equals("net/minecraftforge/common/MinecraftForge"))
            return "net/neoforged/neoforge/common/NeoForge";
        // Forge 1.19.2's phase-bearing tick events became base events whose
        // concrete Pre/Post subclasses encode the old phase. Field reads are
        // adapted by Forge1192TickEventBridge after namespace projection.
        if (value.equals("net/minecraftforge/event/TickEvent$Phase"))
            return "com/fuckingdeveloper/lms/runtime/Forge1192TickEventBridge$Phase";
        if (value.equals("net/minecraftforge/event/TickEvent$PlayerTickEvent"))
            return "net/neoforged/neoforge/event/tick/PlayerTickEvent";
        if (value.equals("net/minecraftforge/event/TickEvent$ServerTickEvent"))
            return "net/neoforged/neoforge/event/tick/ServerTickEvent";
        if (value.equals("net/minecraftforge/event/TickEvent$LevelTickEvent"))
            return "net/neoforged/neoforge/event/tick/LevelTickEvent";
        if (value.equals("net/minecraftforge/event/TickEvent$ClientTickEvent"))
            return "net/neoforged/neoforge/client/event/ClientTickEvent";
        if (value.equals("net/minecraftforge/event/furnace/FurnaceFuelBurnTimeEvent"))
            return "com/fuckingdeveloper/lms/runtime/LegacyFurnaceFuelBurnTimeEvent";
        if (value.equals("net/minecraftforge/event/entity/living/LivingSpawnEvent$CheckSpawn"))
            return "net/neoforged/neoforge/event/entity/living/MobSpawnEvent$PositionCheck";
        // LivingAttackEvent was replaced by the early, cancellable incoming
        // damage event. The legacy contract used by 1.19.2 mods (entity,
        // source, amount and cancellation) is preserved by this target.
        if (value.equals("net/minecraftforge/event/entity/living/LivingAttackEvent"))
            return "net/neoforged/neoforge/event/entity/living/LivingIncomingDamageEvent";
        if (value.equals("net/minecraftforge/event/entity/living/ShieldBlockEvent"))
            return "net/neoforged/neoforge/event/entity/living/LivingShieldBlockEvent";
        if (value.equals("net/minecraftforge/event/level/BlockEvent$BreakEvent"))
            return "com/fuckingdeveloper/lms/runtime/event/LegacyBreakBlockEvent";
        // Common Forge living/player gameplay events retained their semantic
        // event identity under NeoForge. Map only when the exact target class
        // exists; removed/split event families remain explicit migration boundaries.
        if (value.startsWith("net/minecraftforge/event/entity/living/")
                || value.startsWith("net/minecraftforge/event/entity/player/")) {
            String candidate = value.replace("net/minecraftforge/", "net/neoforged/neoforge/");
            if (Forge1192LegacyClassTransformer.class.getClassLoader()
                    .getResource(candidate + ".class") != null) {
                return candidate;
            }
        }
        // IForgeRegistry was removed. Keep only a verifier-visible LMS facade;
        // all executable registry operations are redirected to
        // Forge1192RegistrationContext by semantic adapters below.
        if (value.equals("net/minecraftforge/registries/IForgeRegistry"))
            return "com/fuckingdeveloper/lms/runtime/registry/LegacyForgeRegistry";
        if (value.equals("net/minecraftforge/common/IPlantable"))
            return "com/fuckingdeveloper/lms/runtime/block/LegacyIPlantable";
        if (value.equals("net/minecraftforge/common/PlantType"))
            return "com/fuckingdeveloper/lms/runtime/block/LegacyPlantType";
        if (value.equals("net/minecraftforge/energy/IEnergyStorage"))
            return "com/fuckingdeveloper/lms/runtime/energy/LegacyIEnergyStorage";
        if (value.equals("net/minecraftforge/items/IItemHandler"))
            return "com/fuckingdeveloper/lms/runtime/itemhandler/LegacyIItemHandler";
        if (value.equals("net/minecraftforge/items/IItemHandlerModifiable"))
            return "com/fuckingdeveloper/lms/runtime/itemhandler/LegacyIItemHandlerModifiable";
        if (value.equals("net/minecraft/world/item/Tier"))
            return "com/fuckingdeveloper/lms/runtime/item/LegacyTier";
        if (value.equals("net/minecraft/world/item/AxeItem"))
            return "com/fuckingdeveloper/lms/runtime/item/LegacyAxeItem";
        if (value.equals("net/minecraft/world/item/HoeItem"))
            return "com/fuckingdeveloper/lms/runtime/item/LegacyHoeItem";
        if (value.equals("net/minecraft/world/item/PickaxeItem"))
            return "com/fuckingdeveloper/lms/runtime/item/LegacyPickaxeItem";
        if (value.equals("net/minecraft/world/item/ShovelItem"))
            return "com/fuckingdeveloper/lms/runtime/item/LegacyShovelItem";
        if (value.equals("net/minecraft/world/item/SwordItem"))
            return "com/fuckingdeveloper/lms/runtime/item/LegacySwordItem";
        if (value.equals("net/minecraft/world/item/DyeableLeatherItem"))
            return "com/fuckingdeveloper/lms/runtime/item/LegacyDyeableLeatherItem";
        if (value.equals("net/minecraftforge/common/extensions/IForgeBlock"))
            return "com/fuckingdeveloper/lms/runtime/block/LegacyIForgeBlock";
        if (value.equals("net/minecraftforge/common/extensions/IForgeItem"))
            return "com/fuckingdeveloper/lms/runtime/item/LegacyIForgeItem";
        if (value.equals("net/minecraftforge/registries/RegistryObject"))
            return "com/fuckingdeveloper/lms/runtime/registry/LegacyRegistryObject";
        if (value.equals("net/minecraftforge/common/capabilities/ICapabilityProvider"))
            return "com/fuckingdeveloper/lms/runtime/capability/LegacyICapabilityProvider";
        if (value.equals("net/minecraftforge/common/capabilities/RegisterCapabilitiesEvent"))
            return "net/neoforged/neoforge/capabilities/RegisterCapabilitiesEvent";
        if (value.equals("net/minecraftforge/common/capabilities/CapabilityToken"))
            return "com/fuckingdeveloper/lms/runtime/capability/LegacyCapabilityToken";
        if (value.equals("net/minecraftforge/common/capabilities/CapabilityManager"))
            return "com/fuckingdeveloper/lms/runtime/capability/LegacyCapabilityManager";
        if (value.equals("net/minecraftforge/common/capabilities/Capability"))
            return "com/fuckingdeveloper/lms/runtime/capability/LegacyCapability";
        if (value.equals("net/minecraftforge/common/util/LazyOptional"))
            return "com/fuckingdeveloper/lms/runtime/capability/LegacyLazyOptional";
        if (value.equals("net/minecraftforge/fluids/capability/IFluidHandler"))
            return "com/fuckingdeveloper/lms/runtime/fluid/LegacyIFluidHandler";
        if (value.equals("net/minecraftforge/fluids/IFluidTank"))
            return "com/fuckingdeveloper/lms/runtime/fluid/LegacyIFluidTank";
        if (value.equals("net/minecraftforge/fluids/capability/templates/FluidTank"))
            return "com/fuckingdeveloper/lms/runtime/fluid/LegacyFluidTank";
        if (value.equals("net/minecraft/world/item/ArmorItem"))
            return "com/fuckingdeveloper/lms/runtime/item/LegacyArmorItem";
        if (value.equals("net/minecraft/world/item/SignItem"))
            return "net/minecraft/world/item/StandingAndWallBlockItem";
        if (value.equals("net/minecraft/world/level/block/WoodButtonBlock"))
            return "net/minecraft/world/level/block/ButtonBlock";
        if (value.equals("net/minecraft/world/level/block/state/properties/DirectionProperty"))
            return "net/minecraft/world/level/block/state/properties/EnumProperty";
        // BakedModel was split into unrelated block-state and item rendering
        // contracts. Preserve legacy-owned model identity without pretending
        // either modern interface is a drop-in replacement.
        if (value.equals("net/minecraft/client/resources/model/BakedModel"))
            return "com/fuckingdeveloper/lms/runtime/render/LegacyBakedModel";
        if (value.equals("net/minecraft/world/entity/MobSpawnType"))
            return "net/minecraft/world/entity/EntitySpawnReason";
        if (value.startsWith("net/minecraftforge/client/model/data/"))
            return value.replace("net/minecraftforge/client/model/data/", "net/neoforged/neoforge/model/data/");
        if (value.equals("net/minecraft/resources/ResourceLocation"))
            return "net/minecraft/resources/Identifier";
        // A removed vanilla type must not be relocated solely because an
        // unrelated target class has the same simple name. Moved types require
        // an explicit mapping/semantic adapter above; otherwise fail closed.
        if (value.startsWith("net/minecraftforge/eventbus/"))
            return value.replace("net/minecraftforge/eventbus/", "net/neoforged/bus/");
        if (value.startsWith("net/minecraftforge/fml/"))
            return value.replace("net/minecraftforge/fml/", "net/neoforged/fml/");
        if (value.startsWith("net/minecraftforge/forgespi/"))
            return value.replace("net/minecraftforge/forgespi/", "net/neoforged/neoforgespi/");
        // Main Forge API moved below NeoForge's neoforge namespace.
        if (value.startsWith("net/minecraftforge/")) {
            String candidate = value.replace("net/minecraftforge/", "net/neoforged/neoforge/");
            if (Forge1192LegacyClassTransformer.class.getClassLoader()
                    .getResource(candidate + ".class") != null) {
                return candidate;
            }
            // Absent Forge APIs are semantic migration boundaries. Keeping the
            // legacy name here is intentional: a later family adapter must own
            // the transformation instead of manufacturing a nonexistent
            // NeoForge class name.
            return value;
        }
        return value;
    }

    private static String migrateDescriptor(String descriptor) {
        String migrated = descriptor;
        for (var relocation : EXPLICIT_VANILLA_CLASS_RELOCATIONS.entrySet()) {
            migrated = migrated.replace(relocation.getKey(), relocation.getValue());
        }
        return migrated
                .replace("net/minecraftforge/fml/DistExecutor", "com/fuckingdeveloper/lms/runtime/Forge1192DistExecutorBridge")
                .replace("net/minecraftforge/api/distmarker/Dist", "net/neoforged/api/distmarker/Dist")
                .replace("net/minecraft/client/color/block/BlockColor", "com/fuckingdeveloper/lms/runtime/color/LegacyBlockColor")
                .replace("net/minecraft/client/color/item/ItemColor", "com/fuckingdeveloper/lms/runtime/color/LegacyItemColor")
                .replace("net/minecraftforge/common/IPlantable", "com/fuckingdeveloper/lms/runtime/block/LegacyIPlantable")
                .replace("net/minecraftforge/common/PlantType", "com/fuckingdeveloper/lms/runtime/block/LegacyPlantType")
                .replace("net/minecraftforge/energy/IEnergyStorage", "com/fuckingdeveloper/lms/runtime/energy/LegacyIEnergyStorage")
                .replace("net/minecraftforge/items/IItemHandlerModifiable", "com/fuckingdeveloper/lms/runtime/itemhandler/LegacyIItemHandlerModifiable")
                .replace("net/minecraftforge/items/IItemHandler", "com/fuckingdeveloper/lms/runtime/itemhandler/LegacyIItemHandler")
                .replace("net/minecraft/world/item/Tier", "com/fuckingdeveloper/lms/runtime/item/LegacyTier")
                .replace("net/minecraft/world/item/AxeItem", "com/fuckingdeveloper/lms/runtime/item/LegacyAxeItem")
                .replace("net/minecraft/world/item/HoeItem", "com/fuckingdeveloper/lms/runtime/item/LegacyHoeItem")
                .replace("net/minecraft/world/item/PickaxeItem", "com/fuckingdeveloper/lms/runtime/item/LegacyPickaxeItem")
                .replace("net/minecraft/world/item/ShovelItem", "com/fuckingdeveloper/lms/runtime/item/LegacyShovelItem")
                .replace("net/minecraft/world/item/SwordItem", "com/fuckingdeveloper/lms/runtime/item/LegacySwordItem")
                .replace("net/minecraft/world/item/DyeableLeatherItem", "com/fuckingdeveloper/lms/runtime/item/LegacyDyeableLeatherItem")
                .replace("net/minecraftforge/common/extensions/IForgeBlock", "com/fuckingdeveloper/lms/runtime/block/LegacyIForgeBlock")
                .replace("net/minecraftforge/common/extensions/IForgeItem", "com/fuckingdeveloper/lms/runtime/item/LegacyIForgeItem")
                .replace("net/minecraftforge/registries/RegistryObject", "com/fuckingdeveloper/lms/runtime/registry/LegacyRegistryObject")
                .replace("net/minecraftforge/common/capabilities/ICapabilityProvider", "com/fuckingdeveloper/lms/runtime/capability/LegacyICapabilityProvider")
                .replace("net/minecraftforge/common/capabilities/RegisterCapabilitiesEvent", "net/neoforged/neoforge/capabilities/RegisterCapabilitiesEvent")
                .replace("net/minecraftforge/common/capabilities/CapabilityToken", "com/fuckingdeveloper/lms/runtime/capability/LegacyCapabilityToken")
                .replace("net/minecraftforge/common/capabilities/CapabilityManager", "com/fuckingdeveloper/lms/runtime/capability/LegacyCapabilityManager")
                .replace("net/minecraftforge/common/capabilities/Capability", "com/fuckingdeveloper/lms/runtime/capability/LegacyCapability")
                .replace("net/minecraftforge/common/util/LazyOptional", "com/fuckingdeveloper/lms/runtime/capability/LegacyLazyOptional")
                .replace("net/minecraftforge/fluids/capability/IFluidHandler", "com/fuckingdeveloper/lms/runtime/fluid/LegacyIFluidHandler")
                .replace("net/minecraftforge/fluids/IFluidTank", "com/fuckingdeveloper/lms/runtime/fluid/LegacyIFluidTank")
                .replace("net/minecraftforge/fluids/capability/templates/FluidTank", "com/fuckingdeveloper/lms/runtime/fluid/LegacyFluidTank")
                .replace("net/minecraft/world/item/ArmorItem", "com/fuckingdeveloper/lms/runtime/item/LegacyArmorItem")
                .replace("net/minecraft/world/level/block/state/properties/DirectionProperty", "net/minecraft/world/level/block/state/properties/EnumProperty")
                .replace("net/minecraft/client/resources/model/BakedModel", "com/fuckingdeveloper/lms/runtime/render/LegacyBakedModel")
                .replace("net/minecraftforge/client/model/data/", "net/neoforged/neoforge/model/data/")
                .replace("net/minecraft/resources/ResourceLocation", "net/minecraft/resources/Identifier")
                .replace("net/minecraftforge/eventbus/", "net/neoforged/bus/")
                .replace("net/minecraftforge/fml/", "net/neoforged/fml/")
                .replace("net/minecraftforge/forgespi/", "net/neoforged/neoforgespi/");
    }
}
