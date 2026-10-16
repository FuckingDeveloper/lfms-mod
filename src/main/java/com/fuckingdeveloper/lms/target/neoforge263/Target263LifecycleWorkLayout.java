package com.fuckingdeveloper.lms.target.neoforge263;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;

/** Exact installed native binding; source declaration identity stays profile-owned. */
public final class Target263LifecycleWorkLayout {
    private Target263LifecycleWorkLayout() {}
    public static Handle bridge(String descriptor) {
        if (!descriptor.equals("(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;")
                && !descriptor.equals("(Ljava/util/function/Supplier;)Ljava/util/concurrent/CompletableFuture;"))
            throw new IllegalArgumentException("Unsupported lifecycle work shape: " + descriptor);
        return new Handle(Opcodes.H_INVOKESTATIC,
                "com/fuckingdeveloper/lms/target/neoforge263/Target263Forge1192LifecycleWorkBridge", "enqueueWork",
                "(Lnet/neoforged/fml/event/lifecycle/ParallelDispatchEvent;" + descriptor.substring(1), false);
    }
}
