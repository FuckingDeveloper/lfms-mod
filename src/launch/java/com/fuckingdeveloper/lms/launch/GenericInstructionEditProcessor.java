package com.fuckingdeveloper.lms.launch;

import net.neoforged.neoforgespi.transformation.ProcessorName;
import net.neoforged.neoforgespi.transformation.SimpleClassProcessor;
import net.neoforged.neoforgespi.transformation.SimpleTransformationContext;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

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
            if (bindings.size() != spec.anchors().size()) {
                System.out.println("[LMS/early] unresolved anchors id=" + spec.id()
                        + " expected=" + spec.anchors() + " bound=" + bindings.keySet()
                        + " calls=" + methodCalls(method));
            }
            int hooksBefore = countInsertedHookCalls(method);
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
            int hooksAfter = countInsertedHookCalls(method);
            boolean anchorsStillMatch = spec.anchors().stream().anyMatch(anchor ->
                    containsMatchingCall(method, anchor.method()));
            int maxStackBefore = method.maxStack;
            dumpInsertedRegions(method);
            int computedMaxStack = recomputeMaxStack(input.name, method);
            String bytecodeVerification = verifyBytecode(input.name, method);
            System.out.println("[LMS/early] verify id=" + spec.id()
                    + " methodIdentity=" + Integer.toHexString(System.identityHashCode(method))
                    + " hooksBefore=" + hooksBefore + " hooksAfter=" + hooksAfter
                    + " anchorsStillMatch=" + anchorsStillMatch
                    + " instructionCount=" + method.instructions.size()
                    + " maxStack=" + maxStackBefore + "->" + computedMaxStack
                    + " bytecode=" + bytecodeVerification);
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

    private static List<String> methodCalls(MethodNode method) {
        List<String> calls = new ArrayList<>();
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call) {
                calls.add(call.getOpcode() + " " + call.owner + "#" + call.name + call.desc + " itf=" + call.itf);
            }
        }
        return List.copyOf(calls);
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

    private void dumpInsertedRegions(MethodNode method) {
        Set<MethodRef> hooks = new HashSet<>();
        for (Edit edit : spec.edits()) {
            for (Value value : edit.values()) {
                if (value.kind() == ValueKind.METHOD_CALL && value.method() != null) hooks.add(value.method());
            }
        }
        Set<Integer> dumped = new HashSet<>();
        for (int i = 0; i < method.instructions.size(); i++) {
            AbstractInsnNode n = method.instructions.get(i);
            if (n instanceof MethodInsnNode call && hooks.stream().anyMatch(hook -> matches(call, hook))) {
                int from = Math.max(0, i - 12);
                int to = Math.min(method.instructions.size() - 1, i + 20);
                if (dumped.add(i)) dumpWindow(method, from, to, "inserted-hook@" + i);
            }
        }
    }

    private static void dumpWindow(MethodNode method, int from, int to, String reason) {
        System.out.println("[LMS/early] bytecode-window reason=" + reason + " method=" + method.name + method.desc);
        for (int i = Math.max(0, from); i <= Math.min(to, method.instructions.size() - 1); i++) {
            AbstractInsnNode n = method.instructions.get(i);
            System.out.println("[LMS/early]   insn=" + i + " " + describeInsn(method, n));
        }
    }

    private static String describeInsn(MethodNode method, AbstractInsnNode n) {
        if (n instanceof LabelNode) return "LABEL@" + Integer.toHexString(System.identityHashCode(n));
        if (n instanceof LineNumberNode line) return "LINE " + line.line + " -> " + indexOf(method, line.start);
        if (n instanceof FrameNode frame) return "FRAME type=" + frame.type + " locals=" + frame.local + " stack=" + frame.stack;
        if (n instanceof MethodInsnNode call) return "CALL op=" + call.getOpcode() + " " + call.owner + "#" + call.name + call.desc;
        if (n instanceof VarInsnNode var) return "VAR op=" + var.getOpcode() + " slot=" + var.var;
        if (n instanceof JumpInsnNode jump) return "JUMP op=" + jump.getOpcode() + " -> " + indexOf(method, jump.label);
        if (n instanceof InsnNode) return "INSN op=" + n.getOpcode();
        if (n instanceof FieldInsnNode field) return "FIELD op=" + field.getOpcode() + " " + field.owner + "#" + field.name + ":" + field.desc;
        if (n instanceof TypeInsnNode type) return "TYPE op=" + type.getOpcode() + " " + type.desc;
        if (n instanceof LdcInsnNode ldc) return "LDC " + ldc.cst;
        return n.getClass().getSimpleName() + " op=" + n.getOpcode();
    }

    private static int indexOf(MethodNode method, AbstractInsnNode node) {
        return node == null ? -1 : method.instructions.indexOf(node);
    }

    private static int recomputeMaxStack(String owner, MethodNode method) {
        int original = method.maxStack;
        // Analyzer allocates frames from MethodNode.maxStack, so first give it a
        // conservative ceiling. The exact peak is then derived from analyzed frames.
        method.maxStack = Math.max(original, method.instructions.size() + method.maxLocals + 8);
        try {
            Frame<BasicValue>[] frames = new Analyzer<>(new BasicVerifier()).analyze(owner, method);
            int peak = 0;
            for (Frame<BasicValue> frame : frames) {
                if (frame != null) peak = Math.max(peak, frame.getStackSize());
            }
            method.maxStack = peak;
            return peak;
        } catch (AnalyzerException | RuntimeException e) {
            method.maxStack = original;
            System.out.println("[LMS/early] maxStack recompute failure owner=" + owner
                    + " method=" + method.name + method.desc + " error=" + e);
            e.printStackTrace(System.out);
            return original;
        }
    }

    private static String verifyBytecode(String owner, MethodNode method) {
        try {
            new Analyzer<>(new BasicVerifier()).analyze(owner, method);
            return "OK";
        } catch (AnalyzerException | RuntimeException e) {
            StringBuilder out = new StringBuilder("INVALID:")
                    .append(e.getClass().getSimpleName()).append(':').append(e.getMessage());
            if (e instanceof AnalyzerException analyzer && analyzer.node != null) {
                out.append("@insn=").append(method.instructions.indexOf(analyzer.node));
            }
            System.out.println("[LMS/early] bytecode verification failure owner=" + owner
                    + " method=" + method.name + method.desc + " error=" + out);
            e.printStackTrace(System.out);
            return out.toString();
        }
    }

    private int countInsertedHookCalls(MethodNode method) {
        Set<MethodRef> hooks = new HashSet<>();
        for (Edit edit : spec.edits()) {
            for (Value value : edit.values()) {
                if (value.kind() == ValueKind.METHOD_CALL && value.method() != null) hooks.add(value.method());
            }
        }
        int count = 0;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && hooks.stream().anyMatch(hook -> matches(call, hook))) count++;
        }
        return count;
    }

    private static boolean containsMatchingCall(MethodNode method, MethodRef expected) {
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof MethodInsnNode call && matches(call, expected)) return true;
        }
        return false;
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
