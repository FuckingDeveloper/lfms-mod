package com.fuckingdeveloper.lms.target.neoforge263;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

/** Current native creative contents; no historical array order or permission fiction. */
public final class Target263CreativeEnumeration {
    private static boolean preparing;
    private Target263CreativeEnumeration() {}

    public static CreativeModeTab[] tabs() {
        prepare();
        return CreativeModeTabs.allTabs().toArray(CreativeModeTab[]::new);
    }
    public static CreativeModeTab special(com.fuckingdeveloper.lms.runtime.item.CreativeTabRole role) {
        var key = switch (role) {
            case SEARCH -> CreativeModeTabs.SEARCH;
            case HOTBAR -> CreativeModeTabs.HOTBAR;
            case INVENTORY -> CreativeModeTabs.INVENTORY;
        };
        return BuiltInRegistries.CREATIVE_MODE_TAB.getValueOrThrow(key);
    }
    public static List<ItemStack> contents(CreativeModeTab tab) {
        prepare();
        var key = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(tab)
                .orElseThrow(() -> new IllegalStateException("Unregistered creative tab"));
        if (BuiltInRegistries.CREATIVE_MODE_TAB.getValueOrThrow(key) != tab)
            throw new IllegalStateException("Creative tab registry identity changed");
        return tab.getDisplayItems().stream().map(ItemStack::copy).toList();
    }
    private static void prepare() {
        var client = Minecraft.getInstance();
        if (client == null || !client.isSameThread() || client.player == null || client.level == null
                || client.getConnection() == null || client.player.connection != client.getConnection())
            throw new IllegalStateException("Creative tab enumeration requires a ready local client on its main thread");
        if (preparing) throw new IllegalStateException("Reentrant native creative tab enumeration");
        preparing = true;
        try {
            CreativeModeTabs.tryRebuildTabContents(client.getConnection().enabledFeatures(),
                    client.player.canUseGameMasterBlocks() && client.options.operatorItemsTab().get(),
                    client.level.registryAccess());
        } finally { preparing = false; }
    }
}
