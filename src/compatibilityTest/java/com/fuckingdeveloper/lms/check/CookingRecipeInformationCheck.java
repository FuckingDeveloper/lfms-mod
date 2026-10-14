package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.Forge1192RecipeBridge;
import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.resources.*;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.RegistryBuilder;
import com.mojang.serialization.Lifecycle;
import java.nio.file.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class CookingRecipeInformationCheck {
    private static void check(boolean ok,String message) { if(!ok)throw new AssertionError(message); }
    public static void run() throws Exception {
        var ingredient=Ingredient.of(Items.STONE);
        var recipe=new SmeltingRecipe(new Recipe.CommonInfo(true), new AbstractCookingRecipe.CookingBookInfo(CookingBookCategory.MISC,"fixture"),ingredient,new ItemStackTemplate(Items.COAL,2),0.7F,120);
        try {Forge1192RecipeBridge.legacyId(recipe);throw new AssertionError("ID guessed before holder provenance");}catch(UnsupportedOperationException expected) {}
        var id=Identifier.fromNamespaceAndPath("fixture","cooking");
        var key=ResourceKey.<Recipe<?>>create(Registries.RECIPE,id);
        var isolated=new RegistryBuilder<Recipe<?>>(Registries.RECIPE).disableRegistrationCheck().create();
        net.minecraft.core.Registry.register(isolated,key,recipe);isolated.freeze();
        var manager=new RecipeManager(net.minecraft.core.HolderLookup.Provider.create(java.util.stream.Stream.of(isolated)));
        check(Forge1192RecipeBridge.legacyRecipes(manager).iterator().next()==recipe,"enumeration lost original native recipe identity");
        check(Forge1192RecipeBridge.legacyId(recipe).equals(id),"holder identifier lost");
        var output=Forge1192RecipeBridge.legacyResult(recipe);check(output.is(Items.COAL)&&output.getCount()==2,"actual native result/template count lost");
        output.setCount(8);check(Forge1192RecipeBridge.legacyResult(recipe).getCount()==2,"source info view mutated native template");
        check(Forge1192RecipeBridge.legacyIngredients(recipe).getFirst()==ingredient,"native ingredient identity lost");
        String owner="net/minecraft/world/item/crafting/AbstractCookingRecipe";
        var node=new ClassNode();node.name="fixture/CookingInformation";node.superName="java/lang/Object";node.version=Opcodes.V17;node.access=Opcodes.ACC_PUBLIC;
        String[] names={"m_6423_","m_8043_","m_43753_","m_43750_"};
        String[] returns={"Lnet/minecraft/resources/ResourceLocation;","Lnet/minecraft/world/item/ItemStack;","I","F"};
        for(int i=0;i<names.length;i++) {
            var method=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"query"+i,"(L"+owner+";)"+returns[i],null,null);
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,owner,names[i],"()"+returns[i],false));
            method.instructions.add(new InsnNode(Type.getType(returns[i]).getOpcode(Opcodes.IRETURN)));node.methods.add(method);
            var handle=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"handle"+i,"()Ljava/lang/invoke/MethodHandle;",null,null);
            handle.instructions.add(new LdcInsnNode(new Handle(Opcodes.H_INVOKEVIRTUAL,owner,names[i],"()"+returns[i],false)));
            handle.instructions.add(new InsnNode(Opcodes.ARETURN));node.methods.add(handle);
        }
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);var path=Files.createTempFile("lms-cooking-info", ".jar");
        try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry(node.name+".class"));jar.write(writer.toByteArray());jar.closeEntry();}
        Object[] expected={id,null,120,0.7F};
        try(var loader=new ManagedLegacyClassLoader(path,CookingRecipeInformationCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var clazz=loader.linkOwnedClass(node.name.replace('/','.'));
            for(int i=0;i<names.length;i++) {
                Object actual=clazz.getMethod("query"+i,AbstractCookingRecipe.class).invoke(null,recipe);
                check(i==1?((ItemStack)actual).is(Items.COAL):actual.equals(expected[i]),"concrete source query "+names[i]);
                var handle=(java.lang.invoke.MethodHandle)clazz.getMethod("handle"+i).invoke(null);
                try {actual=handle.invoke(recipe);check(i==1?((ItemStack)actual).getCount()==2:actual.equals(expected[i]),"concrete source handle "+names[i]);}
                catch(Throwable failure){throw new IllegalStateException("source cooking handle failed",failure);}
            }
        }
        var otherKey=ResourceKey.<Recipe<?>>create(Registries.RECIPE,Identifier.fromNamespaceAndPath("fixture","ambiguous"));
        try {Forge1192RecipeBridge.recordRecipeIdentity(new RecipeHolder<>(otherKey,recipe));throw new AssertionError("ambiguous object ID accepted");}catch(IllegalStateException expectedFailure) {}
        try {Forge1192RecipeBridge.legacyId(recipe);throw new AssertionError("ambiguous object ID reused");}catch(IllegalStateException expectedFailure) {}
        System.out.println("PASS: actual native holder enumeration, weak identity provenance/ambiguity rejection, result template count/copy, ingredient identity, concrete source time/XP/id/result calls and handles; native crafting not inferred");
    }
}
