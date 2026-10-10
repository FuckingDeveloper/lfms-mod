package com.fuckingdeveloper.lms.analysis;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarFile;

/**
 * Static, execution-free inventory of Forge 1.19.2 SimpleChannel send sites.
 * This records only explicit direction evidence. It deliberately does not try
 * to infer a registered discriminator from arbitrary object data flow.
 */
public final class Forge1192NetworkFlowAnalyzer {
    public enum Direction { CLIENTBOUND, SERVERBOUND, UNKNOWN }

    public record SendSite(String callerClass, String callerMethod, String channelOwner,
                           String operation, String descriptor, Direction direction,
                           String messageTypeHint) {}

    public List<SendSite> analyze(Path artifact) throws IOException {
        List<SendSite> sites = new ArrayList<>();
        try (JarFile jar = new JarFile(artifact.toFile(), false)) {
            var entries = jar.stream().filter(e -> !e.isDirectory() && e.getName().endsWith(".class"))
                    .sorted(Comparator.comparing(java.util.jar.JarEntry::getName)).toList();
            for (var entry : entries) {
                try (var in = jar.getInputStream(entry)) {
                    new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM9) {
                        private String owner;
                        @Override public void visit(int version, int access, String name, String signature,
                                                    String superName, String[] interfaces) { owner = name; }
                        @Override public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                                   String signature, String[] exceptions) {
                            return new MethodVisitor(Opcodes.ASM9) {
                                @Override public void visitMethodInsn(int opcode, String targetOwner, String targetName,
                                                                     String targetDesc, boolean isInterface) {
                                    if (!targetOwner.equals("net/minecraftforge/network/simple/SimpleChannel")) return;
                                    Direction direction = switch (targetName) {
                                        // SimpleChannel#send(PacketDistributor.PacketTarget, Object)
                                        // is not intrinsically clientbound: direction is encoded by
                                        // the PacketTarget producer. Keep it unresolved until the
                                        // receiver data flow is proven.
                                        case "sendToServer" -> Direction.SERVERBOUND;
                                        default -> Direction.UNKNOWN;
                                    };
                                    Type[] args = Type.getArgumentTypes(targetDesc);
                                    String hint = args.length == 0 ? "" : args[args.length - 1].getClassName();
                                    sites.add(new SendSite(owner.replace('/', '.'), name + descriptor,
                                            targetOwner.replace('/', '.'), targetName, targetDesc, direction, hint));
                                }
                            };
                        }
                    }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                }
            }
        }
        sites.sort(Comparator.comparing(SendSite::callerClass)
                .thenComparing(SendSite::callerMethod).thenComparing(SendSite::operation));
        return List.copyOf(sites);
    }
}
