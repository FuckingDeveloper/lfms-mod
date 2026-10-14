package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import java.lang.invoke.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.client.gui.components.Button;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Independent inherited field fixtures across an original Forge native ancestor. */
public final class WidgetHierarchyFamilyCheck {
    private static final String[] FIELDS = {"f_93618_", "f_93619_", "f_93620_", "f_93621_", "f_93622_", "f_93623_", "f_93624_", "f_93625_"};
    private static String desc(int i) { return i < 4 ? "I" : i < 7 ? "Z" : "F"; }
    public static CallSite fieldSite(MethodHandles.Lookup lookup, String name, MethodType type, MethodHandle getter) { return new ConstantCallSite(getter.asType(type)); }
    public static Object constant(MethodHandles.Lookup lookup, String name, Class<?> type, Object value) { return value; }
    public static void run() throws Exception {
        var root = fixture("fixture/LoaderWidget", "net/minecraftforge/client/gui/widget/ExtendedButton", false);
        var hidden = fixture("fixture/HiddenLoaderWidget", root.name, true);
        var path = Files.createTempFile("lms-widget-hierarchy", ".jar");
        try (var jar = new JarOutputStream(Files.newOutputStream(path))) {
            for (var node : List.of(root, hidden)) { jar.putNextEntry(new JarEntry(node.name + ".class")); jar.write(bytes(node)); jar.closeEntry(); }
        }
        byte[] immutable = Files.readAllBytes(path);
        try (var loader = new ManagedLegacyClassLoader(path, WidgetHierarchyFamilyCheck.class.getClassLoader(), new Forge1192LegacyClassTransformer())) {
            var type = loader.linkOwnedClass(root.name.replace('/', '.'));
            var widget = (Button)type.getConstructor().newInstance();
            var lookup = MethodHandles.privateLookupIn(type, MethodHandles.lookup());
            Object[] values = {103, 29, 13, 17, true, false, false, 0.5F};
            for (int i = 0; i < FIELDS.length; i++) {
                Class<?> fieldType = i < 4 ? int.class : i < 7 ? boolean.class : float.class;
                lookup.findVirtual(type, "write" + i, MethodType.methodType(void.class, fieldType)).invoke(widget, values[i]);
                Object read = lookup.findVirtual(type, "read" + i, MethodType.methodType(fieldType)).invoke(widget);
                check(read.equals(values[i]), "source field lookup lost a sibling across Forge superclass: " + FIELDS[i]);
                var getter = (MethodHandle)lookup.findVirtual(type, "handle" + i, MethodType.methodType(MethodHandle.class)).invoke(widget);
                check(getter.type().equals(MethodType.methodType(fieldType, type)) && getter.invoke(widget).equals(values[i]), "source field handle changed receiver/type/storage");
            }
            check(!widget.active && !widget.visible && widget.getWidth() == 103 && widget.getHeight() == 29, "source storage does not drive actual native widget state");
            check(widget.getX() == 999 && lookup.findVirtual(type, "read2", MethodType.methodType(int.class)).invoke(widget).equals(13), "direct source coordinate read invoked virtual getter");
            check(lookup.findVirtual(type, "indyActive", MethodType.methodType(boolean.class)).invoke(widget).equals(false), "indy field handle was treated as a method or not adapted");
            var setter = (MethodHandle)lookup.findVirtual(type, "setterX", MethodType.methodType(MethodHandle.class)).invoke(widget);
            check(setter.type().equals(MethodType.methodType(void.class, type, int.class)), "field setter receiver/type changed"); setter.invoke(widget, 71);
            check(lookup.findVirtual(type, "read2", MethodType.methodType(int.class)).invoke(widget).equals(71), "field setter did not update raw native coordinate");
            var hiddenType = loader.linkOwnedClass(hidden.name.replace('/', '.')); var hiddenObject = (Button)hiddenType.getConstructor().newInstance();
            var hiddenLookup = MethodHandles.privateLookupIn(hiddenType, MethodHandles.lookup());
            hiddenLookup.findVirtual(hiddenType, "write5", MethodType.methodType(void.class, boolean.class)).invoke(hiddenObject, false);
            check(hiddenObject.active && hiddenLookup.findVirtual(hiddenType, "read5", MethodType.methodType(boolean.class)).invoke(hiddenObject).equals(false), "source hidden field redirected to native active");
            var hiddenHandle = (MethodHandle)hiddenLookup.findVirtual(hiddenType, "handle5", MethodType.methodType(MethodHandle.class)).invoke(hiddenObject);
            check(hiddenHandle.invoke(hiddenObject).equals(false), "hidden source field handle redirected");
        } catch (RuntimeException | Error failure) { throw failure; }
        catch (Throwable failure) { throw new IllegalStateException("Widget hierarchy fixture failed", failure); }
        check(Arrays.equals(immutable, Files.readAllBytes(path)), "source widget JAR changed");
        // Managed definitions override bundled native-loader evidence; a new binding clears both caches.
        var transformer = new Forge1192LegacyClassTransformer();
        transformer.bindManagedClassBytes(name -> name.equals(root.name) ? bytes(root) : null);
        check(mappedActive(transformer, root).equals("active"), "bundled Forge ancestor did not resolve active");
        var sourceAncestor = new ClassNode(); sourceAncestor.version = Opcodes.V17; sourceAncestor.access = Opcodes.ACC_PUBLIC;
        sourceAncestor.name = root.superName; sourceAncestor.superName = "net/minecraft/client/gui/components/Button";
        sourceAncestor.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, FIELDS[5], "Z", null, null));
        transformer.bindManagedClassBytes(name -> name.equals(root.name) ? bytes(root) : name.equals(sourceAncestor.name) ? bytes(sourceAncestor) : null);
        check(mappedActive(transformer, root).equals(FIELDS[5]), "bundled metadata took precedence over managed source field");
        transformer.bindManagedClassBytes(name -> name.equals(root.name) ? bytes(root) : null);
        check(mappedActive(transformer, root).equals("active"), "managed source evidence survived rebinding");
        System.out.println("PASS: eight widget field families across original Forge native ancestry, actual native storage, raw private coordinates independent of virtual getters, hidden source fields, typed getter/setter/indy/nested field handles and immutable source JAR");
    }
    private static String mappedActive(Forge1192LegacyClassTransformer transformer, ClassNode node) {
        var output = new ClassNode(); new ClassReader(transformer.transform(node.name.replace('/', '.'), bytes(node)).bytes()).accept(output, 0);
        for (var method : output.methods) if (method.name.equals("read5")) for (var instruction : method.instructions) if (instruction instanceof FieldInsnNode field) return field.name;
        throw new AssertionError("source active field read missing");
    }
    private static ClassNode fixture(String name, String parent, boolean hidden) {
        var node = new ClassNode(); node.name = name; node.superName = parent; node.version = Opcodes.V17; node.access = Opcodes.ACC_PUBLIC;
        if (hidden) node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, FIELDS[5], "Z", null, null));
        var ctor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null); ctor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        if (!hidden) {
            for (int value : new int[]{1, 2, 3, 4}) ctor.instructions.add(new IntInsnNode(Opcodes.BIPUSH, value));
            ctor.instructions.add(new LdcInsnNode("fixture")); ctor.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/network/chat/Component", "literal", "(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", true));
            ctor.instructions.add(new InsnNode(Opcodes.ACONST_NULL)); // Source default accepts null; this storage-only probe never activates it.
        }
        ctor.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, parent, "<init>", hidden ? "()V" : "(IIIILnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)V", false)); ctor.instructions.add(new InsnNode(Opcodes.RETURN)); node.methods.add(ctor);
        for (int i = 0; i < FIELDS.length; i++) {
            var read = new MethodNode(Opcodes.ACC_PUBLIC, "read" + i, "()" + desc(i), null, null);
            read.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); read.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, name, FIELDS[i], desc(i))); read.instructions.add(new InsnNode(Type.getType(desc(i)).getOpcode(Opcodes.IRETURN))); node.methods.add(read);
            var write = new MethodNode(Opcodes.ACC_PUBLIC, "write" + i, "(" + desc(i) + ")V", null, null);
            write.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); write.instructions.add(new VarInsnNode(Type.getType(desc(i)).getOpcode(Opcodes.ILOAD), 1)); write.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, name, FIELDS[i], desc(i))); write.instructions.add(new InsnNode(Opcodes.RETURN)); node.methods.add(write);
            var handle = new MethodNode(Opcodes.ACC_PUBLIC, "handle" + i, "()Ljava/lang/invoke/MethodHandle;", null, null);
            var field = new Handle(Opcodes.H_GETFIELD, name, FIELDS[i], desc(i), false);
            if (i == 0) {
                var bootstrap = new Handle(Opcodes.H_INVOKESTATIC, Type.getInternalName(WidgetHierarchyFamilyCheck.class), "constant", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;", false);
                handle.instructions.add(new LdcInsnNode(new ConstantDynamic("outer", "Ljava/lang/invoke/MethodHandle;", bootstrap, new ConstantDynamic("inner", "Ljava/lang/invoke/MethodHandle;", bootstrap, field))));
            } else handle.instructions.add(new LdcInsnNode(field));
            handle.instructions.add(new InsnNode(Opcodes.ARETURN)); node.methods.add(handle);
        }
        var getter = new MethodNode(Opcodes.ACC_PUBLIC, "getX", "()I", null, null); getter.instructions.add(new IntInsnNode(Opcodes.SIPUSH, 999)); getter.instructions.add(new InsnNode(Opcodes.IRETURN)); node.methods.add(getter);
        var setter = new MethodNode(Opcodes.ACC_PUBLIC, "setterX", "()Ljava/lang/invoke/MethodHandle;", null, null); setter.instructions.add(new LdcInsnNode(new Handle(Opcodes.H_PUTFIELD, name, FIELDS[2], "I", false))); setter.instructions.add(new InsnNode(Opcodes.ARETURN)); node.methods.add(setter);
        var indy = new MethodNode(Opcodes.ACC_PUBLIC, "indyActive", "()Z", null, null); indy.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        var site = new Handle(Opcodes.H_INVOKESTATIC, Type.getInternalName(WidgetHierarchyFamilyCheck.class), "fieldSite", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;)Ljava/lang/invoke/CallSite;", false);
        indy.instructions.add(new InvokeDynamicInsnNode("active", "(L" + name + ";)Z", site, new Handle(Opcodes.H_GETFIELD, name, FIELDS[5], "Z", false))); indy.instructions.add(new InsnNode(Opcodes.IRETURN)); node.methods.add(indy);
        return node;
    }
    private static byte[] bytes(ClassNode node) { var writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS); node.accept(writer); return writer.toByteArray(); }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
