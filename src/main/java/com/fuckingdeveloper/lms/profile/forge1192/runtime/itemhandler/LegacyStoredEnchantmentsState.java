package com.fuckingdeveloper.lms.profile.forge1192.runtime.itemhandler;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.Forge1192DynamicRegistryBridge;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.resources.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.*;

/** Receiver-owned source list; native component writes supersede the observed mirror. */
final class LegacyStoredEnchantmentsState {
    private ItemEnchantments observed;
    private Tag snapshot;
    private boolean syncing;
    private static final String KEY = "StoredEnchantments";

    void reconcile(ItemStack stack, CompoundTag root) {
        if (syncing) return;
        syncing = true;
        try {
            var components = ((LegacyMutableTagCarrier)(Object)stack).lms$sourceComponents();
            var nativeValue = components.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (observed == null || !nativeValue.equals(observed)) {
                if (!nativeValue.isEmpty()) {
                    var list = new ListTag();
                    for (var entry : nativeValue.entrySet()) {
                        var tag = new CompoundTag();
                        tag.putString("id", entry.getKey().unwrapKey().orElseThrow().identifier().toString());
                        tag.putShort("lvl", (short)entry.getIntValue());
                        list.add(tag);
                    }
                    root.put(KEY, list);
                } else if (observed != null) root.remove(KEY);
                observed = nativeValue;
                snapshot = copy(root.get(KEY));
                return;
            }
            Tag source = root.get(KEY);
            if (java.util.Objects.equals(source, snapshot)) return;
            if (source != null && !(source instanceof ListTag))
                throw new UnsupportedOperationException("StoredEnchantments must be a source compound list");
            var builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            if (source instanceof ListTag list && !list.isEmpty()) {
                var registry = Forge1192DynamicRegistryBridge.enchantments();
                for (Tag value : list) {
                    if (!(value instanceof CompoundTag tag))
                        throw new UnsupportedOperationException("StoredEnchantments contains a non-compound entry");
                    var id = Identifier.parse(tag.getStringOr("id", ""));
                    @SuppressWarnings("unchecked")
                    var typed = (net.minecraft.core.Registry<Enchantment>)registry;
                    int level = tag.getShortOr("lvl", (short)0);
                    if (level < 0 || level > Enchantment.MAX_LEVEL)
                        throw new UnsupportedOperationException("Source stored enchantment level is not representable: " + level);
                    builder.set(typed.getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, id)), level);
                }
            }
            observed = builder.toImmutable();
            stack.set(DataComponents.STORED_ENCHANTMENTS, observed);
            snapshot = copy(source);
        } finally { syncing = false; }
    }
    void assign(ItemStack stack, CompoundTag root) {
        observed = ((LegacyMutableTagCarrier)(Object)stack).lms$sourceComponents()
                .getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        snapshot = new CompoundTag(); // Distinct type from every supported source list or absence.
        reconcile(stack, root == null ? new CompoundTag() : root);
    }
    private static Tag copy(Tag value) { return value == null ? null : value.copy(); }
}
