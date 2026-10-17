package com.fuckingdeveloper.lms.profile.forge1192.runtime.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.IContainerFactory;

/** Source SAM plus an exact native-buffer forwarding entry; no copied or invented opening data. */
@FunctionalInterface
public interface Forge1192ContainerFactory<T extends AbstractContainerMenu> extends IContainerFactory<T> {
    T create(int id, Inventory inventory, FriendlyByteBuf data);

    @Override
    default T create(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
        return create(id, inventory, (FriendlyByteBuf)data);
    }

    @Override
    default T create(int id, Inventory inventory) {
        return create(id, inventory, (FriendlyByteBuf)null);
    }
}
