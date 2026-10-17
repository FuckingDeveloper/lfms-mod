package com.fuckingdeveloper.lms.target.neoforge263;

import com.fuckingdeveloper.lms.classloading.ManagedLegacyClassLoader;
import java.lang.invoke.*;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;

/** Source virtual callbacks remain source-owned; native targets select their side. */
public final class Target263SourceDamageDelivery {
    private Target263SourceDamageDelivery() {}
    private static final ClassValue<Optional<MethodHandle>> SOURCE = new ClassValue<>() {
        @Override protected Optional<MethodHandle> computeValue(Class<?> type) {
            try {
                var method=type.getMethod("m_6469_",DamageSource.class,float.class);
                if (!(method.getDeclaringClass().getClassLoader() instanceof ManagedLegacyClassLoader)) return Optional.empty();
                return Optional.of(MethodHandles.privateLookupIn(method.getDeclaringClass(),MethodHandles.lookup()).unreflect(method));
            } catch(NoSuchMethodException absent) { return Optional.empty(); }
            catch(IllegalAccessException failure) { throw new IllegalStateException("Cannot access source damage callback",failure); }
        }
    };
    public static boolean hurt(Entity victim, DamageSource source, float amount) {
        var callback=SOURCE.get(victim.getClass());
        if(callback.isEmpty()) return victim.hurtOrSimulate(source,amount);
        try { return (boolean)callback.get().invoke(victim,source,amount); }
        catch(RuntimeException|Error failure) { throw failure; }
        catch(Throwable failure) { throw new IllegalStateException("Source damage callback failed",failure); }
    }
}
