package com.fuckingdeveloper.lms.check;

import com.fuckingdeveloper.lms.runtime.*;
import com.fuckingdeveloper.lms.runtime.capability.*;
import com.fuckingdeveloper.lms.runtime.itemhandler.LegacyMutableTagCarrier;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.item.ItemResource;

/** Target fixtures validate applied mixins, without original legacy mod initialization or gameplay. */
public final class LiveTargetContractProbe {
    static final class ProbeChest extends ChestBlockEntity {
        int initialized = 42;
        ProbeChest() { super(BlockPos.ZERO, Blocks.CHEST.defaultBlockState()); }
    }
    static final class ProbeProvider implements LegacyICapabilityProvider, LegacyINBTSerializable<net.minecraft.nbt.CompoundTag> {
        final LegacyCapability<String> cap; final LegacyLazyOptional<String> value; int stored = 7;
        ProbeProvider(LegacyCapability<String> cap, ProbeChest receiver) { this.cap = cap; value = LegacyLazyOptional.of(() -> "initialized=" + receiver.initialized); }
        public <T> LegacyLazyOptional<T> getCapability(LegacyCapability<T> token, Direction side) { return token == cap ? value.cast() : LegacyLazyOptional.empty(); }
        public net.minecraft.nbt.CompoundTag serializeNBT() { var data = new net.minecraft.nbt.CompoundTag(); data.putInt("stored", stored); return data; }
        public void deserializeNBT(net.minecraft.nbt.CompoundTag data) { stored = data.getIntOr("stored", -1); }
    }
    public static void main(String[] args) throws Exception {
        net.minecraft.SharedConstants.tryDetectVersion(); net.minecraft.server.Bootstrap.bootStrap();
        var bus = BusBuilder.builder().build();
        var info = (IModInfo)Proxy.newProxyInstance(IModInfo.class.getClassLoader(), new Class[]{IModInfo.class}, (p,m,a) -> {
            if (m.getName().equals("getModId")) return "native_smoke"; throw new AssertionError(m);
        });
        var host = new ModContainer(info) { @Override public IEventBus getEventBus() { return bus; } };
        var calls = new AtomicInteger(); var invalidations = new AtomicInteger();
        var cap = LegacyCapabilityManager.get(new LegacyCapabilityToken<String>() {});
        ProbeProvider[] provider = new ProbeProvider[1];
        NeoForge.EVENT_BUS.start();
        try (var scope = Forge1192LifecycleBridge.enter("native_smoke", bus, host)) {
            Forge1192EventBusBridge.addGenericListener(NeoForge.EVENT_BUS, BlockEntity.class,
                    (java.util.function.Consumer<com.fuckingdeveloper.lms.runtime.event.LegacyAttachCapabilitiesEvent<BlockEntity>>)event -> {
                        if (!(event.getObject() instanceof ProbeChest receiver)) return;
                        check(receiver.initialized == 0, "source gather ran after subclass initialization"); calls.incrementAndGet();
                        provider[0] = new ProbeProvider(cap, receiver);
                        event.addCapability(net.minecraft.resources.Identifier.fromNamespaceAndPath("native_smoke", "probe"), provider[0]);
                        var captured = provider[0];
                        event.addListener(() -> { invalidations.incrementAndGet(); captured.value.invalidate(); });
                    });
        }
        var receiver = new ProbeChest();
        check((Object)receiver instanceof LegacyBlockCapabilityCarrier && calls.get() == 1, "actual base constructor hook/subject filtering");
        var value = Forge1192BlockCapabilityBridge.baseGetCapability(receiver, cap, Direction.NORTH);
        check(value.orElse("absent").equals("initialized=42"), "eager gather resolved provider before source subclass was initialized");
        var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, net.minecraft.core.RegistryAccess.EMPTY);
        receiver.saveCustomOnly(output);
        var saved = output.buildResult();
        check(saved.getCompoundOrEmpty("ForgeCaps").getCompoundOrEmpty("native_smoke:probe").getIntOr("stored", -1) == 7, "actual source attachment save hook");
        provider[0].stored = 0;
        receiver.loadCustomOnly(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, net.minecraft.core.RegistryAccess.EMPTY, saved));
        check(provider[0].stored == 7, "actual source attachment load hook");
        receiver.onChunkUnloaded();
        check(!value.isPresent() && invalidations.get() == 1, "source chunk unload hook");
        receiver.getClass().getMethod("reviveCaps").invoke(receiver);
        receiver.setRemoved(); check(invalidations.get() == 2, "source removal hook");

        // Declared isolated storage components; this is not a datapack/default-component reload test.
        Items.STONE.builtInRegistryHolder().bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 64).build());
        var stack = new ItemStack(Items.STONE); check((Object)stack instanceof LegacyMutableTagCarrier, "native stack ownership interface missing");
        var tag = Forge1192ItemBridge.getOrCreateTag(stack); tag.putInt("charge", 40);
        var copied = stack.copy(); var resource = ItemResource.of(stack);
        tag.putInt("charge", 30);
        check(Forge1192ItemBridge.getTag(stack) == tag, "native source live tag identity");
        check(copied.get(DataComponents.CUSTOM_DATA).tag.getIntOr("charge", -1) == 40, "applied native copy hook aliases source mutable tag");
        check(!ItemStack.matches(stack, copied), "applied inventory snapshot does not detect changed charge");
        check(resource.toStack(1).get(DataComponents.CUSTOM_DATA).tag.getIntOr("charge", -1) == 40, "applied resource snapshot hook aliases source mutable tag");

        // Force actual FML/mixin transformation of all three queue entry classes.
        for (String name : List.of("MultiNeighborUpdate", "SimpleNeighborUpdate", "FullNeighborUpdate"))
            Class.forName("net.minecraft.world.level.redstone.CollectingNeighborUpdater$" + name);
        Class.forName("net.minecraft.world.level.redstone.InstantNeighborUpdater");
        var origin = new Forge1192NeighborBridge.Origin(null, new BlockPos(9, 8, 7));
        Class<?> simple = Class.forName("net.minecraft.world.level.redstone.CollectingNeighborUpdater$SimpleNeighborUpdate");
        var constructor = simple.getDeclaredConstructor(BlockPos.class, net.minecraft.world.level.block.Block.class, Orientation.class); constructor.setAccessible(true);
        Object[] record = new Object[1];
        Forge1192NeighborBridge.notify(origin, null, () -> {
            try { record[0] = constructor.newInstance(BlockPos.ZERO, Blocks.STONE, null); }
            catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
        });
        var field = Arrays.stream(simple.getDeclaredFields()).filter(f -> f.getType() == Forge1192NeighborBridge.Origin.class).findFirst().orElseThrow(); field.setAccessible(true);
        check(field.get(record[0]) == origin && Forge1192NeighborBridge.capture() == null, "applied queue constructor did not retain origin past source scope");
        var operation = Arrays.stream(simple.getDeclaredMethods()).filter(method ->
                method.getParameterCount() == 7 && method.getParameterTypes()[6] == com.llamalad7.mixinextras.injector.wrapoperation.Operation.class
                        && method.getReturnType() == void.class).findFirst().orElseThrow();
        operation.setAccessible(true);
        var observed = new AtomicInteger();
        com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> checkedOperation = arguments -> {
            check(Forge1192NeighborBridge.requirePosition(null).equals(origin.position()), "actual injected dispatch lost queued origin");
            check(Forge1192NeighborBridge.captureNotification() == null, "dispatch origin incorrectly became a nested native notification origin");
            observed.incrementAndGet(); return null;
        };
        operation.invoke(record[0], null, Blocks.STONE.defaultBlockState(), BlockPos.ZERO, Blocks.STONE, null, false, checkedOperation);
        check(observed.get() == 1 && Forge1192NeighborBridge.capture() == null, "actual injected operation/context restoration");
        Object[] nativeOnly = new Object[1];
        Forge1192NeighborBridge.dispatch(origin, null, () -> {
            try { nativeOnly[0] = constructor.newInstance(BlockPos.ZERO, Blocks.STONE, null); }
            catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
        });
        check(field.get(nativeOnly[0]) == null, "native notification guessed the active callback's source origin");
        System.out.println("PASS: real FML/mixins, pre-subclass gather/lazy provider/removal/unload/NBT, live stack/copy/resource snapshot ownership, queue/instant transformation, retained origin and actual injected dispatch; no world/window/legacy entrypoint initialization");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
