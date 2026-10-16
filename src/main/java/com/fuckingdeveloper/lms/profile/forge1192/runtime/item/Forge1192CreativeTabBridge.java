package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192LifecycleBridge;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayDeque;
import java.util.List;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Forge 1.19.2 custom-tab construction and target population boundary.
 */
public final class Forge1192CreativeTabBridge {
    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(Forge1192CreativeTabBridge.class);
    private static final ThreadLocal<ArrayDeque<LegacyTabIntent>> CONSTRUCTING =
            ThreadLocal.withInitial(ArrayDeque::new);
    private static final List<LegacyTabIntent> TABS = new CopyOnWriteArrayList<>();

    private static final class LegacyTabIntent {
        private final String modId;
        private final String label;
        private CreativeModeTab tab;

        private LegacyTabIntent(String modId, String label) {
            this.modId = modId;
            this.label = label;
        }
    }

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
        LegacyTabIntent intent = new LegacyTabIntent(
                Forge1192LifecycleBridge.requireActive().modId(), label);
        CONSTRUCTING.get().push(intent);
        return CreativeModeTab.builder()
                .title(Component.translatable("itemGroup." + label))
                .icon(() -> legacyIcon(intent))
                .displayItems((parameters, output) -> populate(intent, output));
    }

    /** Called immediately after the transformed source superclass constructor. */
    public static void recordConstructedTab(CreativeModeTab tab) {
        ArrayDeque<LegacyTabIntent> stack = CONSTRUCTING.get();
        LegacyTabIntent intent = stack.poll();
        if (intent == null) {
            throw new IllegalStateException("Legacy CreativeModeTab constructed without source intent");
        }
        if (stack.isEmpty()) CONSTRUCTING.remove();
        if (intent.tab != null) throw new IllegalStateException("Legacy CreativeModeTab recorded twice");
        intent.tab = Objects.requireNonNull(tab, "legacy tab");
        TABS.add(intent);
    }

    /** Register source custom-tab objects only during the target registry phase. */
    public static void onRegister(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.CREATIVE_MODE_TAB)) return;
        java.util.HashSet<Identifier> ids = new java.util.HashSet<>();
        for (LegacyTabIntent intent : TABS) {
            CreativeModeTab tab = requireTab(intent);
            Identifier id = Identifier.fromNamespaceAndPath(intent.modId,
                    "legacy_tab/" + normalizedPath(intent.label));
            if (!ids.add(id)) {
                throw new IllegalStateException("Duplicate legacy creative-tab identity " + id);
            }
            event.register(Registries.CREATIVE_MODE_TAB, id, () -> tab);
            LOG.info("LMS registered legacy creative tab id={} sourceLabel={}", id, intent.label);
        }
    }

    private static ItemStack legacyIcon(LegacyTabIntent intent) {
        CreativeModeTab tab = requireTab(intent);
        for (String methodName : List.of("m_6976_", "makeIcon")) {
            try {
                var method = MethodHandles.privateLookupIn(tab.getClass(), MethodHandles.lookup())
                        .findVirtual(tab.getClass(), methodName,
                                MethodType.methodType(ItemStack.class));
                return (ItemStack) method.invoke(tab);
            } catch (NoSuchMethodException ignored) {
                // Try the other exact source-generation spelling.
            } catch (Throwable cause) {
                throw new IllegalStateException("Legacy creative-tab icon failed for "
                        + intent.label, cause);
            }
        }
        throw new IllegalStateException("Legacy creative tab has no source makeIcon override: "
                + tab.getClass().getName());
    }

    private static void populate(LegacyTabIntent intent, CreativeModeTab.Output output) {
        CreativeModeTab tab = requireTab(intent);
        var unique = ItemStackLinkedSet.createTypeAndComponentsSet();
        for (Identifier itemId : Forge1192VanillaConstructionBridge.legacyCreativeItemIds(tab)) {
            Item item = BuiltInRegistries.ITEM.getValue(itemId);
            if (item == null || !itemId.equals(BuiltInRegistries.ITEM.getKey(item))) {
                throw new IllegalStateException("Legacy creative-tab item is not registered: " + itemId);
            }
            NonNullList<ItemStack> sourceItems = NonNullList.create();
            fillItemCategory(item, tab, sourceItems);
            for (ItemStack stack : sourceItems) {
                if (stack.isEmpty()) continue;
                if (stack.getCount() != 1) {
                    throw new IllegalStateException("Legacy creative-tab stack count is not representable: "
                            + itemId + " count=" + stack.getCount());
                }
                if (unique.add(stack)) output.accept(stack);
            }
        }
        LOG.info("LMS populated legacy creative tab sourceLabel={} stacks={}",
                intent.label, unique.size());
    }

    private static CreativeModeTab requireTab(LegacyTabIntent intent) {
        if (intent.tab == null) {
            throw new IllegalStateException("Legacy CreativeModeTab construction is incomplete: "
                    + intent.label);
        }
        return intent.tab;
    }

    private static String normalizedPath(String label) {
        StringBuilder path = new StringBuilder(label.length());
        for (int i = 0; i < label.length(); i++) {
            char c = Character.toLowerCase(label.charAt(i));
            path.append((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                    || c == '_' || c == '-' || c == '.' ? c : '_');
        }
        if (path.isEmpty()) throw new IllegalArgumentException("Empty normalized creative-tab label");
        return path.toString();
    }
}
