package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.*;
import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import net.minecraft.world.item.*;
import net.minecraft.core.component.*;
import net.minecraft.network.chat.Component;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.jar.*;

public final class StackComparisonFamilyCheck {
    private static void check(boolean ok,String message) { if(!ok)throw new AssertionError(message); }
    public static void run() throws Exception {
        check(Forge1192StackComparisonBridge.hasDefaultSourceCapabilities(Items.GOLD_ORE),"ordinary source BlockItem default rejected");
        var shulkers=new java.util.ArrayList<Item>(Items.DYED_SHULKER_BOX.asList());shulkers.add(Items.SHULKER_BOX);
        check(shulkers.size()==17,"native shulker family differs from source proof");
        for(var item:shulkers)check(!Forge1192StackComparisonBridge.hasDefaultSourceCapabilities(item),"named source shulker provider assumed empty");
        Items.GOLD_ORE.builtInRegistryHolder().bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE,64).build());
        var ore=new ItemStack(Items.GOLD_ORE);
        check(Forge1192StackComparisonBridge.tagMatches(ore,ore.copy()),"ordinary block-item source equality rejected");
        var a=new ItemStack(Items.COAL,3);var b=new ItemStack(Items.COAL,1);
        check(Forge1192StackComparisonBridge.isSameItemSameTags(a,b),"count affected item/tag equality");
        check(!Forge1192StackComparisonBridge.matches(a,b),"full matches ignored count");
        check(Forge1192StackComparisonBridge.isSame(ItemStack.EMPTY,ItemStack.EMPTY),"empty reference identity");
        check(!Forge1192StackComparisonBridge.sameItem(a,ItemStack.EMPTY),"instance empty semantics");
        Forge1192ItemBridge.getOrCreateTag(a);
        check(!Forge1192StackComparisonBridge.tagMatches(a,b),"null and empty tags conflated");
        Forge1192ItemBridge.getOrCreateTag(b);
        check(Forge1192StackComparisonBridge.tagMatches(a,b),"equal empty tags rejected");
        Forge1192ItemBridge.getOrCreateTag(a).putInt("charge",2);
        check(!Forge1192StackComparisonBridge.tagMatches(a,b),"live tag mutation ignored");
        Forge1192ItemBridge.getOrCreateTag(b).putInt("charge",2);
        check(Forge1192StackComparisonBridge.tagMatches(a,b),"source NBT equality lost");
        var named=new ItemStack(Items.COAL);named.set(DataComponents.CUSTOM_NAME,Component.literal("source projection required"));
        try {Forge1192StackComparisonBridge.tagMatches(named,new ItemStack(Items.COAL));throw new AssertionError("unknown component projection silently accepted");}
        catch(UnsupportedOperationException expected) {}
        var lava=new ItemStack(Items.LAVA_BUCKET);
        try {Forge1192StackComparisonBridge.tagMatches(lava,lava.copy());throw new AssertionError("unknown source capabilities assumed empty");}
        catch(UnsupportedOperationException expected) {}
        String owner="net/minecraft/world/item/ItemStack",stack="L"+owner+";",desc="("+stack+stack+")Z";
        var node=new ClassNode();node.name="fixture/StackComparisons";node.superName="java/lang/Object";node.version=Opcodes.V17;node.access=Opcodes.ACC_PUBLIC;
        String[] symbols={"m_41658_","m_41728_","m_41746_","m_41758_","m_150942_","m_41656_","m_41726_"};
        for(int i=0;i<symbols.length;i++) {
            boolean instance=i>=5;var method=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"compare"+i,desc,null,null);
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));method.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
            method.instructions.add(new MethodInsnNode(instance?Opcodes.INVOKEVIRTUAL:Opcodes.INVOKESTATIC,owner,symbols[i],instance?"("+stack+")Z":desc,false));
            method.instructions.add(new InsnNode(Opcodes.IRETURN));node.methods.add(method);
            var handle=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"handle"+i,"()Ljava/lang/invoke/MethodHandle;",null,null);
            handle.instructions.add(new LdcInsnNode(new Handle(instance?Opcodes.H_INVOKEVIRTUAL:Opcodes.H_INVOKESTATIC,owner,symbols[i],instance?"("+stack+")Z":desc,false)));
            handle.instructions.add(new InsnNode(Opcodes.ARETURN));node.methods.add(handle);
        }
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);var path=Files.createTempFile("lms-stack-comparison", ".jar");
        try(var jar=new JarOutputStream(Files.newOutputStream(path))){jar.putNextEntry(new JarEntry(node.name+".class"));jar.write(writer.toByteArray());jar.closeEntry();
            for(boolean custom:new boolean[]{false,true}){String name=custom?"fixture/CustomSourceCapabilityItem":"fixture/DefaultSourceCapabilityItem";var item=new ClassWriter(0);item.visit(Opcodes.V17,Opcodes.ACC_PUBLIC|Opcodes.ACC_ABSTRACT,name,null,"net/minecraft/world/item/Item",null);
                if(custom){var method=item.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_ABSTRACT,"initCapabilities","(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraftforge/common/capabilities/ICapabilityProvider;",null,null);method.visitEnd();}item.visitEnd();jar.putNextEntry(new JarEntry(name+".class"));jar.write(item.toByteArray());jar.closeEntry();}
}
        var original=Files.readAllBytes(path);
        try(var loader=new ManagedLegacyClassLoader(path,StackComparisonFamilyCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
            var clazz=loader.linkOwnedClass(node.name.replace('/','.'));
            var defaults=loader.linkOwnedClass("fixture.DefaultSourceCapabilityItem").asSubclass(Item.class);
            var custom=loader.linkOwnedClass("fixture.CustomSourceCapabilityItem").asSubclass(Item.class);
            check(Forge1192StackComparisonBridge.hasDefaultSourceCapabilities(defaults),"proven source item defaults rejected");
            check(!Forge1192StackComparisonBridge.hasDefaultSourceCapabilities(custom),"custom source initializer ignored");
            for(int i=0;i<symbols.length;i++) {
                boolean expected=i!=1;
                check((boolean)clazz.getMethod("compare"+i,ItemStack.class,ItemStack.class).invoke(null,a,b)==expected,"source comparison call "+symbols[i]);
                var handle=(java.lang.invoke.MethodHandle)clazz.getMethod("handle"+i).invoke(null);
                try {check((boolean)handle.invoke(a,b)==expected,"source comparison handle "+symbols[i]);}
                catch(Throwable failure){throw new IllegalStateException("source comparison handle failed",failure);}
            }
        }
        check(java.util.Arrays.equals(original,Files.readAllBytes(path)),"source fixture mutated");
        System.out.println("PASS: seven source comparisons/callable handles, count/empty/reference/null-vs-empty/live NBT semantics, capability and component projection fail-fast boundaries; source capability-bearing stacks not inferred");
    }
}
