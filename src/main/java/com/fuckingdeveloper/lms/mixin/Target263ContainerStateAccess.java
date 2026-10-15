package com.fuckingdeveloper.lms.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.LockCode;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BaseContainerBlockEntity.class)
public interface Target263ContainerStateAccess {
    @Accessor("name") Component lms$customName();
    @Accessor("name") void lms$customName(Component name);
    @Accessor("lockKey") LockCode lms$lock();
    @Accessor("lockKey") void lms$lock(LockCode lock);
}
