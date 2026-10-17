package com.fuckingdeveloper.lms.profile.forge1192.runtime.event;

import net.minecraft.core.RegistryAccess;
import net.neoforged.bus.api.Event;

/** One concrete source identity over genuine registry publication facts. */
public final class LegacyTagsUpdatedEvent extends Event {
    public enum UpdateCause { SERVER_DATA_LOAD, CLIENT_PACKET_RECEIVED }
    private final RegistryAccess registryAccess;
    private final UpdateCause cause;
    private final boolean integrated;
    public LegacyTagsUpdatedEvent(RegistryAccess registries, boolean fromClientPacket, boolean integrated) {
        this.registryAccess=registries;
        this.cause=fromClientPacket?UpdateCause.CLIENT_PACKET_RECEIVED:UpdateCause.SERVER_DATA_LOAD;
        this.integrated=integrated;
    }
    public RegistryAccess getRegistryAccess(){return registryAccess;}
    public UpdateCause getUpdateCause(){return cause;}
    public boolean shouldUpdateStaticData(){return cause==UpdateCause.SERVER_DATA_LOAD||!integrated;}
}
