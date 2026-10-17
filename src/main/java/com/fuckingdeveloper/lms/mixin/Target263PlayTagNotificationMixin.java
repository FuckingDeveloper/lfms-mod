package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.target.neoforge263.Target263SourceTagNotifications;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPacketListener.class)
abstract class Target263PlayTagNotificationMixin {
    @WrapOperation(method="handleUpdateTags",at=@At(value="INVOKE",target="Lnet/neoforged/bus/api/IEventBus;post(Lnet/neoforged/bus/api/Event;)Lnet/neoforged/bus/api/Event;"),require=1,allow=1)
    private Event lms$publishSourceTags(IEventBus bus,Event event,Operation<Event> original) {
        Event result=original.call(bus,event);
        if(!(event instanceof TagsUpdatedEvent.ClientPacketReceived tags))throw new IllegalStateException("Unexpected play tag publication: "+event.getClass());
        Target263SourceTagNotifications.publication(((ClientPacketListener)(Object)this).getConnection(),tags);
        return result;
    }
}
