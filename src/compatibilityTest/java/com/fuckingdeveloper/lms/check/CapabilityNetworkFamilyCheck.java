package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
import com.fuckingdeveloper.lms.runtime.*;
import com.fuckingdeveloper.lms.runtime.capability.*;
import com.fuckingdeveloper.lms.runtime.itemhandler.Target263ItemHandlerAdapter;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

final class CapabilityNetworkFamilyCheck {
    private static final String PROVIDER = Type.getInternalName(LegacyICapabilityProvider.class);
    private static final String CAP = Type.getDescriptor(LegacyCapability.class);
    private static final String OPTIONAL = Type.getDescriptor(LegacyLazyOptional.class);
    private static final String BE = "net/minecraft/world/level/block/entity/BlockEntity";
    public static class Provider implements LegacyICapabilityProvider {
        final LegacyLazyOptional<Object> value = LegacyLazyOptional.of(Object::new);
        LegacyCapability<?> token; Direction side; int calls;
        public <T> LegacyLazyOptional<T> getCapability(LegacyCapability<T> capability, Direction direction) {
            token = capability; side = direction; calls++; return value.cast();
        }
    }
    public static final class UnsidedProvider extends Provider {
        @Override public <T> LegacyLazyOptional<T> getCapability(LegacyCapability<T> capability) {
            return Forge1192BlockCapabilityBridge.sourceDefaultGetCapability(this, capability);
        }
    }
    static void run() throws Exception {
        var provider = new Provider();
        var cap = LegacyForgeCapabilities.ITEM_HANDLER;
        check(Forge1192BlockCapabilityBridge.getCapability(provider, cap, Direction.EAST) == provider.value,
                "source optional identity");
        check(provider.token == cap && provider.side == Direction.EAST && provider.calls == 1, "source query operands");
        check(Forge1192BlockCapabilityBridge.getCapability(provider, cap) == provider.value && provider.side == null,
                "unsided source default");
        var override = new UnsidedProvider();
        check(Forge1192BlockCapabilityBridge.getCapability(override, cap) == override.value && override.calls == 1,
                "explicit unsided override/base recursion");
        fails(() -> Forge1192BlockCapabilityBridge.getCapability(new Object(), cap));
        transformQueries();
        network();
        // Isolated target data bootstrap needs the exact pinned FML environment,
        // but does not discover mods, start a server, or open an interactive client.
        var constructor = net.neoforged.fml.loading.FMLLoader.class.getDeclaredConstructor(
                ClassLoader.class, String[].class, net.neoforged.api.distmarker.Dist.class, boolean.class, java.nio.file.Path.class);
        constructor.setAccessible(true);
        try (var loader = constructor.newInstance(CapabilityNetworkFamilyCheck.class.getClassLoader(),
                new String[]{"--fml.neoForgeVersion", "26.3.0.58-beta", "--fml.mcVersion", "26.3", "--fml.neoFormVersion", "test"},
                net.neoforged.api.distmarker.Dist.DEDICATED_SERVER, false, java.nio.file.Files.createTempDirectory("lms-transfer-check"))) {
            var loadingList = net.neoforged.fml.loading.FMLLoader.class.getDeclaredField("loadingModList");
            loadingList.setAccessible(true);
            loadingList.set(loader, net.neoforged.fml.loading.LoadingModList.of(java.util.List.of(), java.util.List.of(),
                    java.util.List.of(), java.util.List.of(), java.util.List.of(), java.util.Map.of()));
            net.minecraft.SharedConstants.tryDetectVersion();
            net.minecraft.server.Bootstrap.bootStrap();
            itemTransfers();
        }
        System.out.println("PASS: block capability dispatch/identity/default/super, owner negatives, actual interface lambda linkage, network provenance/deferred scopes, native item transaction commit/rollback/invalidation");
    }
    private static void transformQueries() throws Exception {
        var transformer = new Forge1192LegacyClassTransformer();
        var pass = Forge1192LegacyClassTransformer.class.getDeclaredMethod("rewriteLegacyBlockCapabilityQueries", ClassNode.class);
        pass.setAccessible(true);
        for (boolean sided : new boolean[]{false, true}) {
            String desc = "(" + CAP + (sided ? Type.getDescriptor(Direction.class) : "") + ")" + OPTIONAL;
            for (String owner : new String[]{BE, PROVIDER, "fixture/Unrelated"}) {
                for (int opcode : new int[]{Opcodes.INVOKEVIRTUAL, Opcodes.INVOKESPECIAL}) {
                    var node = node("fixture/Queries");
                    var method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "query", "(L" + owner + ";" + desc.substring(1), null, null);
                    method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    if (sided) method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
                    var call = new MethodInsnNode(opcode, owner, "getCapability", desc, owner.equals(PROVIDER));
                    method.instructions.add(call); method.instructions.add(new InsnNode(Opcodes.ARETURN));
                    method.maxLocals = sided ? 3 : 2; method.maxStack = 3; node.methods.add(method);
                    boolean expected = !owner.equals("fixture/Unrelated") && (opcode != Opcodes.INVOKESPECIAL || owner.equals(BE));
                    check((int)pass.invoke(transformer, node) == (expected ? 1 : 0), "capability owner/super selection");
                    new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
                    if (expected) check(call.name.equals(opcode == Opcodes.INVOKESPECIAL
                            ? sided ? "baseGetCapability" : "sourceDefaultGetCapability" : "getCapability"), "super dispatch");
                }
            }
        }
        // A captured concrete subtype for an interface method requires an exactly typed bridge thunk.
        String concrete = Type.getInternalName(Provider.class);
        String desc = "(" + CAP + ")" + OPTIONAL;
        var node = node("fixture/CapabilityReference");
        String bind = "(L" + concrete + ";)Ljava/util/function/Function;";
        var method = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "bind", bind, null, null);
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        var bootstrap = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false);
        method.instructions.add(new InvokeDynamicInsnNode("apply", bind, bootstrap,
                Type.getMethodType("(Ljava/lang/Object;)Ljava/lang/Object;"),
                new Handle(Opcodes.H_INVOKEINTERFACE, PROVIDER, "getCapability", desc, true), Type.getMethodType(desc)));
        method.instructions.add(new InsnNode(Opcodes.ARETURN)); method.maxLocals = 1; method.maxStack = 1;
        node.methods.add(method);
        check((int)pass.invoke(transformer, node) == 1, "interface handle missed");
        var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); node.accept(writer); byte[] bytes = writer.toByteArray();
        class Loader extends ClassLoader {
            Loader() { super(CapabilityNetworkFamilyCheck.class.getClassLoader()); }
            Class<?> define() { return defineClass(node.name.replace('/', '.'), bytes, 0, bytes.length); }
        }
        var generated = new Loader().define();
        var receiver = new Provider();
        @SuppressWarnings("unchecked") var function = (java.util.function.Function<LegacyCapability<?>, Object>) generated.getMethod("bind", Provider.class).invoke(null, receiver);
        check(function.apply(LegacyForgeCapabilities.ITEM_HANDLER) == receiver.value, "actual captured interface lambda dispatch");
    }
    private static void itemTransfers() {
        // This isolated transfer fixture declares only a 64-item storage contract;
        // it does not claim that a world/datapack component reload was exercised.
        Items.STONE.builtInRegistryHolder().bindComponents(net.minecraft.core.component.DataComponentMap.builder()
                .set(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE, 64).build());
        var target = new ItemStacksResourceHandler(1);
        var valid = new AtomicBoolean(true);
        var adapter = new Target263ItemHandlerAdapter(target, valid::get);
        var input = new ItemStack(Items.STONE, 70);
        check(adapter.getSlots() == 1 && adapter.isItemValid(0, input), "item handler queries");
        check(adapter.getSlotLimit(0) == target.getCapacityAsLong(0, net.neoforged.neoforge.transfer.item.ItemResource.EMPTY), "general capacity");
        check(adapter.insertItem(0, input, true).getCount() == 6 && adapter.getStackInSlot(0).isEmpty(), "simulated insert rollback");
        check(adapter.insertItem(0, input, false).getCount() == 6 && adapter.getStackInSlot(0).getCount() == 64 && input.getCount() == 70, "committed insert/remainder/input integrity");
        check(adapter.extractItem(0, 7, true).getCount() == 7 && adapter.getStackInSlot(0).getCount() == 64, "simulated extract rollback");
        check(adapter.extractItem(0, 7, false).getCount() == 7 && adapter.getStackInSlot(0).getCount() == 57, "committed extract");
        adapter.getStackInSlot(0).setCount(1);
        check(adapter.getStackInSlot(0).getCount() == 57, "read aliases target stack");
        check(adapter.extractItem(0, 100, false).getCount() == 57 && adapter.getStackInSlot(0).isEmpty(), "actual extraction count");
        valid.set(false); fails(adapter::getSlots); fails(() -> adapter.insertItem(0, input, false));
    }
    private static void network() throws Exception {
        IEventBus bus = (IEventBus)Proxy.newProxyInstance(IEventBus.class.getClassLoader(), new Class[]{IEventBus.class},
                (p,m,a) -> { throw new AssertionError("Unexpected bus call " + m); });
        IModInfo info = (IModInfo)Proxy.newProxyInstance(IModInfo.class.getClassLoader(), new Class[]{IModInfo.class},
                (p,m,a) -> { if (m.getName().equals("getModId")) return "network_fixture"; throw new AssertionError(m); });
        var host = new ModContainer(info) { @Override public IEventBus getEventBus() { return bus; } };
        var name = Identifier.fromNamespaceAndPath("network_fixture", "channel");
        Forge1192NetworkBridge.LegacyChannel channel;
        try (var ignored = Forge1192LifecycleBridge.enter("network_fixture", bus, host)) {
            channel = (Forge1192NetworkBridge.LegacyChannel)Forge1192NetworkBridge.newSimpleChannel(name, () -> "1", s -> true, s -> true);
            fails(() -> Forge1192NetworkBridge.newSimpleChannel(name, () -> "1", s -> true, s -> true));
            Forge1192NetworkBridge.registerMessage(channel, 0, String.class, (a,b) -> {}, b -> "", (a,b) -> {
                check(Forge1192LifecycleBridge.requireActive().modId().equals("network_fixture"), "packet handler source context");
                Forge1192NetworkBridge.enqueueWork(((java.util.function.Supplier<?>)b).get(),
                        () -> check(Forge1192LifecycleBridge.requireActive().modId().equals("network_fixture"), "deferred source context"));
            });
            var prior = Forge1192LifecycleBridge.requireActive();
            Forge1192NetworkBridge.runInChannelScope(channel, () -> check(Forge1192LifecycleBridge.requireActive() == prior, "nested scope replaced context"));
            check(Forge1192LifecycleBridge.requireActive() == prior, "nested scope not restored");
        }
        check(Forge1192NetworkBridge.requireChannel(channel) == channel, "gameplay channel requires init scope");
        var forged = new Forge1192NetworkBridge.LegacyChannel(channel.modId(), channel.name(), channel.protocolVersion(),
                channel.clientAcceptedVersions(), channel.serverAcceptedVersions(), channel.payloadType());
        check(forged.equals(channel), "test requires equal but fabricated token");
        fails(() -> Forge1192NetworkBridge.requireChannel(forged));
        fails(() -> Forge1192NetworkBridge.registerMessage(channel, 1, String.class, (a,b) -> {}, b -> "", (a,b) -> {}));
        var queued = new java.util.ArrayList<Runnable>();
        var targetContext = (net.neoforged.neoforge.network.handling.IPayloadContext)Proxy.newProxyInstance(
                CapabilityNetworkFamilyCheck.class.getClassLoader(),
                new Class[]{net.neoforged.neoforge.network.handling.IPayloadContext.class}, (p,m,args) -> {
                    if (m.getName().equals("flow")) return net.minecraft.network.protocol.PacketFlow.CLIENTBOUND;
                    if (m.getName().equals("enqueueWork")) {
                        queued.add((Runnable)args[0]); return java.util.concurrent.CompletableFuture.completedFuture(null);
                    }
                    throw new AssertionError("Unexpected target packet context call " + m);
                });
        var handler = Forge1192NetworkBridge.class.getDeclaredMethod("handlePayload", Forge1192NetworkBridge.LegacyPayload.class,
                net.neoforged.neoforge.network.handling.IPayloadContext.class);
        handler.setAccessible(true);
        handler.invoke(null, new Forge1192NetworkBridge.LegacyPayload(channel, Forge1192NetworkBridge.messages(channel).getFirst(), "fixture"), targetContext);
        fails(Forge1192LifecycleBridge::requireActive);
        check(queued.size() == 1, "actual payload handler did not enqueue work");
        Thread worker = new Thread(queued.getFirst());
        var failure = new java.util.concurrent.atomic.AtomicReference<Throwable>();
        worker.setUncaughtExceptionHandler((t,e) -> failure.set(e)); worker.start(); worker.join();
        if (failure.get() != null) throw new AssertionError("deferred context", failure.get());
        fails(Forge1192LifecycleBridge::requireActive);
        fails(() -> Forge1192NetworkBridge.runInChannelScope(channel, () -> { throw new IllegalStateException("fixture"); }));
        fails(Forge1192LifecycleBridge::requireActive);
        try (var ignored = Forge1192LifecycleBridge.enter("unrelated_fixture", bus, host)) {
            var prior = Forge1192LifecycleBridge.requireActive();
            fails(() -> Forge1192NetworkBridge.runInChannelScope(channel, () -> { throw new AssertionError("wrong owner ran"); }));
            check(Forge1192LifecycleBridge.requireActive() == prior, "unrelated context overwritten");
        }
    }
    private static ClassNode node(String name) {
        var node = new ClassNode(); node.name = name; node.version = Opcodes.V17;
        node.access = Opcodes.ACC_PUBLIC; node.superName = "java/lang/Object"; return node;
    }
    private static void fails(Runnable action) {
        try { action.run(); throw new AssertionError("Expected explicit failure"); }
        catch (IllegalStateException expected) {}
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
