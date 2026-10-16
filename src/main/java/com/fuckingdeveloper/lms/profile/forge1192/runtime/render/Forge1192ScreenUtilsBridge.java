package com.fuckingdeveloper.lms.profile.forge1192.runtime.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import com.fuckingdeveloper.lms.target.neoforge263.Target263LegacyWidgetAssets;
import java.util.*;

/** Source nine-slice tiling with clipped remainder tiles, recorded through existing GUI quads. */
public final class Forge1192ScreenUtilsBridge {
    private Forge1192ScreenUtilsBridge() {}
    public record Tile(int x,int y,int u,int v,int width,int height) {}
    public static List<Tile> tiles(int x,int y,int u,int v,int width,int height,int textureWidth,int textureHeight,int top,int bottom,int left,int right) {
        int sw=textureWidth-left-right,sh=textureHeight-top-bottom,dw=width-left-right,dh=height-top-bottom;
        if(sw<=0||sh<=0||dw<0||dh<0)throw new UnsupportedOperationException("Unproven source border dimensions");
        var tiles=new ArrayList<Tile>();
        tiles.add(new Tile(x,y,u,v,left,top));tiles.add(new Tile(x+left+dw,y,u+left+sw,v,right,top));
        tiles.add(new Tile(x,y+top+dh,u,v+top+sh,left,bottom));tiles.add(new Tile(x+left+dw,y+top+dh,u+left+sw,v+top+sh,right,bottom));
        for(int dx=0;dx<dw;dx+=sw){int w=Math.min(sw,dw-dx);tiles.add(new Tile(x+left+dx,y,u+left,v,w,top));tiles.add(new Tile(x+left+dx,y+top+dh,u+left,v+top+sh,w,bottom));for(int dy=0;dy<dh;dy+=sh)tiles.add(new Tile(x+left+dx,y+top+dy,u+left,v+top,w,Math.min(sh,dh-dy)));}
        for(int dy=0;dy<dh;dy+=sh){int h=Math.min(sh,dh-dy);tiles.add(new Tile(x,y+top+dy,u,v+top,left,h));tiles.add(new Tile(x+left+dw,y+top+dy,u+left+sw,v+top,right,h));}
        return List.copyOf(tiles);
    }
    public static void blitWithBorder(PoseStack pose,Identifier texture,int x,int y,int u,int v,int width,int height,int tw,int th,int border,float z){blitWithBorder(pose,texture,x,y,u,v,width,height,tw,th,border,border,border,border,z);}
    public static void blitWithBorder(PoseStack pose,int x,int y,int u,int v,int width,int height,int tw,int th,int border,float z){blitWithBorder(pose,x,y,u,v,width,height,tw,th,border,border,border,border,z);}
    public static void blitWithBorder(PoseStack pose,Identifier texture,int x,int y,int u,int v,int width,int height,int tw,int th,int top,int bottom,int left,int right,float z){
        if(texture.equals(Target263LegacyWidgetAssets.WIDGETS))Target263LegacyWidgetAssets.requireAvailable();
        var scope=Forge1192TooltipGuiBridge.scope();scope.shader=LegacyGuiShader.POSITION_TEX;Forge1192TooltipGuiBridge.shaderTexture(0,texture);
        blitWithBorder(pose,x,y,u,v,width,height,tw,th,top,bottom,left,right,z);
    }
    public static void blitWithBorder(PoseStack pose,int x,int y,int u,int v,int width,int height,int tw,int th,int top,int bottom,int left,int right,float z){
        var scope=Forge1192TooltipGuiBridge.scope();scope.color=-1;scope.blend=true;
        for(var tile:tiles(x,y,u,v,width,height,tw,th,top,bottom,left,right))drawTexturedModalRect(pose,tile.x,tile.y,tile.u,tile.v,tile.width,tile.height,z);
    }
    public static void drawTexturedModalRect(PoseStack pose,int x,int y,int u,int v,int width,int height,float z) {
        if(width==0||height==0)return;
        var buffer=new LegacyGuiBuffer();buffer.begin(LegacyVertexFormatMode.QUADS,LegacyDefaultVertexFormat.f_85817_);
        var matrix=pose.last().pose();
        buffer.vertex(matrix,x,y+height,z).setUv(u/256F,(v+height)/256F);
        buffer.vertex(matrix,x+width,y+height,z).setUv((u+width)/256F,(v+height)/256F);
        buffer.vertex(matrix,x+width,y,z).setUv((u+width)/256F,v/256F);
        buffer.vertex(matrix,x,y,z).setUv(u/256F,v/256F);buffer.finish();
    }
}
