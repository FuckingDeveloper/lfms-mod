package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import java.lang.invoke.MethodHandle;
import java.nio.file.*;
import java.util.jar.*;
import net.minecraft.world.item.Items;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class VanillaItemRenameCheck {
    public static void run() throws Exception {
        String[] source = {"f_41864_", "f_42026_", "f_42355_", "f_42492_", "f_42493_", "f_42494_", "f_42495_", "f_42496_", "f_42497_", "f_42498_", "f_42535_", "f_42536_", "f_42537_", "f_42538_", "f_42539_", "f_42540_", "f_42489_", "f_42490_", "f_42491_"};
        Object[] target = {Items.SHORT_GRASS, Items.IRON_CHAIN, Items.TURTLE_SCUTE, Items.DYE.pick(net.minecraft.world.item.DyeColor.CYAN), Items.DYE.pick(net.minecraft.world.item.DyeColor.PURPLE), Items.DYE.pick(net.minecraft.world.item.DyeColor.BLUE), Items.DYE.pick(net.minecraft.world.item.DyeColor.BROWN), Items.DYE.pick(net.minecraft.world.item.DyeColor.GREEN), Items.DYE.pick(net.minecraft.world.item.DyeColor.RED), Items.DYE.pick(net.minecraft.world.item.DyeColor.BLACK), Items.DYE.pick(net.minecraft.world.item.DyeColor.WHITE), Items.DYE.pick(net.minecraft.world.item.DyeColor.ORANGE), Items.DYE.pick(net.minecraft.world.item.DyeColor.MAGENTA), Items.DYE.pick(net.minecraft.world.item.DyeColor.LIGHT_BLUE), Items.DYE.pick(net.minecraft.world.item.DyeColor.YELLOW), Items.DYE.pick(net.minecraft.world.item.DyeColor.LIME), Items.DYE.pick(net.minecraft.world.item.DyeColor.PINK), Items.DYE.pick(net.minecraft.world.item.DyeColor.GRAY), Items.DYE.pick(net.minecraft.world.item.DyeColor.LIGHT_GRAY)};
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
        System.out.println("PASS: mapped source grass/chain/turtle-scute and all sixteen dye item identities, native ColorCollection selection and callable field handles; immutable fixture");
    }
}
