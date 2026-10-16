package com.fuckingdeveloper.lms.profile.forge1192.runtime.event;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192LifecycleBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.LegacyForgeGui;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.LegacyGuiOverlay;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import java.util.Objects;

/** Source-shaped Forge 1.19.2 GUI-overlay registration facade. */
public final class LegacyRegisterGuiOverlaysEvent extends Event {
    private final RegisterGuiLayersEvent target;

    public LegacyRegisterGuiOverlaysEvent(RegisterGuiLayersEvent target) {
        this.target = Objects.requireNonNull(target, "target");
    }

    public void registerBelowAll(String name, LegacyGuiOverlay overlay) {
        target.registerBelowAll(key(name), adapt(overlay));
    }

    public void registerBelow(Identifier other, String name, LegacyGuiOverlay overlay) {
        target.registerBelow(other, key(name), adapt(overlay));
    }

    public void registerAbove(Identifier other, String name, LegacyGuiOverlay overlay) {
        target.registerAbove(other, key(name), adapt(overlay));
    }

    public void registerAboveAll(String name, LegacyGuiOverlay overlay) {
        target.registerAboveAll(key(name), adapt(overlay));
    }

    private static Identifier key(String name) {
        return Identifier.fromNamespaceAndPath(
                Forge1192LifecycleBridge.requireActive().modId(), name);
    }

    private static net.neoforged.neoforge.client.gui.GuiLayer adapt(LegacyGuiOverlay overlay) {
        Objects.requireNonNull(overlay, "overlay");
        return (graphics, delta) -> overlay.render(
                new LegacyForgeGui(graphics), new PoseStack(),
                delta.getGameTimeDeltaPartialTick(false),
                graphics.guiWidth(), graphics.guiHeight());
    }
}
