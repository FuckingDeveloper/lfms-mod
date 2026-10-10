package com.fuckingdeveloper.lms.launch;

import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleClassProcessor;
import net.neoforged.neoforgespi.transformation.SimpleTransformationContext;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Set;

/**
 * Generic early bytecode primitive: redirect one method invocation inside one
 * target method. It contains no knowledge of any legacy mod.
 *
 * Specs are deliberately plain data so a later planner can populate them from
 * LegacyInjectionAnalyzer/compatibility-plan output instead of Java code.
 */
public final class GenericMethodCallRedirectProcessor extends SimpleClassProcessor {
    public record MethodRef(int opcode, String owner, String name, String descriptor, boolean isInterface) {}
    public record Spec(
            String id,
            String targetClass,
            String targetMethod,
            String targetDescriptor,
            MethodRef anchor,
            MethodRef replacement
    ) {}

    private final Spec spec;

    public GenericMethodCallRedirectProcessor(Spec spec) {
        this.spec = spec;
    }

    @Override
    public ProcessorName name() {
        return new ProcessorName("lms", spec.id());
    }

    @Override
    public void transform(ClassNode input, SimpleTransformationContext context) {
        int replacements = 0;
        boolean targetFound = false;

        for (var method : input.methods) {
            if (!spec.targetMethod().equals(method.name)
                    || !spec.targetDescriptor().equals(method.desc)) {
                continue;
            }
            targetFound = true;

            for (var instruction = method.instructions.getFirst(); instruction != null; ) {
                var next = instruction.getNext();
                if (instruction instanceof MethodInsnNode call && matches(call, spec.anchor())) {
                    MethodRef replacement = spec.replacement();
                    method.instructions.set(call, new MethodInsnNode(
                            replacement.opcode(),
                            replacement.owner(),
                            replacement.name(),
                            replacement.descriptor(),
                            replacement.isInterface()));
                    replacements++;
                }
                instruction = next;
            }
        }

        System.out.println("[LMS/early] transform id=" + spec.id()
                + " target=" + spec.targetClass()
                + "#" + spec.targetMethod() + spec.targetDescriptor()
                + " targetFound=" + targetFound
                + " replacements=" + replacements
                + " engine=GENERIC_METHOD_CALL_REDIRECT");
    }

    private static boolean matches(MethodInsnNode call, MethodRef expected) {
        return call.getOpcode() == expected.opcode()
                && expected.owner().equals(call.owner)
                && expected.name().equals(call.name)
                && expected.descriptor().equals(call.desc)
                && expected.isInterface() == call.itf;
    }

    @Override
    public Set<Target> targets() {
        return Set.of(new Target(spec.targetClass().replace('/', '.')));
    }
}
