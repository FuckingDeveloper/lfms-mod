package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.render.*;
import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.render.TextureSetup;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

public final class ContainerGraphicsCheck {
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    private static void stateFamily() {
        String rs="com/mojang/blaze3d/systems/RenderSystem";
        var operations=new java.util.LinkedHashMap<String,String>();
        operations.put("m_157179_(ILnet/minecraft/resources/ResourceLocation;)V","shaderTexture");
        operations.put("m_157456_(ILnet/minecraft/resources/ResourceLocation;)V","shaderTexture");
        operations.put("setShaderColor(FFFF)V","shaderColor");
        operations.put("enableScissor(IIII)V","enableScissor");operations.put("disableScissor()V","disableScissor");
        operations.put("colorMask(ZZZZ)V","colorMask");operations.put("setShader(Ljava/util/function/Supplier;)V","setShader");
        for(String name:List.of("enableBlend","disableBlend","defaultBlendFunc","enableDepthTest","disableDepthTest","enableTexture","disableTexture"))operations.put(name+"()V",name);
        for(String name:List.of("enableBlend","disableBlend","enableDepthTest","disableDepthTest","enableTexture","disableTexture"))operations.put("com/mojang/blaze3d/platform/GlStateManager|_"+name+"()V",name);
        for(String name:List.of("getWidth","getHeight","getGuiScaledWidth","getGuiScaledHeight"))operations.put("com/mojang/blaze3d/platform/Window|"+name+"()I",name);
        for(String name:List.of("getWidth","getHeight"))operations.put("com/mojang/blaze3d/platform/NativeImage|"+name+"()I",name);
        operations.put("com/mojang/blaze3d/platform/NativeImage|read(Ljava/io/InputStream;)Lcom/mojang/blaze3d/platform/NativeImage;","read");
        String factors="Lcom/mojang/blaze3d/platform/GlStateManager$SourceFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DestFactor;";
        operations.put("m_69408_("+factors+")V","blendFunc");operations.put("m_69416_("+factors+factors+")V","blendFuncSeparate");
        for(var operation:operations.entrySet()) {
            int separator=operation.getKey().indexOf('|');String owner=separator<0?rs:operation.getKey().substring(0,separator),identity=operation.getKey().substring(separator+1);
            int split=identity.indexOf('(');String name=identity.substring(0,split),desc=identity.substring(split);
            boolean nativeOperation=owner.endsWith("/Window")||owner.endsWith("/NativeImage");
            boolean virtual=nativeOperation&&!name.equals("read");
            String parameters=virtual?"(L"+owner+";"+desc.substring(1):desc;
            String probeDescriptor=Type.getMethodDescriptor(Type.VOID_TYPE,Type.getArgumentTypes(parameters));
            String expectedOwner=nativeOperation?owner:operation.getValue().startsWith("blendFunc")?"com/fuckingdeveloper/lms/runtime/render/LegacyGuiBlend":java.util.Set.of("shaderTexture","shaderColor","enableScissor","disableScissor").contains(operation.getValue())?"com/fuckingdeveloper/lms/runtime/render/Forge1192TooltipGuiBridge":"com/fuckingdeveloper/lms/runtime/render/Forge1192ContainerGraphicsBridge";
            var fixture=new ClassNode();fixture.version=Opcodes.V17;fixture.access=Opcodes.ACC_PUBLIC;fixture.name="fixture/GuiStateFamily";fixture.superName="java/lang/Object";
            var method=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"calls",probeDescriptor,null,null);fixture.methods.add(method);
            int local=0;for(Type type:Type.getArgumentTypes(parameters)){method.instructions.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD),local));local+=type.getSize();}
            method.instructions.add(new MethodInsnNode(virtual?Opcodes.INVOKEVIRTUAL:Opcodes.INVOKESTATIC,owner,name,desc,false));
            if(Type.getReturnType(desc)!=Type.VOID_TYPE)method.instructions.add(new InsnNode(Opcodes.POP));
            var handle=new Handle(virtual?Opcodes.H_INVOKEVIRTUAL:Opcodes.H_INVOKESTATIC,owner,name,desc,false);
            method.instructions.add(new LdcInsnNode(handle));method.instructions.add(new InsnNode(Opcodes.POP));
            method.instructions.add(new InvokeDynamicInsnNode("call","()Ljava/lang/Runnable;",new Handle(Opcodes.H_INVOKESTATIC,"java/lang/invoke/LambdaMetafactory","metafactory","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",false),Type.getMethodType(desc),handle,Type.getMethodType(desc)));
            method.instructions.add(new InsnNode(Opcodes.POP));
            var nested=new ConstantDynamic("nested","Ljava/lang/invoke/MethodHandle;",new Handle(Opcodes.H_INVOKESTATIC,"java/lang/invoke/ConstantBootstraps","explicitCast","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;",false),handle);
            method.instructions.add(new LdcInsnNode(nested));method.instructions.add(new InsnNode(Opcodes.POP));method.instructions.add(new InsnNode(Opcodes.RETURN));
            var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);fixture.accept(writer);var result=new ClassNode();new ClassReader(new Forge1192LegacyClassTransformer().transform(fixture.name.replace('/','.'),writer.toByteArray()).bytes()).accept(result,0);
            int references=0;
            for(var instruction:result.methods.getFirst().instructions){
                Handle projected=null;
                if(instruction instanceof MethodInsnNode call){references++;check(call.owner.equals(expectedOwner)&&call.name.equals(operation.getValue()),"GUI state direct call drift: "+operation.getKey());}
                else if(instruction instanceof LdcInsnNode ldc){if(ldc.cst instanceof Handle h)projected=h;else if(ldc.cst instanceof ConstantDynamic c)projected=(Handle)c.getBootstrapMethodArgument(0);}
                else if(instruction instanceof InvokeDynamicInsnNode indy)projected=(Handle)indy.bsmArgs[1];
                if(projected!=null){references++;check(projected.getOwner().equals(expectedOwner)&&projected.getName().equals(operation.getValue()),"GUI state handle drift: "+operation.getKey());}
            }
            check(references==4,"GUI state encoding coverage incomplete");
        }
        var resolver=com.fuckingdeveloper.lms.mapping.Forge1192SrgRuntimeResolver.load(Path.of(".")).orElseThrow();
        check(resolver.canonicalMethodName(rs,"m_157179_","(ILnet/minecraft/resources/ResourceLocation;)V").orElseThrow().equals("_setShaderTexture"),"internal source binder identity guessed");
    }
    private static void blendFamily() {
        var src=LegacyGuiBlend.SourceFactor.SRC_ALPHA;var dst=LegacyGuiBlend.DestFactor.ONE_MINUS_SRC_ALPHA;
        check(LegacyGuiBlend.SourceFactor.values().length==15&&LegacyGuiBlend.DestFactor.values().length==14,"source factor enum identity/order lost");
        check(src.ordinal()==11&&src.value==770&&src.f_84751_==770&&dst.value==771&&dst.f_84646_==771,"source factor ordinal/numeric ABI changed");
        var replaceAlpha=LegacyGuiBlend.equation(src,dst,LegacyGuiBlend.SourceFactor.ONE,LegacyGuiBlend.DestFactor.ZERO);
        var retainAlpha=LegacyGuiBlend.equation(src,dst,LegacyGuiBlend.SourceFactor.ZERO,LegacyGuiBlend.DestFactor.ONE);
        check(replaceAlpha.equals(LegacyGuiBlend.DEFAULT)&&!replaceAlpha.equals(com.mojang.renderpearl.api.pipeline.BlendFunction.TRANSLUCENT),"source default alpha replaced with target default");
        for(boolean texture:new boolean[]{false,true})for(int mask=0;mask<16;mask++) {
            var first=LegacyGuiBuffer.pipeline(texture,Optional.of(replaceAlpha),mask);var second=LegacyGuiBuffer.pipeline(texture,Optional.of(retainAlpha),mask);
            check(first!=second&&first.getColorTargetStates().getFirst().blendFunction().orElseThrow().equals(replaceAlpha)&&second.getColorTargetStates().getFirst().blendFunction().orElseThrow().equals(retainAlpha),"separate alpha equations share or mutate submitted pipeline");
            check(first==LegacyGuiBuffer.pipeline(texture,Optional.of(replaceAlpha),mask),"native blend pipeline cache lost identity");
        }
        for(var source:LegacyGuiBlend.SourceFactor.values())for(var destination:LegacyGuiBlend.DestFactor.values()) {
            boolean constant=source.name().contains("CONSTANT")||destination.name().contains("CONSTANT");
            try{var equation=LegacyGuiBlend.equation(source,destination,source,destination);check(!constant,"unproven blend constant accepted");check(equation.color().equals(equation.alpha()),"two-factor blend lost alpha equation");}
            catch(UnsupportedOperationException expected){check(constant,"supported factor rejected");}
        }
        try{LegacyGuiBlend.blendFunc(src,dst);throw new AssertionError("unscoped blend state accepted");}catch(IllegalStateException expected){}
    }
    public static void run() throws Exception {
        stateFamily();
        blendFamily();
        check(Forge1192ContainerGraphicsBridge.sourceTextColor(0x404040)==0xFF404040 && Forge1192ContainerGraphicsBridge.sourceTextColor(0x02404040)==0xFF404040 && Forge1192ContainerGraphicsBridge.sourceTextColor(0x80404040)==0x80404040,"source font alpha normalization lost");
        var points=new ArrayList<LegacyGuiBuffer.Point>();
        points.add(new LegacyGuiBuffer.Point(10.25F,20.5F,0,0.1F,0.8F,0x80112233));
        points.add(new LegacyGuiBuffer.Point(30.75F,20.5F,0,0.9F,0.8F,0xFF445566));
        points.add(new LegacyGuiBuffer.Point(30.75F,40.25F,0,0.9F,0.2F,-1));
        points.add(new LegacyGuiBuffer.Point(10.25F,40.25F,0,0.1F,0.2F,-1));
        var bounds=LegacyGuiBuffer.bounds(points);
        check(bounds.left()==10 && bounds.top()==20 && bounds.width()==21 && bounds.height()==21,"fractional quad bounds lost");
        var state=new LegacyGuiBuffer.QuadState(points,RenderPipelines.GUI,TextureSetup.noTexture(),null,bounds);
        points.clear();check(state.points().size()==4 && state.points().getFirst().u()==0.1F && state.points().getFirst().color()==0x80112233,"deferred source data aliases mutable batch");
        for(String receiver:List.of("com/mojang/blaze3d/vertex/BufferBuilder","fixture/UnrelatedVertexHelper")){
            var fixture=new ClassNode();fixture.version=Opcodes.V17;fixture.access=Opcodes.ACC_PUBLIC;fixture.name="fixture/InheritedVertex";fixture.superName="java/lang/Object";
            var method=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"vertex","(L"+receiver+";)Lcom/mojang/blaze3d/vertex/VertexConsumer;",null,null);fixture.methods.add(method);
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));for(int i=0;i<3;i++)method.instructions.add(new InsnNode(Opcodes.DCONST_0));method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,receiver,"m_5483_","(DDD)Lcom/mojang/blaze3d/vertex/VertexConsumer;",false));method.instructions.add(new InsnNode(Opcodes.ARETURN));
            var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);fixture.accept(writer);var projected=new ClassNode();new ClassReader(new Forge1192LegacyClassTransformer().transform(fixture.name.replace('/','.'),writer.toByteArray()).bytes()).accept(projected,0);
            MethodInsnNode operation=null;for(var instruction:projected.methods.getFirst().instructions)if(instruction instanceof MethodInsnNode call)operation=call;
            check(operation!=null&&(receiver.startsWith("com/mojang/")?operation.owner.equals("com/fuckingdeveloper/lms/runtime/render/Forge1192VertexConsumerBridge")&&operation.name.equals("addVertex"):operation.owner.equals(receiver)&&operation.name.equals("m_5483_")),"inherited vertex rule lost owner identity or descriptor-only negative");
        }
        var escaped=new ClassNode();escaped.name="fixture/EscapedGuiBatch";escaped.superName="java/lang/Object";escaped.version=Opcodes.V17;escaped.access=Opcodes.ACC_PUBLIC;
        var escapeMethod=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"escaped","()Lcom/mojang/blaze3d/vertex/Tesselator;",null,null);
        escapeMethod.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"com/mojang/blaze3d/vertex/Tesselator","m_85913_","()Lcom/mojang/blaze3d/vertex/Tesselator;",false));escapeMethod.instructions.add(new InsnNode(Opcodes.ARETURN));escaped.methods.add(escapeMethod);
        var escapeWriter=new ClassWriter(ClassWriter.COMPUTE_MAXS);escaped.accept(escapeWriter);
        var escapeMapped=new Forge1192LegacyClassTransformer().transform(escaped.name.replace('/','.'),escapeWriter.toByteArray()).bytes();
        var escapeNode=new ClassNode();new ClassReader(escapeMapped).accept(escapeNode,0);
        check(((MethodInsnNode)escapeNode.methods.getFirst().instructions.getFirst()).owner.equals("com/mojang/blaze3d/vertex/Tesselator"),"escaped source return replaced with incompatible GUI batch type");
        var binding=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"binding","(Lnet/minecraft/client/renderer/texture/AbstractTexture;)V",null,null);
        binding.instructions.add(new InsnNode(Opcodes.ICONST_0));binding.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
        binding.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"net/minecraft/client/renderer/texture/AbstractTexture","m_117963_","()I",false));
        binding.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"com/mojang/blaze3d/systems/RenderSystem","m_157453_","(II)V",false));binding.instructions.add(new InsnNode(Opcodes.RETURN));escaped.methods.add(binding);
        escapeWriter=new ClassWriter(ClassWriter.COMPUTE_MAXS);escaped.accept(escapeWriter);escapeMapped=new Forge1192LegacyClassTransformer().transform(escaped.name.replace('/','.'),escapeWriter.toByteArray()).bytes();escapeNode=new ClassNode();new ClassReader(escapeMapped).accept(escapeNode,0);
        var bound=escapeNode.methods.get(1);int calls=0;for(var instruction:bound.instructions)if(instruction instanceof MethodInsnNode call){calls++;check(call.owner.equals("com/fuckingdeveloper/lms/runtime/render/LegacyGuiTextureHandles")&&call.desc.equals("(ILnet/minecraft/client/renderer/texture/AbstractTexture;)V"),"texture pair discarded receiver or invented GPU id");}
        check(calls==1,"source texture ID lookup retained before native object binding");
        var quad=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"quad","(Lcom/mojang/blaze3d/vertex/PoseStack;)V",null,null);
        quad.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));quad.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"com/mojang/blaze3d/vertex/PoseStack","m_85850_","()Lcom/mojang/blaze3d/vertex/PoseStack$Pose;",false));
        quad.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"com/mojang/blaze3d/vertex/PoseStack$Pose","m_85861_","()Lcom/mojang/math/Matrix4f;",false));quad.instructions.add(new VarInsnNode(Opcodes.ASTORE,1));
        quad.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"com/mojang/blaze3d/vertex/Tesselator","m_85913_","()Lcom/mojang/blaze3d/vertex/Tesselator;",false));quad.instructions.add(new VarInsnNode(Opcodes.ASTORE,2));
        quad.instructions.add(new VarInsnNode(Opcodes.ALOAD,2));quad.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"com/mojang/blaze3d/vertex/Tesselator","m_85915_","()Lcom/mojang/blaze3d/vertex/BufferBuilder;",false));quad.instructions.add(new VarInsnNode(Opcodes.ASTORE,3));
        quad.instructions.add(new VarInsnNode(Opcodes.ALOAD,3));quad.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));for(int i=0;i<3;i++)quad.instructions.add(new InsnNode(Opcodes.FCONST_0));
        quad.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"com/mojang/blaze3d/vertex/BufferBuilder","m_85982_","(Lcom/mojang/math/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",false));quad.instructions.add(new InsnNode(Opcodes.POP));
        quad.instructions.add(new VarInsnNode(Opcodes.ALOAD,2));quad.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"com/mojang/blaze3d/vertex/Tesselator","m_85914_","()V",false));quad.instructions.add(new InsnNode(Opcodes.RETURN));escaped.methods.add(quad);
        escapeWriter=new ClassWriter(ClassWriter.COMPUTE_MAXS);escaped.accept(escapeWriter);escapeMapped=new Forge1192LegacyClassTransformer().transform(escaped.name.replace('/','.'),escapeWriter.toByteArray()).bytes();escapeNode=new ClassNode();new ClassReader(escapeMapped).accept(escapeNode,0);
        boolean nativePose=false,nativeVertex=false;for(var instruction:escapeNode.methods.get(2).instructions)if(instruction instanceof MethodInsnNode call){nativePose|=call.name.equals("pose")&&call.desc.equals("()Lorg/joml/Matrix4f;");nativeVertex|=call.name.equals("vertex")&&call.desc.equals("(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;");}
        check(nativePose&&nativeVertex,"source pose getter and GUI vertex consumer disagree on native matrix representation");
        var shaderFixture = new ClassNode();
        shaderFixture.name="fixture/GuiShaderReferences"; shaderFixture.superName="java/lang/Object"; shaderFixture.version=Opcodes.V17; shaderFixture.access=Opcodes.ACC_PUBLIC;
        var shaderMethod = new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"references","()V",null,null);
        String[] shaderNames={"getPositionColorShader","getPositionTexShader","getPositionTexColorShader"};
        for(String name:shaderNames){
            var handle=new Handle(Opcodes.H_INVOKESTATIC,"net/minecraft/client/renderer/GameRenderer",name,"()Lnet/minecraft/client/renderer/ShaderInstance;",false);
            shaderMethod.instructions.add(new LdcInsnNode(handle)); shaderMethod.instructions.add(new InsnNode(Opcodes.POP));
            shaderMethod.instructions.add(new InvokeDynamicInsnNode("get","()Ljava/util/function/Supplier;",
                    new Handle(Opcodes.H_INVOKESTATIC,"java/lang/invoke/LambdaMetafactory","metafactory","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",false),
                    Type.getMethodType("()Ljava/lang/Object;"),handle,Type.getMethodType(handle.getDesc())));
            shaderMethod.instructions.add(new InsnNode(Opcodes.POP));
        }
        shaderMethod.instructions.add(new InsnNode(Opcodes.RETURN)); shaderFixture.methods.add(shaderMethod);
        var shaderWriter=new ClassWriter(ClassWriter.COMPUTE_MAXS); shaderFixture.accept(shaderWriter);
        var shaderMapped=new ClassNode(); new ClassReader(new Forge1192LegacyClassTransformer().transform(shaderFixture.name.replace('/','.'),shaderWriter.toByteArray()).bytes()).accept(shaderMapped,0);
        int shaderReferences=0;
        for(var instruction:shaderMapped.methods.getFirst().instructions){
            Handle handle=null;
            if(instruction instanceof LdcInsnNode ldc && ldc.cst instanceof Handle h)handle=h;
            if(instruction instanceof InvokeDynamicInsnNode indy)handle=(Handle)indy.bsmArgs[1];
            if(handle!=null){shaderReferences++;check(handle.getOwner().equals("com/fuckingdeveloper/lms/runtime/render/Forge1192ContainerGraphicsBridge")&&handle.getDesc().equals("()Lcom/fuckingdeveloper/lms/runtime/render/LegacyGuiShader;"),"source GUI shader method reference escaped semantic adapter");}
        }
        check(shaderReferences==6,"shader reference family probe incomplete");
        var source=new ClassNode();source.name="fixture/GuiPhaseParent";source.superName="net/minecraft/client/gui/screens/inventory/AbstractContainerScreen";source.version=Opcodes.V17;source.access=Opcodes.ACC_PUBLIC;
        source.fields.add(new FieldNode(Opcodes.ACC_PUBLIC,"calls","I",null,null));
        String pose="Lcom/mojang/blaze3d/vertex/PoseStack;";
        for(boolean background:new boolean[]{true,false}){
            var method=new MethodNode(Opcodes.ACC_PROTECTED,background?"m_7286_":"m_7027_",background?"("+pose+"FII)V":"("+pose+"II)V",null,null);
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));method.instructions.add(new InsnNode(Opcodes.DUP));method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,source.name,"calls","I"));
            method.instructions.add(new IntInsnNode(Opcodes.BIPUSH,background?1:10));method.instructions.add(new InsnNode(Opcodes.IADD));method.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD,source.name,"calls","I"));method.instructions.add(new InsnNode(Opcodes.RETURN));source.methods.add(method);
        }
        var child=new ClassNode();child.name="fixture/GuiPhaseChild";child.superName=source.name;child.version=Opcodes.V17;child.access=Opcodes.ACC_PUBLIC;
        var bg=new MethodNode(Opcodes.ACC_PROTECTED,"m_7286_","("+pose+"FII)V",null,null);
        bg.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));bg.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));bg.instructions.add(new VarInsnNode(Opcodes.FLOAD,2));bg.instructions.add(new VarInsnNode(Opcodes.ILOAD,3));bg.instructions.add(new VarInsnNode(Opcodes.ILOAD,4));bg.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,source.name,"m_7286_",bg.desc,false));
        bg.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));bg.instructions.add(new InsnNode(Opcodes.DUP));bg.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,source.name,"calls","I"));bg.instructions.add(new IntInsnNode(Opcodes.BIPUSH,2));bg.instructions.add(new InsnNode(Opcodes.IADD));bg.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD,source.name,"calls","I"));bg.instructions.add(new InsnNode(Opcodes.RETURN));child.methods.add(bg);
        var path=Files.createTempFile("lms-gui-phase", ".jar");
        try(var jar=new JarOutputStream(Files.newOutputStream(path))){for(var node:List.of(source,child)){var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);jar.putNextEntry(new JarEntry(node.name+".class"));jar.write(writer.toByteArray());jar.closeEntry();}}
        var original=Files.readAllBytes(path);
        try(var loader=new ManagedLegacyClassLoader(path,ContainerGraphicsCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())){
            var type=loader.linkOwnedClass(child.name.replace('/','.'));
            var unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);var unsafe=(sun.misc.Unsafe)unsafeField.get(null);
            var screen=(LegacyContainerGraphics)unsafe.allocateInstance(type); // State/callback-only probe; no fabricated UI lifecycle.
            screen.lms$background(new PoseStack(),0.5F,3,5);screen.lms$labels(new PoseStack(),3,5);
            check(type.getField("calls").getInt(screen)==13,"source inherited/super callback duplicated or lost");
            type.getMethod("m_93250_",int.class).invoke(screen,7);check((int)type.getMethod("m_93252_").invoke(screen)==7,"source blit-offset live storage");
        }
        check(Arrays.equals(original,Files.readAllBytes(path)),"source GUI fixture mutated");
        try{LegacyGuiTesselator.getInstance();throw new AssertionError("unscoped source drawing accepted");}catch(IllegalStateException expected){}
        System.out.println("PASS: immutable native quad state/UV/color/fractional bounds, actual source GUI phase inheritance/super dispatch, source depth storage and unscoped fail-fast; graphics/window correctness requires real client");
    }
}
