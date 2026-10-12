package com.fuckingdeveloper.lms.runtime;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;

/**
 * Forge 1.19.2 superclass-constructor compatibility boundary.
 *
 * This constructs the modern superclass with the legacy translation key.
 * It intentionally does NOT claim to migrate legacy icon generation,
 * creative inventory population, or registry timing. Those operations
 * must be implemented as separate lifecycle capabilities.
 */
public final class Forge1192CreativeTabBridge {
    private Forge1192CreativeTabBridge() {}

    /** Exact modern identity for Forge 1.19.2's global search tab token. */
    public static CreativeModeTab searchTab() {
        return CreativeModeTabs.searchTab();
    }

    /** Reconstruct Item#allowedIn for preserved legacy tab intent. */
    public static boolean allowedIn(Item item, CreativeModeTab tab) {
        if (tab == searchTab()) return true;
        CreativeModeTab intended = Forge1192VanillaConstructionBridge.legacyCreativeTab(item);
        return intended == tab;
    }

    /** Reconstruct the removed target base implementation for legacy super calls. */
    public static void fillBaseItemCategory(
            Item item, CreativeModeTab tab, NonNullList<ItemStack> output) {
        if (allowedIn(item, tab)) output.add(Forge1192EarlyItemStackBridge.create(item));
    }

    /**
     * Retain virtual dispatch to a legacy fillItemCategory override. Items
     * without such an override receive the verified 1.19.2 base behavior.
     */
    public static void fillItemCategory(
            Item item, CreativeModeTab tab, NonNullList<ItemStack> output) {
        java.lang.invoke.MethodHandle override;
        try {
            Class<?> type = item.getClass();
            override = MethodHandles.privateLookupIn(type, MethodHandles.lookup()).findVirtual(
                    type, "m_6787_",
                    MethodType.methodType(void.class, CreativeModeTab.class, NonNullList.class));
        } catch (NoSuchMethodException e) {
            fillBaseItemCategory(item, tab, output);
            return;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access legacy creative item enumeration for "
                    + item.getClass().getName(), e);
        }
        try {
            override.invoke(item, tab, output);
        } catch (Throwable cause) {
            throw new IllegalStateException("Legacy creative item enumeration failed for "
                    + item.getClass().getName(), cause);
        }
    }

    public static CreativeModeTab.Builder builder(String legacyLabel) {
        String label = Objects.requireNonNull(legacyLabel, "legacy creative tab label");
        if (label.isBlank()) throw new IllegalArgumentException("Empty legacy creative tab label");
        return CreativeModeTab.builder()
                .title(Component.translatable("itemGroup." + label))
                .icon(() -> {
                    throw new IllegalStateException(
                            "Legacy CreativeModeTab icon migration is not installed: " + label);
                })
                .displayItems((parameters, output) -> {
                    throw new IllegalStateException(
                            "Legacy CreativeModeTab contents migration is not installed: " + label);
                });
    }
}
