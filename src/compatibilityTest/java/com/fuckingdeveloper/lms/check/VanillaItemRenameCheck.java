package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import java.lang.invoke.MethodHandle;
import java.nio.file.*;
import java.util.jar.*;
import net.minecraft.world.item.Items;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class VanillaItemRenameCheck {
    public static void run() throws Exception {
        String[] source = {"f_41864_", "f_42026_", "f_42355_"};
        Object[] target = {Items.SHORT_GRASS, Items.IRON_CHAIN, Items.TURTLE_SCUTE};
        String owner = "net/minecraft/world/item/Items", desc = "Lnet/minecraft/world/item/Item;";
        var node = new ClassNode(); node.name="fixture/RenamedItems"; node.superName="java/lang/Object"; node.version=Opcodes.V17; node.access=Opcodes.ACC_PUBLIC;
        for (int i=0;i<source.length;i++) {
            var read = new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"read"+i,"()"+desc,null,null);
            read.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC,owner,source[i],desc)); read.instructions.add(new InsnNode(Opcodes.ARETURN)); node.methods.add(read);
            var handle = new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"handle"+i,"()Ljava/lang/invoke/MethodHandle;",null,null);
            handle.instructions.add(new LdcInsnNode(new Handle(Opcodes.H_GETSTATIC,owner,source[i],desc,false))); handle.instructions.add(new InsnNode(Opcodes.ARETURN)); node.methods.add(handle);
        }
        var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); node.accept(writer);
        var path = Files.createTempFile("lms-item-renames", ".jar");
        try(var jar = new JarOutputStream(Files.newOutputStream(path))) { jar.putNextEntry(new JarEntry(node.name+".class")); jar.write(writer.toByteArray()); jar.closeEntry(); }
        byte[] original = Files.readAllBytes(path);
        try(var loader = new ManagedLegacyClassLoader(path,VanillaItemRenameCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var type=loader.linkOwnedClass(node.name.replace('/','.'));
            for(int i=0;i<source.length;i++) {
                if(type.getMethod("read"+i).invoke(null)!=target[i]) throw new AssertionError("source item identity lost");
                var getter=(MethodHandle)type.getMethod("handle"+i).invoke(null);
                try { if(getter.invoke()!=target[i]) throw new AssertionError("source field handle identity lost"); }
                catch(Throwable failure) { throw new IllegalStateException("Item field handle failed",failure); }
            }
        }
        if(!java.util.Arrays.equals(original,Files.readAllBytes(path))) throw new AssertionError("source fixture modified");
        System.out.println("PASS: mapped source grass/chain/turtle-scute item declarations and native field handles; immutable fixture");
    }
}
