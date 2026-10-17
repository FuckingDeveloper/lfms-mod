package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.*;
import com.fuckingdeveloper.lms.target.neoforge263.render.*;
import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

/** Source dispatch and native model snapshots; full metadata extraction is verified in the client. */
public final class SourceEntityRenderFamilyCheck {
    private static final String ROOT="net/minecraft/client/renderer/entity/EntityRenderer", NAME="probe/SourceRenderer";
    private static final String ENTITY="Lnet/minecraft/world/entity/Entity;", POSE="Lcom/mojang/blaze3d/vertex/PoseStack;", BUFFER="Lnet/minecraft/client/renderer/MultiBufferSource;";
    private static final String RENDER="("+ENTITY+"FF"+POSE+BUFFER+"I)V";
    private static final Identifier TEXTURE=Identifier.fromNamespaceAndPath("probe","renderer");
    private static int calls; private static Entity expected; private static RuntimeException failure;
    public static Identifier texture(){return TEXTURE;}
    public static LegacyMultiBufferSource buffers(Entity entity){return LegacyMultiBufferSource.recording();}
    public static void capture(Entity entity,float yaw,float partial,PoseStack pose,LegacyMultiBufferSource buffers,int light) {
        check(entity==expected&&yaw==25F&&partial==0.25F&&light==1234,"source render operands changed");calls++;
        if(failure!=null)throw failure;
        pose.translate(2,3,4);
    }
    static void run() throws Exception {
        var f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);var unsafe=(sun.misc.Unsafe)f.get(null);
        expected=(PrimedTnt)unsafe.allocateInstance(PrimedTnt.class);expected.yRotO=20;expected.setYRot(40);
        check(Target263EntityBlockContexts.forEntity(expected).orElseThrow()==TntRenderer.BLOCK_DISPLAY_CONTEXT,"native TNT context");
        check(Target263EntityBlockContexts.forEntity((Minecart)unsafe.allocateInstance(Minecart.class)).orElseThrow()==AbstractMinecartRenderer.BLOCK_DISPLAY_CONTEXT,"native minecart context");
        check(Target263EntityBlockContexts.forEntity((Entity)unsafe.allocateInstance(net.minecraft.world.entity.decoration.ArmorStand.class)).isEmpty(),"unknown context fabricated");
        var path=Files.createTempFile("lms-source-renderer", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry(NAME+".class"));jar.write(fixture());jar.closeEntry();jar.putNextEntry(new JarEntry("probe/ChildRenderer.class"));jar.write(childFixture());jar.closeEntry();}
            byte[] original=Files.readAllBytes(path);
            try(var loader=new ManagedLegacyClassLoader(path,SourceEntityRenderFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass(NAME.replace('/','.'));
                check(type.getSuperclass()==Target263SourceEntityRenderer.class,"source construction base lost");
                var context=new EntityRendererProvider.Context(null,new BlockModelResolver(null),null,null,null,null,null,null,null,null,null);
                @SuppressWarnings("unchecked") var renderer=(Target263SourceEntityRenderer<Entity>)type.getConstructor(EntityRendererProvider.Context.class).newInstance(context);
                var state=renderer.createRenderState();state.lightCoords=1234;renderer.lms$extractSource(expected,state,0.25F);
                check(calls==1&&renderer.lmsLegacyTexture(expected)==TEXTURE,"original callback/texture dispatch");
                try{renderer.lms$extractSource(expected,state,0.25F);throw new AssertionError("double extraction accepted");}catch(IllegalStateException ok){}
                try{Target263SourceEntityRenderer.baseRender(renderer,expected,0,0,new PoseStack(),LegacyMultiBufferSource.recording(),0);throw new AssertionError("active source entity leaked");}catch(UnsupportedOperationException ok){}
                failure=new IllegalArgumentException("source-failure");var failed=renderer.createRenderState();failed.lightCoords=1234;
                try{renderer.lms$extractSource(expected,failed,0.25F);throw new AssertionError("callback failure swallowed");}catch(IllegalArgumentException ok){check(ok==failure,"failure identity");}finally{failure=null;}
                renderer.lms$extractSource(expected,failed,0.25F);check(calls==3,"failed extraction left stale state");
                @SuppressWarnings("unchecked") var child=(Target263SourceEntityRenderer<Entity>)loader.linkOwnedClass("probe.ChildRenderer").getConstructor(EntityRendererProvider.Context.class).newInstance(context);
                var childState=child.createRenderState();childState.lightCoords=1234;child.lms$extractSource(expected,childState,0.25F);
                check(calls==4&&child.lmsLegacyTexture(expected)==TEXTURE,"managed child/super dispatch lost");
                check(((java.lang.reflect.ParameterizedType)type.getGenericSuperclass()).getRawType()==Target263SourceEntityRenderer.class,"generic superclass construction lost");
                for(var field:Target263SourceEntityRenderer.State.class.getDeclaredFields())check(!Entity.class.isAssignableFrom(field.getType()),"submission retained live entity");
                check(Arrays.equals(original,Files.readAllBytes(path)),"source artifact changed");
            }
        }finally{Files.deleteIfExists(path);}
        blockSnapshots();
        System.out.println("PASS: original entity renderer construction/callback/texture, exact yaw/partial/light, once-only and failure cleanup, immutable artifact; native TNT/minecart contexts and frozen block pose/model/light/overlay/outline submission; full metadata and pixels require client");
    }
    private static void blockSnapshots() throws Exception {
        var models=new ArrayList<BlockModelRenderState>();var contexts=new ArrayList<BlockDisplayContext>();var states=new ArrayList<BlockState>();
        var resolver=new BlockModelResolver(null){@Override public void update(BlockModelRenderState output,BlockState block,BlockDisplayContext context){models.add(output);contexts.add(context);states.add(block);}};
        var sourcePose=new PoseStack();sourcePose.translate(2,3,4);
        var buffers=LegacyMultiBufferSource.recording(id->{throw new AssertionError("unexpected atlas query");},Optional.of(TntRenderer.BLOCK_DISPLAY_CONTEXT));
        var dispatcher=LegacyBlockRenderDispatcher.from(new EntityRendererProvider.Context(null,resolver,null,null,null,null,null,null,null,null,null));
        Target263SourceEntityRenderer.whiteBlock(dispatcher,Blocks.TNT.defaultBlockState(),sourcePose,buffers,1234,true);
        sourcePose.translate(100,0,0);
        dispatcher.m_110912_(Blocks.STONE.defaultBlockState(),sourcePose,buffers,4567,OverlayTexture.NO_OVERLAY);
        var geometry=buffers.finishRecording();
        check(models.size()==2&&models.get(0)!=models.get(1)&&contexts.stream().allMatch(c->c==TntRenderer.BLOCK_DISPLAY_CONTEXT)&&states.get(0).is(Blocks.TNT),"model/context resolution operands");
        var field=geometry.getClass().getDeclaredField("blocks");field.setAccessible(true);var blocks=(List<?>)field.get(geometry);
        var record=blocks.getFirst();var poseGetter=record.getClass().getDeclaredMethod("pose");poseGetter.setAccessible(true);
        check(((org.joml.Matrix4fc)poseGetter.invoke(record)).m30()==2,"source pose was retained live");
        var overlay=record.getClass().getDeclaredMethod("overlay");overlay.setAccessible(true);
        check(overlay.invoke(record).equals(OverlayTexture.pack(OverlayTexture.u(1F),10)),"source white flash changed");
        // Actual native model submit delegates a special model to the collector after composing the frozen pose.
        int[] submitted={0};
        var special=new net.minecraft.client.renderer.special.SpecialModelRenderer<Object>() {
            public void submit(Object data,PoseStack pose,SubmitNodeCollector collector,int light,int over,boolean foil,int outline){
                check(pose.last().pose().m30()==12&&light==net.minecraft.util.LightCoordsUtil.max(1234,0)&&over==OverlayTexture.pack(OverlayTexture.u(1F),10)&&outline==37,"native submission operands");submitted[0]++;
            }
            public Object extractArgument(net.minecraft.world.item.ItemStack stack){throw new AssertionError();}
            public void getExtents(java.util.function.Consumer<org.joml.Vector3fc> consumer){throw new AssertionError();}
        };
        models.getFirst().setupSpecialModel(special,new org.joml.Matrix4f());
        var collector=(SubmitNodeCollector)java.lang.reflect.Proxy.newProxyInstance(SourceEntityRenderFamilyCheck.class.getClassLoader(),new Class[]{SubmitNodeCollector.class},(proxy,method,args)->{throw new AssertionError(method);});
        var targetPose=new PoseStack();targetPose.translate(10,0,0);geometry.submit(collector,targetPose,37);
        check(submitted[0]==1&&targetPose.last().pose().m30()==10,"native submission/pose restoration");
        try{dispatcher.renderSingleBlock(Blocks.TNT.defaultBlockState(),sourcePose,LegacyMultiBufferSource.recording(),0,0);throw new AssertionError("unknown context rendered");}catch(UnsupportedOperationException ok){}
        try{dispatcher.renderSingleBlock(Blocks.TNT.defaultBlockState(),sourcePose,buffers,0,0);throw new AssertionError("finished recording mutated");}catch(IllegalStateException ok){}
    }
    private static byte[] fixture() {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,NAME,"L"+ROOT+"<"+ENTITY+">;",ROOT,null);
        var m=w.visitMethod(ACC_PUBLIC,"<init>","(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)V",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);m.visitMethodInsn(INVOKESPECIAL,ROOT,"<init>","(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)V",false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
        String hook="com/fuckingdeveloper/lms/check/SourceEntityRenderFamilyCheck";
        m=w.visitMethod(ACC_PROTECTED,"lms$newBuffers","("+ENTITY+")Lcom/fuckingdeveloper/lms/profile/forge1192/runtime/render/LegacyMultiBufferSource;",null,null);m.visitCode();m.visitVarInsn(ALOAD,1);m.visitMethodInsn(INVOKESTATIC,hook,"buffers","("+ENTITY+")Lcom/fuckingdeveloper/lms/profile/forge1192/runtime/render/LegacyMultiBufferSource;",false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_BRIDGE|ACC_SYNTHETIC,"m_7392_",RENDER,null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);m.visitTypeInsn(CHECKCAST,"net/minecraft/world/entity/item/PrimedTnt");m.visitVarInsn(FLOAD,2);m.visitVarInsn(FLOAD,3);m.visitVarInsn(ALOAD,4);m.visitVarInsn(ALOAD,5);m.visitVarInsn(ILOAD,6);m.visitMethodInsn(INVOKEVIRTUAL,NAME,"typed",RENDER.replace(ENTITY,"Lnet/minecraft/world/entity/item/PrimedTnt;"),false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC,"typed",RENDER.replace(ENTITY,"Lnet/minecraft/world/entity/item/PrimedTnt;"),null,null);m.visitCode();loadRender(m,false);m.visitMethodInsn(INVOKESTATIC,hook,"capture",RENDER,false);loadRender(m,true);m.visitMethodInsn(INVOKESPECIAL,ROOT,"m_7392_",RENDER,false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC,"m_5478_","("+ENTITY+")Lnet/minecraft/resources/ResourceLocation;",null,null);m.visitCode();m.visitMethodInsn(INVOKESTATIC,hook,"texture","()Lnet/minecraft/resources/ResourceLocation;",false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();return w.toByteArray();
    }
    private static byte[] childFixture() {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,"probe/ChildRenderer",null,NAME,null);
        var m=w.visitMethod(ACC_PUBLIC,"<init>","(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)V",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);m.visitMethodInsn(INVOKESPECIAL,NAME,"<init>","(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)V",false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC,"m_7392_",RENDER,null,null);m.visitCode();loadRender(m,true);m.visitMethodInsn(INVOKESPECIAL,NAME,"m_7392_",RENDER,false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();return w.toByteArray();
    }
    private static void loadRender(MethodVisitor m,boolean receiver){if(receiver)m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);m.visitVarInsn(FLOAD,2);m.visitVarInsn(FLOAD,3);m.visitVarInsn(ALOAD,4);m.visitVarInsn(ALOAD,5);m.visitVarInsn(ILOAD,6);}
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
