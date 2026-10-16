package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;

/** Source-shaped Forge 1.19.2 immediate GUI overlay callback. */
@FunctionalInterface
public interface LegacyGuiOverlay {
    void render(LegacyForgeGui gui, PoseStack poseStack, float partialTick, int width, int height);
}
