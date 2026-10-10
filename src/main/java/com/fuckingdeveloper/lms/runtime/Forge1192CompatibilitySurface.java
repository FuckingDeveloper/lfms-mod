package com.fuckingdeveloper.lms.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Profile-owned classification of legacy Forge API boundaries.
 *
 * <p>This is deliberately fail-closed: a boundary is executable only after a
 * semantic adapter has been implemented and marked SUPPORTED. Namespace
 * similarity alone is never treated as compatibility proof.</p>
 */
public final class Forge1192CompatibilitySurface {
    public enum State { SUPPORTED, ADAPTER_REQUIRED, UNSUPPORTED }

    public record Requirement(String target, long callSites, State state, String adapter) {}
    public record Assessment(List<Requirement> requirements, boolean executable) {}

    private static final Set<String> LIFECYCLE_PREFIXES = Set.of(
            "net.minecraftforge.eventbus.",
            "net.minecraftforge.fml.",
            "net.minecraftforge.forgespi."
    );
    private static final Set<String> REGISTRATION_PREFIXES = Set.of(
            "net.minecraftforge.registries."
    );
    private static final Set<String> NETWORK_PREFIXES = Set.of(
            "net.minecraftforge.network."
    );
    private static final Set<String> FLUID_PREFIXES = Set.of(
            "net.minecraftforge.fluids."
    );

    /**
     * Semantic bridges implemented by the Forge 1.19.2 profile. These are
     * exact legacy owner/name/descriptor contracts, not namespace guesses.
     */
    private static final Set<String> SUPPORTED_BRIDGES = Set.of(
            "net.minecraftforge.eventbus.api.IEventBus#addListener(Ljava/util/function/Consumer;)V",
            "net.minecraftforge.eventbus.api.IEventBus#addListener(Lnet/minecraftforge/eventbus/api/EventPriority;Ljava/util/function/Consumer;)V",
            "net.minecraftforge.eventbus.api.IEventBus#post(Lnet/minecraftforge/eventbus/api/Event;)Z",
            "net.minecraftforge.fluids.FluidStack#isFluidEqual(Lnet/minecraftforge/fluids/FluidStack;)Z",
            "net.minecraftforge.registries.GameData#checkPrefix(Ljava/lang/String;Z)Lnet/minecraft/resources/ResourceLocation;",
            "net.minecraftforge.registries.IForgeRegistry#containsKey(Lnet/minecraft/resources/ResourceLocation;)Z",
            "net.minecraftforge.registries.IForgeRegistry#getValue(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;",
            "net.minecraftforge.registries.IForgeRegistry#getKey(Ljava/lang/Object;)Lnet/minecraft/resources/ResourceLocation;",
            "net.minecraftforge.registries.IForgeRegistry#iterator()Ljava/util/Iterator;",
            "net.minecraftforge.registries.IForgeRegistry#register(Ljava/lang/String;Ljava/lang/Object;)V",
            "net.minecraftforge.registries.IForgeRegistry#register(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;)V",
            "net.minecraftforge.registries.RegisterEvent#getForgeRegistry()Lnet/minecraftforge/registries/IForgeRegistry;",
            "net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext#get()Lnet/minecraftforge/fml/javafmlmod/FMLJavaModLoadingContext;",
            "net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext#getModEventBus()Lnet/minecraftforge/eventbus/api/IEventBus;",
            "net.minecraftforge.fml.ModLoadingContext#get()Lnet/minecraftforge/fml/ModLoadingContext;",
            "net.minecraftforge.fml.ModLoadingContext#getActiveNamespace()Ljava/lang/String;",
            "net.minecraftforge.fml.ModList#get()Lnet/minecraftforge/fml/ModList;",
            "net.minecraftforge.fml.ModList#getAllScanData()Ljava/util/List;",
            "net.minecraftforge.fml.ModList#getModContainerById(Ljava/lang/String;)Ljava/util/Optional;",
            "net.minecraftforge.fml.ModList#isLoaded(Ljava/lang/String;)Z",
            "net.minecraftforge.fml.loading.FMLPaths#get()Ljava/nio/file/Path;",
            "net.minecraftforge.forgespi.language.ModFileScanData#getAnnotations()Ljava/util/Set;",
            "net.minecraftforge.forgespi.language.ModFileScanData$AnnotationData#annotationType()Lorg/objectweb/asm/Type;",
            "net.minecraftforge.forgespi.language.ModFileScanData$AnnotationData#memberName()Ljava/lang/String;",
            "net.minecraftforge.registries.RegisterEvent#getRegistryKey()Lnet/minecraft/resources/ResourceKey;",
            "net.minecraftforge.server.ServerLifecycleHooks#getCurrentServer()Lnet/minecraft/server/MinecraftServer;"
    );

    public Assessment assess(List<Forge1192RegistrationPlanner.Boundary> boundaries) {
        var grouped = boundaries.stream().collect(java.util.stream.Collectors.groupingBy(
                boundary -> boundary.owner() + "#" + boundary.name() + boundary.descriptor(),
                java.util.TreeMap::new, java.util.stream.Collectors.counting()));
        List<Requirement> requirements = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            String target = entry.getKey();
            State state = SUPPORTED_BRIDGES.contains(target)
                    ? State.SUPPORTED : State.ADAPTER_REQUIRED;
            requirements.add(new Requirement(target, entry.getValue(), state, family(target)));
        }
        boolean executable = requirements.stream()
                .allMatch(requirement -> requirement.state() == State.SUPPORTED);
        return new Assessment(List.copyOf(requirements), executable);
    }

    private static String family(String target) {
        String owner = target.substring(0, target.indexOf('#'));
        if (startsWithAny(owner, REGISTRATION_PREFIXES)) return "REGISTRY";
        if (startsWithAny(owner, NETWORK_PREFIXES)) return "NETWORK";
        if (startsWithAny(owner, FLUID_PREFIXES)) return "FLUID";
        if (startsWithAny(owner, LIFECYCLE_PREFIXES)) return "LIFECYCLE";
        if (owner.startsWith("net.minecraftforge.api.distmarker.")) return "DIST";
        if (owner.startsWith("net.minecraftforge.common.")) return "COMMON";
        if (owner.startsWith("net.minecraftforge.client.")) return "CLIENT";
        if (owner.startsWith("net.minecraftforge.server.")) return "SERVER";
        if (owner.startsWith("net.minecraftforge.event.")) return "GAME_EVENT";
        return "OTHER";
    }

    private static boolean startsWithAny(String value, Set<String> prefixes) {
        return prefixes.stream().anyMatch(value::startsWith);
    }
}
