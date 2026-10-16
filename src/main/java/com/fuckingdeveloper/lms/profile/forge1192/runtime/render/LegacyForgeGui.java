package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Objects;

/** Target extraction context carried alongside a legacy Forge GUI callback. */
public final class LegacyForgeGui {
    private final GuiGraphicsExtractor graphics;

    public LegacyForgeGui(GuiGraphicsExtractor graphics) {
        this.graphics = Objects.requireNonNull(graphics, "graphics");
    }

    public GuiGraphicsExtractor graphics() {
        return graphics;
    }
}
