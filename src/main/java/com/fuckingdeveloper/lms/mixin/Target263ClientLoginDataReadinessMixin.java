package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.target.neoforge263.Target263SourceTagNotifications;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
abstract class Target263ClientLoginDataReadinessMixin {
    @Inject(method="handleLogin",at=@At("RETURN"),require=1)
    private void lms$afterNativeLogin(ClientboundLoginPacket packet,CallbackInfo ci) {
        Target263SourceTagNotifications.loginCompleted(((ClientPacketListener)(Object)this).getConnection());
    }
}
