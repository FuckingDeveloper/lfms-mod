package com.fuckingdeveloper.lms.profile.forge1192.runtime.item;

import java.lang.invoke.*;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.*;

/** Source descriptor construction and read-only queries; no registry mutation. */
public final class Forge1192DamageSourceBridge {
    private Forge1192DamageSourceBridge() {}
    private static final Map<String, TagKey<DamageType>> QUERIES = Map.ofEntries(
            Map.entry("m_19360_", DamageTypeTags.IS_PROJECTILE),
            Map.entry("m_19372_", DamageTypeTags.IS_EXPLOSION),
            Map.entry("m_19376_", DamageTypeTags.BYPASSES_ARMOR),
            Map.entry("m_146705_", DamageTypeTags.DAMAGES_HELMET),
            Map.entry("m_19378_", DamageTypeTags.BYPASSES_INVULNERABILITY),
            Map.entry("m_19379_", DamageTypeTags.BYPASSES_EFFECTS),
            Map.entry("m_238340_", DamageTypeTags.BYPASSES_ENCHANTMENTS),
            Map.entry("m_19384_", DamageTypeTags.IS_FIRE),
            Map.entry("m_146707_", DamageTypeTags.IS_FALL),
            Map.entry("m_181121_", DamageTypeTags.NO_ANGER),
            Map.entry("m_19387_", DamageTypeTags.WITCH_RESISTANT_TO));
    // ClassValue does not retain disposable managed classloaders globally.
    private static final ClassValue<Map<String, MethodHandle>> OVERRIDES = new ClassValue<>() {
        @Override protected Map<String, MethodHandle> computeValue(Class<?> type) {
            var methods = new HashMap<String, MethodHandle>();
            for (var name : QUERIES.keySet()) {
                try {
                    var method = type.getMethod(name);
                    methods.put(name, MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup())
                            .unreflect(method));
                } catch (NoSuchMethodException absent) {
                    // No source override: the native damage tag supplies base behavior.
                } catch (IllegalAccessException failure) {
                    throw new IllegalStateException("Cannot access source damage query: " + type.getName() + "#" + name, failure);
                }
            }
            return Map.copyOf(methods);
        }
    };

    public static Holder<DamageType> descriptor(String name) {
        return Holder.direct(new DamageType(Objects.requireNonNull(name, "source damage message ID"), DamageScaling.NEVER, 0.1F));
    }
    public static DamageSource create(String name) { return new DamageSource(descriptor(name)); }

    public static boolean baseQuery(DamageSource source, String name) {
        var tag = QUERIES.get(name);
        if (tag == null) throw new UnsupportedOperationException("Unproven source damage query: " + name);
        return source.is(tag);
    }
    public static boolean query(DamageSource source, String name) {
        var override = OVERRIDES.get(source.getClass()).get(name);
        if (override == null) return baseQuery(source, name);
        try { return (boolean) override.invoke(source); }
        catch (Throwable failure) { throw new IllegalStateException("Source damage query failed: " + name, failure); }
    }

    public static boolean query_m_19360_(DamageSource source) { return query(source, "m_19360_"); }
    public static boolean baseQuery_m_19360_(DamageSource source) { return baseQuery(source, "m_19360_"); }
    public static boolean query_m_19372_(DamageSource source) { return query(source, "m_19372_"); }
    public static boolean baseQuery_m_19372_(DamageSource source) { return baseQuery(source, "m_19372_"); }
    public static boolean query_m_19376_(DamageSource source) { return query(source, "m_19376_"); }
    public static boolean baseQuery_m_19376_(DamageSource source) { return baseQuery(source, "m_19376_"); }
    public static boolean query_m_146705_(DamageSource source) { return query(source, "m_146705_"); }
    public static boolean baseQuery_m_146705_(DamageSource source) { return baseQuery(source, "m_146705_"); }
    public static boolean query_m_19378_(DamageSource source) { return query(source, "m_19378_"); }
    public static boolean baseQuery_m_19378_(DamageSource source) { return baseQuery(source, "m_19378_"); }
    public static boolean query_m_19379_(DamageSource source) { return query(source, "m_19379_"); }
    public static boolean baseQuery_m_19379_(DamageSource source) { return baseQuery(source, "m_19379_"); }
    public static boolean query_m_238340_(DamageSource source) { return query(source, "m_238340_"); }
    public static boolean baseQuery_m_238340_(DamageSource source) { return baseQuery(source, "m_238340_"); }
    public static boolean query_m_19384_(DamageSource source) { return query(source, "m_19384_"); }
    public static boolean baseQuery_m_19384_(DamageSource source) { return baseQuery(source, "m_19384_"); }
    public static boolean query_m_146707_(DamageSource source) { return query(source, "m_146707_"); }
    public static boolean baseQuery_m_146707_(DamageSource source) { return baseQuery(source, "m_146707_"); }
    public static boolean query_m_181121_(DamageSource source) { return query(source, "m_181121_"); }
    public static boolean baseQuery_m_181121_(DamageSource source) { return baseQuery(source, "m_181121_"); }
    public static boolean query_m_19387_(DamageSource source) { return query(source, "m_19387_"); }
    public static boolean baseQuery_m_19387_(DamageSource source) { return baseQuery(source, "m_19387_"); }
}
