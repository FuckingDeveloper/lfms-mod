package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;
import com.mojang.blaze3d.vertex.PoseStack;
public interface LegacyContainerGraphics {
    void lms$background(PoseStack pose,float partialTick,int mouseX,int mouseY);
    void lms$labels(PoseStack pose,int mouseX,int mouseY);
}
