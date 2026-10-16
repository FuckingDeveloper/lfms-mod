package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.lang.invoke.*;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.*;
import java.util.function.IntSupplier;
import java.util.jar.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Source getters on actual native state; full source jars remain immutable. */
public final class ScreenStateFamilyCheck {
    private static final String BASE = "net/minecraft/client/gui/screens/inventory/AbstractContainerScreen";
    private static final String SLOT = "net/minecraft/world/inventory/Slot";
    private static final List<String> SOURCE = List.of("getGuiLeft", "getGuiTop", "getXSize", "getYSize", "getSlotUnderMouse");
    private static final List<String> TARGET = List.of("getLeftPos", "getTopPos", "getImageWidth", "getImageHeight", "getHoveredSlot");
    private static String desc(int i) { return i == 4 ? "()L" + SLOT + ";" : "()I"; }
    public static Inventory inventory() { return new Inventory(null, new EntityEquipment()); }

    public static void run() throws Exception {
        var transformer = new Forge1192LegacyClassTransformer();
        var parent = screen("fixture/StateParent", BASE, false);
        addPassEvents(parent);
        var child = screen("fixture/StateChild", parent.name, true);
        Map<String, byte[]> originals = Map.of(parent.name, bytes(parent), child.name, bytes(child));
        transformer.bindManagedClassBytes(originals::get);
        var caller = caller(child.name);
        var mapped = transformed(transformer, caller);
        for (int i = 0; i < SOURCE.size(); i++) {
            var call = findCall(mapped.methods.get(i));
            check(call.name.equals(TARGET.get(i)) && call.owner.equals(child.name) && call.desc.equals(desc(i)), "inherited screen getter not projected");
        }
        for (String owner : List.of(BASE, child.name)) {
            var constants = node("fixture/StateConstants", "java/lang/Object");
            var method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "constants", "()V", null, null);
            var bootstrap = new Handle(Opcodes.H_INVOKESTATIC, "fixture/Bootstrap", "constant", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;", false);
            for (int i = 0; i < SOURCE.size(); i++) {
                var virtual = new Handle(Opcodes.H_INVOKEVIRTUAL, owner, SOURCE.get(i), desc(i), false);
                var special = new Handle(Opcodes.H_INVOKESPECIAL, owner, SOURCE.get(i), desc(i), false);
                method.instructions.add(new LdcInsnNode(virtual)); method.instructions.add(new InsnNode(Opcodes.POP));
                method.instructions.add(new LdcInsnNode(special)); method.instructions.add(new InsnNode(Opcodes.POP));
                method.instructions.add(new LdcInsnNode(new ConstantDynamic("outer", "Ljava/lang/Object;", bootstrap, new ConstantDynamic("inner", "Ljava/lang/Object;", bootstrap, virtual)))); method.instructions.add(new InsnNode(Opcodes.POP));
            }
            method.instructions.add(new InsnNode(Opcodes.RETURN)); constants.methods.add(method);
            var output = transformed(transformer, constants);
            for (var instruction : output.methods.getFirst().instructions) if (instruction instanceof LdcInsnNode ldc) verifyConstant(ldc.cst);
        }
        for (String owner : List.of("fixture/Unrelated", BASE)) {
            var negative = node("fixture/NegativeScreen", "java/lang/Object");
            var method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "negative", "()V", null, null);
            var handle = new Handle(owner.equals(BASE) ? Opcodes.H_INVOKESTATIC : Opcodes.H_INVOKEVIRTUAL, owner, "getGuiLeft", "()I", false);
            method.instructions.add(new LdcInsnNode(handle)); method.instructions.add(new InsnNode(Opcodes.POP)); method.instructions.add(new InsnNode(Opcodes.RETURN)); negative.methods.add(method);
            check(((LdcInsnNode)transformed(transformer, negative).methods.getFirst().instructions.getFirst()).cst.equals(handle), "unrelated/static screen member projected");
        }
        var collision = screen("fixture/CollisionScreen", BASE, false);
        var method = new MethodNode(Opcodes.ACC_PUBLIC, "getLeftPos", "()I", null, null); method.instructions.add(new InsnNode(Opcodes.ICONST_0)); method.instructions.add(new InsnNode(Opcodes.IRETURN)); collision.methods.add(method);
        try { transformed(transformer, collision); throw new AssertionError("target-name collision accepted"); }
        catch (IllegalStateException expected) { check(expected.getMessage().contains("getter collision"), "wrong getter collision failure"); }
        System.out.println("PASS: all five container-screen getters, inherited declarations/super dispatch, receiver/opcode negatives, virtual/special handles and nested constants, target-name collisions");
    }
    private static void verifyConstant(Object value) {
        if (value instanceof Handle handle) check(TARGET.contains(handle.getName()), "screen callable reference missed");
        if (value instanceof ConstantDynamic dynamic) for (int i = 0; i < dynamic.getBootstrapMethodArgumentCount(); i++) verifyConstant(dynamic.getBootstrapMethodArgument(i));
    }
    public static void runNative() throws Exception {
        run();
        check(!Modifier.isFinal(AbstractContainerScreen.class.getDeclaredField("imageWidth").getModifiers())
                && !Modifier.isFinal(AbstractContainerScreen.class.getDeclaredField("imageHeight").getModifiers()), "native dimension mutability was not applied");
        var parent = screen("fixture/StateParent", BASE, false);
        addPassEvents(parent);
        var child = screen("fixture/StateChild", parent.name, true);
        var caller = caller(child.name);
        var path = Files.createTempFile("lms-screen-state-source", ".jar");
        try (var jar = new JarOutputStream(Files.newOutputStream(path))) {
            for (var node : List.of(parent, child, caller)) { jar.putNextEntry(new JarEntry(node.name + ".class")); jar.write(bytes(node)); jar.closeEntry(); }
        }
        byte[] immutable = Files.readAllBytes(path);
        try (var loader = new ManagedLegacyClassLoader(path, ScreenStateFamilyCheck.class.getClassLoader(), new Forge1192LegacyClassTransformer())) {
            var childType = loader.linkOwnedClass(child.name.replace('/', '.'));
            // State-only probe: Screen's constructor requires a running Minecraft singleton.
            // Allocate without construction; exercise actual native fields/getters, never UI lifecycle.
            var unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); unsafeField.setAccessible(true);
            var unsafe = (sun.misc.Unsafe)unsafeField.get(null);
            var screen = (AbstractContainerScreen<?>)unsafe.allocateInstance(childType);
            childType.getMethod("pass",boolean.class).invoke(screen,false);
            check(!(boolean)childType.getMethod("pass").invoke(screen),"native screen gameplay-input gate not preserved");
            var passHandle=(MethodHandle)childType.getMethod("passHandle").invoke(null);
            check(passHandle.type().equals(MethodType.methodType(void.class,loader.linkOwnedClass(parent.name.replace('/','.')),boolean.class)),"source screen field handle type changed");
            passHandle.invoke(screen,false);
            try { childType.getMethod("pass",boolean.class).invoke(screen,true); throw new AssertionError("unproven input passthrough accepted"); }
            catch(java.lang.reflect.InvocationTargetException expectedFailure) {check(expectedFailure.getCause() instanceof UnsupportedOperationException,"wrong passEvents boundary");}
            var callerType = loader.linkOwnedClass(caller.name.replace('/', '.'));
            var lookup = MethodHandles.privateLookupIn(childType, MethodHandles.lookup());
            var update = lookup.findVirtual(childType, "state", MethodType.methodType(void.class, int.class, int.class, int.class, int.class, Slot.class));
            var slot = new Slot(new net.minecraft.world.SimpleContainer(1), 0, 1, 2);
            update.invoke(screen, 11, 23, 211, 177, slot);
            int[] expected = {36, 23, 211, 177}; // (native left + parent override 7) * child override 2.
            for (int i = 0; i < SOURCE.size(); i++) {
                Object value = MethodHandles.publicLookup().findStatic(callerType, "read" + i, MethodType.methodType(i == 4 ? Slot.class : int.class, childType)).invoke(screen);
                check(i == 4 ? value == slot : value.equals(expected[i]), "actual screen getter state/override changed: " + SOURCE.get(i));
            }
            var supplier = (IntSupplier)lookup.findVirtual(childType, "reference", MethodType.methodType(IntSupplier.class)).invoke(screen);
            check(supplier.getAsInt() == 36 && screen.getLeftPos() == 36, "lambda/native virtual dispatch bypassed source overrides");
            update.invoke(screen, 21, 31, 221, 187, (Slot)null);
            check(supplier.getAsInt() == 56 && screen.getTopPos() == 31 && screen.getImageWidth() == 221 && screen.getImageHeight() == 187 && screen.getHoveredSlot() == null, "screen getter read snapshot or lost absent slot/dimension writes");
        } catch (RuntimeException | Error failure) { throw failure; }
        catch (Throwable failure) { throw new IllegalStateException("Native source screen fixture failed", failure); }
        check(Arrays.equals(immutable, Files.readAllBytes(path)), "source screen JAR changed");
        System.out.println("PASS: actual native container state, once-only source overrides/super/lambda dispatch, protected width/height writes after construction, live state and genuine absent hovered slot; no screen/window opened");
    }
    private static void addPassEvents(ClassNode node) {
        var write=new MethodNode(Opcodes.ACC_PUBLIC,"pass","(Z)V",null,null);node.methods.add(write);
        write.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));write.instructions.add(new VarInsnNode(Opcodes.ILOAD,1));write.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD,node.name,"f_96546_","Z"));write.instructions.add(new InsnNode(Opcodes.RETURN));
        var read=new MethodNode(Opcodes.ACC_PUBLIC,"pass","()Z",null,null);node.methods.add(read);
        read.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));read.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,node.name,"f_96546_","Z"));read.instructions.add(new InsnNode(Opcodes.IRETURN));
        var handle=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"passHandle","()Ljava/lang/invoke/MethodHandle;",null,null);node.methods.add(handle);
        handle.instructions.add(new LdcInsnNode(new Handle(Opcodes.H_PUTFIELD,node.name,"f_96546_","Z",false)));handle.instructions.add(new InsnNode(Opcodes.ARETURN));
    }
    private static ClassNode screen(String name, String parent, boolean child) {
        var node = node(name, parent);
        var ctor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        ctor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        if (!child) {
            ctor.instructions.add(new InsnNode(Opcodes.ACONST_NULL)); // Menu is not consumed by the isolated getter fixture.
            ctor.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(ScreenStateFamilyCheck.class), "inventory", "()Lnet/minecraft/world/entity/player/Inventory;", false));
            ctor.instructions.add(new LdcInsnNode("fixture"));
            ctor.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/network/chat/Component", "literal", "(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", true));
        }
        ctor.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, parent, "<init>", child ? "()V" : "(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;)V", false));
        ctor.instructions.add(new InsnNode(Opcodes.RETURN)); node.methods.add(ctor);
        var override = new MethodNode(Opcodes.ACC_PUBLIC, "getGuiLeft", "()I", null, null);
        override.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); override.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, parent, "getGuiLeft", "()I", false));
        override.instructions.add(new IntInsnNode(Opcodes.BIPUSH, child ? 2 : 7)); override.instructions.add(new InsnNode(child ? Opcodes.IMUL : Opcodes.IADD)); override.instructions.add(new InsnNode(Opcodes.IRETURN)); node.methods.add(override);
        if (!child) {
            var state = new MethodNode(Opcodes.ACC_PUBLIC, "state", "(IIIIL" + SLOT + ";)V", null, null);
            String[] fields = {"leftPos", "topPos", "imageWidth", "imageHeight", "hoveredSlot"};
            for (int i = 0; i < fields.length; i++) {
                state.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); state.instructions.add(new VarInsnNode(i == 4 ? Opcodes.ALOAD : Opcodes.ILOAD, i + 1));
                state.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, BASE, fields[i], i == 4 ? "L" + SLOT + ";" : "I"));
            }
            state.instructions.add(new InsnNode(Opcodes.RETURN)); node.methods.add(state);
        }
        var reference = new MethodNode(Opcodes.ACC_PUBLIC, "reference", "()Ljava/util/function/IntSupplier;", null, null);
        reference.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        var metafactory = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false);
        reference.instructions.add(new InvokeDynamicInsnNode("getAsInt", "(L" + name + ";)Ljava/util/function/IntSupplier;", metafactory, Type.getMethodType("()I"), new Handle(Opcodes.H_INVOKEVIRTUAL, name, "getGuiLeft", "()I", false), Type.getMethodType("()I")));
        reference.instructions.add(new InsnNode(Opcodes.ARETURN)); node.methods.add(reference);
        return node;
    }
    private static ClassNode caller(String owner) {
        var node = node("fixture/ScreenStateCaller", "java/lang/Object");
        for (int i = 0; i < SOURCE.size(); i++) {
            var method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "read" + i, "(L" + owner + ";)" + (i == 4 ? "L" + SLOT + ";" : "I"), null, null);
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, owner, SOURCE.get(i), desc(i), false)); method.instructions.add(new InsnNode(i == 4 ? Opcodes.ARETURN : Opcodes.IRETURN)); node.methods.add(method);
        }
        return node;
    }
    private static MethodInsnNode findCall(MethodNode method) { for (var instruction : method.instructions) if (instruction instanceof MethodInsnNode call) return call; throw new AssertionError("call absent"); }
    private static ClassNode transformed(Forge1192LegacyClassTransformer transformer, ClassNode source) { var output = new ClassNode(); new ClassReader(transformer.transform(source.name.replace('/', '.'), bytes(source)).bytes()).accept(output, 0); return output; }
    private static ClassNode node(String name, String parent) { var node = new ClassNode(); node.version = Opcodes.V17; node.access = Opcodes.ACC_PUBLIC; node.name = name; node.superName = parent; return node; }
    private static byte[] bytes(ClassNode node) { var writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS); node.accept(writer); return writer.toByteArray(); }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
