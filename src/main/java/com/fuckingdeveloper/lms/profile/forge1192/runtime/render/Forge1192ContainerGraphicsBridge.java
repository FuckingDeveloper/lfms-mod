package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2f;
import java.lang.invoke.*;
import java.util.function.Supplier;

/** Bounded source GUI phases and primitive state; no direct GPU/global state mutation. */
public final class Forge1192ContainerGraphicsBridge {
    private Forge1192ContainerGraphicsBridge() {}
    public static void background(LegacyContainerGraphics screen,GuiGraphicsExtractor graphics,int x,int y,float partial) {
        if(screen instanceof LegacyContainerRender)return;
        Forge1192TooltipGuiBridge.extractGui(graphics,Forge1192TooltipGuiBridge.Phase.BACKGROUND,()->screen.lms$background(new PoseStack(),partial,x,y));
    }
    public static void labels(LegacyContainerGraphics screen,GuiGraphicsExtractor graphics,int x,int y) {
        Forge1192TooltipGuiBridge.extractGui(graphics,Forge1192TooltipGuiBridge.Phase.LABELS,()->screen.lms$labels(new PoseStack(),x,y));
    }
    public static void render(LegacyContainerRender screen,GuiGraphicsExtractor graphics,int x,int y,float partial) {
        Forge1192TooltipGuiBridge.extractGui(graphics,Forge1192TooltipGuiBridge.Phase.RENDER,()->screen.lms$render(new PoseStack(),x,y,partial));
    }
    private static MethodHandle nativePhase(String name,MethodType type) {
        try {
            var lookup=MethodHandles.privateLookupIn(AbstractContainerScreen.class,MethodHandles.lookup());
            return lookup.findSpecial(AbstractContainerScreen.class,name,type,AbstractContainerScreen.class);
        }catch(ReflectiveOperationException error){throw new ExceptionInInitializerError(error);}
    }
    private static final MethodHandle CONTENTS=nativePhase("extractContents",MethodType.methodType(void.class,GuiGraphicsExtractor.class,int.class,int.class,float.class));
    private static final MethodHandle CARRIED=nativePhase("extractCarriedItem",MethodType.methodType(void.class,GuiGraphicsExtractor.class,int.class,int.class));
    private static final MethodHandle TOOLTIP=nativePhase("extractTooltip",MethodType.methodType(void.class,GuiGraphicsExtractor.class,int.class,int.class));
    private static final VarHandle DEFERRED=field(GuiGraphicsExtractor.class,"deferredTooltip",Runnable.class);
    private static final VarHandle FONT=field(net.minecraft.client.gui.screens.Screen.class,"font",Font.class);
    private static VarHandle field(Class<?> owner,String name,Class<?> type) {
        try{return MethodHandles.privateLookupIn(owner,MethodHandles.lookup()).findVarHandle(owner,name,type);}
        catch(ReflectiveOperationException error){throw new ExceptionInInitializerError(error);}
    }
    private static void nativePose(PoseStack pose) {
        if(!pose.last().pose().equals(new org.joml.Matrix4f()))
            throw new UnsupportedOperationException("Transformed source container base/tooltip pose is not adapted");
    }
    private enum TooltipOperation {
        SLOT("m_7025_","renderTooltip",MethodType.methodType(void.class,PoseStack.class,int.class,int.class)),
        COMPONENT("m_96597_","renderComponentTooltip",MethodType.methodType(void.class,PoseStack.class,java.util.List.class,int.class,int.class)),
        FORMATTED("m_96617_","renderTooltip",MethodType.methodType(void.class,PoseStack.class,java.util.List.class,int.class,int.class));
        final String srg,named;final MethodType type;
        TooltipOperation(String srg,String named,MethodType type){this.srg=srg;this.named=named;this.type=type;}
    }
    private static final ClassValue<java.util.Map<TooltipOperation,MethodHandle>> SOURCE_TOOLTIPS=new ClassValue<>() {
        @Override protected java.util.Map<TooltipOperation,MethodHandle> computeValue(Class<?> type) {
            var found=new java.util.EnumMap<TooltipOperation,MethodHandle>(TooltipOperation.class);
            try {
                var lookup=MethodHandles.privateLookupIn(type,MethodHandles.lookup());
                for(var operation:TooltipOperation.values())for(String name:java.util.List.of(operation.srg,operation.named)) {
                    MethodHandle callback;
                    try{callback=lookup.findVirtual(type,name,operation.type);}catch(NoSuchMethodException absent){continue;}
                    if(found.putIfAbsent(operation,callback)!=null)throw new IllegalStateException("Ambiguous source tooltip declarations: "+type.getName());
                }
                return java.util.Map.copyOf(found);
            }catch(IllegalAccessException failure){throw new IllegalStateException("Cannot access source tooltip overrides: "+type.getName(),failure);}
        }
    };
    private static boolean sourceTooltip(net.minecraft.client.gui.screens.Screen screen,TooltipOperation operation,Object... arguments) {
        var callback=SOURCE_TOOLTIPS.get(screen.getClass()).get(operation);if(callback==null)return false;
        var values=new java.util.ArrayList<Object>();values.add(screen);java.util.Collections.addAll(values,arguments);
        try{callback.invokeWithArguments(values);return true;}
        catch(RuntimeException|Error failure){throw failure;}catch(Throwable failure){throw new IllegalStateException("Source tooltip override failed",failure);}
    }
    public static void tooltip(AbstractContainerScreen<?> screen,PoseStack pose,int x,int y) {
        if(!sourceTooltip(screen,TooltipOperation.SLOT,pose,x,y))defaultTooltip(screen,pose,x,y);
    }
    public static void componentTooltip(net.minecraft.client.gui.screens.Screen screen,PoseStack pose,java.util.List<Component> lines,int x,int y) {
        if(!sourceTooltip(screen,TooltipOperation.COMPONENT,pose,lines,x,y))defaultComponentTooltip(screen,pose,lines,x,y);
    }
    public static void formattedTooltip(net.minecraft.client.gui.screens.Screen screen,PoseStack pose,java.util.List<? extends FormattedCharSequence> lines,int x,int y) {
        if(!sourceTooltip(screen,TooltipOperation.FORMATTED,pose,lines,x,y))defaultFormattedTooltip(screen,pose,lines,x,y);
    }
    public static void defaultRender(AbstractContainerScreen<?> screen,PoseStack pose,int x,int y,float partial) {
        nativePose(pose);
        var graphics=Forge1192TooltipGuiBridge.scope().graphics;
        if(!(screen instanceof LegacyContainerGraphics source))throw new IllegalStateException("Source full render lacks panel phases");
        Forge1192TooltipGuiBridge.extractGui(graphics,Forge1192TooltipGuiBridge.Phase.BACKGROUND,()->source.lms$background(pose,partial,x,y));
        try {
            CONTENTS.invoke(screen,graphics,x,y,partial);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.client.event.ScreenEvent.Render.Foreground(screen,graphics,x,y,partial));
            CARRIED.invoke(screen,graphics,x,y);
        }catch(RuntimeException|Error failure){throw failure;}catch(Throwable failure){throw new IllegalStateException("Source base container render failed",failure);}
    }
    public static void defaultTooltip(AbstractContainerScreen<?> screen,PoseStack pose,int x,int y) {
        nativePose(pose);
        var graphics=Forge1192TooltipGuiBridge.scope().graphics;
        Forge1192TooltipGuiBridge.nativeTooltip(()->{
            var previous=(Runnable)DEFERRED.get(graphics);DEFERRED.set(graphics,null);
            try {
                TOOLTIP.invoke(screen,graphics,x,y);
                var produced=(Runnable)DEFERRED.get(graphics);DEFERRED.set(graphics,null);
                if(produced!=null)produced.run();
            }catch(RuntimeException|Error failure){throw failure;}catch(Throwable failure){throw new IllegalStateException("Source base container tooltip failed",failure);}
            finally{DEFERRED.set(graphics,previous);}
        });
    }
    public static void defaultComponentTooltip(net.minecraft.client.gui.screens.Screen screen,PoseStack pose,java.util.List<Component> lines,int x,int y) {
        nativePose(pose);
        var graphics=Forge1192TooltipGuiBridge.scope().graphics;var font=(Font)FONT.get(screen);
        Forge1192TooltipGuiBridge.nativeTooltip(()->{
            var components=net.neoforged.neoforge.client.ClientHooks.gatherTooltipComponents(net.minecraft.world.item.ItemStack.EMPTY,lines,java.util.Optional.empty(),x,graphics.guiWidth(),graphics.guiHeight(),font);
            if(!components.isEmpty())graphics.tooltip(font,components,x,y,net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE,null,true);
        });
    }
    public static void defaultFormattedTooltip(net.minecraft.client.gui.screens.Screen screen,PoseStack pose,java.util.List<? extends FormattedCharSequence> lines,int x,int y) {
        nativePose(pose);
        var graphics=Forge1192TooltipGuiBridge.scope().graphics;var font=(Font)FONT.get(screen);
        Forge1192TooltipGuiBridge.nativeTooltip(()->{
            var components=lines.stream().map(net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent::create).toList();
            if(!components.isEmpty())graphics.tooltip(font,components,x,y,net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE,null,true);
        });
    }
    private static final MethodHandle BASE_LABELS=baseLabelsHandle();
    private static MethodHandle baseLabelsHandle() {
        try {
            var lookup=MethodHandles.privateLookupIn(AbstractContainerScreen.class,MethodHandles.lookup());
            return lookup.findSpecial(AbstractContainerScreen.class,"extractLabels",MethodType.methodType(void.class,GuiGraphicsExtractor.class,int.class,int.class),AbstractContainerScreen.class);
        }catch(ReflectiveOperationException error){throw new ExceptionInInitializerError(error);}
    }
    public static void defaultLabels(AbstractContainerScreen<?> screen,PoseStack pose,int x,int y) {
        try {BASE_LABELS.invoke(screen,Forge1192TooltipGuiBridge.scope().graphics,x,y);}
        catch(RuntimeException|Error failure){throw failure;}catch(Throwable failure){throw new IllegalStateException("Native default labels failed",failure);}
    }
    /** Native default background already ran before the source background callback. */
    public static void defaultBackground(PoseStack pose) {
        var scope=Forge1192TooltipGuiBridge.scope();
        if(scope.phase!=Forge1192TooltipGuiBridge.Phase.BACKGROUND && scope.phase!=Forge1192TooltipGuiBridge.Phase.RENDER)throw new IllegalStateException("Default source background outside native background extraction");
        // Source Screen background/GuiComponent gradient ends with blend disabled and texture enabled.
        scope.blend=false;scope.textureEnabled=true;
    }
    public static LegacyGuiShader positionColorShader(){return LegacyGuiShader.POSITION_COLOR;}
    public static LegacyGuiShader positionTexShader(){return LegacyGuiShader.POSITION_TEX;}
    public static LegacyGuiShader positionTexColorShader(){return LegacyGuiShader.POSITION_TEX_COLOR;}
    public static void setShader(Supplier<LegacyGuiShader> supplier) {if(LegacyWorldRenderScope.active())LegacyWorldRenderScope.scope().shader=java.util.Objects.requireNonNull(supplier.get());else Forge1192TooltipGuiBridge.scope().shader=java.util.Objects.requireNonNull(supplier.get());}
    // Native GUI item atlas selects ITEMS_3D/ITEMS_FLAT from each model's usesBlockLight.
    // Flat source GUI quads have no lighting input; retain intent locally, never mutate GPU state.
    public static void setupFor3DItems(){Forge1192TooltipGuiBridge.scope().flatItemLighting=false;}
    public static void setupForFlatItems(){Forge1192TooltipGuiBridge.scope().flatItemLighting=true;}
    public static void enableDepthTest(){if(LegacyWorldRenderScope.active())LegacyWorldRenderScope.scope().depth=true;else Forge1192TooltipGuiBridge.scope().depthTest=true;}
    public static void disableDepthTest(){if(LegacyWorldRenderScope.active())LegacyWorldRenderScope.scope().depth=false;else Forge1192TooltipGuiBridge.scope().depthTest=false;}
    public static void colorMask(boolean red,boolean green,boolean blue,boolean alpha) {
        int mask=(red?1:0)|(green?2:0)|(blue?4:0)|(alpha?8:0);
        if(LegacyWorldRenderScope.active())LegacyWorldRenderScope.scope().writeMask=mask;else Forge1192TooltipGuiBridge.scope().colorWriteMask=mask;
    }
    public static void enableCull(){LegacyWorldRenderScope.scope().cull=true;}
    public static void disableCull(){LegacyWorldRenderScope.scope().cull=false;}
    public static void depthMask(boolean write){LegacyWorldRenderScope.scope().depthWrite=write;}
    public static void enableBlend(){if(LegacyWorldRenderScope.active())LegacyWorldRenderScope.scope().blend=true;else Forge1192TooltipGuiBridge.scope().blend=true;}
    public static void disableBlend(){if(LegacyWorldRenderScope.active())LegacyWorldRenderScope.scope().blend=false;else Forge1192TooltipGuiBridge.scope().blend=false;}
    public static void enableTexture(){if(LegacyWorldRenderScope.active())LegacyWorldRenderScope.scope().texture=true;else Forge1192TooltipGuiBridge.scope().textureEnabled=true;}
    public static void disableTexture(){if(LegacyWorldRenderScope.active())LegacyWorldRenderScope.scope().texture=false;else Forge1192TooltipGuiBridge.scope().textureEnabled=false;}
    public static void defaultBlendFunc(){if(LegacyWorldRenderScope.active())LegacyWorldRenderScope.scope().blendFunction=LegacyGuiBlend.DEFAULT;else Forge1192TooltipGuiBridge.scope().blendFunction=LegacyGuiBlend.DEFAULT;}

    static Matrix3x2f planar(PoseStack poses) {
        return Forge1192GuiPlaneBridge.planar(poses.last().pose());
    }
    private static int text(Font font,PoseStack pose,Object text,float x,float y,int color,boolean shadow) {
        if(font.getClass().getClassLoader() instanceof com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader)
            throw new UnsupportedOperationException("Source custom font draw callbacks are not adapted");
        color=sourceTextColor(color);
        var state=Forge1192TooltipGuiBridge.scope();
        if(state.colorWriteMask!=com.mojang.renderpearl.api.pipeline.ColorTargetState.WRITE_ALL)
            throw new UnsupportedOperationException("Masked source GUI text is not adapted");
        var graphics=state.graphics;
        var transform=planar(pose);
        Forge1192GuiPlaneBridge.submit(pose.last().pose().m32());
        graphics.pose().pushMatrix();
        try {
            graphics.pose().mul(transform);graphics.pose().translate(x,y);
            int width;
            if(text instanceof Component component){graphics.text(font,component,0,0,color,shadow);width=font.width(component);}
            else if(text instanceof FormattedCharSequence sequence){graphics.text(font,sequence,0,0,color,shadow);width=font.width(sequence);}
            else {var string=(String)text;graphics.text(font,string,0,0,color,shadow);width=font.width(string);}
            return (int)(x+width)+(shadow?1:0);
        }finally{graphics.pose().popMatrix();}
    }
    public static int sourceTextColor(int color){return (color & 0xFC000000)==0?color|0xFF000000:color;}
    public static int draw(Font font,PoseStack pose,Component text,float x,float y,int color){return text(font,pose,text,x,y,color,false);}
    public static int draw(Font font,PoseStack pose,String text,float x,float y,int color){return text==null?0:text(font,pose,text,x,y,color,false);}
    public static int draw(Font font,PoseStack pose,FormattedCharSequence text,float x,float y,int color){return text(font,pose,text,x,y,color,false);}
    public static int drawShadow(Font font,PoseStack pose,Component text,float x,float y,int color){return text(font,pose,text,x,y,color,true);}
    public static int drawShadow(Font font,PoseStack pose,String text,float x,float y,int color){return text==null?0:text(font,pose,text,x,y,color,true);}
    public static int drawShadow(Font font,PoseStack pose,FormattedCharSequence text,float x,float y,int color){return text(font,pose,text,x,y,color,true);}
}
