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
 * Early FML 12 class-processor provider.
 *
 * Diagnostic only: proves LMS can see Player before definition without changing
 * its bytecode. Once this is observed in a real client launch, transform 6 can
 * be routed through the same processor.
 */
public final class LmsClassProcessorProvider implements ClassProcessorProvider {
    @Override
    public void createProcessors(Context context, Collector collector) {
        System.out.println("[LMS/early] FML class processor provider loaded");
        collector.add(new PlayerProbe());
    }

    private static final class PlayerProbe extends SimpleClassProcessor {
        private static final String PLAYER = "net/minecraft/world/entity/player/Player";
        private static final String METHOD = "maybeBackOffFromEdge";
        private static final String DESC =
                "(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/MoverType;)" +
                "Lnet/minecraft/world/phys/Vec3;";

        @Override
        public ProcessorName name() {
            return new ProcessorName("lms", "player_probe");
        }

        @Override
        public void transform(ClassNode input, SimpleTransformationContext context) {
            int matches = 0;
            boolean found = false;
            for (var method : input.methods) {
                if (!METHOD.equals(method.name) || !DESC.equals(method.desc)) continue;
                found = true;
                for (var insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn instanceof MethodInsnNode call
                            && call.getOpcode() == Opcodes.INVOKEVIRTUAL
                            && PLAYER.equals(call.owner)
                            && "maxUpStep".equals(call.name)
                            && "()F".equals(call.desc)) {
                        matches++;
                    }
                }
            }
            System.out.println("[LMS/early] Player intercepted targetFound=" + found
                    + " stepCallsites=" + matches + " mode=DIAGNOSTIC (unchanged)");
        }

        @Override
        public Set<Target> targets() {
            return Set.of(new Target("net.minecraft.world.entity.player.Player"));
        }
    }
}
