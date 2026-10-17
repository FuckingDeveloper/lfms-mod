package com.fuckingdeveloper.lms.profile.forge1192.runtime.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

/** Source interface identity, including custom implementations and native MenuType casts. */
public interface Forge1192MenuType<T> {
    static <T extends AbstractContainerMenu> MenuType<T> create(Forge1192ContainerFactory<T> factory) {
        return com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192VanillaConstructionBridge.menuType(factory);
    }

    T create(int id, Inventory inventory, FriendlyByteBuf data);
}
