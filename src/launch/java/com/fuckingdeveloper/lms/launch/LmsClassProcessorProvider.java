package com.fuckingdeveloper.lms.launch;

import net.neoforged.neoforgespi.transformation.ClassProcessorProvider;
import org.objectweb.asm.Opcodes;

/**
 * Early FML entry point for LMS bytecode transformations.
 *
 * The processor implementation is generic. The single spec below is a temporary
 * reference fixture proving that planner data can drive the early engine; it
 * must disappear once compatibility plans are serialized for the launch stage.
 */
public final class LmsClassProcessorProvider implements ClassProcessorProvider {
    @Override
    public void createProcessors(Context context, Collector collector) {
        System.out.println("[LMS/early] FML class processor provider loaded");

        // Temporary reference fixture from the already verified transform.
        // No mod identity is checked and the generic processor contains no
        // knowledge of IC2. Next milestone: load specs produced by the planner.
        collector.add(new GenericMethodCallRedirectProcessor(
                new GenericMethodCallRedirectProcessor.Spec(
                        "reference_method_redirect",
                        "net/minecraft/world/entity/player/Player",
                        "maybeBackOffFromEdge",
                        "(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/MoverType;)"
                                + "Lnet/minecraft/world/phys/Vec3;",
                        new GenericMethodCallRedirectProcessor.MethodRef(
                                Opcodes.INVOKEVIRTUAL,
                                "net/minecraft/world/entity/player/Player",
                                "maxUpStep",
                                "()F",
                                false),
                        new GenericMethodCallRedirectProcessor.MethodRef(
                                Opcodes.INVOKESTATIC,
                                "com/fuckingdeveloper/lms/launch/StepHeightEarlyBridge",
                                "getStepHeight",
                                "(Lnet/minecraft/world/entity/player/Player;)F",
                                false))));
    }
}
