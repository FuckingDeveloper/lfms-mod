package com.fuckingdeveloper.lms.mixin;

import com.fuckingdeveloper.lms.target.neoforge263.Target263LegacyLootResources;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.loot.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The exact target manager inherits prepare; add an override at that semantic boundary. */
@Mixin(LootModifierManager.class)
abstract class Target263LegacyLootManagerMixin extends SimpleJsonResourceReloadListener<IGlobalLootModifier> {
    @Unique private List<Identifier> lms$sourceOrder = List.of();
    @Unique private boolean lms$legacyActive;
    @Shadow private List<IGlobalLootModifier> sortedModifiers;

    private Target263LegacyLootManagerMixin() {
        super(IGlobalLootModifier.DIRECT_CODEC, FileToIdConverter.registry(
                ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("loot_modifiers"))));
    }

    @Override
    protected Map<Identifier, IGlobalLootModifier> prepare(ResourceManager manager, ProfilerFiller profiler) {
        var plan = Target263LegacyLootResources.plan(manager);
        var decoded = super.prepare(plan.resources(), profiler);
        // Validate before publication. Native decode diagnostics must not silently lose selected entries.
        if (plan.active()) Target263LegacyLootResources.ordered(decoded, List.of(), plan.order());
        lms$sourceOrder = plan.order();
        lms$legacyActive = plan.active();
        return decoded;
    }

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("RETURN"))
    private void lms$preserveSourceOrder(Map<Identifier, IGlobalLootModifier> decoded,
            ResourceManager manager, ProfilerFiller profiler, CallbackInfo callback) {
        if (lms$legacyActive) sortedModifiers = Target263LegacyLootResources.ordered(decoded, sortedModifiers, lms$sourceOrder);
    }
}
