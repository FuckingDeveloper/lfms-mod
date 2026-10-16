package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.runtime.render.*;
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.invoke.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

final class WorldRenderFamilyCheck implements Opcodes {
    static void run(net.neoforged.bus.api.IEventBus ownerBus, net.neoforged.fml.ModContainer host) throws Exception {
        eventDelivery(ownerBus,host);
        var state=new LegacyWorldGeometry.State(Optional.of(LegacyGuiBlend.DEFAULT),false,true,true,-1,15);
        var vertices=new ArrayList<LegacyWorldGeometry.Vertex>();for(int i=0;i<4;i++)vertices.add(new LegacyWorldGeometry.Vertex(i,i+1,i+2,-1));
        var quad=LegacyWorldGeometry.finish(LegacyVertexFormatMode.QUADS,vertices,state);vertices.clear();check(quad.vertices().size()==6&&quad.vertices().get(3).x()==2,"source quad triangulation/immutability lost");
        var pipeline=LegacyWorldGeometry.pipeline(quad);check(!pipeline.getDepthStencilState().writeDepth(),"disabled source depth still writes");
        check(pipeline==LegacyWorldGeometry.pipeline(quad),"world pipeline state not reused");
        try{LegacyWorldGeometry.finish(LegacyVertexFormatMode.TRIANGLES,quad.vertices().subList(0,2),state);throw new AssertionError("partial primitive accepted");}catch(IllegalStateException expected){}
        try{new LegacyWorldGeometry.Vertex(Float.NaN,0,0,0);throw new AssertionError("nonfinite geometry accepted");}catch(IllegalArgumentException expected){}
        var expected=new IllegalArgumentException("scope-probe");
        try{LegacyWorldRenderScope.collect(()->{Forge1192ContainerGraphicsBridge.disableDepthTest();throw expected;});throw new AssertionError("source failure swallowed");}catch(IllegalArgumentException actual){check(actual==expected,"source exception replaced");}
        try{LegacyImmediateTesselator.m_85913_();throw new AssertionError("world extraction scope leaked");}catch(IllegalStateException boundary){}
        var helper=new ClassNode();helper.version=V17;helper.access=ACC_PUBLIC;helper.name="fixture/WorldBatchHelper";helper.superName="java/lang/Object";
        String tess="com/mojang/blaze3d/vertex/Tesselator",builder="com/mojang/blaze3d/vertex/BufferBuilder",pose="Lcom/mojang/blaze3d/vertex/PoseStack;";
        var m=new MethodNode(ACC_PUBLIC|ACC_STATIC,"draw","("+pose+"Ljava/util/function/BiConsumer;)L"+tess+";",null,null);
        m.instructions.add(new MethodInsnNode(INVOKESTATIC,tess,"m_85913_","()L"+tess+";",false));m.instructions.add(new VarInsnNode(ASTORE,2));
        m.instructions.add(new VarInsnNode(ALOAD,1));m.instructions.add(new VarInsnNode(ALOAD,2));m.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,tess,"m_85915_","()L"+builder+";",false));m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new MethodInsnNode(INVOKEINTERFACE,"java/util/function/BiConsumer","accept","(Ljava/lang/Object;Ljava/lang/Object;)V",true));m.instructions.add(new VarInsnNode(ALOAD,2));m.instructions.add(new InsnNode(ARETURN));helper.methods.add(m);
        var path=Files.createTempFile("lms-world-helper", ".jar");try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry(helper.name+".class"));var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);helper.accept(writer);jar.write(writer.toByteArray());jar.closeEntry();}
        try(var loader=new ManagedLegacyClassLoader(path,WorldRenderFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())){
            var type=loader.linkOwnedClass(helper.name.replace('/','.'));var draw=type.getMethod("draw",PoseStack.class,java.util.function.BiConsumer.class);check(draw.getReturnType()==LegacyImmediateTesselator.class,"managed helper escaped source ABI");
            var batches=LegacyWorldRenderScope.collect(()->{
                Forge1192ContainerGraphicsBridge.setShader(Forge1192ContainerGraphicsBridge::positionColorShader);Forge1192ContainerGraphicsBridge.disableDepthTest();Forge1192ContainerGraphicsBridge.enableBlend();
                var poseStack=new PoseStack();poseStack.translate(10,20,30);
                java.util.function.BiConsumer<LegacyImmediateBuffer,PoseStack> write=(b,p)->{
                    b.m_166779_(LegacyVertexFormatMode.TRIANGLES,LegacyDefaultVertexFormat.f_85815_);
                    for(int i=0;i<3;i++)check(b.m_85982_(p.last().pose(),i,i+1,i+2).setColor(1,2,3,128)==b,"source fluent vertex receiver changed");
                };
                try{((LegacyImmediateTesselator)draw.invoke(null,poseStack,write)).m_85914_();}catch(ReflectiveOperationException failure){throw new AssertionError(failure);}
                check(LegacyWorldRenderScope.collect(()->{}).isEmpty(),"nested callback inherited outer geometry");
            });
            check(batches.size()==1&&batches.getFirst().vertices().getFirst().z()==32,"source helper/lambda/3D transform failed");
        }
        Path artifact=Path.of("legacy-mods/IC2Classic-1.19.2-2.1.3.4.jar");
        if(Files.exists(artifact))try(var loader=new ManagedLegacyClassLoader(artifact,WorldRenderFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())){
            var type=loader.linkOwnedClass("ic2.core.block.rendering.world.impl.EuReaderOverlay");
            var callback=MethodHandles.privateLookupIn(type,MethodHandles.lookup()).findStatic(type,"lambda$render$0",MethodType.methodType(void.class,net.minecraft.core.BlockPos.class,LegacyImmediateBuffer.class,PoseStack.class));
            var batches=LegacyWorldRenderScope.collect(()->{
                Forge1192ContainerGraphicsBridge.setShader(Forge1192ContainerGraphicsBridge::positionColorShader);Forge1192ContainerGraphicsBridge.disableTexture();Forge1192ContainerGraphicsBridge.disableDepthTest();Forge1192ContainerGraphicsBridge.enableBlend();Forge1192ContainerGraphicsBridge.defaultBlendFunc();
                var tessellator=LegacyImmediateTesselator.m_85913_();
                try{callback.invoke(new net.minecraft.core.BlockPos(2,3,4),tessellator.m_85915_(),new PoseStack());}catch(Throwable failure){throw new AssertionError("original world cube callback",failure);}
                tessellator.m_85914_();
            });
            check(batches.size()==1&&batches.getFirst().vertices().size()==36,"original color cube geometry lost");
        }
        System.out.println("PASS: source world helper/callable ABI, original color cube vertices, immutable topology/3D transforms, fluent identity, pipeline state and nested/failure restoration; real GPU/stage behavior requires client");
    }
    private static void eventDelivery(net.neoforged.bus.api.IEventBus ownerBus,net.neoforged.fml.ModContainer host) throws Exception {
        var field=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);var unsafe=(sun.misc.Unsafe)field.get(null);
        var renderer=(net.minecraft.client.renderer.LevelRenderer)unsafe.allocateInstance(net.minecraft.client.renderer.LevelRenderer.class);
        var camera=new net.minecraft.client.Camera();var pose=new PoseStack();var projection=new org.joml.Matrix4f();
        var frustum=new net.minecraft.client.renderer.culling.Frustum(projection,projection);
        var event=new LegacyWorldStageEvent(Forge1192RenderStageBridge.Stage.AFTER_WEATHER,renderer,pose,projection,37,.25F,camera,frustum);
        var order=new ArrayList<String>();var fail=new java.util.concurrent.atomic.AtomicBoolean();var expected=new IllegalArgumentException("world-event-probe");
        java.util.function.Consumer<LegacyWorldStageEvent> first=e->{check(e==event&&e.getRenderTick()==37&&e.getCamera()==camera&&e.getPoseStack()==pose&&e.getProjectionMatrix()==projection&&e.getFrustum()==frustum&&e.getLevelRenderer()==renderer&&e.getPartialTick()==.25F,"source event identity/arguments changed");order.add("high");if(fail.get())throw expected;};
        java.util.function.Consumer<LegacyWorldStageEvent> last=e->order.add("low");
        var nativeBus=net.neoforged.neoforge.common.NeoForge.EVENT_BUS;
        try(var ownership=com.fuckingdeveloper.lms.runtime.Forge1192LifecycleBridge.enter("native_smoke",ownerBus,host)){
            com.fuckingdeveloper.lms.runtime.Forge1192EventBusBridge.addListener(nativeBus,net.neoforged.bus.api.EventPriority.LOW,last,false,LegacyWorldStageEvent.class);
            com.fuckingdeveloper.lms.runtime.Forge1192EventBusBridge.addListener(nativeBus,net.neoforged.bus.api.EventPriority.HIGH,first,false,LegacyWorldStageEvent.class);
        }
        try{
            LegacyWorldRenderScope.collect(()->Target263WorldRenderAdapter.sourceBus().post(event));
            check(order.equals(List.of("high","low")),"source world priority/once-only delivery lost: "+order);
            order.clear();nativeBus.post(event);check(order.isEmpty(),"source listeners leaked to native drawing bus");
            fail.set(true);try{LegacyWorldRenderScope.collect(()->Target263WorldRenderAdapter.sourceBus().post(event));throw new AssertionError("world listener failure swallowed");}catch(IllegalArgumentException actual){check(actual==expected,"world listener exception replaced");}
        }finally{
            com.fuckingdeveloper.lms.runtime.Forge1192EventBusBridge.unregister(nativeBus,first);
            com.fuckingdeveloper.lms.runtime.Forge1192EventBusBridge.unregister(nativeBus,last);
        }
        order.clear();LegacyWorldRenderScope.collect(()->Target263WorldRenderAdapter.sourceBus().post(event));check(order.isEmpty(),"world source subscriptions survived unregister");
    }
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
