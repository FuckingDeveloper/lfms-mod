package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192LifecycleBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.recipe.*;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192ItemBridge;
import com.fuckingdeveloper.lms.target.neoforge263.Target263SourceIngredients;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforgespi.language.IModInfo;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

/** Source binary identity, native final-class composition and genuine applied snapshot guards. */
public final class SourceIngredientFamilyCheck {
    private static int calls;
    public static boolean accepts(ItemStack input,ItemStack expected){
        check(Forge1192LifecycleBridge.requireActive().modId().equals("ingredient_fixture"),"source predicate owner");calls++;
        return input!=null&&input.is(expected.getItem())&&input.getCount()==expected.getCount();
    }
    static void run() throws Exception {
        check(java.lang.reflect.Modifier.isFinal(Ingredient.class.getModifiers()),"native Ingredient final flag removed");
        var path=Files.createTempFile("lms-source-ingredient", ".jar");
        try {
            try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry("probe/IngredientProbe.class"));jar.write(fixture());jar.closeEntry();}
            byte[] original=Files.readAllBytes(path);
            try(var loader=new ManagedLegacyClassLoader(path,SourceIngredientFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var source=loader.linkOwnedClass("probe.IngredientProbe");
                var bus=BusBuilder.builder().build();bus.start();
                var info=(IModInfo)java.lang.reflect.Proxy.newProxyInstance(SourceIngredientFamilyCheck.class.getClassLoader(),new Class[]{IModInfo.class},(p,m,a)->{if(m.getName().equals("getModId"))return "ingredient_fixture";throw new AssertionError(m);});
                var host=new ModContainer(info){public IEventBus getEventBus(){return bus;}};
                var stack=new ItemStack(Items.STONE,4);Forge1192ItemBridge.getOrCreateTag(stack).putInt("probe",91);
                LegacyIngredient ingredient;
                try(var scope=Forge1192LifecycleBridge.enter("ingredient_fixture",bus,host,loader)){ingredient=(LegacyIngredient)source.getConstructor(ItemStack.class).newInstance(stack);}
                check(ingredient.getItems()[0]==stack,"source constructor/ItemValue/super array lost stack identity");
                var before=Thread.currentThread().getContextClassLoader();
                var nativeView=Target263SourceIngredients.unwrap(ingredient);
                check(!nativeView.isSimple()&&nativeView.test(stack)&&!nativeView.test(new ItemStack(Items.STONE,1))&&calls==2,"native custom predicate/isSimple/once-only dispatch");
                check(Thread.currentThread().getContextClassLoader()==before&&Forge1192LifecycleBridge.currentContext().isEmpty(),"native predicate scope leaked");
                check(Target263SourceIngredients.wrap(nativeView)==ingredient&&Target263SourceIngredients.unwrap(ingredient)==nativeView,"source/native identity not stable");
                check(nativeView.items().toList().equals(List.of(Items.STONE.builtInRegistryHolder())),"native enumeration changed source item");
                var display=(SlotDisplay.Composite)nativeView.display();var snapshot=((SlotDisplay.ItemStackSlotDisplay)display.contents().getFirst()).stack().create();
                check(snapshot.getCount()==4&&Forge1192ItemBridge.getTag(snapshot).getIntOr("probe",-1)==91,"native display lost source count/NBT");snapshot.setCount(1);check(stack.getCount()==4,"display aliases original source stack");
                fails(()->nativeView.getCustomIngredient().getType(),"unproven source native serializer");
                try{ingredient.toJson();throw new AssertionError("source JSON override dropped");}catch(UnsupportedOperationException expected){check(expected.getMessage().equals("source-json-marker"),"source JSON failure changed");}
                fails(()->{try{source.getMethod("clear").invoke(ingredient);}catch(java.lang.reflect.InvocationTargetException failure){if(failure.getCause() instanceof RuntimeException runtime)throw runtime;throw new AssertionError(failure);}catch(ReflectiveOperationException failure){throw new AssertionError(failure);}},"source invalidation outside reload");
                LegacyIngredient.lms$nativeReload();fails(nativeView::isEmpty,"stale native membership cache");
                check(ingredient.getItems()[0]==stack&&source.getField("invalidations").getInt(ingredient)==1,"native reload did not deliver source invalidation/super callback once");
                var refreshed=Target263SourceIngredients.unwrap(ingredient);check(refreshed!=nativeView&&refreshed.test(stack),"native view not recreated after reload");
                stack.setCount(3);fails(refreshed::display,"source mutation silently reused native snapshot");stack.setCount(4);
                var factory=(java.lang.invoke.MethodHandle)source.getMethod("factoryHandle").invoke(null);
                LegacyIngredient normal;try{normal=(LegacyIngredient)factory.invokeExact(new ItemStack[]{stack});}catch(Throwable failure){throw new AssertionError(failure);}
                check(normal.getItems()[0]==stack&&normal.test(new ItemStack(Items.STONE))&&normal.isVanilla(),"source factory handle/default item predicate");
                check(source.getMethod("empty").invoke(null)==LegacyIngredient.EMPTY&&Target263SourceIngredients.optional(LegacyIngredient.EMPTY).isEmpty(),"source EMPTY/optional absence identity");
                fails(()->Target263SourceIngredients.unwrap(LegacyIngredient.EMPTY),"required slot accepted empty");
                var ordinary=Ingredient.of(Items.STONE);var view=Target263SourceIngredients.wrap(ordinary);
                check(Target263SourceIngredients.wrap(ordinary)==view&&Target263SourceIngredients.unwrap(view)==ordinary,"native-backed source wrapper identity");
                var nativeStacks=view.getItems();check(nativeStacks==view.getItems(),"native-backed source array cache identity");
                nativeStacks[0].setCount(2);fails(ordinary::isEmpty,"native-backed source view mutation silently ignored");nativeStacks[0].setCount(1);
                var recipe=new Forge1192UpgradeRecipe(Identifier.parse("ingredient_fixture:optional"),view,LegacyIngredient.EMPTY,new ItemStack(Items.STONE));
                check(recipe.matches(new net.minecraft.world.item.crafting.SmithingRecipeInput(ItemStack.EMPTY,new ItemStack(Items.STONE),ItemStack.EMPTY),null)&&recipe.placementInfo().ingredients().size()==1,"native optional smithing slot not absent");
                var components=net.minecraft.core.component.DataComponentPatch.builder().set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("native-display-probe")).build();
                var componentIngredient=new net.neoforged.neoforge.common.crafting.DataComponentIngredient(net.minecraft.core.HolderSet.direct(Items.STONE.builtInRegistryHolder()),components,false).toVanilla();
                var componentView=Target263SourceIngredients.wrap(componentIngredient);
                var componentStacks=componentView.getItems();
                check(componentStacks.length==1&&componentStacks[0].is(Items.STONE)&&componentStacks[0].getHoverName().getString().equals("native-display-probe"),"general custom display lost native component data");
                check(!componentView.test(new ItemStack(Items.STONE))&&componentView.test(componentStacks[0]),"display adaptation changed native custom predicate");
                check(Target263SourceIngredients.unwrap(componentView)==componentIngredient&&componentView.getItems()==componentStacks,"custom native identity/cache changed");
                var compound=net.neoforged.neoforge.common.crafting.CompoundIngredient.of(componentIngredient,Ingredient.of(Items.STONE));
                var compoundView=Target263SourceIngredients.wrap(compound);var compoundStacks=compoundView.getItems();
                check(compoundStacks.length==2&&compoundStacks[0].getHoverName().getString().equals("native-display-probe")&&compoundStacks[1].is(Items.STONE),"nested native custom displays lost order or components");
                componentStacks[0].setCount(2);fails(componentIngredient::isEmpty,"native custom source-array mutation ignored");componentStacks[0].setCount(1);
                var oldArray=compoundStacks;LegacyIngredient.lms$nativeReload();
                check(compoundView.getItems()!=oldArray&&compoundView.getItems()[0].getHoverName().getString().equals("native-display-probe"),"native custom display not refreshed after reload");
                var parsed=LegacyIngredient.fromJson(JsonParser.parseString("{\"item\":\"minecraft:stone\"}"));check(parsed.getItems()[0].is(Items.STONE)&&parsed.toJson().getAsJsonObject().get("item").getAsString().equals("minecraft:stone"),"source vanilla JSON identity");
                try{LegacyIngredient.fromJson(new JsonArray());throw new AssertionError("source empty JSON array accepted");}catch(JsonSyntaxException expected){}
                try{LegacyIngredient.fromJson(JsonParser.parseString("{\"item\":\"minecraft:air\"}"));throw new AssertionError("source JSON air accepted");}catch(JsonSyntaxException expected){}
                var missing=LegacyIngredient.of(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,Identifier.parse("ingredient_fixture:absent")));
                check(missing.getItems()[0].is(Items.BARRIER)&&missing.getItems()[0].getHoverName().getString().equals("Empty Tag: ingredient_fixture:absent"),"source Forge tag diagnostic lost");
                var nativeMissing=Target263SourceIngredients.unwrap(missing);check(nativeMissing.isEmpty()&&!nativeMissing.test(missing.getItems()[0]),"source diagnostic became native accepted tag member");
                check(Arrays.equals(original,Files.readAllBytes(path)),"original source ingredient JAR changed");
                Forge1192LifecycleBridge.release("ingredient_fixture",loader);
            }
        }finally{Files.deleteIfExists(path);}
        System.out.println("PASS: immutable source Ingredient inheritance/ItemValue/super/factory handles/EMPTY, native final composition/predicate owner/item snapshot/display NBT, exact native wrapper identity, optional smithing, native reload callback/stale and mutated cache rejection, source JSON/diagnostics and explicit serialization boundary");
    }
    private static byte[] fixture(){
        String root="net/minecraft/world/item/crafting/Ingredient",name="probe/IngredientProbe",stack="Lnet/minecraft/world/item/ItemStack;";
        var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);w.visit(V17,ACC_PUBLIC,name,null,root,null);w.visitField(ACC_PRIVATE,"stack",stack,null,null).visitEnd();w.visitField(ACC_PUBLIC,"invalidations","I",null,null).visitEnd();
        var m=w.visitMethod(ACC_PUBLIC,"<init>","("+stack+")V",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitTypeInsn(NEW,root+"$ItemValue");m.visitInsn(DUP);m.visitVarInsn(ALOAD,1);m.visitMethodInsn(INVOKESPECIAL,root+"$ItemValue","<init>","("+stack+")V",false);m.visitMethodInsn(INVOKESTATIC,"java/util/stream/Stream","of","(Ljava/lang/Object;)Ljava/util/stream/Stream;",true);m.visitMethodInsn(INVOKESPECIAL,root,"<init>","(Ljava/util/stream/Stream;)V",false);m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);m.visitFieldInsn(PUTFIELD,name,"stack",stack);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC,"test","("+stack+")Z",null,null);m.visitCode();m.visitVarInsn(ALOAD,1);m.visitVarInsn(ALOAD,0);m.visitFieldInsn(GETFIELD,name,"stack",stack);m.visitMethodInsn(INVOKESTATIC,"com/fuckingdeveloper/lms/check/SourceIngredientFamilyCheck","accepts","("+stack+stack+")Z",false);m.visitInsn(IRETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC,"isSimple","()Z",null,null);m.visitCode();m.visitInsn(ICONST_0);m.visitInsn(IRETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC,"m_43908_","()["+stack,null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitMethodInsn(INVOKESPECIAL,root,"m_43908_","()["+stack,false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PROTECTED,"invalidate","()V",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitInsn(DUP);m.visitFieldInsn(GETFIELD,name,"invalidations","I");m.visitInsn(ICONST_1);m.visitInsn(IADD);m.visitFieldInsn(PUTFIELD,name,"invalidations","I");m.visitVarInsn(ALOAD,0);m.visitMethodInsn(INVOKESPECIAL,root,"invalidate","()V",false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC,"clear","()V",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitMethodInsn(INVOKEVIRTUAL,name,"invalidate","()V",false);m.visitInsn(RETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC,"m_43942_","()Lcom/google/gson/JsonElement;",null,null);m.visitCode();m.visitTypeInsn(NEW,"java/lang/UnsupportedOperationException");m.visitInsn(DUP);m.visitLdcInsn("source-json-marker");m.visitMethodInsn(INVOKESPECIAL,"java/lang/UnsupportedOperationException","<init>","(Ljava/lang/String;)V",false);m.visitInsn(ATHROW);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"factoryHandle","()Ljava/lang/invoke/MethodHandle;",null,null);m.visitCode();m.visitLdcInsn(new Handle(H_INVOKESTATIC,root,"m_43927_","(["+stack+")L"+root+";",false));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m=w.visitMethod(ACC_PUBLIC|ACC_STATIC,"empty","()L"+root+";",null,null);m.visitCode();m.visitFieldInsn(GETSTATIC,root,"f_43901_","L"+root+";");m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();w.visitEnd();return w.toByteArray();
    }
    private static void fails(Runnable operation,String message){try{operation.run();throw new AssertionError(message);}catch(UnsupportedOperationException expected){}}
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
