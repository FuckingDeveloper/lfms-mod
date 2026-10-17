package com.fuckingdeveloper.lms.target.neoforge263;

import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ServerItemCooldowns;

/** Preserve the source item key while native managers own timing and notifications. */
public final class Target263SourceItemCooldowns {
    private Target263SourceItemCooldowns() {}

    private static ItemStack operand(ItemCooldowns manager, Item item) {
        Objects.requireNonNull(manager);
        Objects.requireNonNull(item);
        if (manager.getClass() != ItemCooldowns.class && manager.getClass() != ServerItemCooldowns.class)
            throw new UnsupportedOperationException("Custom item cooldown manager requires source hook adaptation: " + manager.getClass().getName());
        var key = BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow(() ->
                new UnsupportedOperationException("Source cooldown item is not registered"));
        var stack = com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192EarlyItemStackBridge.create(item);
        if (!manager.getCooldownGroup(stack).equals(key.identifier()))
            throw new UnsupportedOperationException("Source item-keyed cooldown conflicts with native default group: " + key.identifier());
        return stack;
    }

    public static void addCooldown(ItemCooldowns manager, Item item, int ticks) {
        var stack = operand(manager, item);
        manager.addCooldown(manager.getCooldownGroup(stack), ticks);
    }
    public static void removeCooldown(ItemCooldowns manager, Item item) {
        var stack = operand(manager, item);
        manager.removeCooldown(manager.getCooldownGroup(stack));
    }
    public static boolean isOnCooldown(ItemCooldowns manager, Item item) {
        return manager.isOnCooldown(operand(manager, item));
    }
    public static float getCooldownPercent(ItemCooldowns manager, Item item, float partialTick) {
        return manager.getCooldownPercent(operand(manager, item), partialTick);
    }
}
