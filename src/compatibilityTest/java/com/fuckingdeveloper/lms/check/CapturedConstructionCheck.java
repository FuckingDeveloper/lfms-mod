package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.target.neoforge263.Target263ItemPropertiesSnapshots;
import com.fuckingdeveloper.lms.mixin.Target263ItemPropertiesAccess;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

final class CapturedConstructionCheck {
    static void run() throws Exception {
        var source=new Item.Properties().stacksTo(9).setNoCombineRepair().overrideDescription("probe.description");
        var a=Target263ItemPropertiesSnapshots.withId(source,ResourceKey.create(Registries.ITEM,Identifier.fromNamespaceAndPath("probe","a")));
        var b=Target263ItemPropertiesSnapshots.withId(source,ResourceKey.create(Registries.ITEM,Identifier.fromNamespaceAndPath("probe","b")));
        check(a!=b&&a!=source&&((Target263ItemPropertiesAccess)(Object)source).lms$itemId()==null,"captured source properties mutated/aliased");
        check(a.effectiveModel().getPath().equals("a")&&b.effectiveModel().getPath().equals("b")&&a.componentInitializer==source.componentInitializer&&b.componentInitializer==source.componentInitializer,"entry model/component identity");
        check(!((Target263ItemPropertiesAccess)(Object)a).lms$combineRepair()&&((Target263ItemPropertiesAccess)(Object)a).lms$description()==((Target263ItemPropertiesAccess)(Object)source).lms$description(),"source property intent dropped");
        source.stacksTo(13);check(a.componentInitializer!=source.componentInitializer&&b.componentInitializer!=source.componentInitializer,"late source builder mutation changed existing snapshot");
        try{Target263ItemPropertiesSnapshots.withId(new Item.Properties(){},ResourceKey.create(Registries.ITEM,Identifier.fromNamespaceAndPath("probe","unknown")));throw new AssertionError("unproven properties subclass sliced");}catch(UnsupportedOperationException expected){}
        var path=Files.createTempFile("lms-source-block-access", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))){for(boolean receiver:List.of(false,true)){jar.putNextEntry(new JarEntry((receiver?"probe/Receiver":"probe/Caller")+".class"));jar.write(fixture(receiver));jar.closeEntry();}}
            try(var loader=new ManagedLegacyClassLoader(path,CapturedConstructionCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass("probe.Receiver");var caller=loader.linkOwnedClass("probe.Caller");caller.getDeclaredMethods();
                var f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);var unsafe=(sun.misc.Unsafe)f.get(null);
                var block=(Block)unsafe.allocateInstance(type); // Dispatch-only double; no construction, registry, world or block state is fabricated.
                caller.getMethod("call",Block.class).invoke(null,block);
                check(type.getField("calls").getInt(null)==1,"direct cross-block call lost actual virtual override");
                check(Boolean.TRUE.equals(caller.getMethod("query",Block.class).invoke(null,block)),"moved source-public random tick query lost result/dispatch");
                check(type.getField("calls").getInt(null)==2,"moved callback dispatch duplicated or lost");
            }
        }finally{Files.deleteIfExists(path);}
        System.out.println("PASS: separate captured-property identities/models/initializer snapshots and subclass rejection; actual public native cross-block callback access and source virtual dispatch without registry thaw");
    }
    private static byte[] fixture(boolean receiver) {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);String name=receiver?"probe/Receiver":"probe/Caller";
        w.visit(V17,ACC_PUBLIC,name,null,receiver?"net/minecraft/world/level/block/Block":"java/lang/Object",null);
        String desc="(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V";
        if(receiver){w.visitField(ACC_PUBLIC|ACC_STATIC,"calls","I",null,null).visitEnd();var m=w.visitMethod(ACC_PUBLIC,"onPlace",desc,null,null);m.visitCode();m.visitFieldInsn(GETSTATIC,name,"calls","I");m.visitInsn(ICONST_1);m.visitInsn(IADD);m.visitFieldInsn(PUTSTATIC,name,"calls","I");m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();}
        else {var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"call","(Lnet/minecraft/world/level/block/Block;)V",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);for(int i=0;i<4;i++)m.visitInsn(ACONST_NULL);m.visitInsn(ICONST_0);m.visitMethodInsn(INVOKEVIRTUAL,"net/minecraft/world/level/block/Block","onPlace",desc,false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();}
        var query=w.visitMethod(ACC_PUBLIC|(receiver?0:ACC_STATIC),receiver?"isRandomlyTicking":"query",receiver?"(Lnet/minecraft/world/level/block/state/BlockState;)Z":"(Lnet/minecraft/world/level/block/Block;)Z",null,null);
        query.visitCode();
        if(receiver){query.visitFieldInsn(GETSTATIC,name,"calls","I");query.visitInsn(ICONST_1);query.visitInsn(IADD);query.visitFieldInsn(PUTSTATIC,name,"calls","I");query.visitInsn(ICONST_1);}
        else {query.visitVarInsn(ALOAD,0);query.visitInsn(ACONST_NULL);query.visitMethodInsn(INVOKEVIRTUAL,"net/minecraft/world/level/block/Block","isRandomlyTicking","(Lnet/minecraft/world/level/block/state/BlockState;)Z",false);}
        query.visitInsn(IRETURN);query.visitMaxs(0,0);query.visitEnd();
        w.visitEnd();return w.toByteArray();
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
