package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.block.Forge1192NeighborBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.capability.LegacyBlockCapabilityDispatcher;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.capability.LegacyCapability;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.capability.LegacyCapabilityManager;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.capability.LegacyCapabilityToken;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.capability.LegacyICapabilityProvider;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.capability.LegacyINBTSerializable;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.capability.LegacyLazyOptional;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.item.Forge1192DurabilityBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192CompatibilitySurface;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192EventBusBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192LifecycleBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.registry.Forge1192RegistrationPlanner;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.Forge1192ConfigScreenBridge;
import com.fuckingdeveloper.lms.profile.forge1192.runtime.render.LegacyConfigScreenFactory;

import com.fuckingdeveloper.lms.profile.forge1192.Forge1192LegacyClassTransformer;


import com.fuckingdeveloper.lms.profile.forge1192.runtime.event.LegacyAttachCapabilitiesEvent;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

public final class BatchFamilyCheck {
    static final LegacyCapability<String> TOKEN = LegacyCapabilityManager.get(new LegacyCapabilityToken<String>() {});
    static final class SerializableProvider implements LegacyICapabilityProvider, LegacyINBTSerializable<CompoundTag> {
        final LegacyLazyOptional<String> value = LegacyLazyOptional.of(() -> "source-value");
        Direction side; int queries; int persisted = 7;
        public <T> LegacyLazyOptional<T> getCapability(LegacyCapability<T> token, Direction side) {
            this.side = side; queries++; return token == TOKEN ? value.cast() : LegacyLazyOptional.empty();
        }
        public CompoundTag serializeNBT() { var tag = new CompoundTag(); tag.putInt("amount", persisted); return tag; }
        public void deserializeNBT(CompoundTag tag) { persisted = tag.getIntOr("amount", -1); }
    }
    public static final class AttachmentSubscriber {
        final SerializableProvider provider = new SerializableProvider();
        int calls; String owner;
        @SubscribeEvent(priority = EventPriority.HIGH)
        public void attach(LegacyAttachCapabilitiesEvent<BlockEntity> event) {
            calls++; owner = Forge1192LifecycleBridge.requireActive().modId();
            event.addCapability(Identifier.fromNamespaceAndPath("batch_fixture", "storage"), provider);
            event.addListener(provider.value::invalidate);
        }
    }
    static void run() throws Exception {
        ScreenStateFamilyCheck.run();
        ButtonFamilyCheck.run();
        WidgetHierarchyFamilyCheck.run();
        var bus = BusBuilder.builder().build();
        var host = host(bus);
        var subscriber = new AttachmentSubscriber();
        try (var scope = Forge1192LifecycleBridge.enter("batch_fixture", bus, host)) {
            Forge1192EventBusBridge.register(NeoForge.EVENT_BUS, subscriber);
            configExtensions(host);
            reloadListeners();
        }
        NeoForge.EVENT_BUS.start();
        var receiver = new ChestBlockEntity(BlockPos.ZERO, Blocks.CHEST.defaultBlockState());
        var event = new LegacyAttachCapabilitiesEvent<>(BlockEntity.class, receiver);
        var dispatcher = new LegacyBlockCapabilityDispatcher();
        check(!dispatcher.getCapability(TOKEN, null).isPresent(), "gathering superclass query must see empty dispatcher");
        try (var outer = Forge1192LifecycleBridge.enter("batch_outer", bus, host)) {
            var prior = Forge1192LifecycleBridge.requireActive();
            NeoForge.EVENT_BUS.post(event);
            check(Forge1192LifecycleBridge.requireActive() == prior, "nested owner context not restored");
            try (var inner = Forge1192LifecycleBridge.enterCallback("batch_fixture")) {
                check(Forge1192LifecycleBridge.requireActive().modId().equals("batch_fixture"), "callback did not enter recorded owner");
                try (var same = Forge1192LifecycleBridge.enterCallback("batch_fixture")) {
                    check(Forge1192LifecycleBridge.requireActive().modId().equals("batch_fixture"), "same-owner callback scope");
                }
            }
            check(Forge1192LifecycleBridge.requireActive() == prior, "callback owner restoration");
        }
        check(subscriber.calls == 1 && subscriber.owner.equals("batch_fixture"), "real source attachment event delivery/owner");
        dispatcher.complete(event);
        check(dispatcher.getCapability(TOKEN, Direction.WEST) == subscriber.provider.value && subscriber.provider.side == Direction.WEST, "provider identity/sided dispatch");
        var absent = LegacyCapabilityManager.get(new LegacyCapabilityToken<Integer>() {});
        check(!dispatcher.getCapability(absent, null).isPresent(), "genuine custom provider absence");
        var saved = dispatcher.serialize(); subscriber.provider.persisted = 0; dispatcher.deserialize(saved);
        check(subscriber.provider.persisted == 7 && saved.contains("batch_fixture:storage"), "source keyed NBT round trip");
        fails(() -> event.addCapability(Identifier.fromNamespaceAndPath("batch_fixture", "storage"), subscriber.provider));
        dispatcher.invalidate(); check(!subscriber.provider.value.isPresent() && !dispatcher.getCapability(TOKEN, null).isPresent(), "attachment invalidation");
        dispatcher.revive(); check(dispatcher.isValid() && !dispatcher.getCapability(TOKEN, null).isPresent(), "revival must not regenerate invalid optionals");
        fails(() -> dispatcher.complete(event));
        // Separate isolated providers prove ordered first-present selection and null rejection.
        var order = new ArrayList<Integer>(); var orderedEvent = new LegacyAttachCapabilitiesEvent<>(BlockEntity.class, receiver);
        orderedEvent.addCapability(Identifier.fromNamespaceAndPath("batch_fixture", "first"), provider((cap, side) -> { order.add(1); return LegacyLazyOptional.empty(); }));
        var present = LegacyLazyOptional.of(() -> "ordered");
        orderedEvent.addCapability(Identifier.fromNamespaceAndPath("batch_fixture", "second"), provider((cap, side) -> { order.add(2); return present; }));
        orderedEvent.addCapability(Identifier.fromNamespaceAndPath("batch_fixture", "third"), provider((cap, side) -> { throw new AssertionError("queried past present provider"); }));
        var ordered = new LegacyBlockCapabilityDispatcher(); ordered.complete(orderedEvent);
        check(ordered.getCapability(TOKEN, null) == present && order.equals(List.of(1, 2)), "ordered provider selection");
        carbonGate(); durability(); mutableTags(); originScopes(); transformationContracts(); hierarchyCaches(); mixinAnchors();
        SlotRenderFamilyCheck.run();
        SubscriberIndexCheck.run(bus, host);
        System.out.println("PASS: real attachment event dispatch/nested ownership, ordered optional identity/absence/NBT/invalidation, durability ordering, deferred origin scopes, source adapter bytecode, hierarchy rebinding, native hook anchors/config extensions/reload registration");
    }
    private static LegacyICapabilityProvider provider(BiFunction<LegacyCapability<?>, Direction, LegacyLazyOptional<?>> function) {
        return new LegacyICapabilityProvider() {
            @SuppressWarnings("unchecked") public <T> LegacyLazyOptional<T> getCapability(LegacyCapability<T> cap, Direction side) {
                return (LegacyLazyOptional<T>)function.apply(cap, side);
            }
        };
    }
    private static void carbonGate() throws Exception {
        var path = java.nio.file.Path.of("legacy-mods/CarbonConfig-1.19.2-2.0.2.jar");
        if (!java.nio.file.Files.isRegularFile(path)) return;
        var plan = new Forge1192RegistrationPlanner().plan(path, "carbonconfiglib.CarbonConfig");
        var assessment = new Forge1192CompatibilitySurface().assess(plan.boundaries());
        var remaining = assessment.requirements().stream().filter(r -> r.state() != Forge1192CompatibilitySurface.State.SUPPORTED).map(Forge1192CompatibilitySurface.Requirement::target).toList();
        check(assessment.executable(), "source container/bootstrap contracts remain gated");
        System.out.println("External dependency lifecycle inventory: requirements=" + assessment.requirements().size() + " remaining=" + remaining.size() + " " + remaining);
    }
    private static void configExtensions(ModContainer host) {
        var count = new AtomicInteger();
        var source = new LegacyConfigScreenFactory((client, parent) -> parent);
        Forge1192ConfigScreenBridge.registerExtensionPoint(host, LegacyConfigScreenFactory.class, () -> { count.incrementAndGet(); return source; });
        check(count.get() == 0, "source config supplier became eager");
        check(host.getCustomExtension(IConfigScreenFactory.class).orElseThrow() == source && count.get() == 1, "native config key/identity");
        check(Forge1192ConfigScreenBridge.getCustomExtension(host, LegacyConfigScreenFactory.class).orElseThrow() == source, "source config identity on lookup");
        IConfigScreenFactory nativeFactory = (container, parent) -> { check(container == host, "native config lost container ownership"); return parent; };
        host.registerExtensionPoint(IConfigScreenFactory.class, nativeFactory);
        var projection = (LegacyConfigScreenFactory)Forge1192ConfigScreenBridge.getCustomExtension(host, LegacyConfigScreenFactory.class).orElseThrow();
        check(projection.screenFunction().apply(null, null) == null, "native-to-source screen function projection");
    }
    private static void reloadListeners() {
        try (var resources = new net.minecraft.server.packs.resources.ReloadableResourceManager(net.minecraft.server.packs.PackType.CLIENT_RESOURCES)) {
            // The native sorted event requires its real vanilla tail; only registration is exercised here.
            resources.registerReloadListener(new net.minecraft.client.resources.language.LanguageManager(null, "en_us", language -> {}));
            var event = new AddClientReloadListenersEvent(resources);
            var a = listener(); var b = listener();
            Forge1192EventBusBridge.Client.registerClientReloadListener(event, a);
            Forge1192EventBusBridge.Client.registerClientReloadListener(event, b);
            check(event.getRegistry().size() == 3 && event.getRegistry().containsValue(a) && event.getRegistry().containsValue(b), "client reload identity/key disambiguation");
        }
    }
    private static net.minecraft.server.packs.resources.PreparableReloadListener listener() {
        return (net.minecraft.server.packs.resources.PreparableReloadListener)Proxy.newProxyInstance(BatchFamilyCheck.class.getClassLoader(),
                new Class[]{net.minecraft.server.packs.resources.PreparableReloadListener.class}, (p,m,a) -> {
                    if (m.getName().equals("toString")) return "source-listener";
                    if (m.getName().equals("equals")) return p == a[0];
                    if (m.getName().equals("hashCode")) return System.identityHashCode(p);
                    throw new AssertionError("Reload operation was not requested by this registration test: " + m);
                });
    }
    private static void durability() throws Exception {
        Items.STONE.builtInRegistryHolder().bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 1)
                .set(DataComponents.MAX_DAMAGE, 5).set(DataComponents.DAMAGE, 0).build());
        var stack = new ItemStack(Items.STONE, 2); var random = RandomSource.create(13);
        check(!Forge1192DurabilityBridge.hurt(stack, 2, random, null) && stack.getDamageValue() == 2, "raw damage");
        check(!Forge1192DurabilityBridge.hurt(stack, -1, random, null) && stack.getDamageValue() == 1, "negative repair");
        check(Forge1192DurabilityBridge.hurt(stack, 4, random, null) && stack.getDamageValue() == 5, "source break threshold");
        var finish = Forge1192DurabilityBridge.class.getDeclaredMethod("completeBreak", ItemStack.class, Runnable.class, Consumer.class); finish.setAccessible(true);
        var order = new ArrayList<String>();
        finish.invoke(null, stack, (Runnable)() -> { check(stack.getCount() == 2 && stack.getDamageValue() == 5, "break notification timing"); order.add("notify"); },
                (Consumer<Item>)item -> { check(item == Items.STONE && stack.getCount() == 1, "break statistic timing/item"); order.add("statistic"); });
        check(order.equals(List.of("notify", "statistic")) && stack.getCount() == 1 && stack.getDamageValue() == 0, "consume/statistic/reset ordering");
    }
    private static void mutableTags() {
        var state = new com.fuckingdeveloper.lms.profile.forge1192.runtime.itemhandler.LegacyMutableTagState();
        var stack = new ItemStack(Items.STONE); var tag = new CompoundTag(); tag.putInt("charge", 40);
        var shared = new net.minecraft.world.item.component.CustomData(tag);
        stack.set(DataComponents.CUSTOM_DATA, shared);
        ItemStack beforeOwnership = stack.copy();
        CompoundTag live = state.acquire(stack, false);
        check(live != tag && state.acquire(stack, true) == live, "source tag identity/detach from shared data");
        live.putInt("charge", 30);
        check(beforeOwnership.get(DataComponents.CUSTOM_DATA).tag.getIntOr("charge", -1) == 40, "pre-ownership inventory snapshot mutated");
        ItemStack snapshot = stack.copy(); state.detachCopy(stack, snapshot);
        var resource = net.neoforged.neoforge.transfer.item.ItemResource.of(state.resourceSnapshot(stack));
        live.putInt("charge", 20);
        check(snapshot.get(DataComponents.CUSTOM_DATA).tag.getIntOr("charge", -1) == 30, "source stack copy aliases live tag");
        check(!ItemStack.matches(stack, snapshot), "inventory comparison cannot detect charge changes");
        check(resource.toStack(1).get(DataComponents.CUSTOM_DATA).tag.getIntOr("charge", -1) == 30, "transfer resource aliases mutable data");
        var supplied = new CompoundTag(); supplied.putInt("charge", 9); state.assign(stack, supplied);
        check(state.acquire(stack, false) == supplied, "source explicit set lost tag identity");
        stack.remove(DataComponents.CUSTOM_DATA);
        check(state.acquire(stack, false) == null && !state.owns(stack), "native removal did not supersede ownership");
    }

    private static void originScopes() {
        var first = new Forge1192NeighborBridge.Origin(null, new BlockPos(1, 2, 3));
        var second = new Forge1192NeighborBridge.Origin(null, new BlockPos(4, 5, 6));
        var captured = new Forge1192NeighborBridge.Origin[1];
        Forge1192NeighborBridge.run(first, null, () -> {
            Forge1192NeighborBridge.run(second, null, () -> captured[0] = Forge1192NeighborBridge.capture());
            check(Forge1192NeighborBridge.requirePosition(null).equals(first.position()), "nested neighbor scope restoration");
        });
        check(Forge1192NeighborBridge.capture() == null, "origin leaked after source scope");
        Forge1192NeighborBridge.run(captured[0], null, () -> check(Forge1192NeighborBridge.requirePosition(null).equals(second.position()), "deferred origin replay"));
        try { Forge1192NeighborBridge.run(first, null, () -> { throw new IllegalStateException("fixture"); }); } catch (IllegalStateException expected) {}
        check(Forge1192NeighborBridge.capture() == null, "exception leaked origin"); fails(() -> Forge1192NeighborBridge.requirePosition(null));
    }
    private static void transformationContracts() throws Exception {
        var transformer = new Forge1192LegacyClassTransformer();
        var neighborPass = Forge1192LegacyClassTransformer.class.getDeclaredMethod("rewriteLegacyNeighborCallbacks", ClassNode.class); neighborPass.setAccessible(true);
        var block = node("fixture/SourceNeighbor", "net/minecraft/world/level/block/Block");
        String desc = "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos;Z)V";
        var callback = new MethodNode(Opcodes.ACC_PUBLIC, "m_6861_", desc, null, null);
        callback.instructions.add(new InsnNode(Opcodes.RETURN)); callback.maxLocals = 7; block.methods.add(callback);
        check((int)neighborPass.invoke(transformer, block) == 1, "native neighbor callback was not generated");
        var generated = block.methods.getLast(); new Analyzer<>(new BasicVerifier()).analyze(block.name, generated);
        check(generated.instructions.toArray()[5] instanceof VarInsnNode load && load.var == 2, "callback did not supply authoritative level for origin lookup");
        var pass = Forge1192LegacyClassTransformer.class.getDeclaredMethod("rewriteLegacyDurabilityFamily", ClassNode.class); pass.setAccessible(true);
        var item = node("fixture/SourceDamageHook", "net/minecraft/world/item/Item");
        String hook = "(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)I";
        var damage = new MethodNode(Opcodes.ACC_PUBLIC, "damageItem", hook, null, null);
        damage.instructions.add(new InsnNode(Opcodes.ICONST_0)); damage.instructions.add(new InsnNode(Opcodes.IRETURN)); damage.maxStack = 1; damage.maxLocals = 5; item.methods.add(damage);
        check((int)pass.invoke(transformer, item) == 1 && item.methods.getFirst().name.equals("lms$sourceDamageItem"), "erased damage-hook callback semantics not separated");
        for (var method : item.methods) new Analyzer<>(new BasicVerifier()).analyze(item.name, method);
        java.lang.invoke.MethodHandles.publicLookup().findStatic(net.neoforged.neoforge.client.gui.modlist.ModListScreen.class, "create",
                java.lang.invoke.MethodType.methodType(net.neoforged.neoforge.client.gui.modlist.ModListScreen.class, net.minecraft.client.gui.screens.Screen.class));
    }
    private static void hierarchyCaches() throws Exception {
        var transformer = new Forge1192LegacyClassTransformer(); var lookupCount = new AtomicInteger();
        byte[] first = bytes(node("fixture/Rebound", "net/minecraft/world/level/block/Block"));
        transformer.bindManagedClassBytes(name -> { lookupCount.incrementAndGet(); return name.equals("fixture/Rebound") ? first : null; });
        var query = Forge1192LegacyClassTransformer.class.getDeclaredMethod("isManagedSubtypeOf", String.class, String.class); query.setAccessible(true);
        check((boolean)query.invoke(transformer, "fixture/Rebound", "net/minecraft/world/level/block/Block"), "source hierarchy decision");
        int count = lookupCount.get();
        check((boolean)query.invoke(transformer, "fixture/Rebound", "net/minecraft/world/level/block/Block") && count == lookupCount.get(), "subtype cache rereads graph");
        transformer.bindManagedClassBytes(name -> name.equals("fixture/Rebound") ? bytes(node("fixture/Rebound", "java/lang/Object")) : null);
        check(!(boolean)query.invoke(transformer, "fixture/Rebound", "net/minecraft/world/level/block/Block"), "positive cache survived rebinding");
        transformer.bindManagedClassBytes(name -> name.equals("fixture/Rebound") ? first : null);
        check((boolean)query.invoke(transformer, "fixture/Rebound", "net/minecraft/world/level/block/Block"), "negative cache survived rebinding");
    }
    private static void mixinAnchors() throws Exception {
        for (String owner : List.of("MultiNeighborUpdate", "SimpleNeighborUpdate", "FullNeighborUpdate")) {
            var node = read("net/minecraft/world/level/redstone/CollectingNeighborUpdater$" + owner);
            long anchors = node.methods.stream().filter(m -> m.name.equals("runNext")).flatMap(m -> Arrays.stream(m.instructions.toArray()))
                    .filter(i -> i instanceof MethodInsnNode call && call.owner.equals("net/minecraft/world/level/redstone/NeighborUpdater") && call.name.equals("executeUpdate")
                            && call.desc.equals("(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/redstone/Orientation;Z)V")).count();
            check(anchors == 1, "target queue hook anchor drift: " + owner);
        }
        var be = read("net/minecraft/world/level/block/entity/BlockEntity");
        check(be.methods.stream().filter(m -> m.name.equals("<init>")).count() == 1, "constructor hook ambiguity");
        for (String name : List.of("setRemoved", "loadAdditional", "saveAdditional")) check(be.methods.stream().filter(m -> m.name.equals(name)).count() == 1, "target block lifecycle hook drift: " + name);
    }
    private static ClassNode read(String owner) throws Exception {
        try (var input = BatchFamilyCheck.class.getClassLoader().getResourceAsStream(owner + ".class")) {
            var node = new ClassNode(); new ClassReader(Objects.requireNonNull(input)).accept(node, 0); return node;
        }
    }
    private static byte[] bytes(ClassNode node) { var writer = new ClassWriter(0); node.accept(writer); return writer.toByteArray(); }
    private static ClassNode node(String name, String parent) { var node = new ClassNode(); node.version = Opcodes.V17; node.access = Opcodes.ACC_PUBLIC; node.name = name; node.superName = parent; return node; }
    private static ModContainer host(IEventBus bus) {
        var info = (IModInfo)Proxy.newProxyInstance(IModInfo.class.getClassLoader(), new Class[]{IModInfo.class}, (p,m,a) -> {
            if (m.getName().equals("getModId")) return "batch_fixture"; throw new AssertionError(m);
        });
        return new ModContainer(info) { @Override public IEventBus getEventBus() { return bus; } };
    }
    private static void fails(Runnable action) { try { action.run(); throw new AssertionError("Expected explicit boundary failure"); } catch (IllegalStateException expected) {} }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
