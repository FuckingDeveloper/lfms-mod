package com.fuckingdeveloper.lms.runtime;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;

/**
 * Registers target-runtime integrations whose event and payload types exist
 * only on the physical client.
 *
 * <p>The common LMS mod entrypoint must remain loadable on a dedicated
 * server. Keeping client-only symbolic references in this class ensures the
 * JVM does not resolve them unless the entrypoint's physical-side check has
 * selected the client branch.</p>
 */
public final class Target263ClientRuntimeBootstrap {
    private Target263ClientRuntimeBootstrap() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterSpriteSourcesEvent.class, LegacyTextureStitchSpriteSource::register);
    }
}
