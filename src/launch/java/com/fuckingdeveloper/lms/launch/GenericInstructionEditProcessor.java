package com.fuckingdeveloper.lms.launch;

import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleClassProcessor;
import net.neoforged.neoforgespi.transformation.SimpleTransformationContext;
import org.objectweb.asm.tree.*;

import java.util.*;

/**
 * Generic early bytecode primitive for legacy coremod InsnList edits.
 * The processor contains no knowledge of any particular legacy mod.
 */
public final class GenericInstructionEditProcessor extends SimpleClassProcessor {
    public record MethodRef(int opcode, String owner, String name, String descriptor, boolean isInterface) {}
    public record Reference(String variable, int relativeOffset, boolean label) {}
    public enum EditKind { REMOVE, INSERT_BEFORE, INSERT_AFTER, REPLACE }
    public enum ValueKind { METHOD_CALL, SIMPLE_OPCODE, VARIABLE, JUMP }
    public record Value(ValueKind kind, MethodRef method, Integer opcode, Integer variable, Reference target) {}
    public record Edit(EditKind kind, Reference location, List<Value> values) {}
    public record Anchor(String variable, MethodRef method) {}
    public record Spec(String id, String targetClass, String targetMethod, String targetDescriptor,
                       List<Anchor> anchors, List<Edit> edits) {}

    private final Spec spec;

    public GenericInstructionEditProcessor(Spec spec) { this.spec = spec; }

    @Override public ProcessorName name() { return new ProcessorName("lms", spec.id()); }

    @Override
    public void transform(ClassNode input, SimpleTransformationContext context) {
        int applied = 0;
        boolean targetFound = false;
        for (MethodNode method : input.methods) {
            if (!spec.targetMethod().equals(method.name) || !spec.targetDescriptor().equals(method.desc)) continue;
            targetFound = true;
            Map<String, AbstractInsnNode> bindings = bindAnchors(method);
            for (Edit edit : spec.edits()) {
                AbstractInsnNode location = resolve(bindings, edit.location());
                if (location == null) continue;
                switch (edit.kind()) {
                    case REMOVE -> method.instructions.remove(location);
                    case INSERT_BEFORE -> method.instructions.insertBefore(location, build(edit.values(), bindings));
                    case INSERT_AFTER -> method.instructions.insert(location, build(edit.values(), bindings));
                    case REPLACE -> {
                        InsnList replacement = build(edit.values(), bindings);
                        method.instructions.insertBefore(location, replacement);
                        method.instructions.remove(location);
                    }
                }
                applied++;
            }
        }
        System.out.println("[LMS/early] transform id=" + spec.id() + " target=" + spec.targetClass()
                + "#" + spec.targetMethod() + spec.targetDescriptor() + " targetFound=" + targetFound
                + " editsApplied=" + applied + " engine=GENERIC_INSTRUCTION_EDIT");
    }

    private Map<String, AbstractInsnNode> bindAnchors(MethodNode method) {
        Map<String, AbstractInsnNode> result = new HashMap<>();
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof MethodInsnNode call)) continue;
            for (Anchor anchor : spec.anchors()) {
                if (!result.containsKey(anchor.variable()) && matches(call, anchor.method())) {
                    result.put(anchor.variable(), call);
                }
            }
        }
        return result;
    }

    private static AbstractInsnNode resolve(Map<String, AbstractInsnNode> bindings, Reference reference) {
        AbstractInsnNode node = bindings.get(reference.variable());
        if (node == null) return null;
        int offset = reference.relativeOffset();
        while (offset > 0 && node != null) { node = node.getNext(); offset--; }
        while (offset < 0 && node != null) { node = node.getPrevious(); offset++; }
        return node;
    }

    private static InsnList build(List<Value> values, Map<String, AbstractInsnNode> bindings) {
        InsnList result = new InsnList();
        for (Value value : values) {
            switch (value.kind()) {
                case METHOD_CALL -> {
                    MethodRef m = value.method();
                    result.add(new MethodInsnNode(m.opcode(), m.owner(), m.name(), m.descriptor(), m.isInterface()));
                }
                case SIMPLE_OPCODE -> result.add(new InsnNode(value.opcode()));
                case VARIABLE -> result.add(new VarInsnNode(value.opcode(), value.variable()));
                case JUMP -> {
                    AbstractInsnNode target = resolve(bindings, value.target());
                    LabelNode label = target instanceof LabelNode l ? l : null;
                    if (label == null) throw new IllegalStateException("LMS jump target is not a LabelNode: " + value.target());
                    result.add(new JumpInsnNode(value.opcode(), label));
                }
            }
        }
        return result;
    }

    private static boolean matches(MethodInsnNode call, MethodRef expected) {
        return call.getOpcode() == expected.opcode()
                && expected.owner().equals(call.owner)
                && expected.name().equals(call.name)
                && expected.descriptor().equals(call.desc)
                && expected.isInterface() == call.itf;
    }

    @Override public Set<Target> targets() {
        return Set.of(new Target(spec.targetClass().replace('/', '.')));
    }
}
