package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192EnchantedBookBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.*;
import com.mojang.serialization.Lifecycle;
import java.lang.invoke.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

public final class EnchantedBookReadFamilyCheck {
    public static void run() throws Exception {
        var field = Forge1192DynamicRegistryBridge.class.getDeclaredField("ACTIVE_ACCESS");
        field.setAccessible(true);
        @SuppressWarnings("unchecked") var context = (ThreadLocal<RegistryAccess>)field.get(null);
        check(context.get() == null, "test leaked into an active registry scope");
        var key = ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse("fixture:book/read"));
        var enchantment = new Enchantment(Component.literal("arbitrary native text"),
                Enchantment.definition(HolderSet.empty(), 10, 5, Enchantment.constantCost(1), Enchantment.constantCost(10), 1, EquipmentSlotGroup.ANY),
                HolderSet.empty(), DataComponentMap.EMPTY);
        var registry = new MappedRegistry<Enchantment>(Registries.ENCHANTMENT, Lifecycle.stable());
        var holder = registry.register(key, enchantment, RegistrationInfo.BUILT_IN);
        var registryView = LegacyRegistryManagerView.ACTIVE.<Enchantment>getRegistry(Registries.ENCHANTMENT);
        try { registryView.getValue(key.identifier()); throw new AssertionError("inactive global registry fabricated"); }
        catch (IllegalStateException expected) {}
        context.set(new RegistryAccess.ImmutableRegistryAccess(List.of(registry)));
        var path = Files.createTempFile("lms-book-reads-", ".jar");
        try {
            check(registryView == LegacyRegistryManagerView.ACTIVE.<Enchantment>getRegistry(Registries.ENCHANTMENT), "key view identity");
            check(registryView.getValue(key.identifier()) == enchantment, "active registry value identity");
            check(Forge1192EnchantedBookBridge.descriptionId(enchantment).equals("enchantment.fixture.book.read"), "source description identity");
            check(Forge1192EnchantedBookBridge.descriptionId(key).equals("enchantment.fixture.book.read"), "source keyed description identity");
            var values = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY); values.set(holder, 2);
            var book = new ItemStack(Items.ENCHANTED_BOOK.builtInRegistryHolder(), 1, new PatchedDataComponentMap(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 1).build()));
            book.set(DataComponents.STORED_ENCHANTMENTS, values.toImmutable());
            var list = Forge1192EnchantedBookBridge.getEnchantments(book);
            check(list == Forge1192EnchantedBookBridge.getEnchantments(book), "book reader returned a copy");
            check(list.getCompoundOrEmpty(0).getShortOr("lvl", (short)0) == 2, "native enchantment projection");
            list.getCompoundOrEmpty(0).putShort("lvl", (short)4);
            check(book.get(DataComponents.STORED_ENCHANTMENTS).getLevel(holder) == 4, "nested source mutation lost");
            var copied = book.copy();
            Forge1192EnchantedBookBridge.getEnchantments(copied).getCompoundOrEmpty(0).putShort("lvl", (short)3);
            check(copied.get(DataComponents.STORED_ENCHANTMENTS).getLevel(holder) == 3 && book.get(DataComponents.STORED_ENCHANTMENTS).getLevel(holder) == 4, "copy shared source list");
            book.set(DataComponents.STORED_ENCHANTMENTS, values.toImmutable());
            check(Forge1192EnchantedBookBridge.getEnchantments(book).getCompoundOrEmpty(0).getShortOr("lvl", (short)0) == 2, "native write failed to supersede mirror");
            list.getCompoundOrEmpty(0).putShort("lvl", (short)5);
            check(book.get(DataComponents.STORED_ENCHANTMENTS).getLevel(holder) == 2, "detached old list changed receiver");
            Forge1192EnchantedBookBridge.getEnchantments(book).clear();
            check(book.get(DataComponents.STORED_ENCHANTMENTS).isEmpty(), "source list clear lost");
            var empty = new ItemStack(Items.ENCHANTED_BOOK.builtInRegistryHolder(), 1, new PatchedDataComponentMap(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 1).build()));
            var detached = Forge1192EnchantedBookBridge.getEnchantments(empty);
            detached.add(list.getCompoundOrEmpty(0).copy());
            check(Forge1192EnchantedBookBridge.getEnchantments(empty).isEmpty(), "absent list acquired ownership");
            try(var jar = new JarOutputStream(Files.newOutputStream(path))) {
                jar.putNextEntry(new JarEntry("fixture/BookReads.class")); jar.write(fixture()); jar.closeEntry();
            }
            // Source tag assignment and native copies/comparisons must consume pending edits.
            var live = Forge1192EnchantedBookBridge.getEnchantments(copied);
            live.getCompoundOrEmpty(0).putShort("lvl", (short)4);
            var pendingCopy = copied.copy();
            check(pendingCopy.get(DataComponents.STORED_ENCHANTMENTS).getLevel(holder) == 4, "copy missed pending nested edit");
            live.getCompoundOrEmpty(0).putShort("lvl", (short)2);
            check(!ItemStack.isSameItemSameComponents(copied, pendingCopy), "comparison missed pending source edit");
            var root = new CompoundTag(); root.put("StoredEnchantments", live.copy());
            com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192ItemBridge.setTag(copied, root);
            check(copied.get(DataComponents.STORED_ENCHANTMENTS).getLevel(holder) == 2, "tag assignment lost native projection");
            com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192ItemBridge.setTag(copied, null);
            check(copied.get(DataComponents.STORED_ENCHANTMENTS).isEmpty(), "tag removal retained native enchantment");
            var input = Files.readAllBytes(path);
            try(var loader = new ManagedLegacyClassLoader(path, EnchantedBookReadFamilyCheck.class.getClassLoader(), new Forge1192LegacyClassTransformer())) {
                var type = loader.linkOwnedClass("fixture.BookReads");
                check(type.getMethod("read", ItemStack.class).invoke(null, pendingCopy) == Forge1192EnchantedBookBridge.getEnchantments(pendingCopy), "source static reader");
                var handle = (MethodHandle)type.getMethod("handle").invoke(null);
                try { check(handle.invokeWithArguments(pendingCopy) == Forge1192EnchantedBookBridge.getEnchantments(pendingCopy), "callable reader"); }
                catch (Throwable failure) { throw new AssertionError(failure); }
                check(type.getMethod("description", Enchantment.class).invoke(null, enchantment).equals("enchantment.fixture.book.read"), "source SRG description");
            }
            check(Arrays.equals(input, Files.readAllBytes(path)), "source artifact changed");
        } finally { context.remove(); Files.deleteIfExists(path); }
        System.out.println("PASS: original book-reader/description bytecode, keyed active registry, live nested list mutation, copy isolation, native replacement and absent-list detachment");
    }
    private static byte[] fixture() {
        var w = new ClassWriter(ClassWriter.COMPUTE_MAXS); w.visit(V17, ACC_PUBLIC, "fixture/BookReads", null, "java/lang/Object", null);
        String book = "net/minecraft/world/item/EnchantedBookItem", desc = "(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/nbt/ListTag;";
        var m = w.visitMethod(ACC_PUBLIC|ACC_STATIC, "read", desc, null, null); m.visitCode(); m.visitVarInsn(ALOAD,0); m.visitMethodInsn(INVOKESTATIC,book,"m_41163_",desc,false); m.visitInsn(ARETURN); m.visitMaxs(0,0);m.visitEnd();
        m = w.visitMethod(ACC_PUBLIC|ACC_STATIC, "handle", "()Ljava/lang/invoke/MethodHandle;", null, null);m.visitCode();m.visitLdcInsn(new Handle(H_INVOKESTATIC,book,"m_41163_",desc,false));m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        m = w.visitMethod(ACC_PUBLIC|ACC_STATIC, "description", "(Lnet/minecraft/world/item/enchantment/Enchantment;)Ljava/lang/String;", null, null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitMethodInsn(INVOKEVIRTUAL,"net/minecraft/world/item/enchantment/Enchantment","m_44704_","()Ljava/lang/String;",false);m.visitInsn(ARETURN);m.visitMaxs(0,0);m.visitEnd();
        w.visitEnd();return w.toByteArray();
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
