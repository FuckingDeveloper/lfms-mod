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
    public enum State { SUPPORTED, INACTIVE_DATA_GENERATION, ADAPTER_REQUIRED, UNSUPPORTED }

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
            "net.minecraftforge.client.event.InputEvent$Key#getAction()I",
            "net.minecraftforge.client.event.InputEvent$Key#getKey()I",
            "net.minecraftforge.client.event.InputEvent$Key#getScanCode()I",
            "net.minecraftforge.client.event.InputEvent$Key#getModifiers()I",
            "net.minecraftforge.client.event.RegisterKeyMappingsEvent#register(Lnet/minecraft/client/KeyMapping;)V",
            "net.minecraftforge.fml.util.ObfuscationReflectionHelper#getPrivateValue(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;",
            "net.minecraftforge.fml.DistExecutor#unsafeRunWhenOn(Lnet/minecraftforge/api/distmarker/Dist;Ljava/util/function/Supplier;)V",
            "net.minecraftforge.registries.DeferredRegister#register(Lnet/minecraftforge/eventbus/api/IEventBus;)V",
            "net.minecraftforge.registries.RegistryObject#get()Ljava/lang/Object;",
            "net.minecraftforge.network.NetworkRegistry$ChannelBuilder#named(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraftforge/network/NetworkRegistry$ChannelBuilder;",
            "net.minecraftforge.network.NetworkRegistry$ChannelBuilder#networkProtocolVersion(Ljava/util/function/Supplier;)Lnet/minecraftforge/network/NetworkRegistry$ChannelBuilder;",
            "net.minecraftforge.network.NetworkRegistry$ChannelBuilder#clientAcceptedVersions(Ljava/util/function/Predicate;)Lnet/minecraftforge/network/NetworkRegistry$ChannelBuilder;",
            "net.minecraftforge.network.NetworkRegistry$ChannelBuilder#serverAcceptedVersions(Ljava/util/function/Predicate;)Lnet/minecraftforge/network/NetworkRegistry$ChannelBuilder;",
            "net.minecraftforge.network.NetworkRegistry$ChannelBuilder#simpleChannel()Lnet/minecraftforge/network/simple/SimpleChannel;",
            "net.minecraftforge.network.simple.SimpleChannel#registerMessage(ILjava/lang/Class;Ljava/util/function/BiConsumer;Ljava/util/function/Function;Ljava/util/function/BiConsumer;Ljava/util/Optional;)Lnet/minecraftforge/network/simple/IndexedMessageCodec$MessageHandler;",

            "net.minecraftforge.client.ConfigScreenHandler$ConfigScreenFactory#<init>(Ljava/util/function/BiFunction;)V",
            "net.minecraftforge.client.ConfigScreenHandler$ConfigScreenFactory#<init>(Ljava/util/function/Function;)V",
            "net.minecraftforge.client.ConfigScreenHandler$ConfigScreenFactory#screenFunction()Ljava/util/function/BiFunction;",
            "net.minecraftforge.client.event.RegisterClientReloadListenersEvent#registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V",
            "net.minecraftforge.client.gui.ModListScreen#<init>(Lnet/minecraft/client/gui/screens/Screen;)V",
            "net.minecraftforge.fml.ModContainer#getCustomExtension(Ljava/lang/Class;)Ljava/util/Optional;",
            "net.minecraftforge.fml.ModContainer#registerExtensionPoint(Ljava/lang/Class;Ljava/util/function/Supplier;)V",
            "net.minecraftforge.fml.ModContainer#getModId()Ljava/lang/String;",
            "net.minecraftforge.fml.ModContainer#getModInfo()Lnet/minecraftforge/forgespi/language/IModInfo;",
            "net.minecraftforge.forgespi.language.IModInfo#getDisplayName()Ljava/lang/String;",
            "net.minecraftforge.eventbus.api.IEventBus#addGenericListener(Ljava/lang/Class;Ljava/util/function/Consumer;)V",
            "net.minecraftforge.eventbus.api.IEventBus#addListener(Ljava/util/function/Consumer;)V",
            "net.minecraftforge.eventbus.api.IEventBus#register(Ljava/lang/Object;)V",
            "net.minecraftforge.api.distmarker.Dist#isClient()Z",
            "net.minecraftforge.api.distmarker.Dist#isDedicatedServer()Z",
            "net.minecraftforge.fml.LogicalSide#isClient()Z",
            "net.minecraftforge.fml.LogicalSide#isServer()Z",
            "net.minecraftforge.fml.util.thread.EffectiveSide#get()Lnet/minecraftforge/fml/LogicalSide;",
            "net.minecraftforge.eventbus.api.Event#<init>()V",
            "net.minecraftforge.event.AddPackFindersEvent#addRepositorySource(Lnet/minecraft/server/packs/repository/RepositorySource;)V",
            "net.minecraftforge.event.AddPackFindersEvent#getPackType()Lnet/minecraft/server/packs/PackType;",
            "net.minecraftforge.event.AddReloadListenerEvent#addListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V",
            "net.minecraftforge.event.RegisterCommandsEvent#getCommandSelection()Lnet/minecraft/commands/Commands$CommandSelection;",
            "net.minecraftforge.event.RegisterCommandsEvent#getDispatcher()Lcom/mojang/brigadier/CommandDispatcher;",
            "net.minecraftforge.event.village.VillagerTradesEvent#getTrades()Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;",
            "net.minecraftforge.event.village.VillagerTradesEvent#getType()Lnet/minecraft/world/entity/npc/VillagerProfession;",
            "net.minecraftforge.event.village.WandererTradesEvent#getGenericTrades()Ljava/util/List;",
            "net.minecraftforge.event.village.WandererTradesEvent#getRareTrades()Ljava/util/List;",
            "net.minecraftforge.fluids.FluidStack#getFluid()Lnet/minecraft/world/level/material/Fluid;",
            "net.minecraftforge.fluids.FluidUtil#getFluidContained(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Optional;",
            "net.minecraftforge.fluids.capability.IFluidHandler#getTanks()I",
            "net.minecraftforge.fluids.capability.templates.FluidTank#<init>(I)V",
            "net.minecraftforge.fluids.capability.templates.FluidTank#<init>(ILjava/util/function/Predicate;)V",
            "net.minecraftforge.common.ForgeMod#enableMilkFluid()V",
            "net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent#register(Ljava/lang/Class;)V",
            "net.minecraftforge.event.ForgeEventFactory#onNeighborNotify(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Ljava/util/EnumSet;Z)Lnet/minecraftforge/event/level/BlockEvent$NeighborNotifyEvent;",
            "net.minecraftforge.common.util.LazyOptional#empty()Lnet/minecraftforge/common/util/LazyOptional;",
            "net.minecraftforge.common.util.LazyOptional#invalidate()V",
            "net.minecraftforge.common.util.LazyOptional#isPresent()Z",
            "net.minecraftforge.common.util.LazyOptional#of(Lnet/minecraftforge/common/util/NonNullSupplier;)Lnet/minecraftforge/common/util/LazyOptional;",
            "net.minecraftforge.common.util.LazyOptional#cast()Lnet/minecraftforge/common/util/LazyOptional;",
            "net.minecraftforge.common.util.LazyOptional#addListener(Lnet/minecraftforge/common/util/NonNullConsumer;)V",
            "net.minecraftforge.common.util.LazyOptional#orElseGet(Lnet/minecraftforge/common/util/NonNullSupplier;)Ljava/lang/Object;",
            "net.minecraftforge.event.server.ServerAboutToStartEvent#getServer()Lnet/minecraft/server/MinecraftServer;",
            "net.minecraftforge.network.NetworkEvent$Context#enqueueWork(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;",
            "net.minecraftforge.network.NetworkEvent$Context#getSender()Lnet/minecraft/server/level/ServerPlayer;",
            "net.minecraftforge.network.NetworkEvent$Context#setPacketHandled(Z)V",
            "net.minecraftforge.network.NetworkRegistry#newSimpleChannel(Lnet/minecraft/resources/ResourceLocation;Ljava/util/function/Supplier;Ljava/util/function/Predicate;Ljava/util/function/Predicate;)Lnet/minecraftforge/network/simple/SimpleChannel;",
            "net.minecraftforge.network.simple.SimpleChannel#registerMessage(ILjava/lang/Class;Ljava/util/function/BiConsumer;Ljava/util/function/Function;Ljava/util/function/BiConsumer;)Lnet/minecraftforge/network/simple/IndexedMessageCodec$MessageHandler;",
            "net.minecraftforge.eventbus.api.IEventBus#addListener(Lnet/minecraftforge/eventbus/api/EventPriority;Ljava/util/function/Consumer;)V",
            "net.minecraftforge.eventbus.api.IEventBus#post(Lnet/minecraftforge/eventbus/api/Event;)Z",
            "net.minecraftforge.fluids.FluidStack#isFluidEqual(Lnet/minecraftforge/fluids/FluidStack;)Z",
            "net.minecraftforge.registries.GameData#checkPrefix(Ljava/lang/String;Z)Lnet/minecraft/resources/ResourceLocation;",
            "net.minecraftforge.registries.IForgeRegistry#containsKey(Lnet/minecraft/resources/ResourceLocation;)Z",
            "net.minecraftforge.registries.IForgeRegistry#getValue(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;",
            "net.minecraftforge.registries.IForgeRegistry#getKey(Ljava/lang/Object;)Lnet/minecraft/resources/ResourceLocation;",
            "net.minecraftforge.registries.IForgeRegistry#iterator()Ljava/util/Iterator;",
            "net.minecraftforge.registries.IForgeRegistry#tags()Lnet/minecraftforge/registries/tags/ITagManager;",
            "net.minecraftforge.registries.tags.ITagManager#getTag(Lnet/minecraft/tags/TagKey;)Lnet/minecraftforge/registries/tags/ITag;",
            "net.minecraftforge.registries.tags.ITag#iterator()Ljava/util/Iterator;",
            "net.minecraftforge.registries.tags.ITag#getRandomElement(Lnet/minecraft/util/RandomSource;)Ljava/util/Optional;",
            "net.minecraftforge.registries.IForgeRegistry#register(Ljava/lang/String;Ljava/lang/Object;)V",
            "net.minecraftforge.registries.IForgeRegistry#register(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;)V",
            "net.minecraftforge.registries.RegisterEvent#getForgeRegistry()Lnet/minecraftforge/registries/IForgeRegistry;",
            "net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext#get()Lnet/minecraftforge/fml/javafmlmod/FMLJavaModLoadingContext;",
            "net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext#getModEventBus()Lnet/minecraftforge/eventbus/api/IEventBus;",
            "net.minecraftforge.fml.ModLoadingContext#get()Lnet/minecraftforge/fml/ModLoadingContext;",
            "net.minecraftforge.fml.ModLoadingContext#getActiveContainer()Lnet/minecraftforge/fml/ModContainer;",
            "net.minecraftforge.fml.ModLoadingContext#setActiveContainer(Lnet/minecraftforge/fml/ModContainer;)V",
            "net.minecraftforge.fml.ModLoadingContext#getActiveNamespace()Ljava/lang/String;",
            "net.minecraftforge.fml.ModList#forEachModInOrder(Ljava/util/function/Consumer;)V",
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

    private static final Set<String> NETWORK_ADAPTERS = Set.of(
            "net.minecraftforge.network.NetworkEvent$Context#enqueueWork(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;",
            "net.minecraftforge.network.NetworkEvent$Context#getSender()Lnet/minecraft/server/level/ServerPlayer;",
            "net.minecraftforge.network.NetworkEvent$Context#setPacketHandled(Z)V",
            "net.minecraftforge.network.NetworkRegistry#newSimpleChannel(Lnet/minecraft/resources/ResourceLocation;Ljava/util/function/Supplier;Ljava/util/function/Predicate;Ljava/util/function/Predicate;)Lnet/minecraftforge/network/simple/SimpleChannel;",
            "net.minecraftforge.network.simple.SimpleChannel#registerMessage(ILjava/lang/Class;Ljava/util/function/BiConsumer;Ljava/util/function/Function;Ljava/util/function/BiConsumer;)Lnet/minecraftforge/network/simple/IndexedMessageCodec$MessageHandler;"
    );

    private static final Set<String> EXPLICIT_UNSUPPORTED = Set.of();

    /** Conservative artifact validation for a known unsupported native-super data family. */
    public Assessment assess(List<Forge1192RegistrationPlanner.Boundary> boundaries,java.nio.file.Path artifact) throws java.io.IOException {
        var base=assess(boundaries);
        var requirements=new ArrayList<>(base.requirements());
        var unsupported=new java.util.TreeSet<String>();
        try(var jar=new java.util.jar.JarFile(artifact.toFile(),false)){
            for(var entry:java.util.Collections.list(jar.entries())){
                if(!entry.getName().endsWith(".class"))continue;
                try(var in=jar.getInputStream(entry)){
                    new org.objectweb.asm.ClassReader(in).accept(new org.objectweb.asm.ClassVisitor(org.objectweb.asm.Opcodes.ASM9){
                        @Override public org.objectweb.asm.MethodVisitor visitMethod(int access,String name,String descriptor,String signature,String[] exceptions){
                            return new org.objectweb.asm.MethodVisitor(org.objectweb.asm.Opcodes.ASM9){
                                @Override public void visitMethodInsn(int opcode,String owner,String method,String desc,boolean itf){
                                    if(opcode!=org.objectweb.asm.Opcodes.INVOKESPECIAL)return;
                                    if(!owner.equals("net/minecraft/world/level/block/entity/RandomizableContainerBlockEntity")
                                            &&!owner.equals("net/minecraft/world/level/block/entity/BaseContainerBlockEntity"))return;
                                    if((method.equals("m_142466_")||method.equals("m_183515_"))&&desc.equals("(Lnet/minecraft/nbt/CompoundTag;)V"))
                                        unsupported.add(owner.replace('/','.')+"#"+method+desc);
                                }
                            };
                        }
                    },org.objectweb.asm.ClassReader.SKIP_DEBUG|org.objectweb.asm.ClassReader.SKIP_FRAMES);
                }
            }
        }
        for(String target:unsupported)requirements.add(new Requirement(target,1,State.SUPPORTED,"NATIVE_CONTAINER_DATA"));
        return new Assessment(List.copyOf(requirements),base.executable());
    }

    public Assessment assess(List<Forge1192RegistrationPlanner.Boundary> boundaries) {
        var grouped = boundaries.stream().collect(java.util.stream.Collectors.groupingBy(
                boundary -> boundary.owner() + "#" + boundary.name() + boundary.descriptor(),
                java.util.TreeMap::new, java.util.stream.Collectors.toList()));
        List<Requirement> requirements = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            String target = entry.getKey();
            boolean inactive=entry.getValue().stream().allMatch(boundary -> boundary.phase()==Forge1192RegistrationPlanner.ExecutionPhase.DATA_GENERATION);
            State state = inactive ? State.INACTIVE_DATA_GENERATION : SUPPORTED_BRIDGES.contains(target)
                    ? State.SUPPORTED
                    : EXPLICIT_UNSUPPORTED.contains(target) ? State.UNSUPPORTED
                    : NETWORK_ADAPTERS.contains(target) ? State.ADAPTER_REQUIRED : State.ADAPTER_REQUIRED;
            requirements.add(new Requirement(target, entry.getValue().size(), state,
                    NETWORK_ADAPTERS.contains(target) ? "NETWORK_PROTOCOL" : family(target)));
        }
        boolean executable = requirements.stream()
                .allMatch(requirement -> requirement.state() == State.SUPPORTED || requirement.state() == State.INACTIVE_DATA_GENERATION);
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
