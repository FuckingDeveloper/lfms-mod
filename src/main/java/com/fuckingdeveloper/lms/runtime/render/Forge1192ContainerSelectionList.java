package com.fuckingdeveloper.lms.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.invoke.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.input.*;

/** Source viewport and callbacks on the genuine target list/entry ownership model. */
public abstract class Forge1192ContainerSelectionList<E extends Forge1192ContainerSelectionList.Entry<E>> extends ContainerObjectSelectionList<E> {
    public int lms$width,lms$height,lms$y0,lms$y1,lms$x0,lms$x1,lms$headerHeight;
    public final int lms$itemHeight;
    private boolean background=true,separators=true,selection=true,header,scrolling;
    private GuiGraphicsExtractor extracting;
    private static final VarHandle CHILDREN=childrenField();
    private static VarHandle childrenField(){try{return MethodHandles.privateLookupIn(AbstractSelectionList.class,MethodHandles.lookup()).findVarHandle(AbstractSelectionList.class,"children",List.class);}catch(ReflectiveOperationException failure){throw new ExceptionInInitializerError(failure);}}
    public Forge1192ContainerSelectionList(Minecraft minecraft,int width,int screenHeight,int top,int bottom,int itemHeight){
        super(minecraft,width,bottom-top,top,itemHeight);
        lms$width=width;lms$height=screenHeight;lms$y0=top;lms$y1=bottom;lms$x1=width;lms$itemHeight=itemHeight;
    }
    private void sync(){super.setX(lms$x0);super.setY(lms$y0);super.setWidth(lms$width);super.setHeight(lms$y1-lms$y0);}
    @SuppressWarnings("unchecked") public List<E> lms$children(){return (List<E>)CHILDREN.get(this);}
    public int lms$getWidth(){return lms$width;} public int lms$getHeight(){return lms$height;}
    public int lms$getTop(){return lms$y0;} public int lms$getBottom(){return lms$y1;}
    public int lms$getLeft(){return lms$x0;} public int lms$getRight(){return lms$x1;}
    public void lms$updateSize(int width,int height,int top,int bottom){lms$width=width;lms$height=height;lms$y0=top;lms$y1=bottom;lms$x0=0;lms$x1=width;sync();}
    public void lms$setLeftPos(int left){lms$x0=left;lms$x1=left+lms$width;sync();}
    public AbstractSelectionList.Entry<?> lms$getEntry(int index){return lms$children().get(index);}
    public int lms$getMaxPosition(){return getItemCount()*lms$itemHeight+lms$headerHeight;}
    @Override protected int contentHeight(){return lms$getMaxPosition()+4;}
    public double lms$getScrollAmount(){return scrollAmount();}
    public void lms$setScrollAmount(double value){sync();super.setScrollAmount(value);}
    public int lms$getMaxScroll(){return Math.max(0,lms$getMaxPosition()-(lms$y1-lms$y0-4));}
    public int lms$getScrollBottom(){return (int)scrollAmount()-lms$height-lms$headerHeight;}
    public void lms$scroll(int delta){lms$setScrollAmount(scrollAmount()+delta);}
    public int lms$getScrollbarPosition(){return lms$width/2+124;}
    @Override protected int scrollBarX(){return lms$getScrollbarPosition();}
    @Override public int maxScrollAmount(){return lms$getMaxScroll();}
    @Override public int getRowLeft(){return lms$x0+lms$width/2-getRowWidth()/2+2;}
    public int lms$getRowTop(int index){return lms$y0+4-(int)scrollAmount()+index*lms$itemHeight+lms$headerHeight;}
    public int lms$getRowBottom(int index){return lms$getRowTop(index)+lms$itemHeight;}
    public AbstractSelectionList.Entry<?> lms$getEntryAtPosition(double x,double y){
        int left=lms$x0+lms$width/2-getRowWidth()/2;
        int offset=(int)Math.floor(y-lms$y0)-lms$headerHeight+(int)scrollAmount()-4,index=offset/lms$itemHeight;
        return x<lms$getScrollbarPosition()&&x>=left&&x<=left+getRowWidth()&&index>=0&&offset>=0&&index<getItemCount()?lms$getEntry(index):null;
    }
    /** Source AbstractSelectionList focus extension hook; distinct from target widget focus. */
    public boolean lms$isFocused(){return false;}
    public boolean lms$isSelectedItem(int index){return false;}
    public void lms$setRenderSelection(boolean value){selection=value;}
    public void lms$setRenderBackground(boolean value){background=value;}
    public void lms$setRenderTopAndBottom(boolean value){separators=value;}
    public void lms$setRenderHeader(boolean value,int height){header=value;lms$headerHeight=value?height:0;}
    @Override protected void extractListBackground(GuiGraphicsExtractor graphics){if(background)super.extractListBackground(graphics);}
    @Override protected void extractListSeparators(GuiGraphicsExtractor graphics){if(separators)super.extractListSeparators(graphics);}
    @Override public void extractWidgetRenderState(GuiGraphicsExtractor graphics,int x,int y,float partial){
        sync();var previous=extracting;extracting=graphics;
        try{Forge1192TooltipGuiBridge.extractGui(graphics,Forge1192TooltipGuiBridge.Phase.WIDGET,()->lms$render(new PoseStack(),x,y,partial));}finally{extracting=previous;}
    }
    public void lms$render(PoseStack pose,int x,int y,float partial){
        if(extracting==null)throw new IllegalStateException("Source selection list rendered outside native extraction");
        if(header)throw new UnsupportedOperationException("Source selection-list header rendering is not adapted");
        lms$renderBackground(pose);super.extractWidgetRenderState(extracting,x,y,partial);lms$renderDecorations(pose,x,y);
    }
    @Override protected void extractListItems(GuiGraphicsExtractor graphics,int x,int y,float partial){lms$renderList(new PoseStack(),x,y,partial);}
    public void lms$renderList(PoseStack pose,int x,int y,float partial){
        for(int i=0;i<getItemCount();i++){int top=lms$getRowTop(i);if(top+lms$itemHeight>=lms$y0&&top<=lms$y1)lms$renderItem(pose,x,y,partial,i,getRowLeft(),top,getRowWidth(),lms$itemHeight-4);}
    }
    public void lms$renderItem(PoseStack pose,int x,int y,float partial,int index,int left,int top,int width,int height){
        var entry=lms$children().get(index);boolean hover=lms$getEntryAtPosition(x,y)==entry;
        if(selection&&lms$isSelectedItem(index))lms$renderSelection(pose,top,width,height,0xff808080,0xff000000);
        entry.lms$render(pose,index,top,left,width,height,x,y,hover,partial);
    }
    public void lms$renderSelection(PoseStack pose,int top,int width,int height,int outer,int inner){
        int left=lms$x0+(lms$width-width)/2;
        Forge1192TooltipGuiBridge.fill(pose,left,top-2,left+width,top+height+2,outer);
        Forge1192TooltipGuiBridge.fill(pose,left+1,top-1,left+width-1,top+height+1,inner);
    }
    /** Source defaults are empty extension hooks. */
    public void lms$renderDecorations(PoseStack pose,int x,int y){}
    public void lms$renderBackground(PoseStack pose){}
    public void lms$clickedHeader(int x,int y){}
    public void lms$updateScrollingState(double x,double y,int button){scrolling=button==0&&x>=lms$getScrollbarPosition()&&x<lms$getScrollbarPosition()+6;}
    private static MouseButtonEvent event(double x,double y,int button){return new MouseButtonEvent(x,y,new MouseButtonInfo(Forge1192KeyboardBridge.mouseCode(button),0));}
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean twice){sync();return lms$mouseClicked(event.x(),event.y(),Forge1192KeyboardBridge.sourceValue(com.mojang.blaze3d.platform.InputConstants.Type.MOUSE.getOrCreate(event.button())));}
    public boolean lms$mouseClicked(double x,double y,int button){
        lms$updateScrollingState(x,y,button);if(!isMouseOver(x,y))return false;
        @SuppressWarnings("unchecked") E entry=(E)lms$getEntryAtPosition(x,y);
        if(entry!=null){if(entry.lms$mouseClicked(x,y,button)){setFocused(entry);setDragging(true);return true;}}
        else if(button==0){lms$clickedHeader((int)(x-getRowLeft()),(int)(y-lms$y0)+ (int)scrollAmount()-4);return true;}
        return scrolling;
    }
    @Override public boolean mouseReleased(MouseButtonEvent event){return lms$mouseReleased(event.x(),event.y(),Forge1192KeyboardBridge.sourceValue(com.mojang.blaze3d.platform.InputConstants.Type.MOUSE.getOrCreate(event.button())));}
    public boolean lms$mouseReleased(double x,double y,int button){var focused=getFocused();if(focused!=null)focused.lms$mouseReleased(x,y,button);return false;}
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){return lms$mouseDragged(event.x(),event.y(),Forge1192KeyboardBridge.sourceValue(com.mojang.blaze3d.platform.InputConstants.Type.MOUSE.getOrCreate(event.button())),dx,dy);}
    public boolean lms$mouseDragged(double x,double y,int button,double dx,double dy){
        var focused=getFocused();if(focused!=null&&isDragging()&&button==0&&focused.lms$mouseDragged(x,y,button,dx,dy))return true;
        if(!scrolling||button!=0)return false;
        if(y<lms$y0)lms$setScrollAmount(0);else if(y>lms$y1)lms$setScrollAmount(lms$getMaxScroll());else {
            int viewport=lms$y1-lms$y0,thumb=net.minecraft.util.Mth.clamp((int)((float)(viewport*viewport)/lms$getMaxPosition()),32,viewport-8);
            lms$setScrollAmount(scrollAmount()+dy*Math.max(1.0,Math.max(1,lms$getMaxScroll())/(double)(viewport-thumb)));
        }return true;
    }
    @Override public boolean mouseScrolled(double x,double y,double dx,double dy){return lms$mouseScrolled(x,y,Forge1192GuiScrollBridge.verticalAmount(dx,dy));}
    public boolean lms$mouseScrolled(double x,double y,double amount){lms$setScrollAmount(scrollAmount()-amount*lms$itemHeight/2.0);return true;}
    public boolean lms$keyPressed(int key,int scan,int modifiers){throw new UnsupportedOperationException("Source selection-list keyboard navigation is not adapted");}
    public abstract static class Entry<E extends Entry<E>> extends ContainerObjectSelectionList.Entry<E>{
        public abstract void lms$render(PoseStack pose,int index,int top,int left,int width,int height,int mouseX,int mouseY,boolean hovered,float partial);
        @Override public void extractContent(GuiGraphicsExtractor graphics,int x,int y,boolean hovered,float partial){
            int index=list.children().indexOf(this);
            Forge1192TooltipGuiBridge.extractGui(graphics,Forge1192TooltipGuiBridge.Phase.WIDGET,()->lms$render(new PoseStack(),index,getY(),getX(),getWidth(),getHeight(),x,y,hovered,partial));
        }
        @Override public boolean mouseClicked(MouseButtonEvent event,boolean twice){return lms$mouseClicked(event.x(),event.y(),Forge1192KeyboardBridge.sourceValue(com.mojang.blaze3d.platform.InputConstants.Type.MOUSE.getOrCreate(event.button())));}
        public boolean lms$mouseClicked(double x,double y,int button){return super.mouseClicked(event(x,y,button),false);}
        @Override public boolean mouseReleased(MouseButtonEvent event){return lms$mouseReleased(event.x(),event.y(),Forge1192KeyboardBridge.sourceValue(com.mojang.blaze3d.platform.InputConstants.Type.MOUSE.getOrCreate(event.button())));}
        public boolean lms$mouseReleased(double x,double y,int button){return super.mouseReleased(event(x,y,button));}
        @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){return lms$mouseDragged(event.x(),event.y(),Forge1192KeyboardBridge.sourceValue(com.mojang.blaze3d.platform.InputConstants.Type.MOUSE.getOrCreate(event.button())),dx,dy);}
        public boolean lms$mouseDragged(double x,double y,int button,double dx,double dy){return super.mouseDragged(event(x,y,button),dx,dy);}
        @Override public boolean mouseScrolled(double x,double y,double dx,double dy){return lms$mouseScrolled(x,y,Forge1192GuiScrollBridge.verticalAmount(dx,dy));}
        public boolean lms$mouseScrolled(double x,double y,double amount){return super.mouseScrolled(x,y,0,amount);}
    }
}
