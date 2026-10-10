package com.fuckingdeveloper.lms.runtime;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Consumer;

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
        bus.addListener(event -> invokeScoped(modId, listener, event));
    }

    public static <T extends Event> void addListener(IEventBus bus, net.neoforged.bus.api.EventPriority priority,
                                                     Consumer<T> listener) {
        String modId = Forge1192LifecycleBridge.requireActive().modId();
        bus.addListener(priority, event -> invokeScoped(modId, listener, event));
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
