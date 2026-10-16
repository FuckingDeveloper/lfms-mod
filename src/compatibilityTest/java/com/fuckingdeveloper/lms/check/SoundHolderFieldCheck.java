package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.nio.file.*;
import java.util.*;
import net.minecraft.sounds.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

final class SoundHolderFieldCheck {
    static void run() throws Exception {
        String[] source={"f_11955_","f_11688_","f_144059_","f_144060_","f_12406_"};
        Object[] expected={SoundEvents.HOE_TILL.value(),SoundEvents.AXE_STRIP.value(),SoundEvents.AXE_SCRAPE.value(),SoundEvents.AXE_WAX_OFF.value(),SoundEvents.SHOVEL_FLATTEN.value()};
        var node=new ClassNode();node.version=Opcodes.V17;node.access=Opcodes.ACC_PUBLIC;node.name="fixture/SourceSoundFields";node.superName="java/lang/Object";
        for(int i=0;i<source.length;i++) {
            var method=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"sound"+i,"()Lnet/minecraft/sounds/SoundEvent;",null,null);
            method.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC,"net/minecraft/sounds/SoundEvents",source[i],"Lnet/minecraft/sounds/SoundEvent;"));
            method.instructions.add(new InsnNode(Opcodes.ARETURN));node.methods.add(method);
        }
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);
        Path artifact=Files.createTempFile("lms-sound-fields", ".jar");
        try {
            try(var jar=new java.util.jar.JarOutputStream(Files.newOutputStream(artifact))) {
                jar.putNextEntry(new java.util.jar.JarEntry(node.name+".class"));jar.write(writer.toByteArray());jar.closeEntry();
            }
            byte[] original=Files.readAllBytes(artifact);
            try(var loader=new ManagedLegacyClassLoader(artifact,SoundHolderFieldCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.loadClass("fixture.SourceSoundFields");
                for(int i=0;i<source.length;i++)check(type.getMethod("sound"+i).invoke(null)==expected[i],"sound field identity/holder unwrap lost: "+source[i]);
            }
            check(Arrays.equals(original,Files.readAllBytes(artifact)),"source sound fixture mutated");
        } finally {Files.deleteIfExists(artifact);}
        System.out.println("PASS: five mapped source tool sounds, exact native Holder.Reference field ABI and original SoundEvent identity without mutating legacy inputs");
    }
    private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
}
