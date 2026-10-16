package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.*;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/** Source Checkbox ABI on genuine native button geometry and input lifecycle. */
public class Forge1192Checkbox extends AbstractButton {
    private boolean selected;
    private final boolean showLabel;
    public Forge1192Checkbox(int x,int y,int width,int height,Component message,boolean selected){this(x,y,width,height,message,selected,true);}
    public Forge1192Checkbox(int x,int y,int width,int height,Component message,boolean selected,boolean showLabel){super(x,y,width,height,message);this.selected=selected;this.showLabel=showLabel;}
    @Override public void onPress(InputWithModifiers input){lms$onPress();}
    public void lms$onPress(){selected=!selected;}
    public boolean selected(){return selected;}
    @Override public void updateWidgetNarration(NarrationElementOutput output){
        output.add(NarratedElementType.TITLE,createNarrationMessage());
        if(active)output.add(NarratedElementType.USAGE,Component.translatable("narration.checkbox.usage."+(isFocused()?"focused":"hovered")+(selected()?".uncheck":".check")));
    }
    @Override public void extractContents(GuiGraphicsExtractor graphics,int x,int y,float partial){Forge1192TooltipGuiBridge.extractGui(graphics,Forge1192TooltipGuiBridge.Phase.WIDGET,()->m_6303_(new PoseStack(),x,y,partial));}
    private void baseContents(GuiGraphicsExtractor graphics){
        String sprite="widget/checkbox"+(selected()?"_selected":"")+(isFocused()?"_highlighted":"");
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED,Identifier.withDefaultNamespace(sprite),getX(),getY(),20,getHeight(),ARGB.white(alpha));
        if(showLabel)graphics.text(Minecraft.getInstance().font,getMessage(),getX()+24,getY()+(getHeight()-8)/2,0xE0E0E0|((int)Math.ceil(alpha*255)<<24),true);
    }
    /** Original source renderButton superclass ABI: explicit base drawing, never virtual extractor recursion. */
    public void m_6303_(PoseStack pose,int x,int y,float partial){
        var graphics=Forge1192TooltipGuiBridge.scope().graphics;graphics.pose().pushMatrix();
        try{graphics.pose().mul(Forge1192GuiPlaneBridge.planar(pose.last().pose()));Forge1192GuiPlaneBridge.submit(Forge1192GuiPlaneBridge.depth(pose.last().pose(),0));baseContents(graphics);}finally{graphics.pose().popMatrix();}
        m_7906_(pose,Minecraft.getInstance(),x,y);
    }
    public void m_7906_(PoseStack pose,Minecraft client,int x,int y){} // Verified source empty renderBg extension hook.
}
