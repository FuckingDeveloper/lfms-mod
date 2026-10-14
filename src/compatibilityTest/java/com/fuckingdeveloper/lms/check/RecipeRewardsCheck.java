package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.advancement.Forge1192RecipeRewardsBridge;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.core.registries.Registries;
import java.nio.file.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class RecipeRewardsCheck {
    public static void run() throws Exception {
        var id=Identifier.fromNamespaceAndPath("fixture","recipe");
        var builder=Forge1192RecipeRewardsBridge.recipe(id);
        if(Forge1192RecipeRewardsBridge.addRecipe(builder,id)!=builder)throw new AssertionError("builder identity lost");
        var reward=builder.build();
        if(reward.recipes().size()!=2||!reward.recipes().get(0).isFor(Registries.RECIPE)||!reward.recipes().get(0).identifier().equals(id))throw new AssertionError("source order/duplicate/registry key intent lost");
        String owner="net/minecraft/advancements/AdvancementRewards$Builder",arg="Lnet/minecraft/resources/ResourceLocation;",ret="L"+owner+";";
        var node=new ClassNode();node.name="fixture/RecipeRewards";node.superName="java/lang/Object";node.version=Opcodes.V17;node.access=Opcodes.ACC_PUBLIC;
        var method=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"reward","("+arg+")Lnet/minecraft/advancements/AdvancementRewards;",null,null);
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,owner,"m_10009_","("+arg+")"+ret,false));
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,owner,"m_10011_","("+arg+")"+ret,false));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,owner,"m_10004_","()Lnet/minecraft/advancements/AdvancementRewards;",false));method.instructions.add(new InsnNode(Opcodes.ARETURN));node.methods.add(method);
        var handle=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"handle","()Ljava/lang/invoke/MethodHandle;",null,null);
        handle.instructions.add(new LdcInsnNode(new Handle(Opcodes.H_INVOKEVIRTUAL,owner,"m_10011_","("+arg+")"+ret,false)));handle.instructions.add(new InsnNode(Opcodes.ARETURN));node.methods.add(handle);
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);var path=Files.createTempFile("lms-recipe-rewards",".jar");
        try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry(node.name+".class"));jar.write(writer.toByteArray());jar.closeEntry();}
        try(var loader=new ManagedLegacyClassLoader(path,RecipeRewardsCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var type=loader.linkOwnedClass(node.name.replace('/','.'));
            var actual=(AdvancementRewards)type.getMethod("reward",Identifier.class).invoke(null,id);
            if(actual.recipes().size()!=2||!actual.recipes().get(1).identifier().equals(id))throw new AssertionError("source static/fluent reward calls did not execute");
            var getter=(java.lang.invoke.MethodHandle)type.getMethod("handle").invoke(null);
            try{if(getter.invoke(builder,id)!=builder)throw new AssertionError("source handle receiver/return lost");}
            catch(Throwable failure){throw new IllegalStateException("recipe reward handle failed",failure);}
        }
        System.out.println("PASS: source reward static/fluent/handle calls, native recipe registry keys, builder identity/order/duplicates; actual recipe unlock not inferred");
    }
}
