package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.*;
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.*;
import org.joml.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.*;

public final class GuiModelViewFamilyCheck {
    public static final class Fixture {
        public static void draw(Runnable draw) {
            var pose=Forge1192GuiModelViewBridge.stack();
            pose.pushPose();pose.translate(7.25,9.5,0);pose.scale(2,3,1);
            draw.run();Forge1192GuiModelViewBridge.apply();draw.run();
            pose.popPose();draw.run();Forge1192GuiModelViewBridge.apply();draw.run();
        }
        public static PoseStack escape(){return Forge1192GuiModelViewBridge.stack();}
        public static void observe(){Forge1192GuiModelViewBridge.stack().last();}
        public static void detached(){Forge1192GuiModelViewBridge.apply();}
    }
    static void run() throws Exception {
        String original=Fixture.class.getName().replace('.','/'),name="fixture/GuiModelView";
        byte[] bytes;try(var in=GuiModelViewFamilyCheck.class.getResourceAsStream("GuiModelViewFamilyCheck$Fixture.class")){bytes=in.readAllBytes();}
        var writer=new ClassWriter(0);
        new ClassReader(bytes).accept(new ClassRemapper(writer,new Remapper(){public String map(String owner){return owner.equals(original)?name:owner.equals("com/fuckingdeveloper/lms/profile/forge1192/runtime/render/Forge1192GuiModelViewBridge")?"com/mojang/blaze3d/systems/RenderSystem":owner;}
            public String mapMethodName(String owner,String method,String desc){if(owner.equals("com/fuckingdeveloper/lms/profile/forge1192/runtime/render/Forge1192GuiModelViewBridge"))return method.equals("stack")?"m_157191_":method.equals("apply")?"m_157182_":method;return method;}
        }),0);
        var source=new ClassNode();new ClassReader(writer.toByteArray()).accept(source,0);
        for(String rejected:List.of("escape","observe","detached")) {
            var selected=new ClassNode();selected.version=source.version;selected.access=Opcodes.ACC_PUBLIC;selected.name="fixture/RejectedModelView";selected.superName="java/lang/Object";
            var method=source.methods.stream().filter(m->m.name.equals(rejected)).findFirst().orElseThrow();selected.methods.add(method);
            check(unprojected(selected),"open model-view flow accepted: "+rejected);
        }
        source.methods.removeIf(m->!m.name.equals("draw"));
        var path=Files.createTempFile("lms-gui-model-view", ".jar");
        try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry(name+".class"));jar.write(encode(source));jar.closeEntry();}
        byte[] immutable=Files.readAllBytes(path);
        var unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);var unsafe=(sun.misc.Unsafe)unsafeField.get(null);
        var graphics=(GuiGraphicsExtractor)unsafe.allocateInstance(GuiGraphicsExtractor.class);
        var nativeState=new GuiRenderState();set(graphics,"guiRenderState",nativeState);set(graphics,"pose",new Matrix3x2fStack(16).translate(11,13).scale(2));
        var baseline=new Matrix3x2f(graphics.pose());
        Class<?> scissor=Class.forName("net.minecraft.client.gui.GuiGraphicsExtractor$ScissorStack");var constructor=scissor.getDeclaredConstructor(ScreenRectangle.class);constructor.setAccessible(true);set(graphics,"scissorStack",constructor.newInstance(new ScreenRectangle(0,0,500,500)));
        try(var loader=new ManagedLegacyClassLoader(path,GuiModelViewFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var type=loader.linkOwnedClass(name.replace('/','.'));var draw=type.getMethod("draw",Runnable.class);
            Forge1192WidgetGraphicsBridge.extract((pose,x,y,partial)->invoke(draw,()->Forge1192TooltipGuiBridge.fill(new PoseStack(),0,0,1,1,-1)),graphics,0,0,0);
            var elements=new ArrayList<GuiElementRenderState>();nativeState.forEachElement(elements::add,GuiRenderState.TraverseRange.ALL);
            check(elements.size()==4,"source draw count changed");
            var transformed=new Matrix3x2f(baseline).translate(7.25F,9.5F).scale(2,3);
            for(int i=0;i<4;i++) {
                var point=((LegacyGuiBuffer.QuadState)elements.get(i)).points().getFirst();
                var expected=(i==1||i==2?transformed:baseline).transformPosition(new Vector2f(1,0));
                check(point.x()==expected.x&&point.y()==expected.y,"pending/applied/pop snapshot order changed: "+i);
            }
            check(graphics.pose().equals(baseline),"successful model-view leaked native pose");
            var failure=new IllegalArgumentException("expected-source-draw-failure");
            var drawCalls=new java.util.concurrent.atomic.AtomicInteger();
            try{Forge1192WidgetGraphicsBridge.extract((pose,x,y,partial)->invoke(draw,()->{if(drawCalls.incrementAndGet()==2)throw failure;}),graphics,0,0,0);throw new AssertionError("callback failure swallowed");}
            catch(IllegalArgumentException expected){check(expected==failure,"callback failure replaced by balance validation");}
            check(graphics.pose().equals(baseline),"failed model-view leaked native pose");
            check(Arrays.equals(immutable,Files.readAllBytes(path)),"source JAR modified");
        }finally{Files.deleteIfExists(path);}
        expect(()->Forge1192GuiModelViewBridge.stack(),"global operation allowed outside GUI extraction");
        expect(()->Forge1192WidgetGraphicsBridge.extract((pose,x,y,partial)->Forge1192GuiModelViewBridge.stack().pushPose(),graphics,0,0,0),"unbalanced stack accepted");
        expect(()->Forge1192WidgetGraphicsBridge.extract((pose,x,y,partial)->{var stack=Forge1192GuiModelViewBridge.stack();stack.pushPose();stack.translate(0,0,1);Forge1192GuiModelViewBridge.apply();},graphics,0,0,0),"source global Z silently discarded");
        expect(()->Forge1192WidgetGraphicsBridge.extract((pose,x,y,partial)->{var stack=Forge1192GuiModelViewBridge.stack();stack.pushPose();stack.scale(2,3,1);Forge1192GuiModelViewBridge.apply();Forge1192WidgetGraphicsBridge.extract((p,a,b,t)->{},graphics,0,0,0);},graphics,0,0,0),"nested active model-view admitted");
        check(graphics.pose().equals(baseline),"rejection cleanup leaked native pose");
        callableNegative();
        System.out.println("PASS: transformed source local model-view transaction, native immutable quad snapshots, pending-vs-applied state, source pop/apply order, baseline composition, balance/failure/nesting/Z/scope boundaries and escaped/observed/callable rejection");
    }
    private static void callableNegative() {
        var node=new ClassNode();node.version=Opcodes.V17;node.access=Opcodes.ACC_PUBLIC;node.name="fixture/ModelViewHandle";node.superName="java/lang/Object";
        var method=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"handle","()Ljava/lang/invoke/MethodHandle;",null,null);method.instructions.add(new LdcInsnNode(new Handle(Opcodes.H_INVOKESTATIC,"com/mojang/blaze3d/systems/RenderSystem","m_157191_","()Lcom/mojang/blaze3d/vertex/PoseStack;",false)));method.instructions.add(new InsnNode(Opcodes.ARETURN));node.methods.add(method);
        check(unprojected(node),"callable global stack admitted without local proof");
    }
    private static boolean unprojected(ClassNode source) {
        var result=new ClassNode();new ClassReader(new Forge1192LegacyClassTransformer().transform(source.name.replace('/','.'),encode(source)).bytes()).accept(result,0);
        return result.methods.stream().flatMap(m->Arrays.stream(m.instructions.toArray())).noneMatch(i->i instanceof MethodInsnNode call&&call.owner.equals("com/fuckingdeveloper/lms/profile/forge1192/runtime/render/Forge1192GuiModelViewBridge"));
    }
    private static byte[] encode(ClassNode node){var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();}
    private static void set(Object value,String name,Object fieldValue)throws Exception{var field=value.getClass().getDeclaredField(name);field.setAccessible(true);field.set(value,fieldValue);}
    private static void invoke(Method method,Runnable draw){try{method.invoke(null,draw);}catch(InvocationTargetException failure){if(failure.getCause() instanceof RuntimeException runtime)throw runtime;throw new AssertionError(failure.getCause());}catch(ReflectiveOperationException failure){throw new AssertionError(failure);}}
    private static void expect(Runnable body,String message){try{body.run();throw new AssertionError(message);}catch(UnsupportedOperationException|IllegalStateException expected){}}
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
