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
        modBus.addListener(com.fuckingdeveloper.lms.runtime.render.Forge1192MenuScreenBridge::flush);
        modBus.addListener(RegisterSpriteSourcesEvent.class, LegacyTextureStitchSpriteSource::register);
        modBus.addListener(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.ItemTintSources.class,
                event -> event.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("lms", "legacy_item_layer"),
                        com.fuckingdeveloper.lms.runtime.render.Target263LegacyItemTint.CODEC));
        modBus.addListener(net.neoforged.neoforge.client.event.RegisterItemModelsEvent.class,
                event -> event.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("lms", "legacy_gui_item"),
                        com.fuckingdeveloper.lms.runtime.render.Target263LegacyGuiItemModel.Unbaked.CODEC));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                net.neoforged.neoforge.client.event.ScreenEvent.Opening.class,
                event -> Target263LegacyModListBridge.opening(event, sessions.get()));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                net.neoforged.neoforge.client.event.ScreenEvent.Init.Post.class,
                event -> {
                    if (event.getScreen() instanceof net.minecraft.client.gui.screens.TitleScreen)
                        Target263LegacyModListBridge.refreshBranding(sessions.get());
                });
    }
}
