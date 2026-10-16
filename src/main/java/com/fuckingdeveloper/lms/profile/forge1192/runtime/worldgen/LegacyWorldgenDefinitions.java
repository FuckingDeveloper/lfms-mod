package com.fuckingdeveloper.lms.profile.forge1192.runtime.worldgen;

import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.Forge1192RegistrationContext;

/** Staged source declarations, never an implementation of a native Registry. */
public final class LegacyWorldgenDefinitions {
    public enum Domain { CONFIGURED, PLACED }
    public static final Domain CONFIGURED=Domain.CONFIGURED,PLACED=Domain.PLACED;
    private static final Map<Domain,LinkedHashMap<Identifier,Object>> VALUES=new EnumMap<>(Domain.class);
    private static boolean sealed;
    private LegacyWorldgenDefinitions() {}
    public static synchronized Object register(Object registry,Identifier id,Object value){
        if(!(registry instanceof Domain domain))throw new IllegalArgumentException("Unknown source worldgen definition domain: "+registry);
        Forge1192RegistrationContext.requireModId(); // staging intent only; native data loading owns mutation
        return add(domain,id,value);
    }
    private static Object add(Domain domain,Identifier id,Object value){
        Objects.requireNonNull(id);Objects.requireNonNull(value);
        if(sealed)throw new IllegalStateException("Source worldgen declaration arrived after native data loading began: "+id);
        if(domain==Domain.CONFIGURED&&!(value instanceof LegacyConfiguredFeature<?,?>)||domain==Domain.PLACED&&!(value instanceof LegacyPlacedFeature))throw new IllegalArgumentException("Wrong source worldgen value: "+domain+" / "+id);
        var values=VALUES.computeIfAbsent(domain,ignored->new LinkedHashMap<>());
        if(values.putIfAbsent(id,value)!=null)throw new IllegalStateException("Duplicate source worldgen declaration: "+domain+" / "+id);
        return value;
    }
    public static Holder<?> registerHolder(Object registry,Identifier id,Object value){return Holder.direct(register(registry,id,value));}
    public static synchronized Map<Identifier,byte[]> resources(){
        // Encoding completes before sealing; a failed encode does not discard any declaration.
        Map<Identifier,byte[]> result=new LinkedHashMap<>();
        VALUES.forEach((domain,values)->values.forEach((id,value)->{
            var json=domain==Domain.CONFIGURED
                ?net.minecraft.world.level.levelgen.feature.Feature.DIRECT_CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,((LegacyConfiguredFeature<?,?>)value).target()).getOrThrow()
                :net.minecraft.world.level.levelgen.placement.PlacedFeature.DIRECT_CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,((LegacyPlacedFeature)value).target()).getOrThrow();
            result.put(Identifier.fromNamespaceAndPath(id.getNamespace(),"worldgen/"+(domain==Domain.CONFIGURED?"feature/":"placed_feature/")+id.getPath()+".json"),json.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }));
        sealed=true;return Map.copyOf(result);
    }
    public static Holder<?> placement(String name,Holder<LegacyConfiguredFeature<?,?>> feature,net.minecraft.world.level.levelgen.placement.PlacementModifier[] modifiers){
        return registerHolder(PLACED,Identifier.parse(name),new LegacyPlacedFeature(feature,List.of(modifiers)));
    }
    public static synchronized Optional<Identifier> identifier(Domain domain,Object value){
        return VALUES.getOrDefault(domain,new LinkedHashMap<>()).entrySet().stream().filter(e->e.getValue()==value).map(Map.Entry::getKey).findFirst();
    }
    public static synchronized int size(){return VALUES.values().stream().mapToInt(Map::size).sum();}
}
