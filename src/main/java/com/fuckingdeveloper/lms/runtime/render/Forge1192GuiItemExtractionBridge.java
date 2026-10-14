package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import java.lang.invoke.*;

/** Proven null-context, seed-zero, full-bright GUI item operation. */
public final class Forge1192GuiItemExtractionBridge {
    private Forge1192GuiItemExtractionBridge() {}
    private static final MethodHandle ITEM=itemHandle();
    private static MethodHandle itemHandle(){
        try{return MethodHandles.privateLookupIn(GuiGraphicsExtractor.class,MethodHandles.lookup()).findVirtual(GuiGraphicsExtractor.class,"item",MethodType.methodType(void.class,LivingEntity.class,Level.class,ItemStack.class,int.class,int.class,int.class));}
        catch(ReflectiveOperationException failure){throw new ExceptionInInitializerError(failure);}
    }
    /** Target flat GUI pipelines have no depth/stencil state; no global GL state survives extraction. */
    public static void restoreFlatGuiDepth(){Forge1192TooltipGuiBridge.scope();}
    public static LegacyTooltipItemRenderer context(Minecraft ignored){return Forge1192TooltipGuiBridge.scope().renderer;}
    public static void item(LegacyTooltipItemRenderer context,PoseStack pose,ItemStack stack,float x,float y){
        var graphics=Forge1192TooltipGuiBridge.graphics(context);var scope=Forge1192TooltipGuiBridge.scope();
        if(scope.color!=-1)throw new UnsupportedOperationException("Colored source GUI item extraction is not proven");
        graphics.pose().pushMatrix();
        try{
            graphics.pose().mul(Forge1192ContainerGraphicsBridge.planar(pose)).translate(x,y);
            ITEM.invokeExact(graphics,(LivingEntity)null,(Level)null,stack,0,0,0);
            scope.blend=false;
        }catch(RuntimeException|Error failure){throw failure;}
        catch(Throwable failure){throw new IllegalStateException("Native GUI item extraction failed",failure);}
        finally{graphics.pose().popMatrix();}
    }
}
