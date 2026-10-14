package com.fuckingdeveloper.lms.runtime.worldgen;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.world.*;
/** Source three-argument callback; native registry context is supplied by its adapter. */
public interface LegacyBiomeModifierCallbacks {
    void lms$modifyBiome(Holder<Biome> biome,BiomeModifier.Phase phase,ModifiableBiomeInfo.BiomeInfo.Builder builder);
}
