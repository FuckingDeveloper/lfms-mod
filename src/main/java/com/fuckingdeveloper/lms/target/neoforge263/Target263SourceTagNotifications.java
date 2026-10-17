package com.fuckingdeveloper.lms.target.neoforge263;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.event.LegacyTagsUpdatedEvent;
import com.fuckingdeveloper.lms.runtime.event.ClientDataReadiness;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.network.Connection;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

/** Native publication remains native; only source views wait for connection readiness. */
public final class Target263SourceTagNotifications {
    private static final IEventBus SOURCE_BUS=BusBuilder.builder().build();
    private static final ClientDataReadiness<Connection,LegacyTagsUpdatedEvent> CLIENT=new ClientDataReadiness<>();
    private static final Map<TagsUpdatedEvent,LegacyTagsUpdatedEvent> VIEWS=new WeakHashMap<>();
    private Target263SourceTagNotifications(){}
    public static IEventBus sourceBus(){return SOURCE_BUS;}
    public static synchronized LegacyTagsUpdatedEvent view(TagsUpdatedEvent event) {
        return VIEWS.computeIfAbsent(event,e->new LegacyTagsUpdatedEvent(e.getRegistries(),
                e instanceof TagsUpdatedEvent.ClientPacketReceived,!e.shouldUpdateStaticData()));
    }
    public static void configuration(Connection connection,TagsUpdatedEvent.ClientPacketReceived event) {
        if(connection.isConnected())CLIENT.configuration(connection,view(event));
        else CLIENT.disconnected(connection);
    }
    public static void publication(Connection connection,TagsUpdatedEvent.ClientPacketReceived event) {
        if(connection.isConnected())CLIENT.publication(connection,view(event)).ifPresent(SOURCE_BUS::post);
        else CLIENT.disconnected(connection);
    }
    public static void loginCompleted(Connection connection) {
        if(connection.isConnected())CLIENT.loginCompleted(connection).ifPresent(SOURCE_BUS::post);
        else CLIENT.disconnected(connection);
    }
    public static void disconnected(Connection connection){CLIENT.disconnected(connection);}
}
