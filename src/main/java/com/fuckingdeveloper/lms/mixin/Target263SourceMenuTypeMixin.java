package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.menu.Forge1192ContainerFactory;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.menu.Forge1192MenuType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;
import org.spongepowered.asm.mixin.*;

/** Restore source extra-data entrypoints on the actual native menu type and native factory. */
@Mixin(MenuType.class)
abstract class Target263SourceMenuTypeMixin<T extends AbstractContainerMenu> implements Forge1192MenuType<T> {
    @Shadow @Final private MenuType.MenuSupplier<T> constructor;

    @Override
    public T create(int id, Inventory inventory, FriendlyByteBuf data) {
        if (constructor instanceof Forge1192ContainerFactory<?> source) {
            @SuppressWarnings("unchecked") T result = (T)source.create(id, inventory, data);
            return result;
        }
        if (constructor instanceof IContainerFactory<T> nativeFactory) {
            if (data != null && !(data instanceof RegistryFriendlyByteBuf))
                throw new UnsupportedOperationException("Native menu factory requires registry-aware opening data");
            return nativeFactory.create(id, inventory, (RegistryFriendlyByteBuf)data);
        }
        return constructor.create(id, inventory);
    }
}
