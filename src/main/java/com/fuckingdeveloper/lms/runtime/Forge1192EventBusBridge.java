package com.fuckingdeveloper.lms.runtime;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Consumer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Semantic adapters for Forge 1.19.2 event-bus contracts that changed in NeoForge.
 */
public final class Forge1192EventBusBridge {
    private Forge1192EventBusBridge() {}

    /**
     * Forge 1.19.2 IEventBus#post(Event) returned whether the event was cancelled.
     * Current NeoForge returns the posted event. Posting still occurs on the real
     * target bus; only the legacy return contract is reconstructed.
     */
    public static <T extends Event> void addListener(IEventBus bus, Consumer<T> listener) {
        String modId = Forge1192LifecycleBridge.requireActive().modId();
        bus.addListener((T event) -> invokeScoped(modId, listener, event));
    }

    public static <T extends Event> void addListener(IEventBus bus, net.neoforged.bus.api.EventPriority priority,
                                                     Consumer<T> listener) {
        String modId = Forge1192LifecycleBridge.requireActive().modId();
        bus.addListener(priority, (T event) -> invokeScoped(modId, listener, event));
    }

    /**
     * Forge 1.19.2 register(Object) discovers @SubscribeEvent methods. Register
     * each handler explicitly so LMS can wrap every callback in the owning
     * legacy mod/registration scope rather than losing provenance on the
     * shared NeoForge mod bus.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register(IEventBus bus, Object subscriber) {
        String modId = Forge1192LifecycleBridge.requireActive().modId();
        if (subscriber == null) throw new NullPointerException("subscriber");

        List<Method> handlers = new ArrayList<>();
        for (Class<?> type = subscriber.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                if (method.getAnnotation(net.neoforged.bus.api.SubscribeEvent.class) == null) continue;
                if (method.getParameterCount() != 1 || !Event.class.isAssignableFrom(method.getParameterTypes()[0]))
                    throw new IllegalArgumentException("Invalid legacy event subscriber method: " + method);
                handlers.add(method);
            }
        }
        handlers.sort(Comparator.comparing(Method::toGenericString));
        if (handlers.isEmpty())
            throw new IllegalArgumentException("Legacy event subscriber has no @SubscribeEvent methods: "
                    + subscriber.getClass().getName());

        for (Method method : handlers) {
            var annotation = method.getAnnotation(net.neoforged.bus.api.SubscribeEvent.class);
            Class<? extends Event> eventType = method.getParameterTypes()[0].asSubclass(Event.class);
            method.setAccessible(true);
            Consumer listener = event -> invokeScoped(modId, ignored -> invokeSubscriber(subscriber, method, ignored), (Event) event);
            bus.addListener(annotation.priority(), false, eventType, listener);
        }
    }

    private static void invokeSubscriber(Object subscriber, Method method, Event event) {
        try {
            method.invoke(subscriber, event);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) throw runtime;
            if (cause instanceof Error error) throw error;
            throw new IllegalStateException("Legacy event subscriber failed: " + method, cause);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot invoke legacy event subscriber: " + method, e);
        }
    }

    private static <T extends Event> void invokeScoped(String modId, Consumer<T> listener, T event) {
        try (var mod = Forge1192RegistrationContext.enterMod(modId)) {
            if (event instanceof RegisterEvent registration) {
                try (var registry = Forge1192RegistrationContext.enterRegistration(registration)) {
                    listener.accept(event);
                } catch (RuntimeException | Error e) {
                    throw e;
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to close legacy registration scope", e);
                }
            } else {
                listener.accept(event);
            }
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close legacy mod scope", e);
        }
    }

    public static boolean post(IEventBus bus, Event event) {
        Event posted = bus.post(event);
        return posted instanceof ICancellableEvent cancellable && cancellable.isCanceled();
    }
}
