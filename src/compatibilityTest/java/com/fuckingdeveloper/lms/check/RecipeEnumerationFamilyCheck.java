package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.recipe.Forge1192RecipeBridge;
import java.lang.invoke.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

public final class RecipeEnumerationFamilyCheck {
    public static Object constant(MethodHandles.Lookup lookup,String name,Class<?> type,Object value){return value;}
    public static void run() throws Exception {
        var recipe=new SmeltingRecipe(new Recipe.CommonInfo(true),new AbstractCookingRecipe.CookingBookInfo(CookingBookCategory.MISC,"fixture"),Ingredient.of(Items.STONE),new ItemStackTemplate(Items.COAL,1),0.3F,90);
        var id=Identifier.parse("fixture:enumerated");
        var registry=new net.neoforged.neoforge.registries.RegistryBuilder<Recipe<?>>(Registries.RECIPE).disableRegistrationCheck().create();
        Registry.register(registry,ResourceKey.create(Registries.RECIPE,id),recipe);registry.freeze();
        var manager=new RecipeManager(HolderLookup.Provider.create(java.util.stream.Stream.of(registry)));
        check(Forge1192RecipeBridge.legacyRecipeByKey(manager,id).orElseThrow()==recipe,"holder escaped key lookup");
        check(Forge1192RecipeBridge.legacyId(recipe).equals(id),"lookup did not record holder provenance");
        check(Forge1192RecipeBridge.legacyRecipeByKey(manager,Identifier.parse("fixture:missing")).isEmpty(),"missing recipe fabricated");
        check(Forge1192RecipeBridge.legacyRecipeByKey(manager,null).isEmpty(),"source null lookup changed");
        var all=Forge1192RecipeBridge.legacyRecipes(manager);all.clear();
        check(manager.getRecipes().size()==1,"source detached collection mutated native manager");
        var typed=Forge1192RecipeBridge.legacyRecipesByType(manager,RecipeType.SMELTING);
        check(typed.equals(List.of(recipe)),"type enumeration lost actual recipe");
        try{typed.clear();throw new AssertionError("source type list became mutable");}catch(UnsupportedOperationException expected){}
        check(Forge1192RecipeBridge.legacyRecipesByType(manager,RecipeType.SMOKING).isEmpty(),"wrong recipe type admitted");
        check(Forge1192RecipeBridge.legacyRecipeIds(manager).toList().equals(List.of(id)),"authoritative identifier stream");
        var path=Files.createTempFile("source-recipe-enumeration-",".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry("fixture/RecipeQueries.class"));jar.write(fixture());jar.closeEntry();}
            var original=Files.readAllBytes(path);
            try(var loader=new ManagedLegacyClassLoader(path,RecipeEnumerationFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass("fixture.RecipeQueries");
                for(int i=0;i<4;i++) {
                    Object[] args=switch(i){case 1->new Object[]{manager,RecipeType.SMELTING};case 2->new Object[]{manager,id};default->new Object[]{manager};};
                    // Select exact fixture names; generic erasure must not leak RecipeHolder.
                    Object result=type.getMethod("query"+i,i==1?new Class<?>[]{RecipeManager.class,RecipeType.class}:i==2?new Class<?>[]{RecipeManager.class,Identifier.class}:new Class<?>[]{RecipeManager.class}).invoke(null,args);
                    verify(i,result,recipe,id);
                    for(boolean nested:List.of(false,true))try {
                        var handle=(MethodHandle)type.getMethod("handle"+i+nested).invoke(null);
                        verify(i,handle.invokeWithArguments(args),recipe,id);
                    }catch(Throwable failure){throw new AssertionError(failure);}
                }
            }
            check(Arrays.equals(original,Files.readAllBytes(path)),"immutable source changed");
        } finally {Files.deleteIfExists(path);}
        System.out.println("PASS: authoritative native recipe type/all/key/id projections, actual recipe identity/provenance, immutable typed list, detached mutable collection, missing/null lookup and original direct/callable/nested bytecode");
    }
    private static void verify(int index,Object result,Recipe<?> recipe,Identifier id) {
        switch(index) {
            case 0,1 -> check(((Collection<?>)result).iterator().next()==recipe,"holder escaped erased source collection");
            case 2 -> check(((Optional<?>)result).orElseThrow()==recipe,"holder escaped erased source optional");
            case 3 -> {try(var stream=(java.util.stream.Stream<?>)result){check(stream.toList().equals(List.of(id)),"source stream identity");}}
        }
    }
    private static byte[] fixture() {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,"fixture/RecipeQueries",null,"java/lang/Object",null);
        String owner="net/minecraft/world/item/crafting/RecipeManager",receiver="L"+owner+";";
        String[] names={"m_44051_","m_44013_","m_44043_","m_44073_"};
        String[] desc={"()Ljava/util/Collection;","(Lnet/minecraft/world/item/crafting/RecipeType;)Ljava/util/List;","(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;","()Ljava/util/stream/Stream;"};
        var bootstrap=new Handle(H_INVOKESTATIC,Type.getInternalName(RecipeEnumerationFamilyCheck.class),"constant","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;",false);
        for(int i=0;i<names.length;i++) {
            var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"query"+i,"("+receiver+desc[i].substring(1),null,null);m.visitCode();m.visitVarInsn(ALOAD,0);if(i==1||i==2)m.visitVarInsn(ALOAD,1);m.visitMethodInsn(INVOKEVIRTUAL,owner,names[i],desc[i],false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
            var handle=new Handle(H_INVOKEVIRTUAL,owner,names[i],desc[i],false);
            for(boolean nested:List.of(false,true)){m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"handle"+i+nested,"()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();m.visitLdcInsn(nested?new ConstantDynamic("outer","Ljava/lang/invoke/MethodHandle;",bootstrap,new ConstantDynamic("inner","Ljava/lang/invoke/MethodHandle;",bootstrap,handle)):handle);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();}
        }
        w.visitEnd();return w.toByteArray();
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
