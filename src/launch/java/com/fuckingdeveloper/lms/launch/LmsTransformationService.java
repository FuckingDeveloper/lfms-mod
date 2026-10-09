package com.fuckingdeveloper.lms.launch;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;
import java.util.Set;

/**
 * Early, read-only proof that ModLauncher can reach Player before class definition.
 * This service must be discoverable in ModLauncher's SERVICE layer; a regular mod
 * JAR in the mods directory is not sufficient evidence of service discovery.
 */
public final class LmsTransformationService implements ITransformationService {
    @Override public String name() { return "lms"; }
    @Override public void initialize(IEnvironment environment) { }
    @Override public void onLoad(IEnvironment environment, Set<String> otherServices) {
        System.out.println("[LMS/early] transformation service loaded");
    }
    @Override public List<? extends ITransformer<?>> transformers() {
        return List.of(new PlayerProbe());
    }

    private static final class PlayerProbe implements ITransformer<ClassNode> {
        private static final String PLAYER = "net/minecraft/world/entity/player/Player";
        private static final String METHOD = "maybeBackOffFromEdge";
        private static final String DESC = "(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/MoverType;)Lnet/minecraft/world/phys/Vec3;";

        @Override public ClassNode transform(ClassNode input, ITransformerVotingContext context) {
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
                            && "()F".equals(call.desc)) matches++;
                }
            }
            System.out.println("[LMS/early] Player intercepted targetFound=" + found
                    + " stepCallsites=" + matches + " mode=DIAGNOSTIC (unchanged)");
            return input;
        }
        @Override public TransformerVoteResult castVote(ITransformerVotingContext context) {
            return TransformerVoteResult.YES;
        }
        @Override public Set<Target<ClassNode>> targets() {
            return Set.of(Target.targetClass("net.minecraft.world.entity.player.Player"));
        }
        @Override public String[] labels() { return new String[] {"lms-player-probe"}; }
    }
}
