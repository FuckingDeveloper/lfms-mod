package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.runtime.render.Forge1192ButtonBridge;
import java.lang.invoke.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Bounded construction/activation fixture; does not render a source widget. */
public final class ButtonFamilyCheck {
    private static final String BASE = "net/minecraft/client/gui/components/Button";
    private static final String SOURCE_CTOR = "(IIIILnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)V";
    private static Button last; private static int calls;
    public static Button.OnPress listener() { return receiver -> { calls++; last = receiver; }; }
    public static void run() throws Exception {
        var parent = fixture("fixture/ButtonParent", BASE, false);
        var child = fixture("fixture/ButtonChild", parent.name, true);
        var extended = fixture("fixture/ExtendedButtonSource", "net/minecraftforge/client/gui/widget/ExtendedButton", false);
        var path = Files.createTempFile("lms-button-source", ".jar");
        try (var jar = new JarOutputStream(Files.newOutputStream(path))) {
            for (var node : List.of(parent, child, extended)) { jar.putNextEntry(new JarEntry(node.name + ".class")); jar.write(bytes(node)); jar.closeEntry(); }
        }
        byte[] immutable = Files.readAllBytes(path); calls = 0; last = null;
        try (var loader = new ManagedLegacyClassLoader(path, ButtonFamilyCheck.class.getClassLoader(), new Forge1192LegacyClassTransformer())) {
            var type = loader.linkOwnedClass(child.name.replace('/', '.'));
            var button = (Button)type.getConstructor().newInstance();
            check(button.getX() == 13 && button.getY() == 17 && button.getWidth() == 101 && button.getHeight() == 21 && button.getMessage().getString().equals("source-button"), "source constructor argument identity/value lost");
            button.onPress(new KeyEvent(32, 0, 0));
            check(type.getField("count").getInt(button) == 11 && calls == 1 && last == button, "native activation lost source override/super order or duplicated callback");
            Forge1192ButtonBridge.press(button);
            check(type.getField("count").getInt(button) == 22 && calls == 2 && last == button, "direct source activation lost source virtual dispatch");
            var lookup = MethodHandles.privateLookupIn(type, MethodHandles.lookup());
            var reference = (Runnable)lookup.findVirtual(type, "reference", MethodType.methodType(Runnable.class)).invoke(button);
            reference.run();
            check(type.getField("count").getInt(button) == 33 && calls == 3 && last == button, "source activation callable reference changed capture/dispatch");
            Button nativeButton = Button.builder(Component.literal("native-button"), listener()).bounds(2, 3, 4, 5).build();
            Forge1192ButtonBridge.press(nativeButton);
            check(calls == 4 && last == nativeButton, "native default source activation changed receiver/callback");
            var extendedType = loader.linkOwnedClass(extended.name.replace('/', '.'));
            var extendedButton = (Button)extendedType.getConstructor().newInstance();
            extendedButton.onPress(new KeyEvent(32, 0, 0));
            check(calls == 5 && last == extendedButton && extendedType.getField("count").getInt(extendedButton) == 1, "source ExtendedButton inherited default activation changed");
        } catch (RuntimeException | Error failure) { throw failure; }
        catch (Throwable failure) { throw new IllegalStateException("Source button fixture failed", failure); }
        check(Arrays.equals(immutable, Files.readAllBytes(path)), "original source button JAR mutated");
        var unsupported = fixture("fixture/DirectButtonAllocation", BASE, false);
        var method = new MethodNode(Opcodes.ACC_PUBLIC, "allocate", "()V", null, null);
        method.instructions.add(new TypeInsnNode(Opcodes.NEW, BASE)); method.instructions.add(new InsnNode(Opcodes.POP)); method.instructions.add(new InsnNode(Opcodes.RETURN)); unsupported.methods.add(method);
        try { new Forge1192LegacyClassTransformer().transform(unsupported.name.replace('/', '.'), bytes(unsupported)); throw new AssertionError("unproven direct allocation accepted"); }
        catch (IllegalStateException expected) { check(expected.getMessage().contains("direct legacy Button allocation"), "wrong direct allocation diagnostic"); }
        System.out.println("PASS: source Button superclass construction on actual target, uninitialized-this safety, original geometry/title/callback, native/source/lambda activation, inherited super order and immutable JAR; rendering/input routing unverified");
    }
    private static ClassNode fixture(String name, String parent, boolean child) {
        var node = new ClassNode(); node.name = name; node.superName = parent; node.version = Opcodes.V17; node.access = Opcodes.ACC_PUBLIC;
        if (!child) node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "count", "I", null, null));
        var ctor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        ctor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        if (!child) {
            for (int value : new int[]{13, 17, 101, 21}) ctor.instructions.add(new IntInsnNode(Opcodes.BIPUSH, value));
            ctor.instructions.add(new LdcInsnNode("source-button")); ctor.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/network/chat/Component", "literal", "(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", true));
            ctor.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(ButtonFamilyCheck.class), "listener", "()L" + BASE + "$OnPress;", false));
        }
        ctor.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, parent, "<init>", child ? "()V" : SOURCE_CTOR, false)); ctor.instructions.add(new InsnNode(Opcodes.RETURN)); node.methods.add(ctor);
        var press = new MethodNode(Opcodes.ACC_PUBLIC, "m_5691_", "()V", null, null);
        press.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); press.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, parent, "m_5691_", "()V", false));
        press.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); press.instructions.add(new InsnNode(Opcodes.DUP)); press.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, name, "count", "I")); press.instructions.add(new IntInsnNode(Opcodes.BIPUSH, child ? 10 : 1)); press.instructions.add(new InsnNode(Opcodes.IADD)); press.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, name, "count", "I")); press.instructions.add(new InsnNode(Opcodes.RETURN)); node.methods.add(press);
        var reference = new MethodNode(Opcodes.ACC_PUBLIC, "reference", "()Ljava/lang/Runnable;", null, null);
        reference.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        var metafactory = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false);
        reference.instructions.add(new InvokeDynamicInsnNode("run", "(L" + name + ";)Ljava/lang/Runnable;", metafactory, Type.getMethodType("()V"), new Handle(Opcodes.H_INVOKEVIRTUAL, name, "m_5691_", "()V", false), Type.getMethodType("()V")));
        reference.instructions.add(new InsnNode(Opcodes.ARETURN)); node.methods.add(reference);
        return node;
    }
    private static byte[] bytes(ClassNode node) { var writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS); node.accept(writer); return writer.toByteArray(); }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
