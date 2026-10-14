package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.*;
import com.fuckingdeveloper.lms.runtime.item.LegacyFuelItem;
import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.core.component.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.jar.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class FuelQueryFamilyCheck {
    private static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    public static void run() throws Exception {
        Items.COAL.builtInRegistryHolder().bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE,64).build());
        Items.LAVA_BUCKET.builtInRegistryHolder().bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE,1).build());
        Items.BUCKET.builtInRegistryHolder().bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE,16).build());
        var lava = new ItemStack(Items.LAVA_BUCKET);
        check(Forge1192CraftingRemainderBridge.has(lava),"native construction remainder absent");
        var bucket = Forge1192CraftingRemainderBridge.get(lava);
        check(bucket.is(Items.BUCKET) && bucket.getCount()==1,"lava remainder identity/count lost");
        bucket.setCount(5);
        check(Forge1192CraftingRemainderBridge.get(lava).getCount()==1 && lava.getCount()==1,"remainder query aliased result/input");
        var coal = new ItemStack(Items.COAL,3);
        check(!Forge1192CraftingRemainderBridge.has(coal) && Forge1192CraftingRemainderBridge.get(coal)==ItemStack.EMPTY,"absent source remainder fabricated");
        check(Forge1192FuelBridge.stackBurnTime(coal, RecipeType.SMELTING)==-1,"source default must not resolve target fuel");
        check(Forge1192FuelBridge.getBurnTime(coal, RecipeType.SMELTING)==1600,"coal baseline");
        check(Forge1192FuelBridge.getBurnTime(coal, RecipeType.SMOKING)==1600,"source fallback must not use native fast cooking context");
        check(Forge1192FuelBridge.getBurnTime(new ItemStack(Items.LAVA_BUCKET),null)==20000,"lava rule/nullable source recipe type");
        check(Forge1192FuelBridge.getBurnTime(new ItemStack(Items.STONE),null)==0,"nonfuel rule");
        var calls = new AtomicInteger(); var canceledCalls = new AtomicInteger();
        Consumer<LegacyFurnaceFuelBurnTimeEvent> first = e -> {
            check(e.getItemStack()==coal && e.getRecipeType()==RecipeType.SMOKING,"actual stack/type identity");
            check(e.getBurnTime()==1600,"materialized target fuel double applied");
            e.setBurnTime(-4); check(e.getBurnTime()==1600 && !e.isCanceled(),"negative override changed source state");
            calls.incrementAndGet(); e.setBurnTime(1700);
        };
        Consumer<LegacyFurnaceFuelBurnTimeEvent> skipped = e -> { throw new AssertionError("canceled source listener ran"); };
        Consumer<LegacyFurnaceFuelBurnTimeEvent> canceled = e -> { check(e.isCanceled(),"source override did not cancel"); canceledCalls.incrementAndGet(); };
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH,false,LegacyFurnaceFuelBurnTimeEvent.class,first);
        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,LegacyFurnaceFuelBurnTimeEvent.class,skipped);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW,true,LegacyFurnaceFuelBurnTimeEvent.class,canceled);
        try {
            check(Forge1192FuelBridge.getBurnTime(ItemStack.EMPTY,null)==0 && calls.get()==0,"empty stack posted event");
            check(Forge1192FuelBridge.getBurnTime(coal,RecipeType.SMOKING)==1700,"override not returned");
            check(Forge1192FuelBridge.getBurnTime(coal,RecipeType.SMOKING)==1700 && calls.get()==2 && canceledCalls.get()==2,"events cached or duplicated");
        } finally { NeoForge.EVENT_BUS.unregister(first); NeoForge.EVENT_BUS.unregister(skipped); NeoForge.EVENT_BUS.unregister(canceled); }
        String stack="Lnet/minecraft/world/item/ItemStack;",type="Lnet/minecraft/world/item/crafting/RecipeType;",desc="("+stack+type+")I";
        var node=new ClassNode();node.name="fixture/FuelQueries";node.superName="java/lang/Object";node.version=Opcodes.V17;node.access=Opcodes.ACC_PUBLIC;
        var query=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"query",desc,null,null);
        query.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));query.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
        query.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"net/minecraftforge/common/ForgeHooks","getBurnTime",desc,false));
        query.instructions.add(new InsnNode(Opcodes.IRETURN));node.methods.add(query);
        var handle=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"handle","()Ljava/lang/invoke/MethodHandle;",null,null);
        handle.instructions.add(new LdcInsnNode(new Handle(Opcodes.H_INVOKESTATIC,"net/minecraftforge/common/ForgeHooks","getBurnTime",desc,false)));
        handle.instructions.add(new InsnNode(Opcodes.ARETURN));node.methods.add(handle);
        for (boolean has : new boolean[]{true,false}) {
            var remainder = new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,has?"hasRemainder":"remainder","("+stack+")"+(has?"Z":stack),null,null);
            remainder.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
            remainder.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"net/minecraft/world/item/ItemStack",has?"hasCraftingRemainingItem":"getCraftingRemainingItem","()"+(has?"Z":stack),false));
            remainder.instructions.add(new InsnNode(has?Opcodes.IRETURN:Opcodes.ARETURN));node.methods.add(remainder);
        }
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);var path=Files.createTempFile("lms-source-fuel", ".jar");
        var sourceItem=new ClassNode();sourceItem.name="fixture/SourceResourceItem";sourceItem.superName="net/minecraft/world/item/Item";
        sourceItem.version=Opcodes.V17;sourceItem.access=Opcodes.ACC_PUBLIC|Opcodes.ACC_ABSTRACT;
        var fuelOverride=new MethodNode(Opcodes.ACC_PUBLIC,"getBurnTime",desc,null,null);
        fuelOverride.instructions.add(new IntInsnNode(Opcodes.SIPUSH,2500));fuelOverride.instructions.add(new InsnNode(Opcodes.IRETURN));sourceItem.methods.add(fuelOverride);
        var predicate=new MethodNode(Opcodes.ACC_PUBLIC,"hasCraftingRemainingItem","("+stack+")Z",null,null);
        predicate.instructions.add(new InsnNode(Opcodes.ICONST_1));predicate.instructions.add(new InsnNode(Opcodes.IRETURN));sourceItem.methods.add(predicate);
        var result=new MethodNode(Opcodes.ACC_PUBLIC,"getCraftingRemainingItem","("+stack+")"+stack,null,null);
        result.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));result.instructions.add(new InsnNode(Opcodes.ARETURN));sourceItem.methods.add(result);
        var itemWriter=new ClassWriter(ClassWriter.COMPUTE_MAXS);sourceItem.accept(itemWriter);
        try(var jar=new JarOutputStream(Files.newOutputStream(path))) {
            jar.putNextEntry(new JarEntry(node.name+".class"));jar.write(writer.toByteArray());jar.closeEntry();
            jar.putNextEntry(new JarEntry(sourceItem.name+".class"));jar.write(itemWriter.toByteArray());jar.closeEntry();
        }
        byte[] original=Files.readAllBytes(path);
        try(var loader=new ManagedLegacyClassLoader(path,FuelQueryFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var clazz=loader.linkOwnedClass(node.name.replace('/','.'));
            var sourceClass=loader.linkOwnedClass(sourceItem.name.replace('/','.'));
            check(LegacyFuelItem.class.isAssignableFrom(sourceClass) && com.fuckingdeveloper.lms.runtime.item.LegacyCraftingItem.class.isAssignableFrom(sourceClass),"source override view missing");
            check(sourceClass.getMethod("lms$burnTime",ItemStack.class,RecipeType.class).getReturnType()==int.class,"fuel view descriptor failed verifier");
            check(sourceClass.getMethod("lms$craftingRemainder",ItemStack.class).getReturnType()==ItemStack.class,"source mutable result view lost");
            check((int)clazz.getMethod("query",ItemStack.class,RecipeType.class).invoke(null,coal,RecipeType.SMELTING)==1600,"source static query not bridged");
            check((boolean)clazz.getMethod("hasRemainder",ItemStack.class).invoke(null,lava),"source predicate call not adapted");
            check(((ItemStack)clazz.getMethod("remainder",ItemStack.class).invoke(null,lava)).is(Items.BUCKET),"source result call not adapted");
            var getter=(java.lang.invoke.MethodHandle)clazz.getMethod("handle").invoke(null);
            try { check((int)getter.invoke(coal,RecipeType.SMOKING)==1600,"source handle not bridged"); }
            catch(Throwable failure){throw new IllegalStateException("source fuel handle failed",failure);}
        }
        check(java.util.Arrays.equals(original,Files.readAllBytes(path)),"source artifact changed");
        System.out.println("PASS: source fuel defaults, coal/lava/nonfuel, recipe context, event stack/type identity, negative override/cancellation/priority and per-query dispatch; transformed calls/handles and source override view linkage; lava bucket fresh result/absence/source calls; native furnace gameplay not inferred");
    }
}
