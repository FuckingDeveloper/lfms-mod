package com.fuckingdeveloper.lms.runtime.worldgen;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.*;

/** Per-callback native data registries, including reload/pack override identity. */
public final class Target263BiomeModifierBridge {
    private Target263BiomeModifierBridge() {}
    private static final ThreadLocal<RegistryAccess> ACTIVE=new ThreadLocal<>();
    public static void modify(LegacyBiomeModifierCallbacks source,RegistryAccess registries,Holder<Biome> biome,BiomeModifier.Phase phase,ModifiableBiomeInfo.BiomeInfo.Builder builder){
        var previous=ACTIVE.get();ACTIVE.set(java.util.Objects.requireNonNull(registries));
        try{source.lms$modifyBiome(biome,phase,builder);}finally{if(previous==null)ACTIVE.remove();else ACTIVE.set(previous);}
    }
    @SuppressWarnings("unchecked")
    public static BiomeGenerationSettings.PlainBuilder addFeature(BiomeGenerationSettings.PlainBuilder builder,GenerationStep.Decoration step,Holder<?> source){
        var value=source.value();Holder<PlacedFeature> nativeHolder;
        if(value instanceof LegacyPlacedFeature placed){
            var access=ACTIVE.get();if(access==null)throw new IllegalStateException("Source biome placement needs native data-registry callback context");
            var id=LegacyWorldgenDefinitions.identifier(LegacyWorldgenDefinitions.PLACED,placed);
            nativeHolder=id.isPresent()?access.lookupOrThrow(Registries.PLACED_FEATURE).getOrThrow(ResourceKey.create(Registries.PLACED_FEATURE,id.get())):Holder.direct(placed.target());
        }else if(value instanceof PlacedFeature)nativeHolder=(Holder<PlacedFeature>)source;
        else throw new IllegalArgumentException("Unadapted biome feature holder: "+value);
        return builder.addFeature(step,nativeHolder);
    }
}
