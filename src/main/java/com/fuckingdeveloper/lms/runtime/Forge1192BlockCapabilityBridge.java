package com.fuckingdeveloper.lms.runtime;

import com.fuckingdeveloper.lms.runtime.capability.*;
import com.fuckingdeveloper.lms.runtime.itemhandler.Target263ItemHandlerAdapter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;

/** Source provider dispatch plus the target's sided block services and invalidation. */
public final class Forge1192BlockCapabilityBridge {
    private Forge1192BlockCapabilityBridge() {}

    private static ClassValue<Optional<MethodHandle>> sourceQuery(boolean sided) { return new ClassValue<>() {
        @Override protected Optional<MethodHandle> computeValue(Class<?> type) {
            try {
                var method = sided ? type.getMethod("getCapability", LegacyCapability.class, Direction.class)
                        : type.getMethod("getCapability", LegacyCapability.class);
                if (method.getReturnType() != LegacyLazyOptional.class)
                    throw new IllegalStateException("Invalid source capability return contract: " + type.getName());
                return Optional.of(MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup()).unreflect(method));
            } catch (NoSuchMethodException absent) { return Optional.empty(); }
            catch (IllegalAccessException failure) { throw new IllegalStateException("Cannot access source capability provider", failure); }
        }
    }; }
    private static final ClassValue<Optional<MethodHandle>> SOURCE_QUERY = sourceQuery(true);
    private static final ClassValue<Optional<MethodHandle>> SOURCE_QUERY_ONE = sourceQuery(false);

    @SuppressWarnings("unchecked")
    public static <T> LegacyLazyOptional<T> getCapability(Object receiver, LegacyCapability<T> capability) {
        Objects.requireNonNull(receiver); Objects.requireNonNull(capability);
        if (!(receiver instanceof BlockEntity) && !(receiver instanceof LegacyICapabilityProvider))
            throw new IllegalStateException("Unadapted capability receiver " + receiver.getClass().getName());
        var source = SOURCE_QUERY_ONE.get(receiver.getClass());
        if (source.isPresent()) {
            try { return Objects.requireNonNull((LegacyLazyOptional<T>) source.get().invoke(receiver, capability)); }
            catch (RuntimeException | Error failure) { throw failure; }
            catch (Throwable failure) { throw new IllegalStateException("Source unsided capability query failed", failure); }
        }
        return getCapability(receiver, capability, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> LegacyLazyOptional<T> getCapability(Object receiver, LegacyCapability<T> capability, Direction side) {
        Objects.requireNonNull(receiver); Objects.requireNonNull(capability);
        if (!(receiver instanceof BlockEntity) && !(receiver instanceof LegacyICapabilityProvider))
            throw new IllegalStateException("Unadapted capability receiver " + receiver.getClass().getName());
        var source = SOURCE_QUERY.get(receiver.getClass());
        if (source.isPresent()) {
            try {
                return Objects.requireNonNull((LegacyLazyOptional<T>) source.get().invoke(receiver, capability, side),
                        "Source capability provider returned null");
            } catch (RuntimeException | Error failure) { throw failure; }
            catch (Throwable failure) { throw new IllegalStateException("Source capability query failed", failure); }
        }
        if (receiver instanceof BlockEntity blockEntity) return baseGetCapability(blockEntity, capability, side);
        throw new IllegalStateException("Missing source provider query contract: " + receiver.getClass().getName());
    }

    /** The source base unsided operation dispatches the sided override, not an unsided override. */
    public static <T> LegacyLazyOptional<T> sourceDefaultGetCapability(Object receiver, LegacyCapability<T> capability) {
        return getCapability(receiver, capability, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> LegacyLazyOptional<T> baseGetCapability(BlockEntity blockEntity, LegacyCapability<T> capability, Direction side) {
        Objects.requireNonNull(blockEntity); Objects.requireNonNull(capability);
        if (!(blockEntity instanceof LegacyBlockCapabilityCarrier carrier))
            throw new IllegalStateException("Source block capability lifecycle hook is missing");
        if (!carrier.lms$sourceCapabilities().isValid() || !carrier.lms$sourceCapabilities().isInitialized()) return LegacyLazyOptional.empty();
        var attached = carrier.lms$sourceCapabilities().getCapability(capability, side);
        if (attached.isPresent()) return attached;
        if (capability != LegacyForgeCapabilities.ITEM_HANDLER && capability != LegacyForgeCapabilities.ENERGY
                && capability != LegacyForgeCapabilities.FLUID_HANDLER) return attached;
        if (!(blockEntity.getLevel() instanceof ServerLevel level))
            throw new IllegalStateException("Native block capability migration requires an attached server level");
        if (blockEntity.isRemoved()) return LegacyLazyOptional.empty();
        if (level.getBlockEntity(blockEntity.getBlockPos()) != blockEntity)
            throw new IllegalStateException("Native capability query references a replaced block entity");
        if (capability == LegacyForgeCapabilities.ITEM_HANDLER)
            return (LegacyLazyOptional<T>) nativeView(Capabilities.Item.BLOCK, level, blockEntity, side,
                    Target263ItemHandlerAdapter::new);
        if (capability == LegacyForgeCapabilities.ENERGY)
            return (LegacyLazyOptional<T>) nativeView(Capabilities.Energy.BLOCK, level, blockEntity, side,
                    com.fuckingdeveloper.lms.runtime.energy.Target263EnergyHandlerAdapter::new);
        if (capability == LegacyForgeCapabilities.FLUID_HANDLER)
            return (LegacyLazyOptional<T>) nativeView(Capabilities.Fluid.BLOCK, level, blockEntity, side,
                    (handler, valid) -> { throw new IllegalStateException("Native source fluid handler operations are not adapted"); });
        throw new IllegalStateException("Source attached/custom block capability is not adapted: " + capability.getName());
    }

    private static <N, T> LegacyLazyOptional<T> nativeView(BlockCapability<N, Direction> capability,
            ServerLevel level, BlockEntity blockEntity, Direction side, BiFunction<N, BooleanSupplier, T> adapter) {
        var view = new AtomicReference<LegacyLazyOptional<T>>();
        var cache = BlockCapabilityCache.create(capability, level, blockEntity.getBlockPos(), side,
                () -> view.get() == null || view.get().isPresent(),
                () -> { var current = view.get(); if (current != null) current.invalidate(); });
        N handler = cache.getCapability();
        if (handler == null) return LegacyLazyOptional.empty();
        var optional = LegacyLazyOptional.of(() -> adapter.apply(
                Objects.requireNonNull(cache.getCapability(), "Target provider disappeared without invalidation"),
                () -> view.get().isPresent()));
        view.set(optional);
        return optional;
    }
}
