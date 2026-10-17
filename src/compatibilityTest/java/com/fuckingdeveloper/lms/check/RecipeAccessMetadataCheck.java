package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.*;
import java.lang.invoke.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.entity.EntityType;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

public final class RecipeAccessMetadataCheck {
    private static final String ITEM="net/minecraft/world/item/Item", STACK="Lnet/minecraft/world/item/ItemStack;";
    private static final String DESC="("+STACK+")Ljava/lang/String;";
    public static Object constant(MethodHandles.Lookup lookup,String name,Class<?> type,Object value){return value;}
    public static void run() throws Exception {
        NativeDefaultComponentFixture.run(() -> { try { checks(); } catch (Exception failure) { throw new AssertionError(failure); } });
    }
    private static void checks() throws Exception {
        var stone=new ItemStack(Items.STONE);
        check(Forge1192CreatorMetadataBridge.creatorModId(Items.COAL,stone).equals("minecraft"),"default must inspect supplied stack");
        var potion=Forge1192EarlyItemStackBridge.create(Items.POTION);
        Forge1192PotionBridge.setPotion(potion, Potions.HEALING.value());
        check(Forge1192CreatorMetadataBridge.defaultCreatorModId(potion).equals("minecraft"),"typed potion creator");
        var egg=Forge1192EarlyItemStackBridge.create(Items.PIG_SPAWN_EGG);
        egg.set(DataComponents.ENTITY_DATA,TypedEntityData.<EntityType<?>>of(net.minecraft.world.entity.EntityTypes.COW, new net.minecraft.nbt.CompoundTag()));
        check(Forge1192CreatorMetadataBridge.defaultCreatorModId(egg).equals("minecraft"),"source egg default type");
        var path=Files.createTempFile("source-recipe-metadata-",".jar");
        try {
            var item=new ClassNode(); item.version=V17;item.access=ACC_PUBLIC;item.name="fixture/CreatorItem";item.superName=ITEM;
            var ctor=new MethodNode(ACC_PUBLIC,"<init>","(L"+ITEM+"$Properties;)V",null,null);
            ctor.instructions.add(new VarInsnNode(ALOAD,0));ctor.instructions.add(new VarInsnNode(ALOAD,1));
            ctor.instructions.add(new MethodInsnNode(INVOKESPECIAL,ITEM,"<init>",ctor.desc,false));ctor.instructions.add(new InsnNode(RETURN));item.methods.add(ctor);
            var override=new MethodNode(ACC_PUBLIC,"getCreatorModId",DESC,null,null);
            override.instructions.add(new VarInsnNode(ALOAD,1));override.instructions.add(new FieldInsnNode(PUTSTATIC,item.name,"last",STACK));
            override.instructions.add(new LdcInsnNode("source-override"));override.instructions.add(new InsnNode(ARETURN));item.methods.add(override);
            item.fields.add(new FieldNode(ACC_PUBLIC|ACC_STATIC,"last",STACK,null,null));
            var base=new MethodNode(ACC_PUBLIC,"base",DESC,null,null);base.instructions.add(new VarInsnNode(ALOAD,0));base.instructions.add(new VarInsnNode(ALOAD,1));
            base.instructions.add(new MethodInsnNode(INVOKESPECIAL,ITEM,"getCreatorModId",DESC,false));base.instructions.add(new InsnNode(ARETURN));item.methods.add(base);
            var queries=new ClassNode();queries.version=V17;queries.access=ACC_PUBLIC;queries.name="fixture/CreatorQueries";queries.superName="java/lang/Object";
            for(String owner:List.of(ITEM,item.name,"net/minecraftforge/common/extensions/IForgeItem")) {
                int index=queries.methods.size();
                var call=new MethodNode(ACC_PUBLIC|ACC_STATIC,"query"+index,"(L"+owner+";"+STACK+")Ljava/lang/String;",null,null);
                call.instructions.add(new VarInsnNode(ALOAD,0));call.instructions.add(new VarInsnNode(ALOAD,1));
                boolean itf=owner.contains("IForgeItem");
                call.instructions.add(new MethodInsnNode(itf?INVOKEINTERFACE:INVOKEVIRTUAL,owner,"getCreatorModId",DESC,itf));call.instructions.add(new InsnNode(ARETURN));queries.methods.add(call);
                Handle handle=new Handle(itf?H_INVOKEINTERFACE:H_INVOKEVIRTUAL,owner,"getCreatorModId",DESC,itf);
                for(boolean nested:List.of(false,true)) {
                    var m=new MethodNode(ACC_PUBLIC|ACC_STATIC,"handle"+index+nested,"()Ljava/lang/invoke/MethodHandle;",null,null);
                    var bootstrap=new Handle(H_INVOKESTATIC,Type.getInternalName(RecipeAccessMetadataCheck.class),"constant","(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;",false);
                    m.instructions.add(new LdcInsnNode(nested?new ConstantDynamic("outer","Ljava/lang/invoke/MethodHandle;",bootstrap,new ConstantDynamic("inner","Ljava/lang/invoke/MethodHandle;",bootstrap,handle)):handle));m.instructions.add(new InsnNode(ARETURN));queries.methods.add(m);
                }
            }
            try(var jar=new JarOutputStream(Files.newOutputStream(path))) {for(var n:List.of(item,queries)){var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);n.accept(w);jar.putNextEntry(new JarEntry(n.name+".class"));jar.write(w.toByteArray());jar.closeEntry();}}
            var original=Files.readAllBytes(path);
            try(var loader=new ManagedLegacyClassLoader(path,RecipeAccessMetadataCheck.class.getClassLoader(),new Forge1192LegacyClassTransformer())) {
                var type=loader.linkOwnedClass("fixture.CreatorItem");var q=loader.linkOwnedClass("fixture.CreatorQueries");
                // Registry is frozen in this isolated probe; construction is not under test.
                var unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);
                var unsafe=(sun.misc.Unsafe)unsafeField.get(null);
                var value=(Item)unsafe.allocateInstance(type);
                check(Forge1192CreatorMetadataBridge.creatorModId(value,stone).equals("source-override"),"override erased");
                check(value.getCreatorModId(null,stone).equals("source-override"),"native callback erased source override");
                check(type.getField("last").get(null)==stone,"callback stack identity");
                check(type.getMethod("base",ItemStack.class).invoke(value,stone).equals("minecraft"),"explicit source base redispatched");
                for(var m:q.getMethods()) {
                    if(m.getName().startsWith("query"))check(m.invoke(null,value,stone).equals("source-override"),"source direct/inherited/interface dispatch");
                    if(m.getName().startsWith("handle"))try {
                        var h=(MethodHandle)m.invoke(null);check(h.invokeWithArguments(value,stone).equals("source-override"),"source callable dispatch");
                    }catch(Throwable failure){throw new AssertionError(failure);}
                }
            }
            check(Arrays.equals(original,Files.readAllBytes(path)),"immutable source input changed");
        } finally {Files.deleteIfExists(path);}
        bookMetadata();
        recipeShapes();
        System.out.println("PASS: source creator default/override/base/native callback, original stack identity, inherited/interface calls and nested handles; recipe family exact owners, callable receiver types and unrelated-owner negative; remote full recipes remain unsupported");
    }
    private static void bookMetadata() throws Exception {
        var field=com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.Forge1192DynamicRegistryBridge.class.getDeclaredField("ACTIVE_ACCESS");field.setAccessible(true);
        @SuppressWarnings("unchecked") var context=(ThreadLocal<net.minecraft.core.RegistryAccess>)field.get(null);
        var previous=context.get();
        var registry=new net.minecraft.core.MappedRegistry<net.minecraft.world.item.enchantment.Enchantment>(Registries.ENCHANTMENT,com.mojang.serialization.Lifecycle.stable());
        var enchantment=new net.minecraft.world.item.enchantment.Enchantment(net.minecraft.network.chat.Component.literal("fixture"),
                net.minecraft.world.item.enchantment.Enchantment.definition(net.minecraft.core.HolderSet.empty(),10,5,
                        net.minecraft.world.item.enchantment.Enchantment.constantCost(1),net.minecraft.world.item.enchantment.Enchantment.constantCost(10),1,net.minecraft.world.entity.EquipmentSlotGroup.ANY),
                net.minecraft.core.HolderSet.empty(),net.minecraft.core.component.DataComponentMap.EMPTY);
        var holder=registry.register(ResourceKey.create(Registries.ENCHANTMENT,Identifier.parse("fixture:creator")),enchantment,net.minecraft.core.RegistrationInfo.BUILT_IN);
        registry.freeze();context.set(new net.minecraft.core.RegistryAccess.ImmutableRegistryAccess(List.of(registry)));
        try {
            var data=new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);data.set(holder,2);
            for(var item:List.of(Items.ENCHANTED_BOOK,Items.STONE)) {
                var stack=new ItemStack(item.builtInRegistryHolder(),1,new net.minecraft.core.component.PatchedDataComponentMap(net.minecraft.core.component.DataComponentMap.builder()
                        .set(DataComponents.MAX_STACK_SIZE,1).set(DataComponents.STORED_ENCHANTMENTS,data.toImmutable()).build()));
                check(Forge1192CreatorMetadataBridge.defaultCreatorModId(stack).equals(item==Items.ENCHANTED_BOOK?"fixture":"minecraft"),"source book identity vs arbitrary stored-enchantment component");
            }
        } finally {if(previous==null)context.remove();else context.set(previous);}
    }
    private static void recipeShapes() throws Exception {
        String result="Lnet/minecraft/world/item/crafting/RecipeManager;";
        for(String owner:List.of("net/minecraft/world/level/Level","net/minecraft/client/multiplayer/ClientLevel","net/minecraft/server/level/ServerLevel","net/minecraft/client/multiplayer/ClientPacketListener","net/minecraft/server/MinecraftServer","fixture/Unrelated")) {
            String name=owner.endsWith("ClientPacketListener")?"m_105141_":owner.endsWith("MinecraftServer")?"m_129894_":"m_7465_";
            var n=new ClassNode();n.version=V17;n.access=ACC_PUBLIC;n.name="fixture/Recipes";n.superName="java/lang/Object";
            var m=new MethodNode(ACC_PUBLIC|ACC_STATIC,"query","(L"+owner+";)"+result,null,null);m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,owner,name,"()"+result,false));m.instructions.add(new InsnNode(ARETURN));n.methods.add(m);
            var h=new MethodNode(ACC_PUBLIC|ACC_STATIC,"handle","()Ljava/lang/invoke/MethodHandle;",null,null);h.instructions.add(new LdcInsnNode(new Handle(H_INVOKEVIRTUAL,owner,name,"()"+result,false)));h.instructions.add(new InsnNode(ARETURN));n.methods.add(h);
            var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);n.accept(w);var out=new ClassNode();new ClassReader(new Forge1192LegacyClassTransformer().transform(n.name.replace('/','.'),w.toByteArray()).bytes()).accept(out,0);
            var call=(MethodInsnNode)out.methods.getFirst().instructions.get(1);
            boolean unrelated=owner.equals("fixture/Unrelated");
            check(unrelated?call.owner.equals(owner):call.owner.endsWith("Forge1192RecipeBridge"),"recipe owner proof: "+owner);
            var handle=(Handle)((LdcInsnNode)out.methods.get(1).instructions.getFirst()).cst;
            if(!unrelated)check(handle.getDesc().equals("(L"+owner+";)"+result),"callable receiver type changed: "+owner);
        }
        var override=new ClassNode();override.version=V17;override.access=ACC_PUBLIC|ACC_ABSTRACT;
        override.name="fixture/RecipeOverride";override.superName="net/minecraft/server/level/ServerLevel";
        override.methods.add(new MethodNode(ACC_PUBLIC|ACC_ABSTRACT,"m_7465_","()"+result,null,null));
        var writer=new ClassWriter(0);override.accept(writer);
        try {new Forge1192LegacyClassTransformer().transform(override.name.replace('/','.'),writer.toByteArray());throw new AssertionError("source recipe override bypassed");}
        catch(UnsupportedOperationException expected){check(expected.getMessage().contains("full-recipe accessor override"),"wrong override boundary");}
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
