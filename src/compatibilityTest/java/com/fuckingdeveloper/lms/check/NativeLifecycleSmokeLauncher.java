package com.fuckingdeveloper.lms.check;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.startup.Entrypoint;

/** Real physical-client FML/mixin startup; never starts Minecraft's server main, a world or a window. */
public final class NativeLifecycleSmokeLauncher extends Entrypoint {
    public static void main(String[] args) throws Throwable {
        try (var result = startup(args, true, Dist.CLIENT, true)) {
            var probe = createMainMethodCallable(result, "com.fuckingdeveloper.lms.check.LiveTargetContractProbe");
            probe.invokeExact(new String[0]);
        }
    }
}
