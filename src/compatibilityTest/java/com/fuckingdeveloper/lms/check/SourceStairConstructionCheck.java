package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.nio.file.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.jar.*;
import java.lang.invoke.MethodHandle;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

final class SourceStairConstructionCheck {
    static void run() throws Exception {
        String root="net/minecraft/world/level/block/StairBlock",props="Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;",desc="(Ljava/util/function/Supplier;"+props+")V";
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,"probe/SourceStairs",null,root,null);
        var m=w.visitMethod(ACC_PUBLIC,"<init>",desc,null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);m.visitVarInsn(ALOAD,2);m.visitMethodInsn(INVOKESPECIAL,root,"<init>",desc,false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"handle","()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();m.visitLdcInsn(new Handle(H_NEWINVOKESPECIAL,root,"<init>",desc,false));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();
        var path=Files.createTempFile("lms-source-stairs", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry("probe/SourceStairs.class"));jar.write(w.toByteArray());jar.closeEntry();}
            byte[] original=Files.readAllBytes(path);
            try(var loader=new ManagedLegacyClassLoader(path,SourceStairConstructionCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass("probe.SourceStairs");int[] calls={0};BlockState[] state={Blocks.STONE.defaultBlockState()};Supplier<BlockState> supplier=()->{calls[0]++;return state[0];};
                var properties=BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,Identifier.fromNamespaceAndPath("probe","stairs")));
                // Builtins are already frozen in this headless probe. Reach the real native
                // constructor, but never thaw it or manufacture a registered block for a test.
                try{type.getConstructor(Supplier.class,BlockBehaviour.Properties.class).newInstance(supplier,properties);throw new AssertionError("frozen block registry unexpectedly accepted construction");}
                catch(java.lang.reflect.InvocationTargetException expected){check(expected.getCause() instanceof IllegalStateException&&expected.getCause().getMessage().contains("frozen"),"source super did not reach authoritative native constructor");}
                check(calls[0]==1,"source base was not resolved exactly once before native construction");
                var handle=(MethodHandle)type.getMethod("handle").invoke(null);
                check(handle.type().returnType()==StairBlock.class,"constructor handle return changed");
                try{handle.invoke(supplier,properties);throw new AssertionError("frozen registry bypassed by constructor handle");}
                catch(IllegalStateException expected){check(expected.getMessage().contains("frozen")&&calls[0]==2,"callable construction bypassed native lifecycle");}
                catch(Throwable failure){throw new AssertionError(failure);}
                check(com.fuckingdeveloper.lms.target.neoforge263.Target263SourceStairs.resolve(supplier)==state[0]&&calls[0]==3,"base state identity changed");
                var failure=new IllegalArgumentException("base-failure");Supplier<BlockState> broken=()->{throw failure;};
                try{type.getConstructor(Supplier.class,BlockBehaviour.Properties.class).newInstance(broken,properties);throw new AssertionError("supplier failure swallowed");}catch(java.lang.reflect.InvocationTargetException expected){check(expected.getCause()==failure,"source exception identity changed");}
                check(Arrays.equals(original,Files.readAllBytes(path)),"source JAR changed");
            }
        }finally{Files.deleteIfExists(path);}
        System.out.println("PASS: supplier stair source-super verifier safety and constructor handle, once-only base resolution/identity and frozen native registry rejection, source failure and immutable artifact; actual native construction/snapshot and gameplay require a valid runtime registration phase");
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
