package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.Forge1192CustomItemRenderer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.LegacyMultiBufferSource;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.LegacyRenderType;
import com.fuckingdeveloper.lms.target.neoforge263.render.Target263CustomItemSnapshot;
import com.fuckingdeveloper.lms.target.neoforge263.render.Target263DeferredCustomItem;
import com.fuckingdeveloper.lms.target.neoforge263.render.Target263LegacyGuiItemModel;


import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192ItemBridge;
import com.mojang.blaze3d.vertex.*;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import org.joml.*;

final class DeferredCustomItemCheck {
    static void run() throws Exception {
        NativeDefaultComponentFixture.run(()->{
            var stack=com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192EarlyItemStackBridge.create(Items.STICK);
            stack.setCount(4);Forge1192ItemBridge.getOrCreateTag(stack).putInt("charge",42);
            stack.remove(DataComponents.ITEM_NAME);
            var expectedPatch=stack.copy().getComponentsPatch();
            var snapshot=Target263CustomItemSnapshot.capture(stack);
            stack.setCount(1);Forge1192ItemBridge.getOrCreateTag(stack).putInt("charge",99);
            var first=snapshot.materialize();
            check(first.getCount()==4 && first.getComponentsPatch().equals(expectedPatch),"snapshot lost original prototype/patch/count/removal intent");
            Forge1192ItemBridge.getOrCreateTag(first).putInt("charge",5);first.setCount(7);
            check(Forge1192ItemBridge.getOrCreateTag(snapshot.materialize()).getIntOr("charge",-1)==42
                    && snapshot.materialize().getCount()==4,"materialization mutated source snapshot");
            check(Forge1192ItemBridge.getOrCreateTag(stack).getIntOr("charge",-1)==99,"deferred snapshot mutated live input");

            var admitted=EnumSet.of(ItemDisplayContext.NONE,ItemDisplayContext.FIRST_PERSON_LEFT_HAND,ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                    ItemDisplayContext.THIRD_PERSON_LEFT_HAND,ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,ItemDisplayContext.HEAD,ItemDisplayContext.FIXED);
            for(var display:ItemDisplayContext.values())check(Target263DeferredCustomItem.supports(display)==admitted.contains(display),"unsupported context admitted: "+display);
            for(var display:admitted)try { verifyNativeDraw(snapshot,display); } catch(Exception failure){throw new AssertionError(failure);}
        });
        System.out.println("PASS: seven deferred source contexts, no extraction callback, once-per-submit source draw, actual varying light/overlay/pose, isolated count/component/NBT snapshot and native vertex submission without double transforms; early bounds rejection and native outlines");
    }
    private static void verifyNativeDraw(Target263CustomItemSnapshot snapshot,ItemDisplayContext display) throws Exception {
        var colors=new ArrayList<Integer>();var types=new ArrayList<net.minecraft.client.renderer.rendertype.RenderType>();
        var calls=new ArrayList<List<Integer>>();var vertices=new ArrayList<Vector3f>();var uv=new ArrayList<List<Integer>>();
        var expectedPose=new Matrix4f().translation(10,20,30).translate(-0.5F,-0.5F,-0.5F).rotateY(0.3F);
        var renderer=new Forge1192CustomItemRenderer(null,null) {
            @Override public void m_108829_(ItemStack input,ItemDisplayContext context,PoseStack pose,LegacyMultiBufferSource buffers,int light,int overlay) {
                check(context==display && pose.last().pose().equals(expectedPose,0.00001F),"source lost exact native context or transformed pose");
                check(input.getCount()==4 && Forge1192ItemBridge.getOrCreateTag(input).getIntOr("charge",-1)==42,"source renderer saw a stale/mutated snapshot");
                Forge1192ItemBridge.getOrCreateTag(input).putInt("charge",0);
                calls.add(List.of(light,overlay));
                buffers.getBuffer(LegacyRenderType.m_110452_(net.minecraft.resources.Identifier.parse("probe:custom")))
                        .addVertex(pose.last(),1,2,3).setUv2(light&65535,light>>>16).setUv1(overlay&65535,overlay>>>16);
            }
        };
        var model=new Target263LegacyGuiItemModel(new ModelRenderProperties(false,null,
                net.minecraft.client.resources.model.cuboid.ItemTransforms.NO_TRANSFORMS),new Matrix4f().rotateY(0.3F),null);
        var state=new ItemStackRenderState();
        var resolver=new ItemModelResolver(null) {
            @Override public void appendItemLayers(ItemStackRenderState output,ItemStack stack,ItemDisplayContext context,
                    net.minecraft.world.level.Level level,net.minecraft.world.entity.ItemOwner owner,int seed) {
                model.installDeferred(output,stack,context);
            }
        };
        resolver.updateForTopItem(state,snapshot.materialize(),display,null,null,13);
        check(calls.isEmpty(),"source callback invoked during extraction with guessed light");
        try{state.getModelBoundingBox();throw new AssertionError("invented deferred geometry bounds");}catch(UnsupportedOperationException expected){}
        var layers=ItemStackRenderState.class.getDeclaredField("layers");layers.setAccessible(true);
        var layer=((ItemStackRenderState.LayerRenderState[])layers.get(state))[0];
        var argumentField=layer.getClass().getDeclaredField("argumentForSpecialRendering");argumentField.setAccessible(true);
        var argument=(Target263DeferredCustomItem.Argument)argumentField.get(layer);
        // Resolve a controlled source renderer at the submission boundary; leave actual native layer transforms intact.
        layer.setupSpecialModel(new SpecialModelRenderer<Target263DeferredCustomItem.Argument>() {
            public Target263DeferredCustomItem.Argument extractArgument(ItemStack stack){throw new AssertionError("late recapture");}
            public void getExtents(java.util.function.Consumer<Vector3fc> output){throw new AssertionError("invented bounds");}
            public void submit(Target263DeferredCustomItem.Argument value,PoseStack pose,SubmitNodeCollector collector,int light,int overlay,boolean foil,int outline) {
                Target263DeferredCustomItem.draw(renderer,value.snapshot().materialize(),value.display(),pose,collector,light,overlay,outline,
                        id->{throw new AssertionError("unexpected sprite");});
            }
        },argument);
        var vertex=(VertexConsumer)Proxy.newProxyInstance(DeferredCustomItemCheck.class.getClassLoader(),new Class[]{VertexConsumer.class},(p,m,a)->{
            if(m.getName().equals("addVertex"))vertices.add(new Vector3f((float)a[0],(float)a[1],(float)a[2]));
            else if(m.getName().equals("setColor"))colors.add((int)a[0]);
            else if(m.getName().equals("setUv1") || m.getName().equals("setUv2"))uv.add(List.of((int)a[0],(int)a[1]));
            else throw new AssertionError(m);
            return p;
        });
        var collector=(SubmitNodeCollector)Proxy.newProxyInstance(DeferredCustomItemCheck.class.getClassLoader(),new Class[]{SubmitNodeCollector.class},(p,m,a)->{
            check(m.getName().equals("submitCustomGeometry"),"source bypassed native custom geometry");
            types.add((net.minecraft.client.renderer.rendertype.RenderType)a[1]);
            ((SubmitNodeCollector.CustomGeometryRenderer)a[2]).render(((PoseStack)a[0]).last(),vertex);return null;
        });
        var parent=new PoseStack();parent.translate(10,20,30);
        state.submit(parent,collector,0x123456,0x654321,0);
        state.submit(parent,collector,0x223344,0x554433,0);
        check(calls.equals(List.of(List.of(0x123456,0x654321),List.of(0x223344,0x554433))),"source callback count or final light/overlay changed");
        var point=new Vector3f(1,2,3).mulPosition(expectedPose);
        check(vertices.size()==2 && vertices.getFirst().equals(point,0.00001F) && vertices.getLast().equals(point,0.00001F),"source geometry was transformed twice");
        check(uv.equals(List.of(List.of(0x3456,0x12),List.of(0x4321,0x65),List.of(0x3344,0x22),List.of(0x4433,0x55))),"source light/overlay vertex data changed");
        check(parent.last().pose().equals(new Matrix4f().translation(10,20,30)),"source draw leaked native parent pose");
        state.submit(parent,collector,0x123456,0x654321,0xFF998877);
        check(calls.size()==3 && vertices.size()==4 && vertices.getLast().equals(point,0.00001F),"outline duplicated callback/transforms or omitted normal geometry");
        check(types.getLast()==types.getFirst().outline().orElseThrow(),"deferred native outline type lost");
        check(colors.equals(List.of(0xFF998877)),"deferred outline color lost");
    }
    private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
}
