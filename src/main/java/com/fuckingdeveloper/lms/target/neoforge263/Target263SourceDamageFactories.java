package com.fuckingdeveloper.lms.target.neoforge263;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;

/** Native registered sources with explicit world evidence and original attribution. */
public final class Target263SourceDamageFactories {
    private Target263SourceDamageFactories() {}
    private static Level level(Level context, Entity actor) {
        Level actual = actor == null ? context : actor.level();
        if (actual == null) throw new UnsupportedOperationException("Ownerless source damage factory requires proven world context");
        if (context != null && context != actual && context.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)
                != actual.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE))
            throw new UnsupportedOperationException("Ambiguous source damage factory registries");
        return actual;
    }
    public static DamageSource explosion(Level context, LivingEntity actor) {
        return level(context, actor).damageSources().explosion(actor, actor);
    }
    public static DamageSource explosion(LivingEntity actor) { return explosion(null, actor); }
    public static DamageSource explosion(Level context, Explosion explosion) {
        return explosion(explosion == null ? context : explosion.level(), explosion == null ? null : explosion.getIndirectSourceEntity());
    }
    public static DamageSource explosion(Explosion explosion) { return explosion(null, explosion); }
    public static DamageSource mobAttack(LivingEntity actor) { return level(null, actor).damageSources().mobAttack(actor); }
    public static DamageSource playerAttack(Player actor) { return level(null, actor).damageSources().playerAttack(actor); }
    public static DamageSource thorns(Entity actor) { return level(null, actor).damageSources().thorns(actor); }
}
