package com.fuckingdeveloper.lms.runtime;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifies the mechanically plausible Forge -> NeoForge namespace migration
 * against actual target bytecode without loading or initializing target classes.
 */
public final class Forge1192NeoForgeApiVerifier {
    public enum State { EXACT_TARGET, TARGET_CLASS_MISSING, TARGET_METHOD_MISSING }

    public record Verification(String legacyTarget, String targetOwner, String targetDescriptor,
                               State state) {}

    public List<Verification> verify(List<Forge1192RegistrationPlanner.Boundary> boundaries,
                                     ClassLoader targetLoader) {
        return boundaries.stream()
                .map(boundary -> verify(boundary, targetLoader))
                .distinct()
                .sorted(java.util.Comparator.comparing(Verification::legacyTarget))
                .toList();
    }

    private Verification verify(Forge1192RegistrationPlanner.Boundary boundary,
                                ClassLoader targetLoader) {
        String legacyOwner = boundary.owner();
        String targetOwner = migrate(legacyOwner);
        String targetDescriptor = migrate(boundary.descriptor());
        String legacyTarget = legacyOwner + "#" + boundary.name() + boundary.descriptor();
        String resource = targetOwner.replace('.', '/') + ".class";

        try (InputStream in = targetLoader.getResourceAsStream(resource)) {
            if (in == null) {
                return new Verification(legacyTarget, targetOwner, targetDescriptor,
                        State.TARGET_CLASS_MISSING);
            }
            boolean[] found = {false};
            new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                 String signature, String[] exceptions) {
                    if (boundary.name().equals(name) && targetDescriptor.equals(descriptor)) {
                        found[0] = true;
                    }
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return new Verification(legacyTarget, targetOwner, targetDescriptor,
                    found[0] ? State.EXACT_TARGET : State.TARGET_METHOD_MISSING);
        } catch (IOException e) {
            return new Verification(legacyTarget, targetOwner, targetDescriptor,
                    State.TARGET_CLASS_MISSING);
        }
    }

    private static String migrate(String value) {
        return value.replace("net.minecraftforge.", "net.neoforged.")
                .replace("net/minecraftforge/", "net/neoforged/");
    }
}
