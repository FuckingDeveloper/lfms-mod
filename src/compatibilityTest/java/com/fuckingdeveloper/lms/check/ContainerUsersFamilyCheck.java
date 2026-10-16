package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

final class ContainerUsersFamilyCheck {
    static void run() throws Exception {
        String player="Lnet/minecraft/world/entity/player/Player;",container="net/minecraft/world/Container";
        var node=new ClassNode();node.name="fixture/SourceContainerUsers";node.superName="net/minecraft/world/SimpleContainer";node.access=Opcodes.ACC_PUBLIC;node.version=Opcodes.V17;
        node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC,"calls","I",null,null));
        var init=new MethodNode(Opcodes.ACC_PUBLIC,"<init>","()V",null,null);node.methods.add(init);
        init.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));init.instructions.add(new InsnNode(Opcodes.ICONST_1));init.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,node.superName,"<init>","(I)V",false));init.instructions.add(new InsnNode(Opcodes.RETURN));
        for(var name:List.of("m_5856_","m_5785_")) {
            var method=new MethodNode(Opcodes.ACC_PUBLIC,name,"("+player+")V",null,null);node.methods.add(method);
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));method.instructions.add(new InsnNode(Opcodes.DUP));method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,node.name,"calls","I"));method.instructions.add(new InsnNode(Opcodes.ICONST_1));method.instructions.add(new InsnNode(Opcodes.IADD));method.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD,node.name,"calls","I"));
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));method.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,node.superName,name,"("+player+")V",false));method.instructions.add(new InsnNode(Opcodes.RETURN));
            var handle=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"handle"+name,"()Ljava/lang/invoke/MethodHandle;",null,null);node.methods.add(handle);
            handle.instructions.add(new LdcInsnNode(new Handle(Opcodes.H_INVOKEINTERFACE,container,name,"("+player+")V",true)));handle.instructions.add(new InsnNode(Opcodes.ARETURN));
        }
        var path=Files.createTempFile("lms-container-users", ".jar");var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);
        try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry(node.name+".class"));jar.write(writer.toByteArray());jar.closeEntry();}
        byte[] original=Files.readAllBytes(path);
        try(var loader=new ManagedLegacyClassLoader(path,ContainerUsersFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var type=loader.linkOwnedClass(node.name.replace('/','.'));var instance=(net.minecraft.world.Container)type.getConstructor().newInstance();
            var field=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);var unsafe=(sun.misc.Unsafe)field.get(null);
            // Identity-only callback probe: no player/world lifecycle or gameplay is simulated here.
            var sourcePlayer=(net.minecraft.world.entity.player.Player)unsafe.allocateInstance(net.minecraft.server.level.ServerPlayer.class);
            instance.startOpen(sourcePlayer);instance.stopOpen(sourcePlayer);
            for(var name:List.of("m_5856_","m_5785_")) {
                var handle=(java.lang.invoke.MethodHandle)type.getMethod("handle"+name).invoke(null);
                check(handle.type().equals(java.lang.invoke.MethodType.methodType(void.class,net.minecraft.world.Container.class,net.minecraft.world.entity.player.Player.class)),"source handle type changed");
                try{handle.invokeExact(instance,sourcePlayer);}catch(Throwable failure){throw new AssertionError(failure);}
            }
            check(type.getField("calls").getInt(instance)==4,"source/native container callbacks lost, duplicated or recursed");
            var nonPlayer=new net.minecraft.world.entity.ContainerUser(){public boolean hasContainerOpen(net.minecraft.world.level.block.entity.ContainerOpenersCounter counter,net.minecraft.core.BlockPos pos){return false;}public double getContainerInteractionRange(){return 4;}};
            try{instance.startOpen(nonPlayer);throw new AssertionError("nonplayer silently coerced");}catch(UnsupportedOperationException expected){}
        }
        check(Arrays.equals(original,Files.readAllBytes(path)),"source artifact mutated");
        System.out.println("PASS: native container-user dispatch, source open/close callbacks and super order, exact source method-handle types, nonplayer fail-fast and immutable artifact");
    }
    private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
}
