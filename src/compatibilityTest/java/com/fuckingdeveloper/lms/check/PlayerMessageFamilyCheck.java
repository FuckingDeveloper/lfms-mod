package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

final class PlayerMessageFamilyCheck implements Opcodes {
    static void run() throws Exception {
        String player="net/minecraft/world/entity/player/Player",server="net/minecraft/server/level/ServerPlayer",component="Lnet/minecraft/network/chat/Component;",desc="("+component+"Z)V";
        var base=node("fixture/MessageServer",server);
        base.fields.add(new FieldNode(ACC_PUBLIC,"message",component,null,null));base.fields.add(new FieldNode(ACC_PUBLIC,"overlay","Z",null,null));base.fields.add(new FieldNode(ACC_PUBLIC,"count","I",null,null));
        var send=new MethodNode(ACC_PUBLIC,"sendSystemMessage",desc,null,null);
        send.instructions.add(new VarInsnNode(ALOAD,0));send.instructions.add(new VarInsnNode(ALOAD,1));send.instructions.add(new FieldInsnNode(PUTFIELD,base.name,"message",component));send.instructions.add(new VarInsnNode(ALOAD,0));send.instructions.add(new VarInsnNode(ILOAD,2));send.instructions.add(new FieldInsnNode(PUTFIELD,base.name,"overlay","Z"));increment(send,base.name);send.instructions.add(new InsnNode(RETURN));base.methods.add(send);
        var child=node("fixture/MessageOverride",base.name);
        var override=new MethodNode(ACC_PUBLIC,"m_5661_",desc,null,null);
        increment(override,base.name);override.instructions.add(new VarInsnNode(ALOAD,0));override.instructions.add(new VarInsnNode(ALOAD,1));override.instructions.add(new VarInsnNode(ILOAD,2));override.instructions.add(new MethodInsnNode(INVOKESPECIAL,server,"m_5661_",desc,false));override.instructions.add(new InsnNode(RETURN));child.methods.add(override);
        var caller=node("fixture/MessageCaller","java/lang/Object");String parameters="(L"+player+";"+component+"Z)V";
        var call=new MethodNode(ACC_PUBLIC|ACC_STATIC,"send",parameters,null,null);
        call.instructions.add(new VarInsnNode(ALOAD,0));call.instructions.add(new VarInsnNode(ALOAD,1));call.instructions.add(new VarInsnNode(ILOAD,2));call.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,player,"m_5661_",desc,false));call.instructions.add(new InsnNode(RETURN));caller.methods.add(call);
        var handle=new MethodNode(ACC_PUBLIC|ACC_STATIC,"reference","()Ljava/lang/invoke/MethodHandle;",null,null);handle.instructions.add(new LdcInsnNode(new Handle(H_INVOKEVIRTUAL,player,"m_5661_",desc,false)));handle.instructions.add(new InsnNode(ARETURN));caller.methods.add(handle);
        var path=Files.createTempFile("lms-player-message", ".jar");try(var jar=new JarOutputStream(Files.newOutputStream(path))){for(var node:List.of(base,child,caller)){jar.putNextEntry(new JarEntry(node.name+".class"));jar.write(bytes(node));jar.closeEntry();}}
        byte[] immutable=Files.readAllBytes(path);
        try(var loader=new ManagedLegacyClassLoader(path,PlayerMessageFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var field=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);var unsafe=(sun.misc.Unsafe)field.get(null);
            var consumer=loader.linkOwnedClass(caller.name.replace('/','.'));var method=consumer.getMethod("send",Player.class,Component.class,boolean.class);
            for(var node:List.of(base,child)) {
                var type=loader.linkOwnedClass(node.name.replace('/','.'));var receiver=(Player)unsafe.allocateInstance(type);var message=Component.literal("probe");
                method.invoke(null,receiver,message,false);check(type.getField("message").get(receiver)==message&&!type.getField("overlay").getBoolean(receiver),"chat identity/flag lost");
                var mh=(java.lang.invoke.MethodHandle)consumer.getMethod("reference").invoke(null);
                try{mh.invoke(receiver,message,true);}catch(Throwable error){throw new AssertionError(error);}
                check(type.getField("message").get(receiver)==message&&type.getField("overlay").getBoolean(receiver),"overlay identity/handle lost");
                check(type.getField("count").getInt(receiver)==(node==child?4:2),"virtual source override/native source-super duplicated or skipped");
                com.fuckingdeveloper.lms.profile.forge1192.runtime.world.Forge1192PlayerMessageBridge.baseDisplay(receiver,message,true);
                check(type.getField("count").getInt(receiver)==(node==child?4:2),"empty source Player super acquired native behavior");
            }
        }
        check(Arrays.equals(immutable,Files.readAllBytes(path)),"source artifact changed");
        Path artifact=Path.of("legacy-mods/IC2Classic-1.19.2-2.1.3.4.jar");if(Files.exists(artifact))try(var jar=new JarFile(artifact.toFile())) {
            String owner="ic2/core/item/tool/EUReaderTool";byte[] original;try(var input=jar.getInputStream(jar.getJarEntry(owner+".class"))){original=input.readAllBytes();}
            var mapped=new ClassNode();new ClassReader(new Forge1192LegacyClassTransformer().transform(owner.replace('/','.'),original).bytes()).accept(mapped,0);int count=0;
            for(var m:mapped.methods)for(var i:m.instructions)if(i instanceof MethodInsnNode c){check(!c.name.equals("m_5661_"),"original player message escaped");if(c.owner.endsWith("Forge1192PlayerMessageBridge"))count++;}
            check(count>0,"original EU reader message sites absent");
        }
        System.out.println("PASS: source player message virtual override/super, server chat/overlay flags, message identity, callable handle, source base empty behavior and immutable original EU reader sites; local visual delivery requires client");
    }
    private static void increment(MethodNode method,String owner){method.instructions.add(new VarInsnNode(ALOAD,0));method.instructions.add(new InsnNode(DUP));method.instructions.add(new FieldInsnNode(GETFIELD,owner,"count","I"));method.instructions.add(new InsnNode(ICONST_1));method.instructions.add(new InsnNode(IADD));method.instructions.add(new FieldInsnNode(PUTFIELD,owner,"count","I"));}
    private static ClassNode node(String name,String parent){var n=new ClassNode();n.version=V17;n.access=ACC_PUBLIC;n.name=name;n.superName=parent;return n;}
    private static byte[] bytes(ClassNode node){var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(w);return w.toByteArray();}
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
