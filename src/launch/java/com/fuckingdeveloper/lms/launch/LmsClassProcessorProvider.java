package com.fuckingdeveloper.lms.launch;

import net.neoforged.neoforgespi.transformation.ClassProcessorProvider;
import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleClassProcessor;
import net.neoforged.neoforgespi.transformation.SimpleTransformationContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Set;

/**
 * Early FML 12 replacement for IC2 Classic coremod transform-6.
 *
 * The old Forge coremod replaced Player#getStepHeight() with an IC2 hook.
 * On the current runtime the equivalent call site is Player#maxUpStep().
 * For now LMS routes that call through an early-library bridge which preserves
 * vanilla behaviour; a legacy delegate can be wired in later.
 */
public final class LmsClassProcessorProvider implements ClassProcessorProvider {
    @Override
    public void createProcessors(Context context, Collector collector) {
        System.out.println("[LMS/early] FML class processor provider loaded");
        collector.add(new PlayerTransform6());
    }

    private static final class PlayerTransform6 extends SimpleClassProcessor {
        private static final String PLAYER = "net/minecraft/world/entity/player/Player";
        private static final String METHOD = "maybeBackOffFromEdge";
        private static final String DESC =
                "(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/MoverType;)" +
                "Lnet/minecraft/world/phys/Vec3;";
        private static final String BRIDGE = "com/fuckingdeveloper/lms/launch/StepHeightEarlyBridge";

        @Override
        public ProcessorName name() {
            return new ProcessorName("lms", "ic2_transform_6");
        }

        @Override
        public void transform(ClassNode input, SimpleTransformationContext context) {
            int replacements = 0;
            boolean found = false;
            for (var method : input.methods) {
                if (!METHOD.equals(method.name) || !DESC.equals(method.desc)) continue;
                found = true;
                for (var insn = method.instructions.getFirst(); insn != null; ) {
                    var next = insn.getNext();
                    if (insn instanceof MethodInsnNode call
                            && call.getOpcode() == Opcodes.INVOKEVIRTUAL
                            && PLAYER.equals(call.owner)
                            && "maxUpStep".equals(call.name)
                            && "()F".equals(call.desc)) {
                        method.instructions.set(call, new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                BRIDGE,
                                "getStepHeight",
                                "(Lnet/minecraft/world/entity/player/Player;)F",
                                false));
                        replacements++;
                    }
                    insn = next;
                }
            }
            System.out.println("[LMS/early] transform-6 Player targetFound=" + found
                    + " replacements=" + replacements + " mode=ACTIVE");
        }

        @Override
        public Set<Target> targets() {
            return Set.of(new Target("net.minecraft.world.entity.player.Player"));
        }
    }
}
