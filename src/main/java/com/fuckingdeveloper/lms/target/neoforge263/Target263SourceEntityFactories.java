package com.fuckingdeveloper.lms.target.neoforge263;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192LifecycleBridge;
import java.util.*;
import java.util.function.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;

/** Original source construction callbacks at the actual native packet boundary. */
public final class Target263SourceEntityFactories {
    private Target263SourceEntityFactories() {}
    public record Intent(String owner,BiFunction<Target263SourceSpawnEntity,Level,Entity> factory) {}
    public interface Access {
        Intent lms$sourceClientFactory();
        void lms$sourceClientFactory(Intent intent);
    }
    private record PacketScope(Entity entity,ServerEntity tracker) {}
    private static final ThreadLocal<PacketScope> PACKET_SCOPE=new ThreadLocal<>();
    @SuppressWarnings({"unchecked","rawtypes"})
    public static <T extends Entity> EntityType.Builder<T> setCustomClientFactory(EntityType.Builder<T> builder,BiFunction<?,?,?> factory) {
        var owner=Forge1192LifecycleBridge.requireActive().modId();
        ((Access)Objects.requireNonNull(builder)).lms$sourceClientFactory(new Intent(owner,(BiFunction)Objects.requireNonNull(factory)));
        return builder;
    }
    public static <T extends Entity> EntityType<T> bind(EntityType.Builder<T> builder,EntityType<T> type) {
        var source=(Access)builder;var intent=source.lms$sourceClientFactory();
        if(intent!=null){((Access)type).lms$sourceClientFactory(intent);source.lms$sourceClientFactory(null);}
        return type;
    }
    public static Entity create(EntityType<?> type,Level level,ClientboundAddEntityPacket packet,Supplier<Entity> original) {
        var intent=((Access)type).lms$sourceClientFactory();
        if(intent==null)return original.get();
        if(packet.getType()!=type)throw new IllegalArgumentException("Native spawn packet/type mismatch");
        try(var scope=Forge1192LifecycleBridge.enterRecorded(intent.owner())) {
            return intent.factory().apply(new Target263SourceSpawnEntity(packet),level);
        }catch(RuntimeException|Error failure){throw failure;}
        catch(Exception failure){throw new IllegalStateException("Source client entity factory scope failed",failure);}
    }
    public static Packet<?> sourcePacket(Entity entity) {
        if(!(entity instanceof Target263SourceSpawnPacket source))throw new UnsupportedOperationException("Source immediate spawn-packet query on native-only entity");
        return source.lms$sourceSpawnPacket();
    }
    public static Packet<?> spawnPacket(Entity entity,ServerEntity tracker) {
        var previous=PACKET_SCOPE.get();PACKET_SCOPE.set(new PacketScope(entity,tracker));
        try{return ((Target263SourceSpawnPacket)entity).lms$sourceSpawnPacket();}
        finally{if(previous==null)PACKET_SCOPE.remove();else PACKET_SCOPE.set(previous);}
    }
    public static Packet<?> getEntitySpawningPacket(Entity entity) {
        var scope=PACKET_SCOPE.get();
        if(scope==null||scope.entity()!=entity)throw new UnsupportedOperationException("Source spawn packet requires its actual native entity/tracker callback");
        return new ClientboundAddEntityPacket(entity,scope.tracker());
    }
}
