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

    public static void register(IEventBus modBus,
            java.util.function.Supplier<java.util.List<Forge1192RuntimeSession>> sessions) {
        modBus.addListener(RegisterSpriteSourcesEvent.class, LegacyTextureStitchSpriteSource::register);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                net.neoforged.neoforge.client.event.ScreenEvent.Opening.class,
                event -> Target263LegacyModListBridge.opening(event, sessions.get()));
    }
}
