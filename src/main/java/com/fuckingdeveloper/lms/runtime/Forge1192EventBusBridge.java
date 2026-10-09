package com.fuckingdeveloper.lms.runtime;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.ICancellableEvent;

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
    public static boolean post(IEventBus bus, Event event) {
        Event posted = bus.post(event);
        return posted instanceof ICancellableEvent cancellable && cancellable.isCanceled();
    }
}
