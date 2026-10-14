package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.render.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.*;
import org.joml.*;
import java.util.*;

/** Native extracted strata/quad state, without creating a GPU window. */
public final class GuiPlaneFamilyCheck {
    public static void run() throws Exception {
        var order=new Forge1192GuiPlaneBridge.Order();
        check(!order.advance(0,true)&&order.advance(100,true)&&!order.advance(100,true),"source plane transition/equal order lost");
        check(!order.advance(0,false),"depth-disabled painter draw moved stratum");
        expect(()->order.advance(99,true));
        check(order.advance(200,true),"rejected descending draw changed high-water state");
        for(float z:new float[]{-1,1000,Float.NaN,Float.POSITIVE_INFINITY})expect(()->new Forge1192GuiPlaneBridge.Order().advance(z,true));
        var matrix=new Matrix4f().translate(7.25F,9.5F,100).scale(2,3,1);
        var xy=Forge1192GuiPlaneBridge.planar(matrix).transformPosition(new Vector2f(2,4));check(xy.x==11.25F&&xy.y==21.5F,"source XY pose changed with depth projection");
        for(float scale:new float[]{0.5F,0.25F,2F,-1F,0F}) {
            var scaled=new Matrix4f().translate(7.25F,9.5F,25).scale(scale);
            var projected=Forge1192GuiPlaneBridge.planar(scaled).transformPosition(new Vector2f(2,4));
            var source=scaled.transformPosition(new Vector3f(2,4,0));
            check(projected.x==source.x&&projected.y==source.y&&source.z==25,"scaled flat text/geometry changed XY or effective plane");
            check(Forge1192GuiPlaneBridge.depth(scaled,100)==scaled.transformPosition(new Vector3f(0,0,100)).z,"explicit blit depth ignored Z scaling");
        }
        expect(()->Forge1192GuiPlaneBridge.planar(new Matrix4f().rotateX(0.1F)));
        expect(()->Forge1192GuiPlaneBridge.planar(new Matrix4f().m03(0.5F)));
        expect(()->Forge1192GuiPlaneBridge.planar(new Matrix4f().m20(1)));
        expect(()->LegacyGuiBuffer.uniformPlane(List.of(new LegacyGuiBuffer.Point(0,0,0,0,0,-1),new LegacyGuiBuffer.Point(1,1,100,0,0,-1))));

        var unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);var unsafe=(sun.misc.Unsafe)unsafeField.get(null);
        var graphics=(GuiGraphicsExtractor)unsafe.allocateInstance(GuiGraphicsExtractor.class);
        var state=new GuiRenderState();
        set(graphics,"guiRenderState",state);set(graphics,"pose",new Matrix3x2fStack(16));
        Class<?> scissor=Class.forName("net.minecraft.client.gui.GuiGraphicsExtractor$ScissorStack");var ctor=scissor.getDeclaredConstructor(ScreenRectangle.class);ctor.setAccessible(true);set(graphics,"scissorStack",ctor.newInstance(new ScreenRectangle(0,0,500,500)));
        Forge1192WidgetGraphicsBridge.extract((pose,x,y,partial)->{
            Forge1192ContainerGraphicsBridge.setShader(Forge1192ContainerGraphicsBridge::positionColorShader);
            quad(pose);pose.translate(7.25F,9.5F,100);quad(pose);quad(pose);
        },graphics,0,0,0);
        var elements=new ArrayList<GuiElementRenderState>();state.forEachElement(elements::add,GuiRenderState.TraverseRange.ALL);
        check(elements.size()==3,"actual native GUI state lost source primitives");
        var shifted=(LegacyGuiBuffer.QuadState)elements.get(1);
        check(shifted.points().getFirst().x()==7.25F&&shifted.points().getFirst().y()==9.5F,"source XY vertex projection changed");
        check(shifted.points().stream().allMatch(p->p.z()==0),"raw source depth escaped native GUI stratum model");
        var strata=GuiRenderState.class.getDeclaredField("strata");strata.setAccessible(true);check(((List<?>)strata.get(state)).size()==2,"native stratum did not advance exactly once for 0 -> 100 -> 100");
        var scaledState=new GuiRenderState();set(graphics,"guiRenderState",scaledState);
        Forge1192WidgetGraphicsBridge.extract((pose,x,y,partial)->{
            Forge1192ContainerGraphicsBridge.setShader(Forge1192ContainerGraphicsBridge::positionColorShader);
            pose.translate(7.25F,9.5F,0);pose.scale(0.5F,0.5F,0.5F);
            quad(pose);quad(pose,200);
            expect(()->Forge1192GuiItemExtractionBridge.item(Forge1192GuiItemExtractionBridge.context(null),pose,net.minecraft.world.item.ItemStack.EMPTY,0,0));
            pose.scale(2,2,2);quad(pose,100);
        },graphics,0,0,0);
        var scaledElements=new ArrayList<GuiElementRenderState>();scaledState.forEachElement(scaledElements::add,GuiRenderState.TraverseRange.ALL);
        check(scaledElements.size()==3,"scaled/restored source primitives lost");
        var half=(LegacyGuiBuffer.QuadState)scaledElements.get(0);
        var restored=(LegacyGuiBuffer.QuadState)scaledElements.get(2);
        check(half.points().get(2).x()==15.25F&&half.points().get(2).y()==17.5F,"uniform half scale not preserved in native geometry");
        check(restored.points().get(2).x()==23.25F&&restored.points().get(2).y()==25.5F,"reciprocal scale did not restore XY");
        check(((List<?>)strata.get(scaledState)).size()==2,"scaled local Z=200 and restored Z=100 must occupy equal native planes");
        try{Forge1192WidgetGraphicsBridge.extract((pose,x,y,partial)->{Forge1192ContainerGraphicsBridge.setShader(Forge1192ContainerGraphicsBridge::positionColorShader);pose.translate(0,0,100);quad(pose);pose.translate(0,0,-50);quad(pose);},graphics,0,0,0);throw new AssertionError("descending geometry accepted");}catch(UnsupportedOperationException expected){}
        try{Forge1192GuiPlaneBridge.submit(0);throw new AssertionError("source depth scope leaked");}catch(IllegalStateException expected){}
        System.out.println("PASS: actual native GUI strata and immutable quad projection, source Z=100, independent Z scales, half-scale/restoration and explicit local depths, XY preservation, equal/disabled/descending planes, 3D/perspective/mixed-plane rejection and failure scope restoration");
    }
    private static void quad(PoseStack pose){quad(pose,0);}
    private static void quad(PoseStack pose,float z){var buffer=new LegacyGuiBuffer();buffer.begin(LegacyVertexFormatMode.QUADS,LegacyDefaultVertexFormat.f_85815_);for(float[] xy:new float[][]{{0,0},{0,16},{16,16},{16,0}})buffer.vertex(pose.last().pose(),xy[0],xy[1],z).setColor(-1);buffer.finish();}
    private static void set(Object target,String name,Object value)throws Exception{var field=GuiGraphicsExtractor.class.getDeclaredField(name);field.setAccessible(true);field.set(target,value);}
    private static void expect(Runnable work){try{work.run();throw new AssertionError("unproven source plane accepted");}catch(UnsupportedOperationException expected){}}
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
