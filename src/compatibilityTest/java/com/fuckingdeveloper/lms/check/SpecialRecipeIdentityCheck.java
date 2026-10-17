package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

public final class SpecialRecipeIdentityCheck {
    private static final String BASE="net/minecraft/world/item/crafting/";
    private static final List<String> NAMES=List.of("TippedArrowRecipe","ShulkerBoxColoring","SuspiciousStewRecipe");
    public static void run() throws Exception {
        var path=Files.createTempFile("source-special-types-",".jar");
        var imbue=new ImbueRecipe(new Recipe.CommonInfo(true),new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC,"fixture"),
                Ingredient.of(Items.LINGERING_POTION),Ingredient.of(Items.ARROW),new ItemStackTemplate(Items.TIPPED_ARROW,8));
        check(!imbue.isSpecial(),"native data-driven recipe classified as old special recipe");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry("fixture/SpecialTypes.class"));jar.write(fixture());jar.closeEntry();}
            var original=Files.readAllBytes(path);
            try(var loader=new ManagedLegacyClassLoader(path,SpecialRecipeIdentityCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass("fixture.SpecialTypes");var identities=new HashSet<Class<?>>();
                for(String name:NAMES) {
                    var token=(Class<?>)type.getMethod(name).invoke(null);identities.add(token);
                    check(token.getSimpleName().equals(name) && token.getSuperclass()==CustomRecipe.class,"source nominal identity/ancestry");
                    check(!token.isInstance(imbue) && !(boolean)type.getMethod("is"+name,Object.class).invoke(null,imbue),"native recipe class was substituted");
                }
                check(identities.size()==3 && !identities.contains(ImbueRecipe.class) && !identities.contains(TransmuteRecipe.class) && !identities.contains(ShapelessRecipe.class),"removed source kinds collapsed");
                check(type.getField("typed").getGenericType().getTypeName().contains("recipe.identity.TippedArrowRecipe"),"generic source token projection");
            }
            check(Arrays.equals(original,Files.readAllBytes(path)),"source input changed");
            for(String name:NAMES)for(int shape=0;shape<3;shape++)reject(name,shape);
        } finally {Files.deleteIfExists(path);}
        var jei=Path.of("legacy-mods/jei-1.19.2-forge-11.8.1.1032.jar");
        if(Files.isRegularFile(jei))try(var loader=new ManagedLegacyClassLoader(jei,SpecialRecipeIdentityCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var stackHelper=loader.linkOwnedClass("mezz.jei.api.helpers.IStackHelper");
            var method=loader.linkOwnedClass("mezz.jei.library.plugins.vanilla.VanillaPlugin").getDeclaredMethod("replaceSpecialCraftingRecipes",List.class,stackHelper);method.setAccessible(true);
            check(((List<?>)method.invoke(null,List.of(imbue),null)).isEmpty(),"original class-key replacement selected new ordinary recipe");
        }
        System.out.println("PASS: three distinct source nominal class/generic/instanceof identities, actual native ImbueRecipe identity/non-special classification, constructor/subclass/nested-constructor negatives and original JEI exact-class selection");
    }
    private static byte[] fixture() {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,"fixture/SpecialTypes",null,"java/lang/Object",null);
        w.visitField(ACC_PUBLIC,"typed","Ljava/util/List;","Ljava/util/List<L"+BASE+"TippedArrowRecipe;>;",null).visitEnd();
        for(String name:NAMES){var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,name,"()Ljava/lang/Class;",null,null);m.visitCode();m.visitLdcInsn(Type.getObjectType(BASE+name));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
            m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"is"+name,"(Ljava/lang/Object;)Z",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitTypeInsn(INSTANCEOF,BASE+name);m.visitInsn(IRETURN);m.visitMaxs(0,0);m.visitEnd();}
        w.visitEnd();return w.toByteArray();
    }
    private static void reject(String name,int shape)throws Exception {
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC|ACC_ABSTRACT,"fixture/UnsupportedSpecial",null,shape==1?BASE+name:"java/lang/Object",null);
        if(shape!=1){var m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"bad","()V",null,null);m.visitCode();
            if(shape==0){m.visitTypeInsn(NEW,BASE+name);m.visitInsn(POP);}
            else{m.visitLdcInsn(new ConstantDynamic("constructor","Ljava/lang/invoke/MethodHandle;",new Handle(H_INVOKESTATIC,"fixture/Bootstrap","value","()V",false),new Handle(H_NEWINVOKESPECIAL,BASE+name,"<init>","(Lnet/minecraft/resources/ResourceLocation;)V",false)));m.visitInsn(POP);}
            m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();}
        w.visitEnd();try{new Forge1192LegacyClassTransformer().transform("fixture.UnsupportedSpecial",w.toByteArray());throw new AssertionError("unproven special-recipe execution accepted");}
        catch(UnsupportedOperationException expected){check(expected.getMessage().contains("nominal type identity only"),"wrong unsupported boundary");}
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
