package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192EventBusBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192LifecycleBridge;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;

import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** A removed utility-method type must not prevent a supported listener linking. */
public final class SubscriberIndexCheck {
    public static class ProbeEvent extends Event implements ICancellableEvent {}
    public static class SecondEvent extends Event {}
    public static final AtomicInteger STATIC_CALLS = new AtomicInteger();

    public static void run(IEventBus ownerBus, ModContainer host) throws Exception {
        var parent = subscriber("fixture/SubscriberParent", "java/lang/Object", true, false, false);
        var child = subscriber("fixture/SubscriberChild", parent.name, false, false, false);
        var canceled = subscriber("fixture/CanceledSubscriber", "java/lang/Object", true, true, false);
        var statics = subscriber("fixture/StaticSubscriber", "java/lang/Object", true, false, true);
        var invalid = subscriber("fixture/InvalidSubscriber", "java/lang/Object", true, false, false);
        invalid.methods.add(handler(invalid.name, "zMissing", "Lfixture/UnsupportedEvent;", true, false, false));
        var failing = subscriber("fixture/FailingSubscriber", "java/lang/Object", true, false, false);
        failing.methods.add(handler(failing.name, "zSecond", Type.getDescriptor(SecondEvent.class), true, false, false));
        var path = Files.createTempFile("lms-subscriber-source", ".jar");
        try (var jar = new JarOutputStream(Files.newOutputStream(path))) {
            for (var node : List.of(parent, child, canceled, statics, invalid, failing)) {
                jar.putNextEntry(new JarEntry(node.name + ".class")); jar.write(bytes(node)); jar.closeEntry();
            }
        }
        byte[] immutable = Files.readAllBytes(path);
        try (var loader = new ManagedLegacyClassLoader(path, SubscriberIndexCheck.class.getClassLoader(), new Forge1192LegacyClassTransformer())) {
            Object remembered; Class<?> rememberedType; IEventBus rememberedBus;
            try (var scope = Forge1192LifecycleBridge.enter("subscriber_check", ownerBus, host)) {
            Class<?> type = loader.linkOwnedClass(child.name.replace('/', '.'));
            Object instance = type.getConstructor().newInstance();
            try { type.getDeclaredMethods(); throw new AssertionError("fixture utility type unexpectedly linked"); }
            catch (NoClassDefFoundError expected) { check(expected.getMessage().contains("ItemRenderer"), "wrong missing utility cause"); }
            byte[] snapshot = loader.definedClassBytes(type.getName()); snapshot[0] = 0;
            check(loader.definedClassBytes(type.getName())[0] != 0, "defined-class metadata exposed mutable storage");
            var bus = BusBuilder.builder().build(); bus.start();
            Forge1192EventBusBridge.register(bus, instance); Forge1192EventBusBridge.register(bus, instance);
            bus.post(new ProbeEvent()); check(calls(type, instance) == 1, "inherited annotation/virtual override or duplicate registration");
            var event = new ProbeEvent(); event.setCanceled(true); bus.post(event);
            check(calls(type, instance) == 1, "receiveCanceled=false was ignored");
            var canceledType = loader.linkOwnedClass(canceled.name.replace('/', '.')); Object canceledObject = canceledType.getConstructor().newInstance();
            Forge1192EventBusBridge.register(bus, canceledObject); bus.post(event);
            check(calls(canceledType, canceledObject) == 1, "receiveCanceled=true lost");
            Forge1192EventBusBridge.unregister(bus, instance); bus.post(new ProbeEvent());
            check(calls(type, instance) == 1 && calls(canceledType, canceledObject) == 2, "unregister removed another subscriber or retained a wrapper");
            Forge1192EventBusBridge.register(bus, instance); bus.post(new ProbeEvent());
            check(calls(type, instance) == 2, "close/reopen subscriber registration failed");
            var staticType = loader.linkOwnedClass(statics.name.replace('/', '.'));
            Forge1192EventBusBridge.register(bus, staticType); bus.post(new ProbeEvent());
            check(STATIC_CALLS.get() == 1, "Class registration did not select static handlers");
            Forge1192EventBusBridge.unregister(bus, staticType);
            var invalidType = loader.linkOwnedClass(invalid.name.replace('/', '.')); Object invalidObject = invalidType.getConstructor().newInstance();
            try { Forge1192EventBusBridge.register(bus, invalidObject); throw new AssertionError("missing annotated event silently skipped"); }
            catch (IllegalStateException expected) { check(expected.getMessage().contains("zMissing"), "missing handler lacked symbol evidence"); }
            bus.post(new ProbeEvent()); check(calls(invalidType, invalidObject) == 0, "invalid batch partially installed");
            var failingType = loader.linkOwnedClass(failing.name.replace('/', '.')); Object failingObject = failingType.getConstructor().newInstance();
            var fault = new IllegalStateException("injected second registration failure");
            var proxy = (IEventBus)java.lang.reflect.Proxy.newProxyInstance(IEventBus.class.getClassLoader(), new Class<?>[]{IEventBus.class}, (p,m,args) -> {
                if (m.getName().equals("addListener") && args.length == 4 && args[2] == SecondEvent.class) throw fault;
                try { return m.invoke(bus, args); } catch (java.lang.reflect.InvocationTargetException e) { throw e.getCause(); }
            });
            try { Forge1192EventBusBridge.register(proxy, failingObject); throw new AssertionError("registration failure swallowed"); }
            catch (IllegalStateException expected) { check(expected == fault, "registration cause changed"); }
            bus.post(new ProbeEvent()); check(calls(failingType, failingObject) == 0, "partial registration rollback retained a native listener");
            AtomicInteger lambdaCalls = new AtomicInteger(); java.util.function.Consumer<ProbeEvent> lambda = ignored -> lambdaCalls.incrementAndGet();
            Forge1192EventBusBridge.addListener(bus, lambda, false, ProbeEvent.class);
            bus.post(new ProbeEvent()); check(lambdaCalls.get() == 1, "lambda wrapper missing");
            Forge1192EventBusBridge.unregister(bus, lambda); bus.post(new ProbeEvent()); check(lambdaCalls.get() == 1, "lambda source identity did not remove its wrapper");
            AtomicInteger ticks = new AtomicInteger(); java.util.function.Consumer<net.neoforged.neoforge.event.tick.LevelTickEvent> tick = ignored -> ticks.incrementAndGet();
            Forge1192EventBusBridge.addListener(bus, tick, false, net.neoforged.neoforge.event.tick.LevelTickEvent.class);
            bus.post(new net.neoforged.neoforge.event.tick.LevelTickEvent.Pre(() -> true, null)); bus.post(new net.neoforged.neoforge.event.tick.LevelTickEvent.Post(() -> true, null));
            check(ticks.get() == 2, "source tick registration did not fan out");
            Forge1192EventBusBridge.unregister(bus, tick);
            bus.post(new net.neoforged.neoforge.event.tick.LevelTickEvent.Pre(() -> true, null)); bus.post(new net.neoforged.neoforge.event.tick.LevelTickEvent.Post(() -> true, null));
            check(ticks.get() == 2, "source unregister retained a fanout wrapper");
            Forge1192EventBusBridge.unregister(bus, instance); Forge1192EventBusBridge.unregister(bus, canceledObject);
            remembered = instance; rememberedType = type; rememberedBus = bus;
            }
            check(Forge1192LifecycleBridge.currentContext().isEmpty(), "registration fixture retained a source context");
            int before = calls(rememberedType, remembered);
            Forge1192EventBusBridge.register(rememberedBus, remembered);
            rememberedBus.post(new ProbeEvent());
            check(calls(rememberedType, remembered) == before + 1 && Forge1192LifecycleBridge.currentContext().isEmpty(), "native reinitialization lost recorded owner or leaked context");
            Forge1192EventBusBridge.unregister(rememberedBus, remembered);
            Object unknown = rememberedType.getConstructor().newInstance();
            try { Forge1192EventBusBridge.register(rememberedBus, unknown); throw new AssertionError("unknown source owner silently inferred"); }
            catch (IllegalStateException expected) { check(expected.getMessage().contains("No source owner recorded"), "wrong unknown owner failure"); }
        }
        check(Arrays.equals(immutable, Files.readAllBytes(path)), "source fixture JAR was changed");
        System.out.println("PASS: exact defined metadata, absent unrelated utility types, inherited/virtual/static handlers, cancellation, duplicate prevention, unregister/reopen, lambda ownership, missing handler rejection and partial-install rollback");
    }
    private static int calls(Class<?> type, Object receiver) throws Exception { return type.getField("calls").getInt(receiver); }
    private static ClassNode subscriber(String name, String parent, boolean annotation, boolean canceled, boolean staticHandler) {
        var node = new ClassNode(); node.version = Opcodes.V17; node.access = Opcodes.ACC_PUBLIC; node.name = name; node.superName = parent;
        if (parent.equals("java/lang/Object")) node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "calls", "I", null, null));
        var constructor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); constructor.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, parent, "<init>", "()V", false)); constructor.instructions.add(new InsnNode(Opcodes.RETURN)); node.methods.add(constructor);
        node.methods.add(handler(name, "aProbe", Type.getDescriptor(ProbeEvent.class), annotation, canceled, staticHandler));
        var utility = new MethodNode(Opcodes.ACC_PUBLIC, "unusedRenderer", "(Lnet/minecraft/client/renderer/entity/ItemRenderer;)V", null, null);
        utility.instructions.add(new InsnNode(Opcodes.RETURN)); node.methods.add(utility); return node;
    }
    private static MethodNode handler(String owner, String name, String event, boolean annotation, boolean canceled, boolean isStatic) {
        var method = new MethodNode(Opcodes.ACC_PUBLIC | (isStatic ? Opcodes.ACC_STATIC : 0), name, "(" + event + ")V", null, null);
        if (annotation) {
            var subscribed = new AnnotationNode("Lnet/minecraftforge/eventbus/api/SubscribeEvent;");
            subscribed.values = List.of("receiveCanceled", canceled); method.visibleAnnotations = List.of(subscribed);
        }
        if (isStatic) {
            method.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, Type.getInternalName(SubscriberIndexCheck.class), "STATIC_CALLS", "Ljava/util/concurrent/atomic/AtomicInteger;"));
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/util/concurrent/atomic/AtomicInteger", "incrementAndGet", "()I", false)); method.instructions.add(new InsnNode(Opcodes.POP));
        } else {
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); method.instructions.add(new InsnNode(Opcodes.DUP));
            method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, owner, "calls", "I")); method.instructions.add(new InsnNode(Opcodes.ICONST_1)); method.instructions.add(new InsnNode(Opcodes.IADD)); method.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, owner, "calls", "I"));
        }
        method.instructions.add(new InsnNode(Opcodes.RETURN)); return method;
    }
    private static byte[] bytes(ClassNode node) { var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); node.accept(writer); return writer.toByteArray(); }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
