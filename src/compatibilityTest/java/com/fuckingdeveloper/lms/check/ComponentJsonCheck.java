package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.data.Forge1192ComponentJsonBridge;
import net.minecraft.network.chat.*;
import net.minecraft.ChatFormatting;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

public final class ComponentJsonCheck {
    public static void run() throws Exception {
        var component=Component.translatable("fixture.label",Component.literal("argument").withStyle(ChatFormatting.RED), 7)
                .withStyle(style -> style.withBold(true)).append(Component.literal(" suffix"));
        var json=Forge1192ComponentJsonBridge.toJsonTree(component);
        var parsed=Forge1192ComponentJsonBridge.fromJson(json);
        var expectedComponent=Component.translatable("fixture.label",Component.literal("argument").withStyle(ChatFormatting.RED), "7")
                .withStyle(style -> style.withBold(true)).append(Component.literal(" suffix"));
        if(!expectedComponent.equals(parsed))throw new AssertionError("native component content/style/siblings changed");
        if(!expectedComponent.equals(Forge1192ComponentJsonBridge.fromJson(Forge1192ComponentJsonBridge.toStableJson(component))))throw new AssertionError("stable JSON roundtrip changed component");
        if(Forge1192ComponentJsonBridge.fromJson(com.google.gson.JsonNull.INSTANCE)!=null)throw new AssertionError("source null tree information changed");
        try{Forge1192ComponentJsonBridge.fromJson((String)null);throw new AssertionError("null String no longer fails as source StringReader does");}catch(NullPointerException expected){}
        try{Forge1192ComponentJsonBridge.fromJson("{\"text\":\"x\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"y\"}}");throw new AssertionError("old rich events silently lost");}catch(UnsupportedOperationException expected){}
        var node=new ClassNode();node.name="fixture/ComponentJson";node.superName="java/lang/Object";node.version=Opcodes.V17;node.access=Opcodes.ACC_PUBLIC;
        String desc="(Lnet/minecraft/network/chat/Component;)Ljava/lang/String;";
        var method=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"write",desc,null,null);
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"net/minecraft/network/chat/Component$Serializer","m_130703_",desc,false));method.instructions.add(new InsnNode(Opcodes.ARETURN));node.methods.add(method);
        var getter=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"getter","()Ljava/lang/invoke/MethodHandle;",null,null);
        getter.instructions.add(new LdcInsnNode(new Handle(Opcodes.H_INVOKESTATIC,"net/minecraft/network/chat/Component$Serializer","m_130703_",desc,false)));getter.instructions.add(new InsnNode(Opcodes.ARETURN));node.methods.add(getter);
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);
        var file=Files.createTempFile("lms-component-json", ".jar");try(var jar=new JarOutputStream(Files.newOutputStream(file))){jar.putNextEntry(new JarEntry(node.name+".class"));jar.write(writer.toByteArray());jar.closeEntry();}
        byte[] original=Files.readAllBytes(file);
        try(var loader=new ManagedLegacyClassLoader(file,ComponentJsonCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var type=loader.linkOwnedClass(node.name.replace('/','.'));
            if(!json.toString().equals(type.getMethod("write",Component.class).invoke(null,component)))throw new AssertionError("direct source serializer call not projected");
            var handle=(MethodHandle)type.getMethod("getter").invoke(null);
            try{if(!json.toString().equals(handle.invoke(component)))throw new AssertionError("source serializer method handle not projected");}catch(Throwable error){throw new IllegalStateException(error);}
        }
        if(!Arrays.equals(original,Files.readAllBytes(file)))throw new AssertionError("legacy artifact mutated");
        System.out.println("PASS: native component JSON roundtrip/style/translation/siblings, stable order, source direct/handle calls and rich-event fail-fast; source JSON wire format not claimed");
    }
}
