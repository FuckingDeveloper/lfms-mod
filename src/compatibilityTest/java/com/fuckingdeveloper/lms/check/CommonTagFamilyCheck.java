package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import java.lang.invoke.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.tags.TagKey;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Source metadata must never serve as evidence for target field existence. */
public final class CommonTagFamilyCheck {
    private static final String TAG = "Lnet/minecraft/tags/TagKey;";
    private static final String SOURCE = "net/minecraftforge/common/Tags$";
    private static final String TARGET = "net/neoforged/neoforge/common/Tags$";
    private static final String[][] ALIASES = {
        {"Blocks", "COBBLESTONE", "COBBLESTONES"}, {"Items", "COBBLESTONE", "COBBLESTONES"},
        {"Blocks", "GLASS", "GLASS_BLOCKS"}, {"Items", "GLASS", "GLASS_BLOCKS"},
        {"Blocks", "GRAVEL", "GRAVELS"}, {"Items", "GRAVEL", "GRAVELS"},
        {"Blocks", "SANDSTONE", "SANDSTONE_BLOCKS"}, {"Blocks", "STONE", "STONES"},
        {"Items", "STONE", "STONES"}, {"Items", "INGOTS_BRICK", "BRICKS_NORMAL"},
        {"Items", "INGOTS_NETHER_BRICK", "BRICKS_NETHER"}, {"Items", "OBSIDIAN", "OBSIDIANS"},
        {"Items", "SHEARS", "TOOLS_SHEAR"}, {"Biomes", "IS_WATER", "IS_AQUATIC"}
    };
    public static Object constant(MethodHandles.Lookup lookup, String name, Class<?> type, Object value) { return value; }
    public static CallSite site(MethodHandles.Lookup lookup, String name, MethodType type, MethodHandle getter) { return new ConstantCallSite(getter.asType(type)); }
    public static void run() throws Exception {
        var node = new ClassNode(); node.version = Opcodes.V17; node.access = Opcodes.ACC_PUBLIC;
        node.name = "fixture/CommonTags"; node.superName = "java/lang/Object";
        var bootstrap = new Handle(Opcodes.H_INVOKESTATIC, Type.getInternalName(CommonTagFamilyCheck.class), "constant",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;", false);
        var site = new Handle(Opcodes.H_INVOKESTATIC, Type.getInternalName(CommonTagFamilyCheck.class), "site",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;)Ljava/lang/invoke/CallSite;", false);
        for (int i = 0; i < ALIASES.length; i++) {
            String[] alias = ALIASES[i]; String owner = SOURCE + alias[0];
            var handle = new Handle(Opcodes.H_GETSTATIC, owner, alias[1], TAG, false);
            add(node, "direct" + i, "()" + TAG, new FieldInsnNode(Opcodes.GETSTATIC, owner, alias[1], TAG));
            add(node, "handle" + i, "()Ljava/lang/invoke/MethodHandle;", new LdcInsnNode(handle));
            add(node, "nested" + i, "()Ljava/lang/invoke/MethodHandle;", new LdcInsnNode(new ConstantDynamic("outer", "Ljava/lang/invoke/MethodHandle;", bootstrap,
                    new ConstantDynamic("inner", "Ljava/lang/invoke/MethodHandle;", bootstrap, handle))));
            add(node, "indy" + i, "()" + TAG, new InvokeDynamicInsnNode("tag", "()" + TAG, site, handle));
        }
        var path = Files.createTempFile("lms-common-tags", ".jar");
        try (var jar = new JarOutputStream(Files.newOutputStream(path))) { jar.putNextEntry(new JarEntry(node.name + ".class")); jar.write(bytes(node)); jar.closeEntry(); }
        byte[] immutable = Files.readAllBytes(path);
        try (var loader = new ManagedLegacyClassLoader(path, CommonTagFamilyCheck.class.getClassLoader(), new Forge1192LegacyClassTransformer())) {
            Class<?> type = loader.linkOwnedClass(node.name.replace('/', '.'));
            for (int i = 0; i < ALIASES.length; i++) {
                String[] alias = ALIASES[i]; Object nativeTag = Class.forName((TARGET + alias[0]).replace('/', '.')).getField(alias[2]).get(null);
                for (String kind : List.of("direct", "handle", "nested", "indy")) {
                    Object result = type.getMethod(kind + i).invoke(null);
                    if (result instanceof MethodHandle handle) {
                        check(handle.type().equals(MethodType.methodType(TagKey.class)), "tag getter handle type changed");
                        try { result = handle.invoke(); } catch (Throwable failure) { throw new IllegalStateException("Tag handle failed", failure); }
                    }
                    check(result == nativeTag, "target tag identity lost: " + kind + Arrays.toString(alias));
                }
            }
        }
        check(Arrays.equals(immutable, Files.readAllBytes(path)), "original fixture JAR changed");
        var transformer = new Forge1192LegacyClassTransformer();
        var poison = new ClassNode(); poison.version = Opcodes.V17; poison.access = Opcodes.ACC_PUBLIC;
        poison.name = SOURCE + "Items"; poison.superName = "java/lang/Object";
        poison.fields.add(new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "SHEARS", TAG, null, null));
        transformer.bindManagedClassBytes(name -> name.equals(poison.name) ? bytes(poison) : null);
        var output = new ClassNode(); new ClassReader(transformer.transform(node.name.replace('/', '.'), bytes(node)).bytes()).accept(output, 0);
        var direct = output.methods.stream().filter(m -> m.name.equals("direct12")).findFirst().orElseThrow();
        check(((FieldInsnNode)direct.instructions.getFirst()).name.equals("TOOLS_SHEAR"), "managed/source evidence shadowed target ABI");
        var negative = new ClassNode(); negative.version = Opcodes.V17; negative.access = Opcodes.ACC_PUBLIC;
        negative.name = "fixture/UnrelatedTags"; negative.superName = "java/lang/Object";
        add(negative, "other", "()" + TAG, new FieldInsnNode(Opcodes.GETSTATIC, "fixture/OtherTags", "SHEARS", TAG));
        add(negative, "wrongDescriptor", "()Ljava/lang/Object;", new FieldInsnNode(Opcodes.GETSTATIC, TARGET + "Items", "SHEARS", "Ljava/lang/Object;"));
        add(negative, "unknownAlias", "()" + TAG, new FieldInsnNode(Opcodes.GETSTATIC, TARGET + "Items", "UNKNOWN", TAG));
        add(negative, "writeHandle", "()Ljava/lang/invoke/MethodHandle;", new LdcInsnNode(new Handle(Opcodes.H_PUTSTATIC, TARGET + "Items", "SHEARS", TAG, false)));
        var pass = Forge1192LegacyClassTransformer.class.getDeclaredMethod("rewriteCommonTagReferences", ClassNode.class); pass.setAccessible(true);
        check((int)pass.invoke(transformer, negative) == 0, "unverified tag identity rewritten");
        inventory();
        System.out.println("PASS: 14 existing common-tag aliases, native identity, LDC/indy/nested condy, source evidence isolation and immutable source JAR");
    }
    private static void inventory() throws Exception {
        var root = Path.of("legacy-mods"); if (!Files.isDirectory(root)) return;
        Set<String> identities = new HashSet<>(); int sites = 0;
        try (var paths = Files.list(root)) {
            for (var path : paths.filter(p -> p.toString().endsWith(".jar")).toList()) try (var jar = new JarFile(path.toFile())) {
                for (var entry : jar.stream().filter(e -> e.getName().endsWith(".class")).toList()) {
                    var node = new ClassNode(); try (var input = jar.getInputStream(entry)) { new ClassReader(input.readAllBytes()).accept(node, 0); }
                    for (var method : node.methods) for (var instruction : method.instructions) if (instruction instanceof FieldInsnNode field && field.owner.startsWith(SOURCE) && field.desc.equals(TAG)) {
                        String nested = field.owner.substring(SOURCE.length()), target = field.name;
                        for (String[] alias : ALIASES) if (alias[0].equals(nested) && alias[1].equals(field.name)) target = alias[2];
                        Class.forName((TARGET + nested).replace('/', '.')).getField(target);
                        identities.add(nested + "#" + field.name); sites++;
                    }
                }
            }
        }
        System.out.println("Original artifact common-tag inventory: identities=" + identities.size() + ", sites=" + sites + "; all native declarations exist after verified aliases (membership unverified)");
    }
    private static void add(ClassNode node, String name, String desc, AbstractInsnNode value) {
        var method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, name, desc, null, null);
        method.instructions.add(value); method.instructions.add(new InsnNode(Opcodes.ARETURN)); node.methods.add(method);
    }
    private static byte[] bytes(ClassNode node) { var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); node.accept(writer); return writer.toByteArray(); }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
