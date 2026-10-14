package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.recipe.Forge1192UpgradeRecipe;
import com.fuckingdeveloper.lms.runtime.Forge1192ItemBridge;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.mojang.serialization.JsonOps;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class SmithingFamilyCheck {
    public static void run() throws Exception {
        var id = Identifier.fromNamespaceAndPath("fixture", "upgrade");
        var ingredient = Ingredient.of(Items.STONE);
        var result = new ItemStack(Items.STONE, 3);
        var recipe = new Forge1192UpgradeRecipe(id, ingredient, ingredient, result);
        check(recipe.m_8043_() == result && recipe.m_6423_().equals(id), "source result/id identity lost");
        var resultTag = new CompoundTag(); resultTag.putInt("result", 8); Forge1192ItemBridge.setTag(result, resultTag);
        var base = new ItemStack(Items.STONE); var addition = new ItemStack(Items.STONE);
        var input = new SmithingRecipeInput(ItemStack.EMPTY, base, addition);
        check(recipe.matches(input, null), "source two-slot recipe does not match native input");
        check(!recipe.matches(new SmithingRecipeInput(base, base, addition), null), "native template incorrectly accepted");
        var absent = recipe.assemble(input);
        check(Forge1192ItemBridge.getTag(absent).getIntOr("result", -1) == 8, "absent base NBT discarded result NBT");
        var baseTag = new CompoundTag(); baseTag.putInt("charge", 40); Forge1192ItemBridge.setTag(base, baseTag);
        var output = recipe.assemble(input);
        var outputTag = Forge1192ItemBridge.getTag(output);
        check(output != result && output.getCount() == 3 && outputTag != baseTag && outputTag.getIntOr("charge", -1) == 40 && !outputTag.contains("result"), "source copied replacement NBT semantics lost");
        outputTag.putInt("charge", 20);
        check(baseTag.getIntOr("charge", -1) == 40 && resultTag.getIntOr("result", -1) == 8, "assembly aliases input/result NBT");
        check(recipe.lms$legacyIngredients().isEmpty() && recipe.placementInfo().ingredients().size() == 2, "source/native ingredient views confused");
        check(recipe.m_8004_(2, 1) && !recipe.m_8004_(1, 1) && recipe.m_44535_(addition), "source sibling queries lost");
        var override = new Forge1192UpgradeRecipe(id, ingredient, ingredient, result) {
            @Override public ItemStack m_5874_(net.minecraft.world.Container container) { check(container.getItem(0) == base && container.getItem(1) == addition, "source slot mapping lost"); return result; }
        };
        check(override.m_5874_(new net.minecraft.world.SimpleContainer(base, addition)) == result, "direct source callback lost actual supplied Container");
        try { override.assemble(input); throw new AssertionError("native immutable input pretended to preserve mutable source subclass Container"); } catch (UnsupportedOperationException expected) {}
        try { override.getSerializer(); throw new AssertionError("subclass serialization silently discarded callbacks"); } catch (UnsupportedOperationException expected) {}
        try { recipe.m_7707_(); throw new AssertionError("native codec pretended to implement source wire protocol"); } catch (UnsupportedOperationException expected) {}
        var lookup = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var ops = lookup.createSerializationContext(JsonOps.INSTANCE);
        var json = Forge1192UpgradeRecipe.CODEC.codec().encodeStart(ops, recipe).getOrThrow();
        var decoded = Forge1192UpgradeRecipe.CODEC.codec().parse(ops, json).getOrThrow();
        check(decoded.m_6423_().equals(id) && decoded.matches(input, null) && decoded.assemble(input).getCount() == 3, "native JSON roundtrip lost source smithing behavior");
        // The smoke launcher does not run native mod registry-sync initialization.
        // Use actual isolated synced registries; never change global/frozen registry flags.
        var syncItems = new net.neoforged.neoforge.registries.RegistryBuilder<Item>(net.minecraft.core.registries.Registries.ITEM)
                .disableRegistrationCheck().sync(true).create();
        var syncStone = ((MappedRegistry<Item>) syncItems).register(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM,
                Identifier.withDefaultNamespace("stone")), Items.STONE, RegistrationInfo.BUILT_IN);
        syncStone.bindComponents(Items.STONE.builtInRegistryHolder().components());
        syncItems.freeze();
        var syncComponents = new net.neoforged.neoforge.registries.RegistryBuilder<net.minecraft.core.component.DataComponentType<?>>(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE)
                .disableRegistrationCheck().sync(true).create();
        for (var entry : BuiltInRegistries.DATA_COMPONENT_TYPE.entrySet()) ((MappedRegistry<net.minecraft.core.component.DataComponentType<?>>) syncComponents).register(entry.getKey(), entry.getValue(), RegistrationInfo.BUILT_IN);
        syncComponents.freeze();
        var networkLookup = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var isolatedRegistries = new ArrayList<Registry<?>>();
        networkLookup.registries().map(RegistryAccess.RegistryEntry::value)
                .filter(r -> !r.key().equals(syncItems.key()) && !r.key().equals(syncComponents.key())).forEach(isolatedRegistries::add);
        isolatedRegistries.add(syncItems); isolatedRegistries.add(syncComponents);
        var syncLookup = new RegistryAccess.ImmutableRegistryAccess(isolatedRegistries);
        var buffer = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), syncLookup,
                net.neoforged.neoforge.network.connection.ConnectionType.NEOFORGE);
        try {
            Forge1192UpgradeRecipe.STREAM_CODEC.encode(buffer, recipe);
            var network = Forge1192UpgradeRecipe.STREAM_CODEC.decode(buffer);
            check(network.m_6423_().equals(id) && Forge1192ItemBridge.getTag(network.m_8043_()).getIntOr("result", -1) == 8, "native stream roundtrip lost id/result data");
        } finally { buffer.release(); }
        var node = new ClassNode(); node.name = "fixture/SourceUpgradeConstructor"; node.superName = "java/lang/Object"; node.version = Opcodes.V17; node.access = Opcodes.ACC_PUBLIC;
        String source = "net/minecraft/world/item/crafting/UpgradeRecipe";
        String args = "(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/item/crafting/Ingredient;Lnet/minecraft/world/item/crafting/Ingredient;Lnet/minecraft/world/item/ItemStack;)";
        var method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "create", args + "L" + source + ";", null, null);
        method.instructions.add(new TypeInsnNode(Opcodes.NEW, source)); method.instructions.add(new InsnNode(Opcodes.DUP));
        for (int i=0;i<4;i++) method.instructions.add(new VarInsnNode(Opcodes.ALOAD,i));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, source, "<init>", args + "V", false)); method.instructions.add(new InsnNode(Opcodes.ARETURN)); node.methods.add(method);
        var idRead = new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC, "sourceId",
                "(Lnet/minecraft/world/item/crafting/Recipe;)Lnet/minecraft/resources/ResourceLocation;",null,null);
        idRead.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
        idRead.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE,"net/minecraft/world/item/crafting/Recipe","m_6423_","()Lnet/minecraft/resources/ResourceLocation;",true));
        idRead.instructions.add(new InsnNode(Opcodes.ARETURN));node.methods.add(idRead);
        var idHandle = new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"idHandle","()Ljava/lang/invoke/MethodHandle;",null,null);
        idHandle.instructions.add(new LdcInsnNode(new Handle(Opcodes.H_INVOKEINTERFACE,"net/minecraft/world/item/crafting/Recipe","m_6423_","()Lnet/minecraft/resources/ResourceLocation;",true)));
        idHandle.instructions.add(new InsnNode(Opcodes.ARETURN));node.methods.add(idHandle);
        var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); node.accept(writer);
        var path = Files.createTempFile("lms-source-upgrade", ".jar");
        try (var jar = new JarOutputStream(Files.newOutputStream(path))) { jar.putNextEntry(new JarEntry(node.name+".class")); jar.write(writer.toByteArray()); jar.closeEntry(); }
        byte[] immutable = Files.readAllBytes(path);
        try (var loader = new ManagedLegacyClassLoader(path, SmithingFamilyCheck.class.getClassLoader(), new Forge1192LegacyClassTransformer())) {
            var type = loader.linkOwnedClass(node.name.replace('/','.'));
            var adapted = (Forge1192UpgradeRecipe)type.getMethod("create", Identifier.class, Ingredient.class, Ingredient.class, ItemStack.class).invoke(null,id,ingredient,ingredient,result);
            check(adapted.m_8043_() == result && adapted.matches(input,null), "transformed source allocation does not implement actual native contract");
            check(type.getMethod("sourceId",Recipe.class).invoke(null,adapted).equals(id),"source Recipe interface identifier call not bridged");
            var idGetter=(java.lang.invoke.MethodHandle)type.getMethod("idHandle").invoke(null);
            try {check(idGetter.invoke(adapted).equals(id),"source Recipe identity handle lost receiver/result");}
            catch(Throwable failure){throw new IllegalStateException("Recipe identifier handle failed",failure);}
        }
        check(Arrays.equals(immutable, Files.readAllBytes(path)), "source smithing fixture modified");
        System.out.println("PASS: source two-slot/native template mapping, NBT identity/copy/absence, result/count/id, source queries/direct overrides, explicit native subclass boundary, native JSON/stream codecs and immutable transformed constructor; gameplay not asserted");
    }
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
