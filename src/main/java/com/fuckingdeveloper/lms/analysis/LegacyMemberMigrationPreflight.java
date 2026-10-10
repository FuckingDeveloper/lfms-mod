package com.fuckingdeveloper.lms.analysis;

import org.objectweb.asm.*;
import java.io.InputStream;
import java.util.*;

/**
 * Non-executing classifier for member-level migration boundaries.
 * It checks exact owner/name/descriptor against target bytecode resources.
 * Absence is evidence for migration; it never guesses a replacement member.
 */
public final class LegacyMemberMigrationPreflight {
    public enum State { EXACT_TARGET, OWNER_MISSING, MEMBER_MISSING, DESCRIPTOR_CHANGED, LEGACY_FORGE_API }
    public record Finding(LegacyMemberPreflight.Boundary boundary, State state, List<String> sameNameDescriptors) {}
    public record Report(List<Finding> findings, Map<State,Integer> states) {}

    public Report classify(LegacyMemberPreflight.Report members, ClassLoader targetLoader) {
        List<Finding> out = new ArrayList<>();
        for (var b : members.boundaries()) out.add(classify(b, targetLoader));
        Map<State,Integer> states = new EnumMap<>(State.class);
        for (var f : out) states.merge(f.state(), 1, Integer::sum);
        return new Report(List.copyOf(out), Map.copyOf(states));
    }

    private Finding classify(LegacyMemberPreflight.Boundary b, ClassLoader loader) {
        if (b.owner().startsWith("net/minecraftforge/"))
            return new Finding(b, State.LEGACY_FORGE_API, List.of());
        String resource = b.owner() + ".class";
        InputStream raw = loader.getResourceAsStream(resource);
        if (raw == null) raw = LegacyMemberMigrationPreflight.class.getClassLoader().getResourceAsStream(resource);
        if (raw == null) return new Finding(b, State.OWNER_MISSING, List.of());
        try (InputStream in = raw) {
            List<String> sameName = new ArrayList<>();
            boolean[] exact = {false};
            new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] ex) {
                    if (b.kind() == LegacyMemberPreflight.Kind.METHOD && name.equals(b.name())) {
                        sameName.add(desc); if (desc.equals(b.descriptor())) exact[0] = true;
                    }
                    return null;
                }
                @Override public FieldVisitor visitField(int access, String name, String desc, String sig, Object value) {
                    if (b.kind() == LegacyMemberPreflight.Kind.FIELD && name.equals(b.name())) {
                        sameName.add(desc); if (desc.equals(b.descriptor())) exact[0] = true;
                    }
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            if (exact[0]) return new Finding(b, State.EXACT_TARGET, List.copyOf(sameName));
            return new Finding(b, sameName.isEmpty() ? State.MEMBER_MISSING : State.DESCRIPTOR_CHANGED,
                    List.copyOf(sameName));
        } catch (Exception e) {
            return new Finding(b, State.MEMBER_MISSING, List.of());
        }
    }
}
