package com.fuckingdeveloper.lms.analysis;

import org.objectweb.asm.*;
import com.fuckingdeveloper.lms.profile.Forge1192LegacyClassTransformer;
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
        String projectedOwner = Forge1192LegacyClassTransformer.projectInternalName(b.owner());
        String projectedDescriptor = Forge1192LegacyClassTransformer.projectDescriptor(b.descriptor());
        LegacyMemberPreflight.Boundary projected = new LegacyMemberPreflight.Boundary(
                b.callerClass(), b.callerMethod(), b.kind(), b.opcode(),
                projectedOwner, b.name(), projectedDescriptor, b.interfaceCall());
        if (projectedOwner.startsWith("net/minecraftforge/"))
            return new Finding(projected, State.LEGACY_FORGE_API, List.of());
        String resource = projectedOwner + ".class";
        InputStream raw = open(loader, resource);
        if (raw == null) return new Finding(projected, State.OWNER_MISSING, List.of());
        try (InputStream in = raw) {
            List<String> sameName = new ArrayList<>();
            boolean[] exact = {false};
            List<String> parents = new ArrayList<>();
            new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override public void visit(int version, int access, String name, String signature,
                                            String superName, String[] interfaces) {
                    if (superName != null) parents.add(superName);
                    if (interfaces != null) parents.addAll(Arrays.asList(interfaces));
                }
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
            if (exact[0]) return new Finding(projected, State.EXACT_TARGET, List.copyOf(sameName));
            if (findInherited(loader, parents, projected, sameName, new HashSet<>()))
                return new Finding(b, State.EXACT_TARGET, List.copyOf(sameName));
            return new Finding(projected, sameName.isEmpty() ? State.MEMBER_MISSING : State.DESCRIPTOR_CHANGED,
                    List.copyOf(sameName));
        } catch (Exception e) {
            return new Finding(projected, State.MEMBER_MISSING, List.of());
        }
    }

    private static boolean findInherited(ClassLoader loader, List<String> owners,
                                         LegacyMemberPreflight.Boundary b, List<String> sameName,
                                         Set<String> visited) {
        for (String owner : owners) {
            if (owner == null || !visited.add(owner)) continue;
            try (InputStream in = open(loader, owner + ".class")) {
                if (in == null) continue;
                boolean[] exact = {false};
                List<String> parents = new ArrayList<>();
                new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM9) {
                    @Override public void visit(int version, int access, String name, String signature,
                                                String superName, String[] interfaces) {
                        if (superName != null) parents.add(superName);
                        if (interfaces != null) parents.addAll(Arrays.asList(interfaces));
                    }
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
                if (exact[0] || findInherited(loader, parents, b, sameName, visited)) return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    private static InputStream open(ClassLoader loader, String resource) {
        InputStream in = loader.getResourceAsStream(resource);
        return in != null ? in : LegacyMemberMigrationPreflight.class.getClassLoader().getResourceAsStream(resource);
    }
}
