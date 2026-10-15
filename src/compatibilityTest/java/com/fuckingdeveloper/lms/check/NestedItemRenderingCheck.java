package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.render.*;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

final class NestedItemRenderingCheck {
    static void run() throws Exception {
        sourceAliases(false);
        sourceAliases(true);
        NativeDefaultComponentFixture.run(() -> {
            var outlines=new ArrayList<Integer>();
            var calls=new ArrayList<String>(); var poses=new ArrayList<Float>();
            ItemStack original=com.fuckingdeveloper.lms.runtime.Forge1192EarlyItemStackBridge.create(Items.STICK);
            original.setCount(4);
            var renderer=new SpecialModelRenderer<Integer>() {
                public Integer extractArgument(ItemStack s){throw new AssertionError("late extraction");}
                public void getExtents(java.util.function.Consumer<org.joml.Vector3fc> out){throw new AssertionError("invented extents");}
                public void submit(Integer count,PoseStack pose,SubmitNodeCollector output,int light,int overlay,boolean foil,int outline) {
                    outlines.add(outline);calls.add("submit:"+count+":"+light+":"+overlay);poses.add(pose.last().pose().m30());
                }
            };
            var resolver=new ItemModelResolver(null) {
                @Override public void updateForTopItem(ItemStackRenderState state,ItemStack copy,ItemDisplayContext display,Level level,ItemOwner owner,int seed) {
                    check(copy!=original && copy.getCount()==4,"input stack not isolated");
                    check(display==ItemDisplayContext.NONE && level==null && owner==null && seed==37,"source context/seed changed");
                    calls.add("extract");
                    var layer=state.newLayer();layer.setupSpecialModel(renderer,copy.getCount());
                    layer.setExtents(()->new org.joml.Vector3fc[]{new org.joml.Vector3f(0.5F,0.5F,0.5F)});
                }
            };
            var buffers=LegacyMultiBufferSource.recording();var sourcePose=new PoseStack();sourcePose.translate(3,4,5);
            Forge1192NestedItemBridge.renderStatic(resolver,original,ItemDisplayContext.NONE,123,456,sourcePose,buffers,37);
            original.setCount(1);sourcePose.translate(70,0,0);
            var geometry=buffers.finishRecording();
            check(calls.equals(List.of("extract")),"source nested draw submitted during extraction");
            var extents=geometry.extents();check(extents.length==1 && extents[0].x()==3 && extents[0].y()==4,"nested bounds lost source transform");
            var parent=new PoseStack();parent.translate(100,0,0);
            var collector=(SubmitNodeCollector)java.lang.reflect.Proxy.newProxyInstance(NestedItemRenderingCheck.class.getClassLoader(),new Class[]{SubmitNodeCollector.class},(p,m,a)->{throw new AssertionError(m);});
            geometry.submit(collector,parent);
            check(calls.equals(List.of("extract","submit:4:123:456")) && poses.equals(List.of(102.5F)),"deferred native submission lost pose/count/light/overlay or repeated extraction");
            geometry.submit(collector,parent,0xFF123456);
            check(outlines.equals(List.of(0,0xFF123456)),"nested native outline propagation lost");
            check(parent.last().pose().m30()==100,"nested submission leaked parent pose");
            try { Forge1192NestedItemBridge.renderStatic(resolver,original,ItemDisplayContext.FIRST_PERSON_LEFT_HAND,1,2,new PoseStack(),LegacyMultiBufferSource.recording(),0);
                throw new AssertionError("unproven independent handedness admitted"); } catch(UnsupportedOperationException expected) {}
            try { buffers.recordItem(resolver,original,ItemDisplayContext.NONE,new PoseStack(),null,null,1,2,37);
                throw new AssertionError("finished nested recording mutable"); } catch(IllegalStateException expected) {}
        });
        class Properties extends Item.Properties { String sourceName(){return effectiveDescriptionId();} }
        var properties=new Properties();properties.setId(net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.ITEM,net.minecraft.resources.Identifier.parse("probe:unrelated_item")));
        var block=net.minecraft.world.level.block.Blocks.DIAMOND_BLOCK;
        com.fuckingdeveloper.lms.runtime.Forge1192VanillaConstructionBridge.withBlockItemDescription(properties,block);
        check(properties.sourceName().equals(block.getDescriptionId()),"source block name replaced by item registry name");
        System.out.println("PASS: source nested draw once-only extraction, isolated input stack/pose, native deferred light/overlay/bounds/transform, rejection boundaries and block-owned name intent");
    }
    private static void sourceAliases(boolean foreignUse) throws Exception {
        var node=new org.objectweb.asm.tree.ClassNode();node.version=org.objectweb.asm.Opcodes.V17;
        node.access=org.objectweb.asm.Opcodes.ACC_PUBLIC;node.name="probe/NestedAlias";node.superName="java/lang/Object";
        String renderer="net/minecraft/client/renderer/entity/ItemRenderer";
        String descriptor="(Lnet/minecraft/client/Minecraft;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)"+(foreignUse?"L"+renderer+";":"V");
        var method=new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC|org.objectweb.asm.Opcodes.ACC_STATIC,"draw",descriptor,null,null);node.methods.add(method);
        method.visitVarInsn(25,0);method.visitMethodInsn(182,"net/minecraft/client/Minecraft","m_91291_","()L"+renderer+";",false);method.visitVarInsn(58,5);
        for(int i=0;i<2;i++) {
            method.visitVarInsn(25,5);method.visitVarInsn(25,1);method.visitVarInsn(25,2);method.visitInsn(4);method.visitInsn(5);
            method.visitVarInsn(25,3);method.visitVarInsn(25,4);method.visitInsn(3);
            method.visitMethodInsn(182,renderer,"m_174269_","(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;IILcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",false);
        }
        if(foreignUse)method.visitVarInsn(25,5);
        method.visitInsn(foreignUse?176:177);method.visitMaxs(8,6);
        var writer=new org.objectweb.asm.ClassWriter(0);node.accept(writer);
        var result=new com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer().transform("probe.NestedAlias",writer.toByteArray());
        var transformed=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(result.bytes()).accept(transformed,0);
        long adapted=transformed.methods.stream().flatMap(m->Arrays.stream(m.instructions.toArray())).filter(i->i instanceof org.objectweb.asm.tree.MethodInsnNode c
                && c.owner.equals("com/fuckingdeveloper/lms/runtime/render/Forge1192NestedItemBridge")).count();
        check(adapted==(foreignUse?0:3),"local-alias repeated calls or foreign receiver-use boundary changed");
    }
    private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
}
