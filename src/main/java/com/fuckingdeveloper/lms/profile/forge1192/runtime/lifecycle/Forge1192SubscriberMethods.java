package com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import java.io.*;
import java.lang.invoke.*;
import java.util.*;
import net.neoforged.bus.api.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Select only source event methods before linking their parameter classes. */
final class Forge1192SubscriberMethods {
    record Subscription(EventPriority priority, boolean receiveCanceled) {}
    record Member(String name, String descriptor, String signature, int access, Subscription annotation) {}
    record Handler(String symbol, Class<? extends Event> eventType, EventPriority priority,
            boolean receiveCanceled, String signature, MethodHandle invocation) {
        void invoke(Event event) {
            try { invocation.invokeExact(event); }
            catch (RuntimeException | Error failure) { throw failure; }
            catch (Throwable failure) { throw new IllegalStateException("Legacy event subscriber failed: " + symbol, failure); }
        }
    }
    private static final ClassValue<List<Member>> INDEX = new ClassValue<>() {
        @Override protected List<Member> computeValue(Class<?> type) {
            byte[] bytes;
            if (type.getClassLoader() instanceof ManagedLegacyClassLoader managed) bytes = managed.definedClassBytes(type.getName());
            else try (var input = type.getResourceAsStream("/" + type.getName().replace('.', '/') + ".class")) {
                if (input == null) throw new IllegalStateException("Subscriber class-file metadata unavailable: " + type.getName());
                bytes = input.readAllBytes();
            } catch (IOException failure) { throw new UncheckedIOException(failure); }
            var node = new ClassNode(); new ClassReader(bytes).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            if (!node.name.equals(type.getName().replace('.', '/'))) throw new IllegalStateException("Subscriber metadata identity mismatch: " + type.getName());
            var methods = new ArrayList<Member>();
            for (var method : node.methods) {
                if ((method.access & Opcodes.ACC_PUBLIC) == 0 || method.name.startsWith("<")) continue;
                Subscription subscription = null;
                if (method.visibleAnnotations != null) for (var annotation : method.visibleAnnotations) {
                    if (!annotation.desc.equals("Lnet/neoforged/bus/api/SubscribeEvent;")) continue;
                    EventPriority priority = EventPriority.NORMAL; boolean canceled = false;
                    if (annotation.values != null) for (int i = 0; i < annotation.values.size(); i += 2) {
                        String key = (String)annotation.values.get(i); Object value = annotation.values.get(i + 1);
                        if (key.equals("priority")) priority = EventPriority.valueOf(((String[])value)[1]);
                        else if (key.equals("receiveCanceled")) canceled = (Boolean)value;
                    }
                    subscription = new Subscription(priority, canceled);
                }
                methods.add(new Member(method.name, method.desc, method.signature, method.access, subscription));
            }
            return List.copyOf(methods);
        }
    };

    static List<Handler> prepare(Object subscriber) {
        Objects.requireNonNull(subscriber, "subscriber");
        boolean staticOnly = subscriber instanceof Class<?>;
        Class<?> root = staticOnly ? (Class<?>)subscriber : subscriber.getClass();
        var hierarchy = new ArrayList<Class<?>>(); collectTypes(root, hierarchy, new HashSet<>(), !staticOnly);
        var effective = new LinkedHashMap<String, Member>();
        var annotations = new HashMap<String, Subscription>();
        for (Class<?> type : hierarchy) for (Member member : INDEX.get(type)) {
            if (((member.access() & Opcodes.ACC_STATIC) != 0) != staticOnly) continue;
            String key = member.name() + member.descriptor();
            boolean present = effective.putIfAbsent(key, member) != null;
            if (staticOnly && present) continue; // Public static hiding, as Class.getMethods.
            if (member.annotation() != null) {
                Subscription previous = annotations.putIfAbsent(key, member.annotation());
                if (previous != null && !previous.equals(member.annotation()))
                    throw new IllegalStateException("Conflicting source event annotations: " + root.getName() + "#" + key);
            }
        }
        var handlers = new ArrayList<Handler>();
        for (var entry : effective.entrySet()) {
            Subscription annotation = annotations.get(entry.getKey()); if (annotation == null) continue;
            Member member = entry.getValue(); String symbol = root.getName() + "#" + entry.getKey();
            Type[] parameters = Type.getArgumentTypes(member.descriptor());
            if (parameters.length != 1 || parameters[0].getSort() != Type.OBJECT || !Type.getReturnType(member.descriptor()).equals(Type.VOID_TYPE))
                throw new IllegalArgumentException("Invalid source event subscriber signature: " + symbol);
            try {
                Class<? extends Event> event = Class.forName(parameters[0].getClassName(), false, root.getClassLoader()).asSubclass(Event.class);
                MethodType signature = MethodType.methodType(void.class, event);
                var lookup = MethodHandles.privateLookupIn(root, MethodHandles.lookup());
                MethodHandle handle = staticOnly ? lookup.findStatic(root, member.name(), signature)
                        : lookup.findVirtual(root, member.name(), signature).bindTo(subscriber);
                handlers.add(new Handler(symbol, event, annotation.priority(), annotation.receiveCanceled(), member.signature(),
                        handle.asType(MethodType.methodType(void.class, Event.class))));
            } catch (ReflectiveOperationException | ClassCastException failure) {
                throw new IllegalStateException("Cannot link source event subscriber: " + symbol, failure);
            }
        }
        handlers.sort(Comparator.comparing(Handler::symbol)); return List.copyOf(handlers);
    }
    private static void collectTypes(Class<?> type, List<Class<?>> output, Set<Class<?>> seen, boolean interfaces) {
        if (type == null || type == Object.class || !seen.add(type)) return;
        output.add(type); collectTypes(type.getSuperclass(), output, seen, interfaces);
        if (interfaces) for (Class<?> iface : type.getInterfaces()) collectTypes(iface, output, seen, true);
    }
}
