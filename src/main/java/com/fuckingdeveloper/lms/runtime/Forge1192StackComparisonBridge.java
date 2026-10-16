package com.fuckingdeveloper.lms.runtime;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.Objects;

/** Source NBT equality is distinct from native component equality. */
public final class Forge1192StackComparisonBridge {
    private Forge1192StackComparisonBridge() {}
    /** Exact source ShulkerItemStackInvWrapper.createDefaultProvider identities. */
    private static final java.util.Set<Item> DEFAULT_PROVIDER_ITEMS = java.util.stream.Stream.concat(
            java.util.stream.Stream.of(net.minecraft.world.item.Items.SHULKER_BOX),
            net.minecraft.world.item.Items.DYED_SHULKER_BOX.asList().stream())
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
    private static final ClassValue<Boolean> DEFAULT_SOURCE_CAPABILITIES = new ClassValue<>() {
        @Override protected Boolean computeValue(Class<?> type) {
            for (Class<?> current=type;current!=Item.class;current=current.getSuperclass()) {
                // Pinned source BlockItem declares neither an initializer nor extra interfaces.
                if(current==net.minecraft.world.item.BlockItem.class)return true;
                if(current==null || !(current.getClassLoader() instanceof com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader loader)) return false;
                if(!declaresNoInitializer(loader,current.getName(),new java.util.HashSet<>())) return false;
            }
            return true;
        }
    };
    private static boolean declaresNoInitializer(com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader loader,String name,java.util.Set<String> seen) {
        if(!seen.add(name))return true;
        if(name.startsWith("java/") || name.startsWith("java."))return true;
        if(name.equals("net/minecraftforge/common/extensions/IForgeItem"))return true;
        try {
            byte[] bytes=loader.originalClassBytes(name.replace('/','.'));
            if(bytes==null)return false;
            var node=new org.objectweb.asm.tree.ClassNode();
            new org.objectweb.asm.ClassReader(bytes).accept(node,org.objectweb.asm.ClassReader.SKIP_CODE|org.objectweb.asm.ClassReader.SKIP_DEBUG|org.objectweb.asm.ClassReader.SKIP_FRAMES);
            for(var method:node.methods)if((method.access&org.objectweb.asm.Opcodes.ACC_STATIC)==0 && method.name.equals("initCapabilities")
                    && method.desc.equals("(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraftforge/common/capabilities/ICapabilityProvider;"))return false;
            for(String iface:node.interfaces)if(!declaresNoInitializer(loader,iface,seen))return false;
            return true;
        } catch(java.io.IOException failure){throw new IllegalStateException("Cannot prove source item capability defaults",failure);}
    }
    public static boolean hasDefaultSourceCapabilities(Class<? extends Item> type) {
        return DEFAULT_SOURCE_CAPABILITIES.get(type);
    }
    public static boolean hasDefaultSourceCapabilities(Item item) {
        return hasDefaultSourceCapabilities(item.getClass()) && !DEFAULT_PROVIDER_ITEMS.contains(item);
    }
    public static boolean sameItem(ItemStack a, ItemStack b) { return !b.isEmpty() && a.is(b.getItem()); }
    public static boolean isSame(ItemStack a, ItemStack b) {
        return a==b || (!a.isEmpty() && !b.isEmpty() && sameItem(a,b));
    }
    public static boolean tagMatches(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) return true;
        if (a.isEmpty() || b.isEmpty()) return false;
        if (!Objects.equals(Forge1192ItemBridge.getTag(a), Forge1192ItemBridge.getTag(b))) return false;
        requireSourceTagProjection(a); requireSourceTagProjection(b);
        // A proven default class still requires the exact source shulker-item exclusions.
        // Source stack attachment subjects are currently rejected by the event bridge.
        if (!hasDefaultSourceCapabilities(a.getItem()) || !hasDefaultSourceCapabilities(b.getItem()))
            throw new UnsupportedOperationException("Source stack capability equality requires proven provider state: "
                    + a.getItem().getClass().getName() + ", " + b.getItem().getClass().getName());
        return true;
    }
    private static void requireSourceTagProjection(ItemStack stack) {
        if (stack.isDamageableItem()) throw new UnsupportedOperationException("Source comparison requires legacy damage/NBT projection");
        for (var entry : stack.getComponentsPatch().keySet()) {
            if (entry!=net.minecraft.core.component.DataComponents.CUSTOM_DATA)
                throw new UnsupportedOperationException("Source comparison requires native component/NBT projection: " + entry);
        }
    }
    public static boolean isSameItemSameTags(ItemStack a, ItemStack b) { return a.is(b.getItem()) && tagMatches(a,b); }
    public static boolean matches(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) return true;
        return !a.isEmpty() && !b.isEmpty() && a.getCount()==b.getCount() && isSameItemSameTags(a,b);
    }
}
