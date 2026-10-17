package com.fuckingdeveloper.lms.target.neoforge263;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.world.Forge1192ExplosionBlockInteraction;
import java.util.Objects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;

/** Scoped explicit source modes through the actual native explosion/event/packet operation. */
public final class Target263SourceExplosions {
    private Target263SourceExplosions() {}
    private static final ThreadLocal<Scope> CURRENT=new ThreadLocal<>();
    private static final class Scope {
        final ServerLevel level; final Explosion.BlockInteraction mode; ServerExplosion result; boolean claimed;
        Scope(ServerLevel level,Explosion.BlockInteraction mode){this.level=level;this.mode=mode;}
    }
    public static Explosion.BlockInteraction mode(Forge1192ExplosionBlockInteraction source) {
        return switch(Objects.requireNonNull(source)) {
            case NONE -> Explosion.BlockInteraction.KEEP;
            case BREAK -> Explosion.BlockInteraction.DESTROY;
            case DESTROY -> Explosion.BlockInteraction.DESTROY_WITH_DECAY;
        };
    }
    public static Explosion explode(Level level,Entity source,double x,double y,double z,float radius,Forge1192ExplosionBlockInteraction mode) {
        return explode(level,source,x,y,z,radius,false,mode);
    }
    public static Explosion explode(Level level,Entity source,double x,double y,double z,float radius,boolean fire,Forge1192ExplosionBlockInteraction mode) {
        return explode(level,source,null,null,x,y,z,radius,fire,mode);
    }
    public static Explosion explode(Level level,Entity source,DamageSource damage,ExplosionDamageCalculator calculator,
            double x,double y,double z,float radius,boolean fire,Forge1192ExplosionBlockInteraction mode) {
        Objects.requireNonNull(level);
        if(level.getClass()!=ServerLevel.class)
            throw new UnsupportedOperationException("Source explosion execution currently requires native ServerLevel; client/custom level explosion contracts are not established: "+level.getClass().getName());
        var scope=new Scope((ServerLevel)level,mode(mode));var previous=CURRENT.get();CURRENT.set(scope);
        try {
            level.explode(source,damage,calculator,x,y,z,radius,fire,Level.ExplosionInteraction.NONE);
            if(scope.result==null)throw new IllegalStateException("Native explosion capture hook was not applied");
            return scope.result;
        } finally {if(previous==null)CURRENT.remove();else CURRENT.set(previous);}
    }
    public static net.minecraft.world.entity.LivingEntity getSourceMob(Explosion explosion) {
        return explosion.getIndirectSourceEntity();
    }
    private static ServerExplosion server(Explosion explosion) {
        Objects.requireNonNull(explosion);
        if(explosion instanceof ServerExplosion server)return server;
        throw new UnsupportedOperationException("Source explosion query requires native ServerExplosion storage: "+explosion.getClass().getName());
    }
    public static DamageSource getDamageSource(Explosion explosion) { return server(explosion).getDamageSource(); }
    public static java.util.Map<net.minecraft.world.entity.player.Player,net.minecraft.world.phys.Vec3> getHitPlayers(Explosion explosion) {
        return server(explosion).getHitPlayers();
    }
    /** Claims this construction before invoking it, so nested construction cannot steal the result. */
    public static ServerExplosion construct(ServerLevel level,Explosion.BlockInteraction nativeMode,
            java.util.function.Function<Explosion.BlockInteraction,ServerExplosion> original) {
        var scope=CURRENT.get();
        if(scope==null||scope.level!=level||scope.claimed)return original.apply(nativeMode);
        scope.claimed=true;
        var result=original.apply(scope.mode);scope.result=Objects.requireNonNull(result);return result;
    }
}
