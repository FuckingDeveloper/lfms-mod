package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.runtime.render.*;
import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

final class ContainerRenderLifecycleCheck implements Opcodes {
    static void run() throws Exception {
        String base="net/minecraft/client/gui/screens/inventory/AbstractContainerScreen",pose="Lcom/mojang/blaze3d/vertex/PoseStack;",full="("+pose+"IIF)V";
        var parent=node("fixture/FullRenderParent",base);parent.fields.add(new FieldNode(ACC_PUBLIC,"calls","I",null,null));
        var render=new MethodNode(ACC_PUBLIC,"m_6305_",full,null,null);
        render.instructions.add(new VarInsnNode(ALOAD,0));render.instructions.add(new InsnNode(ICONST_1));render.instructions.add(new FieldInsnNode(PUTFIELD,parent.name,"calls","I"));render.instructions.add(new InsnNode(RETURN));parent.methods.add(render);
        var child=node("fixture/FullRenderChild",parent.name);
        var method=new MethodNode(ACC_PUBLIC,"m_6305_",full,null,null);
        for(int i=0;i<4;i++)method.instructions.add(new VarInsnNode(i<2?ALOAD:ILOAD,i));method.instructions.add(new VarInsnNode(FLOAD,4));
        method.instructions.add(new MethodInsnNode(INVOKESPECIAL,parent.name,"m_6305_",full,false));
        method.instructions.add(new VarInsnNode(ALOAD,0));method.instructions.add(new InsnNode(DUP));method.instructions.add(new FieldInsnNode(GETFIELD,parent.name,"calls","I"));method.instructions.add(new IntInsnNode(BIPUSH,10));method.instructions.add(new InsnNode(IADD));method.instructions.add(new FieldInsnNode(PUTFIELD,parent.name,"calls","I"));method.instructions.add(new InsnNode(RETURN));child.methods.add(method);
        for(String name:List.of("m_7025_","m_96597_","m_96617_")) {
            String desc="("+pose+(name.equals("m_7025_")?"":"Ljava/util/List;")+"II)V";
            var override=new MethodNode(ACC_PUBLIC,name,desc,null,null);
            override.instructions.add(new VarInsnNode(ALOAD,0));override.instructions.add(new InsnNode(DUP));override.instructions.add(new FieldInsnNode(GETFIELD,parent.name,"calls","I"));override.instructions.add(new InsnNode(ICONST_1));override.instructions.add(new InsnNode(IADD));override.instructions.add(new FieldInsnNode(PUTFIELD,parent.name,"calls","I"));override.instructions.add(new InsnNode(RETURN));child.methods.add(override);
        }
        var path=Files.createTempFile("lms-full-render", ".jar");
        try(var jar=new JarOutputStream(Files.newOutputStream(path))){for(var node:List.of(parent,child)){jar.putNextEntry(new JarEntry(node.name+".class"));jar.write(bytes(node));jar.closeEntry();}}
        var immutable=Files.readAllBytes(path);
        try(var loader=new ManagedLegacyClassLoader(path,ContainerRenderLifecycleCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())){
            var type=loader.linkOwnedClass(child.name.replace('/','.'));
            var field=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);var unsafe=(sun.misc.Unsafe)field.get(null);
            var source=(LegacyContainerRender)unsafe.allocateInstance(type);
            var graphics=(net.minecraft.client.gui.GuiGraphicsExtractor)unsafe.allocateInstance(net.minecraft.client.gui.GuiGraphicsExtractor.class);
            type.getMethod("extractRenderState",graphics.getClass(),int.class,int.class,float.class).invoke(source,graphics,7,9,0.25F);
            check(type.getField("calls").getInt(source)==11,"native extraction lost/duplicated source virtual/super callbacks");
            var nativeScreen=(net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?>)source;
            Forge1192ContainerGraphicsBridge.tooltip(nativeScreen,new PoseStack(),7,9);
            Forge1192ContainerGraphicsBridge.componentTooltip(nativeScreen,new PoseStack(),List.of(),7,9);
            Forge1192ContainerGraphicsBridge.formattedTooltip(nativeScreen,new PoseStack(),List.of(),7,9);
            check(type.getField("calls").getInt(source)==14,"native-owner tooltip calls lost dynamic source overrides");
            var expected=new IllegalStateException("render-probe");
            try{Forge1192ContainerGraphicsBridge.render((p,x,y,f)->{check(x==7&&y==9&&f==0.25F,"arguments changed");Forge1192ContainerGraphicsBridge.defaultBackground(p);throw expected;},graphics,7,9,0.25F);throw new AssertionError("exception swallowed");}
            catch(IllegalStateException actual){check(actual==expected,"exception replaced");}
            try{Forge1192ContainerGraphicsBridge.defaultBackground(new PoseStack());throw new AssertionError("render scope leaked");}catch(IllegalStateException boundary){}
        }
        check(Arrays.equals(immutable,Files.readAllBytes(path)),"source binary mutated");
        var probe=node("fixture/FullRenderBaseCall",base);var source=new MethodNode(ACC_PUBLIC,"m_6305_",full,null,null);
        for(int i=0;i<4;i++)source.instructions.add(new VarInsnNode(i<2?ALOAD:ILOAD,i));source.instructions.add(new VarInsnNode(FLOAD,4));source.instructions.add(new MethodInsnNode(INVOKESPECIAL,base,"m_6305_",full,false));source.instructions.add(new InsnNode(RETURN));probe.methods.add(source);
        var mapped=new ClassNode();new ClassReader(new Forge1192LegacyClassTransformer().transform(probe.name.replace('/','.'),bytes(probe)).bytes()).accept(mapped,0);
        check(mapped.methods.stream().flatMap(m->java.util.stream.StreamSupport.stream(m.instructions.spliterator(),false)).anyMatch(i->i instanceof MethodInsnNode c&&c.name.equals("defaultRender")&&c.getOpcode()==INVOKESTATIC),"native source-super call escaped");
        Path artifact=Path.of("legacy-mods/IC2Classic-1.19.2-2.1.3.4.jar");
        if(Files.exists(artifact))try(var jar=new JarFile(artifact.toFile())) {
            String owner="ic2/core/inventory/gui/IC2Screen";byte[] original;
            try(var input=jar.getInputStream(jar.getJarEntry(owner+".class"))){original=input.readAllBytes();}
            var actual=new ClassNode();new ClassReader(new Forge1192LegacyClassTransformer().transform(owner.replace('/','.'),original).bytes()).accept(actual,0);
            var operations=new java.util.HashSet<String>();
            for(var m:actual.methods)for(var i:m.instructions)if(i instanceof MethodInsnNode c && c.owner.equals("com/fuckingdeveloper/lms/runtime/render/Forge1192ContainerGraphicsBridge"))operations.add(c.name);
            check(operations.containsAll(List.of("render","defaultRender","tooltip","formattedTooltip")),"original full render/tooltip family incomplete: "+operations);
        }
        System.out.println("PASS: full source container native extraction entry, inherited virtual/special order, arguments, exception/scope restoration and original binary immutability; actual slots/tooltip visuals require client");
    }
    private static ClassNode node(String name,String parent){var n=new ClassNode();n.version=V17;n.access=ACC_PUBLIC;n.name=name;n.superName=parent;return n;}
    private static byte[] bytes(ClassNode node){var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(w);return w.toByteArray();}
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
