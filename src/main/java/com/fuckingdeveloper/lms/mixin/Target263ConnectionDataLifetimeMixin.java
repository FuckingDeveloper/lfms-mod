package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.target.neoforge263.Target263SourceTagNotifications;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
abstract class Target263ConnectionDataLifetimeMixin {
    @Inject(method="disconnect(Lnet/minecraft/network/DisconnectionDetails;)V",at=@At("HEAD"),require=1,allow=1)
    private void lms$dropPendingData(CallbackInfo ci){Target263SourceTagNotifications.disconnected((Connection)(Object)this);}
    @Inject(method="handleDisconnection",at=@At("RETURN"),require=1)
    private void lms$dropClosedData(CallbackInfo ci){
        var connection=(Connection)(Object)this;
        if(!connection.isConnected())Target263SourceTagNotifications.disconnected(connection);
    }
}
