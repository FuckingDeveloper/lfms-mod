package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.target.neoforge263.Target263VanillaTagBridge;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import java.lang.invoke.MethodHandle;
import java.nio.file.*;
import java.util.jar.*;
import net.minecraft.tags.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class PairedVanillaTagCheck {
    public static void run() throws Exception {
        int count=0;
        try(var input=Forge1192LegacyClassTransformer.class.getResourceAsStream("/lms/profile/forge1192/vanilla-tags.tsv")) {
            for(String line:new String(input.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8).split("\n")) {
                if(line.isBlank()||line.startsWith("#"))continue;
                String[] r=line.split("\t");
                java.lang.reflect.Field field;
                try { field=BlockItemTags.class.getField(r[1]); } catch(NoSuchFieldException unknown) { continue; }
                if(field.getType()!=BlockItemTagId.class)throw new AssertionError("wrong native pair type");
                var pair=(BlockItemTagId)field.get(null);
                boolean item=r[0].endsWith("ItemTags");
                if(!(item?pair.item():pair.block()).location().toString().equals(r[2])) {
                    try { if(item)Target263VanillaTagBridge.item(r[1],r[2]); else Target263VanillaTagBridge.block(r[1],r[2]);
                        throw new AssertionError("different target tag identity silently accepted"); } catch(IllegalStateException expected) {}
                    continue;
                }
                var tag=item?Target263VanillaTagBridge.item(r[1],r[2]):Target263VanillaTagBridge.block(r[1],r[2]);
                if(tag!=(item?pair.item():pair.block()))throw new AssertionError("native tag identity lost");count++;
            }
        }
        try { Target263VanillaTagBridge.item("FLOWERS","minecraft:planks");throw new AssertionError("mismatched source location accepted"); } catch(IllegalStateException expected) {}
        var node=new ClassNode();node.name="fixture/PairedTag";node.superName="java/lang/Object";node.version=Opcodes.V17;node.access=Opcodes.ACC_PUBLIC;
        var read=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"read","()Lnet/minecraft/tags/TagKey;",null,null);
        read.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC,"net/minecraft/tags/ItemTags","f_13149_","Lnet/minecraft/tags/TagKey;"));read.instructions.add(new InsnNode(Opcodes.ARETURN));node.methods.add(read);
        var getter=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"handle","()Ljava/lang/invoke/MethodHandle;",null,null);
        getter.instructions.add(new LdcInsnNode(new Handle(Opcodes.H_GETSTATIC,"net/minecraft/tags/ItemTags","f_13149_","Lnet/minecraft/tags/TagKey;",false)));getter.instructions.add(new InsnNode(Opcodes.ARETURN));node.methods.add(getter);
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);var path=Files.createTempFile("lms-paired-tags",".jar");
        try(var jar=new JarOutputStream(Files.newOutputStream(path))) {jar.putNextEntry(new JarEntry(node.name+".class"));jar.write(writer.toByteArray());jar.closeEntry();}
        try(var loader=new ManagedLegacyClassLoader(path,PairedVanillaTagCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var type=loader.linkOwnedClass(node.name.replace('/','.'));
            if(type.getMethod("read").invoke(null)!=BlockItemTags.FLOWERS.item())throw new AssertionError("source flowers field migration failed");
            var handle=(MethodHandle)type.getMethod("handle").invoke(null);
            try {if(handle.invoke()!=BlockItemTags.FLOWERS.item())throw new AssertionError("source flowers getter handle failed");}
            catch(Throwable failure){throw new IllegalStateException("paired tag handle failed",failure);}
        }
        System.out.println("PASS: "+count+" pinned source/native pair identities, registry projections, source field/handle ABI and mismatched-identity rejection; tag contents not inferred");
    }
}
