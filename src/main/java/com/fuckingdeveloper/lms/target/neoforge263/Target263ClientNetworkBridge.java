package com.fuckingdeveloper.lms.target.neoforge263;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Client-only target operation kept out of the common legacy network bridge. */
public final class Target263ClientNetworkBridge {
    private Target263ClientNetworkBridge() {}

    public static void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }
}
