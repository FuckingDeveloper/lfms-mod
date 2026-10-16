package com.fuckingdeveloper.lms.profile.forge1192.runtime.block;

import java.lang.invoke.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Preserve source origin on the target execution/queue lifetime. */
public final class Forge1192NeighborBridge {
    private Forge1192NeighborBridge() {}
    public record Origin(Level level, BlockPos position) {}
    private static final ThreadLocal<Origin> CURRENT = new ThreadLocal<>();
    private static final ThreadLocal<Origin> NOTIFICATION = new ThreadLocal<>();
    private static final ClassValue<Optional<MethodHandle>> SOURCE_CALLBACK = new ClassValue<>() {
        @Override protected Optional<MethodHandle> computeValue(Class<?> type) {
            try {
                var method = type.getMethod("m_6861_", BlockState.class, Level.class, BlockPos.class, Block.class, BlockPos.class, boolean.class);
                return Optional.of(MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup()).unreflect(method));
            } catch (NoSuchMethodException absent) { return Optional.empty(); }
            catch (IllegalAccessException failure) { throw new IllegalStateException("Cannot access source neighbor callback", failure); }
        }
    };
    public static Origin capture() { return CURRENT.get(); }
    public static Origin captureNotification() { return NOTIFICATION.get(); }
    public static void notify(Origin origin, Level level, Runnable work) {
        Origin previous = NOTIFICATION.get(); NOTIFICATION.set(origin);
        try { run(origin, level, work); }
        finally { if (previous == null) NOTIFICATION.remove(); else NOTIFICATION.set(previous); }
    }
    public static void dispatch(Origin origin, Level level, Runnable work) {
        Origin previous = NOTIFICATION.get(); NOTIFICATION.remove();
        try { run(origin, level, work); }
        finally { if (previous == null) NOTIFICATION.remove(); else NOTIFICATION.set(previous); }
    }
    public static void run(Origin origin, Level level, Runnable work) {
        if (origin != null && origin.level() != level) throw new IllegalStateException("Neighbor origin crossed levels");
        Origin previous = CURRENT.get();
        if (origin == null) CURRENT.remove(); else CURRENT.set(origin);
        try { work.run(); }
        finally { if (previous == null) CURRENT.remove(); else CURRENT.set(previous); }
    }
    public static BlockPos requirePosition(Level level) {
        var origin = CURRENT.get();
        if (origin == null || origin.level() != level) throw new IllegalStateException("Source neighbor callback has no authoritative origin");
        return origin.position();
    }
    public static void neighborChanged(Level level, BlockPos pos, Block changed, BlockPos origin) {
        notify(new Origin(level, origin.immutable()), level, () -> level.neighborChanged(pos, changed, null));
    }
    public static void neighborChanged(Level level, BlockState state, BlockPos pos, Block changed, BlockPos origin, boolean moved) {
        notify(new Origin(level, origin.immutable()), level, () -> level.neighborChanged(state, pos, changed, null, moved));
    }
    public static void updateNeighborsAt(Level level, BlockPos pos, Block block) {
        notify(new Origin(level, pos.immutable()), level, () -> level.updateNeighborsAt(pos, block, null));
    }
    public static void updateNeighborsAtExceptFromFacing(Level level, BlockPos pos, Block block, net.minecraft.core.Direction skip) {
        notify(new Origin(level, pos.immutable()), level, () -> level.updateNeighborsAtExceptFromFacing(pos, block, skip, null));
    }
    public static void callback(Block receiver, BlockState state, Level level, BlockPos pos, Block changed, BlockPos origin, boolean moved) {
        var source = SOURCE_CALLBACK.get(receiver.getClass());
        if (source.isPresent()) {
            dispatch(new Origin(level, origin.immutable()), level, () -> {
                try { source.get().invoke(receiver, state, level, pos, changed, origin, moved); }
                catch (RuntimeException | Error failure) { throw failure; }
                catch (Throwable failure) { throw new IllegalStateException("Source neighbor callback failed", failure); }
            });
        } else {
            if (state.getBlock() != receiver) throw new IllegalStateException("Native source neighbor receiver differs from its state");
            dispatch(new Origin(level, origin.immutable()), level, () -> state.handleNeighborChanged(level, pos, changed, null, moved));
        }
    }
    /** Verified source Block base callback is empty; explicit super must not recurse. */
    public static void baseCallback(Block receiver, BlockState state, Level level, BlockPos pos, Block changed, BlockPos origin, boolean moved) {}
}
