package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.lang.invoke.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.nbt.*;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

public final class ListTagReadFamilyCheck {
    private record Read(String name, String result, Tag value, Object expected, Object empty) {}
    public static void run() throws Exception {
        var compound = new CompoundTag(); compound.putString("id", "fixture:identity");
        var nested = new ListTag(); nested.add(IntTag.valueOf(42));
        int[] ints = {4, 8}; long[] longs = {12, 16};
        var reads = List.of(new Read("getCompound", "Lnet/minecraft/nbt/CompoundTag;", compound, compound, new CompoundTag()),
                new Read("getList", "Lnet/minecraft/nbt/ListTag;", nested, nested, new ListTag()),
                new Read("getShort", "S", ShortTag.valueOf((short)7), (short)7, (short)0),
                new Read("getInt", "I", IntTag.valueOf(11), 11, 0),
                new Read("getDouble", "D", DoubleTag.valueOf(1.5), 1.5, 0.0),
                new Read("getFloat", "F", FloatTag.valueOf(2.5f), 2.5f, 0.0f),
                new Read("getIntArray", "[I", new IntArrayTag(ints), ints, new int[0]),
                new Read("getLongArray", "[J", new LongArrayTag(longs), longs, new long[0]),
                new Read("getString", "Ljava/lang/String;", StringTag.valueOf("unquoted"), "unquoted", ""));
        var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);writer.visit(V17,ACC_PUBLIC,"fixture/ListReads",null,"java/lang/Object",null);
        for(var read:reads) {
            String desc="(I)"+read.result();
            var method=writer.visitMethod(ACC_PUBLIC|ACC_STATIC,read.name(),"(Lnet/minecraft/nbt/ListTag;I)"+read.result(),null,null);method.visitCode();method.visitVarInsn(ALOAD,0);method.visitVarInsn(ILOAD,1);method.visitMethodInsn(INVOKEVIRTUAL,"net/minecraft/nbt/ListTag",read.name(),desc,false);method.visitInsn(Type.getType(read.result()).getOpcode(IRETURN));method.visitMaxs(0,0);method.visitEnd();
            method=writer.visitMethod(ACC_PUBLIC|ACC_STATIC,read.name()+"Handle","()Ljava/lang/invoke/MethodHandle;",null,null);method.visitCode();method.visitLdcInsn(new Handle(H_INVOKEVIRTUAL,"net/minecraft/nbt/ListTag",read.name(),desc,false));method.visitInsn(ARETURN);method.visitMaxs(0,0);method.visitEnd();
        }
        var method=writer.visitMethod(ACC_PUBLIC|ACC_STATIC,"srg","(Lnet/minecraft/nbt/ListTag;I)Lnet/minecraft/nbt/CompoundTag;",null,null);method.visitCode();method.visitVarInsn(ALOAD,0);method.visitVarInsn(ILOAD,1);method.visitMethodInsn(INVOKEVIRTUAL,"net/minecraft/nbt/ListTag","m_128728_","(I)Lnet/minecraft/nbt/CompoundTag;",false);method.visitInsn(ARETURN);method.visitMaxs(0,0);method.visitEnd();writer.visitEnd();
        var file=Files.createTempFile("lms-list-tag-reads-", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(file))){jar.putNextEntry(new JarEntry("fixture/ListReads.class"));jar.write(writer.toByteArray());jar.closeEntry();}
            try(var loader=new ManagedLegacyClassLoader(file,ListTagReadFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass("fixture.ListReads");
                for(var read:reads) {
                    var list=new ListTag();list.add(read.value());
                    for(int index: new int[]{0,-1,1}) {
                        Object result=type.getMethod(read.name(),ListTag.class,int.class).invoke(null,list,index);
                        check(Objects.deepEquals(result,index==0?read.expected():read.empty()),"typed list/default read "+read.name());
                        var handle=(MethodHandle)type.getMethod(read.name()+"Handle").invoke(null);
                        try { check(Objects.deepEquals(handle.invokeWithArguments(list,index),result),"callable typed read "+read.name()); }
                        catch(Throwable failure){throw new AssertionError(failure);}
                    }
                    if(!read.name().equals("getString")) {
                        var wrong=new ListTag();wrong.add(StringTag.valueOf("wrong"));
                        check(Objects.deepEquals(type.getMethod(read.name(),ListTag.class,int.class).invoke(null,wrong,0),read.empty()),"wrong typed list default "+read.name());
                    }
                }
                var list=new ListTag();list.add(compound);
                check(type.getMethod("srg",ListTag.class,int.class).invoke(null,list,0)==compound,"SRG compound lost live identity");
                var numeric=new ListTag();numeric.add(IntTag.valueOf(17));
                check(type.getMethod("getShort",ListTag.class,int.class).invoke(null,numeric,0).equals((short)0),"native numeric coercion widened source semantics");
                check(type.getMethod("getString",ListTag.class,int.class).invoke(null,numeric,0).equals("17"),"source non-string SNBT fallback");
            }
        } finally { Files.deleteIfExists(file); }
        System.out.println("PASS: complete source typed ListTag reads, exact numeric types/defaults/index bounds, live compound identity, strings/SNBT and callable forms");
    }
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
