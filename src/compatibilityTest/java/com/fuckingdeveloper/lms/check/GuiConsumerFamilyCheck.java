package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.runtime.render.*;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

public final class GuiConsumerFamilyCheck {
    public static void run() throws Exception {
        for(boolean texture:new boolean[]{false,true})for(boolean blend:new boolean[]{false,true})for(int mask=0;mask<16;mask++){
            var pipeline=LegacyGuiBuffer.pipeline(texture,blend,mask);var color=pipeline.getColorTargetStates().getFirst();
            check(color.writeMask()==mask,"source mask channel lost");check(color.blendFunction().isPresent()==blend,"source blend semantics lost");
            check(LegacyGuiBuffer.pipeline(texture,blend,mask)==pipeline,"bounded pipeline cache does not reuse identity");
        }
        check(!LegacyGuiBuffer.pipeline(false,true,ColorTargetState.WRITE_COLOR).getColorTargetStates().getFirst().writeAlpha(),"RGB highlight writes alpha");
        var root=screen("fixture/SourceRenderScreen","net/minecraft/client/gui/screens/Screen",true);
        var child=screen("fixture/OverriddenRenderScreen",root.name,false);
        var caller=new ClassNode();caller.version=Opcodes.V17;caller.access=Opcodes.ACC_PUBLIC;caller.name="fixture/Consumer";caller.superName="java/lang/Object";
        var body=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"draw","(L"+root.name+";Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/gui/Font;)V",null,null);
        body.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"com/mojang/blaze3d/platform/Lighting","m_84931_","()V",false));
        body.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));body.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,root.name,"renderer","()Lnet/minecraft/client/renderer/entity/ItemRenderer;",false));body.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));body.instructions.add(new InsnNode(Opcodes.ICONST_0));body.instructions.add(new InsnNode(Opcodes.ICONST_0));body.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"net/minecraft/client/renderer/entity/ItemRenderer","m_115203_","(Lnet/minecraft/world/item/ItemStack;II)V",false));
        for(int i=0;i<4;i++)body.instructions.add(new InsnNode(i==3?Opcodes.ICONST_0:Opcodes.ICONST_1));body.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"com/mojang/blaze3d/systems/RenderSystem","m_69444_","(ZZZZ)V",false));body.instructions.add(new InsnNode(Opcodes.RETURN));caller.methods.add(body);
        var transformer=new Forge1192LegacyClassTransformer();transformer.bindManagedClassBytes(name->name.equals(root.name)?bytes(root):null);
        var output=new ClassNode();new ClassReader(transformer.transform(caller.name.replace('/','.'),bytes(caller)).bytes()).accept(output,0);
        var mappedCalls=java.util.stream.StreamSupport.stream(output.methods.getFirst().instructions.spliterator(),false).filter(i->i instanceof MethodInsnNode).map(i->(MethodInsnNode)i).toList();
        check(mappedCalls.stream().anyMatch(c->c.name.equals("screenRenderer")&&c.owner.endsWith("Forge1192GuiItemExtractionBridge")),"proven source getter not scoped");
        check(mappedCalls.stream().anyMatch(c->c.name.equals("renderAndDecorateItem")&&c.owner.endsWith("LegacyTooltipItemRenderer")),"source GUI item consumer unresolved");
        check(mappedCalls.stream().anyMatch(c->c.name.equals("setupFor3DItems")&&c.owner.endsWith("Forge1192ContainerGraphicsBridge")),"source lighting unresolved");
        check(mappedCalls.stream().anyMatch(c->c.name.equals("colorMask")&&c.owner.endsWith("Forge1192ContainerGraphicsBridge")),"source color mask unresolved");
        var jarPath=Files.createTempFile("lms-gui-getter-dispatch",".jar");
        try(var jar=new JarOutputStream(Files.newOutputStream(jarPath))){for(var node:List.of(root,child)){jar.putNextEntry(new JarEntry(node.name+".class"));jar.write(bytes(node));jar.closeEntry();}}
        try(var loader=new ManagedLegacyClassLoader(jarPath,GuiConsumerFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())){
            var base=loader.linkOwnedClass(root.name.replace('/','.'));var override=loader.linkOwnedClass(child.name.replace('/','.'));
            var field=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);var unsafe=(sun.misc.Unsafe)field.get(null);
            try{Forge1192GuiItemExtractionBridge.screenRenderer(unsafe.allocateInstance(base),base,"renderer");throw new AssertionError("unscoped getter accepted");}catch(IllegalStateException expected){check(expected.getMessage().contains("outside extraction"),"source declaration lookup failed before scope guard");}
            try{Forge1192GuiItemExtractionBridge.screenRenderer(unsafe.allocateInstance(override),base,"renderer");throw new AssertionError("custom virtual getter bypassed");}catch(UnsupportedOperationException expected){}
        }
        var external=Path.of("legacy-mods/IC2Classic-1.19.2-2.1.3.4.jar");
        if(Files.exists(external))try(var jar=new JarFile(external.toFile())){
            var exact=new Forge1192LegacyClassTransformer();exact.bindManagedClassBytes(name->{var entry=jar.getJarEntry(name+".class");if(entry==null)return null;try(var stream=jar.getInputStream(entry)){return stream.readAllBytes();}});
            String filter="ic2/core/inventory/gui/components/simple/FilterComponent";byte[] original;try(var stream=jar.getInputStream(jar.getJarEntry(filter+".class"))){original=stream.readAllBytes();}
            var result=new ClassNode();new ClassReader(exact.transform(filter.replace('/','.'),original).bytes()).accept(result,0);
            var background=result.methods.stream().filter(m->m.name.equals("drawBackground")).findFirst().orElseThrow();
            for(var instruction:background.instructions)if(instruction instanceof MethodInsnNode call){
                check(!call.owner.equals("com/mojang/blaze3d/platform/Lighting"),"external source lighting remained unprojected");
                check(!call.owner.equals("net/minecraft/client/renderer/entity/ItemRenderer"),"external source GUI item/decorations remained unprojected");
                check(!(call.owner.equals("com/mojang/blaze3d/systems/RenderSystem")&&java.util.Set.of("m_69444_","m_69465_","m_69461_").contains(call.name)),"external source mask/depth remained unprojected");
            }
        }
        System.out.println("PASS: GUI lighting/consumer/color-mask projection, all 64 native mask pipelines, unscoped failure and virtual getter override rejection");
    }
    private static ClassNode screen(String name,String parent,boolean field){var node=new ClassNode();node.version=Opcodes.V17;node.access=Opcodes.ACC_PUBLIC;node.name=name;node.superName=parent;var getter=new MethodNode(Opcodes.ACC_PUBLIC,"renderer","()Lnet/minecraft/client/renderer/entity/ItemRenderer;",null,null);if(field){getter.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));getter.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,name,"f_96542_","Lnet/minecraft/client/renderer/entity/ItemRenderer;"));}else getter.instructions.add(new InsnNode(Opcodes.ACONST_NULL));getter.instructions.add(new InsnNode(Opcodes.ARETURN));node.methods.add(getter);return node;}
    private static byte[] bytes(ClassNode node){var out=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(out);return out.toByteArray();}
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
