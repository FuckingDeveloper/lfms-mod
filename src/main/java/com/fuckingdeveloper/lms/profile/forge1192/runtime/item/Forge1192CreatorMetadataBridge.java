package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;

/** Exact source informational metadata rules; never an ownership transfer. */
public final class Forge1192CreatorMetadataBridge {
    private Forge1192CreatorMetadataBridge() {}

    public static String creatorModId(Item item, ItemStack stack) {
        Objects.requireNonNull(item, "item");
        return item instanceof LegacyIForgeItem source ? source.getCreatorModId(stack) : defaultCreatorModId(stack);
    }

    public static String interfaceCreatorModId(LegacyIForgeItem item, ItemStack stack) {
        return Objects.requireNonNull(item, "item").getCreatorModId(stack);
    }

    public static String itemDefault(Item item, ItemStack stack) {
        Objects.requireNonNull(item, "item");
        return defaultCreatorModId(stack);
    }

    public static String interfaceDefault(LegacyIForgeItem item, ItemStack stack) {
        Objects.requireNonNull(item, "item");
        return defaultCreatorModId(stack);
    }

    public static String defaultCreatorModId(ItemStack stack) {
        Item item = Objects.requireNonNull(stack, "stack").getItem();
        Identifier id = BuiltInRegistries.ITEM.getResourceKey(item).map(key -> key.identifier()).orElse(null);
        String namespace = id == null ? null : id.getNamespace();
        if (!"minecraft".equals(namespace)) return namespace;
        if (item == Items.ENCHANTED_BOOK) {
            var enchantments = Forge1192EnchantedBookBridge.getEnchantments(stack);
            if (enchantments.size() == 1) {
                var enchantment = Identifier.tryParse(enchantments.getCompoundOrEmpty(0).getStringOr("id", ""));
                if (enchantment != null && com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.Forge1192DynamicRegistryBridge.enchantments().containsKey(enchantment))
                    return enchantment.getNamespace();
            }
        } else if (item instanceof PotionItem || item instanceof TippedArrowItem) {
            var potion = Forge1192PotionBridge.getPotion(stack);
            // Source EMPTY is absence, not the defaulted native registry entry.
            if (potion != Forge1192PotionBridge.EMPTY) {
                var key = BuiltInRegistries.POTION.getResourceKey(potion);
                if (key.isPresent()) return key.get().identifier().getNamespace();
            }
        } else if (item instanceof SpawnEggItem) {
            var type = SpawnEggItem.getType(Forge1192EarlyItemStackBridge.create(item));
            if (type != null) {
                var key = BuiltInRegistries.ENTITY_TYPE.getResourceKey(type);
                if (key.isPresent()) return key.get().identifier().getNamespace();
            }
        }
        return namespace;
    }
}
