package com.fuckingdeveloper.lms.runtime.render;
import com.mojang.blaze3d.vertex.PoseStack;
/** Full source callback, distinct from its background/label phases. */
public interface LegacyContainerRender {
    void lms$render(PoseStack pose,int mouseX,int mouseY,float partialTick);
}
