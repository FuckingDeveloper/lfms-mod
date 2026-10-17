package com.fuckingdeveloper.lms.target.neoforge263;

import com.fuckingdeveloper.lms.profile.forge1192.runtime.lifecycle.Forge1192LifecycleBridge;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.neoforged.fml.event.lifecycle.ParallelDispatchEvent;

/** Preserve native work queue/future semantics while restoring recorded source callback ownership. */
public final class Target263Forge1192LifecycleWorkBridge {
    private Target263Forge1192LifecycleWorkBridge() {}
    public static CompletableFuture<Void> enqueueWork(ParallelDispatchEvent event, Runnable work) {
        String owner = Forge1192LifecycleBridge.requireActive().modId(); Objects.requireNonNull(work);
        return event.enqueueWork(() -> scoped(owner, event, () -> { work.run(); return null; }));
    }
    public static <T> CompletableFuture<T> enqueueWork(ParallelDispatchEvent event, Supplier<T> work) {
        String owner = Forge1192LifecycleBridge.requireActive().modId(); Objects.requireNonNull(work);
        return event.enqueueWork(() -> scoped(owner, event, work));
    }
    private static <T> T scoped(String owner, ParallelDispatchEvent event, Supplier<T> work) {
        try (var scope = Forge1192LifecycleBridge.enterCallback(owner);
             var phase = Target263ClientSetupScope.enter(event)) { return work.get(); }
        catch (RuntimeException | Error failure) { throw failure; }
        catch (Exception failure) { throw new IllegalStateException("Source deferred-work scope failed: " + owner, failure); }
    }
}
