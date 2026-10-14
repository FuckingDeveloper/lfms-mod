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
        try{Forge1192WidgetGraphicsBridge.extract((pose,x,y,partial)->{Forge1192ContainerGraphicsBridge.setShader(Forge1192ContainerGraphicsBridge::positionColorShader);pose.translate(0,0,100);quad(pose);pose.translate(0,0,-50);quad(pose);},graphics,0,0,0);throw new AssertionError("descending geometry accepted");}catch(UnsupportedOperationException expected){}
        try{Forge1192GuiPlaneBridge.submit(0);throw new AssertionError("source depth scope leaked");}catch(IllegalStateException expected){}
        System.out.println("PASS: actual native GUI strata and immutable quad projection, source Z=100, XY preservation, equal/disabled/descending planes, 3D/perspective/mixed-plane rejection and failure scope restoration");
    }
    private static void quad(PoseStack pose){var buffer=new LegacyGuiBuffer();buffer.begin(LegacyVertexFormatMode.QUADS,LegacyDefaultVertexFormat.f_85815_);for(float[] xy:new float[][]{{0,0},{0,16},{16,16},{16,0}})buffer.vertex(pose.last().pose(),xy[0],xy[1],0).setColor(-1);buffer.finish();}
    private static void set(Object target,String name,Object value)throws Exception{var field=GuiGraphicsExtractor.class.getDeclaredField(name);field.setAccessible(true);field.set(target,value);}
    private static void expect(Runnable work){try{work.run();throw new AssertionError("unproven source plane accepted");}catch(UnsupportedOperationException expected){}}
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
