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
    private static final ClassValue<java.util.Map<String,Class<?>>> SCREEN_GETTERS = new ClassValue<>() {
        @Override protected java.util.Map<String,Class<?>> computeValue(Class<?> type) {
            var result=new java.util.HashMap<String,Class<?>>();
            for(Class<?> current=type;current!=null;current=current.getSuperclass()) {
                if(!(current.getClassLoader() instanceof com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader loader))continue;
                try {
                    byte[] bytes=loader.originalClassBytes(current.getName());if(bytes==null)throw new IllegalStateException("Source screen declaration evidence missing");
                    var node=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(bytes).accept(node,org.objectweb.asm.ClassReader.SKIP_CODE|org.objectweb.asm.ClassReader.SKIP_DEBUG|org.objectweb.asm.ClassReader.SKIP_FRAMES);
                    for(var method:node.methods)if((method.access&org.objectweb.asm.Opcodes.ACC_STATIC)==0 && method.desc.equals("()Lnet/minecraft/client/renderer/entity/ItemRenderer;"))result.putIfAbsent(method.name,current);
                }catch(java.io.IOException failure){throw new IllegalStateException("Cannot inspect source screen renderer dispatch",failure);}
            }
            return java.util.Map.copyOf(result);
        }
    };
    public static LegacyTooltipItemRenderer screenContext(Object screen) {
        if(!(screen instanceof net.minecraft.client.gui.screens.Screen))throw new IllegalStateException("Invalid source screen renderer receiver");
        return Forge1192TooltipGuiBridge.scope().renderer;
    }
    public static LegacyTooltipItemRenderer screenRenderer(Object screen,Class<?> owner,String method) {
        java.util.Objects.requireNonNull(screen);
        if(!owner.isInstance(screen)||SCREEN_GETTERS.get(screen.getClass()).get(method)!=owner)
            throw new UnsupportedOperationException("Source screen renderer getter override is not proven: "+screen.getClass().getName()+"#"+method);
        return screenContext(screen);
    }
    public static void itemWithContext(LegacyTooltipItemRenderer context,LivingEntity owner,ItemStack stack,int x,int y,int seed) {
        var graphics=Forge1192TooltipGuiBridge.graphics(context);
        try{ITEM.invokeExact(graphics,owner,(Level)null,stack,x,y,seed);}
        catch(RuntimeException|Error failure){throw failure;}
        catch(Throwable failure){throw new IllegalStateException("Native GUI item extraction failed",failure);}
        var scope=Forge1192TooltipGuiBridge.scope();scope.color=-1;scope.blend=false;
        scope.texture=net.minecraft.resources.Identifier.withDefaultNamespace("textures/atlas/blocks.png");scope.textureObject=null;
    }
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
