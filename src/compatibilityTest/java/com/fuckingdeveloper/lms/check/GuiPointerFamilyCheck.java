package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.Forge1192GuiPointerBridge;
import net.minecraft.client.gui.components.events.*;
import net.minecraft.client.input.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

final class GuiPointerFamilyCheck implements Opcodes {
    public static class NativeBase extends AbstractContainerEventHandler {
        public int calls, sourceButton;
        public double sourceX, sourceY, dx, dy;
        public MouseButtonEvent seen;
        public boolean twice;
        @Override public List<GuiEventListener> children() { return List.of(); }
        @Override public boolean mouseClicked(MouseButtonEvent event, boolean twice) { calls += 100; seen = event; this.twice = twice; return true; }
        @Override public boolean mouseReleased(MouseButtonEvent event) { calls += 100; seen = event; return false; }
        @Override public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) { calls += 100; seen = event; this.dx = dx; this.dy = dy; return true; }
    }
    static void run() throws Exception {
        context();
        String base = "fixture/ListenerParent", child = "fixture/ListenerChild";
        String nativeBase = Type.getInternalName(NativeBase.class);
        var nodes = new ArrayList<ClassNode>();
        for (String owner : List.of(base, child)) {
            var node = new ClassNode(); node.version = V17; node.access = ACC_PUBLIC; node.name = owner; node.superName = owner.equals(base) ? nativeBase : base;
            for (String[] operation : List.of(new String[]{"m_6375_", "mouseClicked"}, new String[]{"m_6348_", "mouseReleased"}, new String[]{"m_7979_", "mouseDragged"})) {
                boolean drag = operation[1].equals("mouseDragged"); String descriptor = drag ? "(DDIDD)Z" : "(DDI)Z";
                var method = new MethodNode(ACC_PUBLIC, operation[0], descriptor, null, null); node.methods.add(method);
                method.instructions.add(new VarInsnNode(ALOAD, 0)); method.instructions.add(new InsnNode(DUP));
                method.instructions.add(new FieldInsnNode(GETFIELD, nativeBase, "calls", "I"));
                method.instructions.add(new IntInsnNode(BIPUSH, owner.equals(base) ? 1 : 10)); method.instructions.add(new InsnNode(IADD));
                method.instructions.add(new FieldInsnNode(PUTFIELD, nativeBase, "calls", "I"));
                for (String axis : List.of("sourceX", "sourceY")) {
                    method.instructions.add(new VarInsnNode(ALOAD, 0)); method.instructions.add(new VarInsnNode(DLOAD, axis.equals("sourceX") ? 1 : 3));
                    method.instructions.add(new FieldInsnNode(PUTFIELD, nativeBase, axis, "D"));
                }
                method.instructions.add(new VarInsnNode(ALOAD, 0)); method.instructions.add(new VarInsnNode(ILOAD, 5)); method.instructions.add(new FieldInsnNode(PUTFIELD, nativeBase, "sourceButton", "I"));
                method.instructions.add(new VarInsnNode(ALOAD, 0)); method.instructions.add(new VarInsnNode(DLOAD, 1)); method.instructions.add(new VarInsnNode(DLOAD, 3)); method.instructions.add(new VarInsnNode(ILOAD, 5));
                if (drag) { method.instructions.add(new VarInsnNode(DLOAD, 6)); method.instructions.add(new VarInsnNode(DLOAD, 8)); }
                method.instructions.add(new MethodInsnNode(INVOKESPECIAL, node.superName, operation[0], descriptor, false)); method.instructions.add(new InsnNode(IRETURN));
                var handle = new MethodNode(ACC_PUBLIC | ACC_STATIC, operation[1] + "Handle", "()Ljava/lang/invoke/MethodHandle;", null, null);
                handle.instructions.add(new LdcInsnNode(new Handle(H_INVOKEVIRTUAL, owner, operation[0], descriptor, false))); handle.instructions.add(new InsnNode(ARETURN)); node.methods.add(handle);
            }
            nodes.add(node);
        }
        var unrelated = new ClassNode(); unrelated.version = V17; unrelated.access = ACC_PUBLIC; unrelated.name = "fixture/UnrelatedPointer"; unrelated.superName = "java/lang/Object";
        var negative = new MethodNode(ACC_PUBLIC, "m_6375_", "(DDI)Z", null, null); negative.instructions.add(new InsnNode(ICONST_0)); negative.instructions.add(new InsnNode(IRETURN)); unrelated.methods.add(negative); nodes.add(unrelated);
        var defaults = new ClassNode(); defaults.version = V17; defaults.access = ACC_PUBLIC; defaults.name = "fixture/ListenerDefaults"; defaults.superName = "java/lang/Object"; defaults.interfaces.add("net/minecraft/client/gui/components/events/GuiEventListener");
        for (String[] operation : List.of(new String[]{"m_6375_", "mouseClicked"}, new String[]{"m_6348_", "mouseReleased"}, new String[]{"m_7979_", "mouseDragged"})) {
            boolean drag = operation[1].equals("mouseDragged"); String descriptor = drag ? "(DDIDD)Z" : "(DDI)Z";
            var method = new MethodNode(ACC_PUBLIC, operation[0], descriptor, null, null); defaults.methods.add(method);
            method.instructions.add(new VarInsnNode(ALOAD, 0)); method.instructions.add(new VarInsnNode(DLOAD, 1)); method.instructions.add(new VarInsnNode(DLOAD, 3)); method.instructions.add(new VarInsnNode(ILOAD, 5));
            if (drag) { method.instructions.add(new VarInsnNode(DLOAD, 6)); method.instructions.add(new VarInsnNode(DLOAD, 8)); }
            method.instructions.add(new MethodInsnNode(INVOKESPECIAL, defaults.interfaces.getFirst(), operation[0], descriptor, true)); method.instructions.add(new InsnNode(IRETURN));
        }
        nodes.add(defaults);
        Path path = Files.createTempFile("lms-listener-pointer", ".jar");
        try (var jar = new JarOutputStream(Files.newOutputStream(path))) {
            for (var node : nodes) { var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); node.accept(writer); jar.putNextEntry(new JarEntry(node.name + ".class")); jar.write(writer.toByteArray()); jar.closeEntry(); }
        }
        var field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); field.setAccessible(true); var unsafe = (sun.misc.Unsafe) field.get(null);
        try (var loader = new ManagedLegacyClassLoader(path, GuiPointerFamilyCheck.class.getClassLoader(), new Forge1192LegacyClassTransformer())) {
            var type = loader.linkOwnedClass(child.replace('/', '.')); var listener = (NativeBase) unsafe.allocateInstance(type);
            var event = new MouseButtonEvent(7.25, 13.5, new MouseButtonInfo(3, com.mojang.blaze3d.platform.InputConstants.MOD_CONTROL));
            check(listener.mouseClicked(event, true), "click consumption lost"); check(!listener.mouseReleased(event), "release consumption changed"); check(listener.mouseDragged(event, 2.5, -3.5), "drag consumption lost");
            check(listener.calls == 333, "source virtual/special order duplicated or bypassed: " + listener.calls);
            check(listener.sourceButton == 1 && listener.sourceX == 7.25 && listener.sourceY == 13.5 && listener.dx == 2.5 && listener.dy == -3.5, "source mouse tuple changed");
            check(listener.seen == event && listener.twice, "native event identity/modifiers/double-click lost through source super");
            for (String op : List.of("mouseClicked", "mouseReleased", "mouseDragged")) {
                var handle = (java.lang.invoke.MethodHandle) type.getMethod(op + "Handle").invoke(null);
                check(handle.type().parameterType(0) == type, "source callable receiver widened");
                try { if (op.equals("mouseDragged")) handle.invokeWithArguments(listener, 20., 30., 0, 4., 5.); else handle.invokeWithArguments(listener, 20., 30., 0); }
                catch (RuntimeException | Error failure) { throw failure; } catch (Throwable failure) { throw new AssertionError(failure); }
            }
            check(listener.calls == 666 && listener.seen.button() == 1 && listener.seen.modifiers() == 0 && !listener.twice, "source callable/synthetic input dispatch lost");
            var unrelatedType = loader.linkOwnedClass(unrelated.name.replace('/', '.')); unrelatedType.getDeclaredMethod("m_6375_", double.class, double.class, int.class);
            check(Arrays.stream(unrelatedType.getDeclaredMethods()).noneMatch(m -> m.getName().equals("mouseClicked")), "descriptor-only owner rewritten");
            var defaultView = (GuiEventListener) unsafe.allocateInstance(loader.linkOwnedClass(defaults.name.replace('/', '.')));
            check(!defaultView.mouseClicked(event, true) && !defaultView.mouseReleased(event) && !defaultView.mouseDragged(event, 1, 2), "source listener interface default changed");
        } finally { Files.deleteIfExists(path); }
        originalArtifact();
        Path carbon=Path.of("legacy-mods/CarbonConfig-1.19.2-2.0.2.jar");
        if(Files.exists(carbon))try(var loader=new ManagedLegacyClassLoader(carbon,GuiPointerFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())){loader.linkOwnedClass("carbonconfiglib.gui.screens.WidgetAlignerScreen").getDeclaredMethods();}
        System.out.println("PASS: source listener click/release/drag native entries, exact tuple/consumption, inherited virtual/special/default dispatch, callable receiver identity, native gesture/modifier context, nested/failure restoration and original slider/entry ABI; interactive acceptance remains separate");
    }
    private static void context() {
        var outer = new MouseButtonEvent(1, 2, new MouseButtonInfo(1, 64)); var inner = new MouseButtonEvent(3, 4, new MouseButtonInfo(3, 128));
        var expected = new IllegalArgumentException("pointer-context");
        check(Forge1192GuiPointerBridge.clicked((x, y, b) -> {
            check(Forge1192GuiPointerBridge.event(x, y, b) == outer && Forge1192GuiPointerBridge.twice(), "outer native input not retained");
            try { Forge1192GuiPointerBridge.released((ix, iy, ib) -> { check(Forge1192GuiPointerBridge.event(ix, iy, ib) == inner && !Forge1192GuiPointerBridge.twice(), "nested native input not isolated"); throw expected; }, inner); throw new AssertionError("failure swallowed"); }
            catch (IllegalArgumentException actual) { check(actual == expected, "failure identity changed"); }
            check(Forge1192GuiPointerBridge.event(x, y, b) == outer && Forge1192GuiPointerBridge.twice(), "failed nested input leaked");
            return true;
        }, outer, true), "context result changed");
        check(!Forge1192GuiPointerBridge.twice() && Forge1192GuiPointerBridge.event(1, 2, 0).modifiers() == 0, "completed native context leaked");
    }
    private static void originalArtifact() throws Exception {
        Path artifact = Path.of("legacy-mods/IC2Classic-1.19.2-2.1.3.4.jar"); if (!Files.exists(artifact)) return;
        try (var loader = new ManagedLegacyClassLoader(artifact, GuiPointerFamilyCheck.class.getClassLoader(), new Forge1192LegacyClassTransformer())) {
            for (String name : List.of("ic2.core.inventory.gui.IC2Screen", "ic2.core.inventory.gui.ComponentContainerScreen")) {
                var type = loader.linkOwnedClass(name);
                check(Forge1192GuiPointerBridge.Click.class.isAssignableFrom(type) && Forge1192GuiPointerBridge.Release.class.isAssignableFrom(type), "original source pointer interfaces missing");
                // Do not resolve unrelated source Widget signatures while checking this callback family.
                var projected=new ClassNode();new ClassReader(loader.definedClassBytes(type.getName())).accept(projected,ClassReader.SKIP_CODE);
                for(String operation:List.of("mouseClicked","mouseReleased"))check(projected.methods.stream().anyMatch(m->m.name.equals(operation)&&m.desc.contains("MouseButtonEvent")),"original source native pointer entry missing");
                if(name.endsWith("ComponentContainerScreen"))check(projected.methods.stream().anyMatch(m->m.name.equals("mouseDragged")&&m.desc.contains("MouseButtonEvent")),"original drag entry missing");
            }
            var boxType = loader.linkOwnedClass("ic2.core.utils.math.geometry.Box2i");
            var sliderType = loader.linkOwnedClass("ic2.core.inventory.gui.components.simple.SliderComponent");
            var box = boxType.getConstructor(int.class, int.class, int.class, int.class).newInstance(0, 0, 12, 45);
            var texture = boxType.getConstructor(int.class, int.class, int.class, int.class).newInstance(0, 0, 12, 15);
            var slider = sliderType.getConstructor(boxType, boxType).newInstance(box, texture);
            sliderType.getMethod("setNonEmptyRows", int.class).invoke(slider, 2); sliderType.getMethod("setMax", int.class).invoke(slider, 5);
            sliderType.getMethod("onMouseClick", int.class, int.class, int.class).invoke(slider, 5, 44, 0);
            check((int) sliderType.getMethod("getCurrent").invoke(slider) > 0, "original slider click failed");
            var dragging = sliderType.getDeclaredField("dragging"); dragging.setAccessible(true); check(dragging.getBoolean(slider), "original dragging not started");
            sliderType.getMethod("onMouseRelease", int.class, int.class, int.class).invoke(slider, 5, 44, 0); check(!dragging.getBoolean(slider), "original drag release lost");
            sliderType.getMethod("onMouseScroll", int.class, int.class, int.class).invoke(slider, 5, 44, 10);
            check((int) sliderType.getMethod("getCurrent").invoke(slider) == 0, "original source wheel behavior changed");
            // Execute the original screen routing at the actual slider hit coordinates.
            // The source callback deliberately narrows its double amount to int.
            var unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);
            var unsafe=(sun.misc.Unsafe)unsafeField.get(null);
            var screenType=loader.linkOwnedClass("ic2.core.inventory.gui.ComponentContainerScreen");
            var screen=(GuiEventListener)unsafe.allocateInstance(screenType);
            var requestType=loader.linkOwnedClass("ic2.core.inventory.gui.components.GuiWidget$ActionRequest");
            Object[] requests=(Object[])requestType.getMethod("values").invoke(null);
            java.util.List<?>[] components=new java.util.List<?>[requests.length];
            Arrays.fill(components,List.of());
            Object scrollRequest=requestType.getField("MOUSE_SCROLL").get(null);
            int index=(int)requestType.getMethod("getIndex").invoke(scrollRequest);
            components[index]=List.of(slider);
            var componentField=screenType.getDeclaredField("components");componentField.setAccessible(true);componentField.set(screen,components);
            for(int i=0;i<10;i++)check(screen.mouseScrolled(5,20,0,-.4),"original fractional source scroll did not reach hit slider");
            check((int)sliderType.getMethod("getCurrent").invoke(slider)==0,"source integer consumer unexpectedly accumulates fractional deltas");
            check(screen.mouseScrolled(5,20,0,-1),"whole source step not consumed at slider");
            check((int)sliderType.getMethod("getCurrent").invoke(slider)==1,"original screen whole-step route failed");
            check(screen.mouseScrolled(5,20,0,1),"reverse whole step not consumed");
            check((int)sliderType.getMethod("getCurrent").invoke(slider)==0,"original reverse source step failed");
            System.out.println("PASS: actual original component-screen native wheel entry -> original slider; ten fractional events remain zero by source D2I, +/-1 steps scroll at the same hit coordinates");
        }
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
