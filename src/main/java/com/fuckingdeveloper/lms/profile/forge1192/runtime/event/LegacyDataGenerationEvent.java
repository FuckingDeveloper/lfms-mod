package com.fuckingdeveloper.lms.profile.forge1192.runtime.event;

/**
 * Source datagen listener type for ordinary client/server registration.
 * No datagen service is provided: listeners stay registered, but this event
 * cannot be instantiated/emitted by the runtime. Preflight tracks callback-only
 * datagen reachability separately; a direct runtime call remains unsupported.
 */
public final class LegacyDataGenerationEvent extends net.neoforged.bus.api.Event
        implements net.neoforged.fml.event.IModBusEvent {
    private LegacyDataGenerationEvent(){throw new UnsupportedOperationException("Source data generation is not a runtime LMS service");}
}
