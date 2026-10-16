package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.block.Forge1192BonemealBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

/** Actual interface dispatch to original source signatures, without a fabricated world lifecycle. */
public final class BonemealFamilyCheck {
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    public static void run() throws Exception {
        var source = new ClassNode(); source.name="fixture/Fertilizable"; source.superName="java/lang/Object";
        source.version=Opcodes.V17; source.access=Opcodes.ACC_PUBLIC;
        source.interfaces.add("net/minecraft/world/level/block/BonemealableBlock");
        source.fields.add(new FieldNode(Opcodes.ACC_PUBLIC,"calls","I",null,null));
        source.fields.add(new FieldNode(Opcodes.ACC_PUBLIC,"lastWorld","Ljava/lang/Object;",null,null));
        String pos="Lnet/minecraft/core/BlockPos;", state="Lnet/minecraft/world/level/block/state/BlockState;", random="Lnet/minecraft/util/RandomSource;";
        String[] names={"m_7370_","m_214167_","m_214148_"};
        String[] descs={"(Lnet/minecraft/world/level/BlockGetter;"+pos+state+"Z)Z","(Lnet/minecraft/world/level/Level;"+random+pos+state+")Z","(Lnet/minecraft/server/level/ServerLevel;"+random+pos+state+")V"};
        for(int i=0;i<3;i++) {
            var method=new MethodNode(Opcodes.ACC_PUBLIC,names[i],descs[i],null,null);
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0)); method.instructions.add(new InsnNode(Opcodes.DUP));
            method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,source.name,"calls","I"));
            method.instructions.add(new InsnNode(Opcodes.ICONST_1)); method.instructions.add(new InsnNode(Opcodes.IADD));
            method.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD,source.name,"calls","I"));
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0)); method.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
            method.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD,source.name,"lastWorld","Ljava/lang/Object;"));
            if(i<2) method.instructions.add(i==0?new VarInsnNode(Opcodes.ILOAD,4):new InsnNode(Opcodes.ICONST_0));
            method.instructions.add(new InsnNode(i<2?Opcodes.IRETURN:Opcodes.RETURN)); source.methods.add(method);
        }
        var file=Files.createTempFile("lms-bonemeal", ".jar");
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS); source.accept(writer);
        try(var jar=new JarOutputStream(Files.newOutputStream(file))){jar.putNextEntry(new JarEntry(source.name+".class"));jar.write(writer.toByteArray());jar.closeEntry();}
        byte[] original=Files.readAllBytes(file);
        var unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);var unsafe=(sun.misc.Unsafe)unsafeField.get(null);
        try(var loader=new ManagedLegacyClassLoader(file,BonemealFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var type=loader.linkOwnedClass(source.name.replace('/','.'));
            var block=(BonemealableBlock)unsafe.allocateInstance(type);
            var server=(ServerLevel)unsafe.allocateInstance(ServerLevel.class); // Only native side field is queried.
            var side=Level.class.getDeclaredField("isClientSide");long offset=unsafe.objectFieldOffset(side);
            var position=new BlockPos(3,4,5);var stateValue=Blocks.STONE.defaultBlockState();var rng=RandomSource.create(37);
            check(!block.isValidBonemealTarget(server,position,stateValue,BonemealSource.INTERACTION),"source server-side boolean lost");
            unsafe.putBoolean(server,offset,true);
            check(block.isValidBonemealTarget(server,position,stateValue,BonemealSource.INTERACTION),"source client-side boolean lost");
            check(!block.isBonemealSuccess(server,rng,position,stateValue,BonemealSource.INTERACTION),"source false result replaced");
            block.performBonemeal(server,rng,position,stateValue,BonemealSource.INTERACTION);
            check(type.getField("calls").getInt(block)==4 && type.getField("lastWorld").get(block)==server,"source callbacks missing/duplicated or world copied");
            try{block.performBonemeal(server,rng,position,stateValue,BonemealSource.MOB);throw new AssertionError("unknown source origin accepted");}catch(UnsupportedOperationException expected){}
            check(type.getField("calls").getInt(block)==4,"unsupported origin invoked source callback");
        }
        check(Arrays.equals(original,Files.readAllBytes(file)),"legacy artifact mutated");
        try{Forge1192BonemealBridge.requireInteraction(null);throw new AssertionError("null source accepted");}catch(UnsupportedOperationException expected){}
        System.out.println("PASS: native three-callback bonemeal interface dispatch, authoritative side, false result, once-only delivery and unsupported origin rejection; actual tree growth requires client");
    }
}
