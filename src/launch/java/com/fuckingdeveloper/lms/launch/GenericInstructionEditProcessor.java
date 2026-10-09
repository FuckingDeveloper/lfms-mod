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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

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
        for (int methodIndex = 0; methodIndex < input.methods.size(); methodIndex++) {
            MethodNode method = input.methods.get(methodIndex);
            if (!spec.targetMethod().equals(method.name) || !spec.targetDescriptor().equals(method.desc)) continue;
            targetFound = true;

            BindingResult bindingResult = bindAnchors(method);
            if (!bindingResult.ambiguous().isEmpty() || bindingResult.bindings().size() != spec.anchors().size()) {
                System.out.println("[LMS/early] skipped unsafe transform id=" + spec.id()
                        + " reason=anchor-binding expected=" + spec.anchors()
                        + " bound=" + bindingResult.bindings().keySet()
                        + " ambiguous=" + bindingResult.ambiguous()
                        + " calls=" + methodCalls(method));
                continue;
            }

            MethodNode original = cloneMethod(method);
            Map<String, AbstractInsnNode> bindings = bindingResult.bindings();
            int hooksBefore = countInsertedHookCalls(method);
            int methodApplied = 0;
            try {
                List<Edit> executableEdits = adaptLegacyResultRewrite(method, bindings);
                if (executableEdits.isEmpty() && !spec.edits().isEmpty()) {
                    methodApplied = spec.edits().size();
                }
                for (Edit edit : executableEdits) {
                    AbstractInsnNode location = resolve(bindings, edit.location());
                    if (location == null || method.instructions.indexOf(location) < 0) {
                        throw new IllegalStateException("edit location is no longer in method: " + edit.location());
                    }
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
                    methodApplied++;
                }

                int maxStackBefore = method.maxStack;
                Verification verification = analyzeAndComputeMaxStack(input.name, method);
                if (!verification.valid()) {
                    dumpInsertedRegions(method);
                    throw new IllegalStateException("post-transform bytecode invalid: " + verification.error());
                }
                method.maxStack = verification.maxStack();

                int hooksAfter = countInsertedHookCalls(method);
                boolean anchorsStillMatch = spec.anchors().stream().anyMatch(anchor ->
                        containsMatchingCall(method, anchor.method()));
                System.out.println("[LMS/early] verify id=" + spec.id()
                        + " methodIdentity=" + Integer.toHexString(System.identityHashCode(method))
                        + " hooksBefore=" + hooksBefore + " hooksAfter=" + hooksAfter
                        + " anchorsStillMatch=" + anchorsStillMatch
                        + " instructionCount=" + method.instructions.size()
                        + " maxStack=" + maxStackBefore + "->" + method.maxStack
                        + " bytecode=OK");
                applied += methodApplied;
            } catch (RuntimeException failure) {
                input.methods.set(methodIndex, original);
                System.out.println("[LMS/early] rolled back transform id=" + spec.id()
                        + " target=" + spec.targetClass() + "#" + spec.targetMethod() + spec.targetDescriptor()
                        + " reason=" + failure.getMessage());
            }
        }
        System.out.println("[LMS/early] transform id=" + spec.id() + " target=" + spec.targetClass()
                + "#" + spec.targetMethod() + spec.targetDescriptor() + " targetFound=" + targetFound
                + " editsApplied=" + applied + " engine=GENERIC_INSTRUCTION_EDIT");
    }


    /**
     * Adapts the common legacy shape "remove anchor successor; insert hook/check after
     * anchor; remove anchor" when the migrated call result is now consumed by a larger
     * current expression. Literal relative-node replay is unsafe in that situation.
     *
     * The adaptation is structural, not mod-specific: it requires one anchor, a
     * same-stack-signature replacement call, a store/reload/check/conditional-return
     * suffix, and a current successor that consumes the anchor result. It rewrites
     * the anchor call to the replacement call, preserves the current consumer, and
     * moves the result check after that consumer.
     */
    private List<Edit> adaptLegacyResultRewrite(MethodNode method, Map<String, AbstractInsnNode> bindings) {
        if (spec.anchors().size() != 1 || spec.edits().size() != 3) return spec.edits();
        Anchor anchor = spec.anchors().getFirst();
        AbstractInsnNode anchorNode = bindings.get(anchor.variable());
        if (!(anchorNode instanceof MethodInsnNode)) return spec.edits();

        Edit removeNext = null, insertAfter = null, removeAnchor = null;
        for (Edit edit : spec.edits()) {
            if (!edit.location().variable().equals(anchor.variable())) return spec.edits();
            if (edit.kind() == EditKind.REMOVE && edit.location().relativeOffset() == 1) removeNext = edit;
            else if (edit.kind() == EditKind.INSERT_AFTER && edit.location().relativeOffset() == 0) insertAfter = edit;
            else if (edit.kind() == EditKind.REMOVE && edit.location().relativeOffset() == 0) removeAnchor = edit;
        }
        if (removeNext == null || insertAfter == null || removeAnchor == null) return spec.edits();

        List<Value> values = insertAfter.values();
        if (values.size() < 6 || values.getFirst().kind() != ValueKind.METHOD_CALL) return spec.edits();
        MethodRef replacement = values.getFirst().method();
        MethodRef original = anchor.method();
        if (replacement == null
                || !Type.getReturnType(replacement.descriptor()).equals(Type.getReturnType(original.descriptor()))
                || argumentStackSlots(replacement) != argumentStackSlots(original)) return spec.edits();

        Value store = values.get(1), load = values.get(2);
        Value jump = values.get(values.size() - 2), ret = values.getLast();
        if (store.kind() != ValueKind.VARIABLE || load.kind() != ValueKind.VARIABLE
                || !Objects.equals(store.variable(), load.variable())
                || jump.kind() != ValueKind.JUMP || ret.kind() != ValueKind.SIMPLE_OPCODE
                || ret.opcode() != Opcodes.RETURN) return spec.edits();

        Type result = Type.getReturnType(original.descriptor());
        ConsumerSite consumerSite = findConsumerOfTopValue(anchorNode, result);
        if (consumerSite == null) {
            System.out.println("[LMS/early] semantic-adapt skipped id=" + spec.id()
                    + " reason=no-safe-stack-consumer successor=" + describeNode(nextExecutable(anchorNode)));
            return spec.edits();
        }
        MethodInsnNode consumer = consumerSite.consumer();
        System.out.println("[LMS/early] semantic-consumer id=" + spec.id()
                + " anchor=" + describeNode(anchorNode)
                + " successor=" + describeNode(nextExecutable(anchorNode))
                + " consumer=" + consumer.owner + "#" + consumer.name + consumer.desc);

        // Replace the migrated producer in-place. Its current consumer remains intact.
        anchorNode = bindings.get(anchor.variable());
        MethodInsnNode replacementNode = new MethodInsnNode(
                replacement.opcode(), replacement.owner(), replacement.name(),
                replacement.descriptor(), replacement.isInterface());
        method.instructions.set(anchorNode, replacementNode);

        // Keep the current consumer, then perform the legacy result check from the
        // local written by the hook. Store a duplicate before the consumer so the
        // consumer still receives the hook result.
        // Save the hook result immediately after its producer, before any current
        // stack shuffle (notably SWAP) changes which value is on top.
        InsnList saveResult = new InsnList();
        saveResult.add(new InsnNode(result.getSize() == 2 ? Opcodes.DUP2 : Opcodes.DUP));
        saveResult.add(new VarInsnNode(store.opcode(), store.variable()));
        method.instructions.insert(replacementNode, saveResult);

        InsnList suffix = build(values.subList(2, values.size() - 2), bindings);
        LabelNode continueLabel = new LabelNode();
        suffix.add(new JumpInsnNode(jump.opcode(), continueLabel));
        suffix.add(new InsnNode(Opcodes.RETURN));
        suffix.add(continueLabel);
        method.instructions.insert(consumer, suffix);
        System.out.println("[LMS/early] semantic-adapt id=" + spec.id()
                + " pattern=RESULT_REWRITE_PRESERVE_CONSUMER consumer="
                + consumer.owner + "#" + consumer.name + consumer.desc);
        return List.of();
    }

    private record ConsumerSite(MethodInsnNode consumer, AbstractInsnNode insertionPoint) {}

    private static ConsumerSite findConsumerOfTopValue(AbstractInsnNode producer, Type producedType) {
        AbstractInsnNode n = nextExecutable(producer);
        if (n == null) return null;

        // NeoForge-style expression lowering may rotate the freshly produced value
        // below an already prepared argument with SWAP. For a category-1 value this
        // proves that the next invocation consumes it as its penultimate stack item.
        boolean swappedCategory1 = false;
        if (n.getOpcode() == Opcodes.SWAP) {
            if (producedType.getSize() != 1) return null;
            swappedCategory1 = true;
            n = nextExecutable(n);
            System.out.println("[LMS/early] semantic-scan swap-next=" + describeNode(n)
                    + " produced=" + producedType.getDescriptor());
        }

        // Do not guess through arbitrary stack programs. A straight consumer or the
        // single proven SWAP form are the only shapes accepted here.
        if (!(n instanceof MethodInsnNode call)) return null;
        Type[] args = Type.getArgumentTypes(call.desc);
        if (!swappedCategory1) {
            if (args.length == 0 || !args[args.length - 1].equals(producedType)) return null;
        } else {
            // Before SWAP the produced value is directly below the current top
            // operand. After SWAP it becomes the top operand and is therefore
            // consumed as the last explicit argument of the invocation.
            if (args.length == 0 || !args[args.length - 1].equals(producedType)) return null;
        }
        return new ConsumerSite(call, call);
    }

    private static String describeNode(AbstractInsnNode node) {
        if (node == null) return "<null>";
        if (node instanceof MethodInsnNode m) return "CALL " + m.owner + "#" + m.name + m.desc;
        return node.getClass().getSimpleName() + " opcode=" + node.getOpcode();
    }

    private static AbstractInsnNode nextExecutable(AbstractInsnNode node) {
        if (node == null) return null;
        for (AbstractInsnNode n = node.getNext(); n != null; n = n.getNext()) {
            if (n.getOpcode() >= 0) return n;
        }
        return null;
    }

    private static int argumentStackSlots(MethodRef method) {
        int slots = method.opcode() == Opcodes.INVOKESTATIC ? 0 : 1;
        for (Type type : Type.getArgumentTypes(method.descriptor())) slots += type.getSize();
        return slots;
    }

    private record BindingResult(Map<String, AbstractInsnNode> bindings, Set<String> ambiguous) {}

    private BindingResult bindAnchors(MethodNode method) {
        Map<String, AbstractInsnNode> result = new HashMap<>();
        Set<String> ambiguous = new LinkedHashSet<>();
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (!(insn instanceof MethodInsnNode call)) continue;
            for (Anchor anchor : spec.anchors()) {
                if (!matches(call, anchor.method())) continue;
                if (result.containsKey(anchor.variable())) ambiguous.add(anchor.variable());
                else result.put(anchor.variable(), call);
            }
        }
        return new BindingResult(result, ambiguous);
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
        if (node == null || !reference.label()) return node;
        if (node instanceof LabelNode label) return label;
        if (node instanceof JumpInsnNode jump) return jump.label;
        return null;
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

    private static MethodNode cloneMethod(MethodNode source) {
        MethodNode copy = new MethodNode(source.access, source.name, source.desc, source.signature,
                source.exceptions == null ? null : source.exceptions.toArray(String[]::new));
        source.accept(copy);
        return copy;
    }

    private record Verification(boolean valid, int maxStack, String error) {}

    private static Verification analyzeAndComputeMaxStack(String owner, MethodNode method) {
        int original = method.maxStack;
        method.maxStack = Math.max(original, method.instructions.size() + method.maxLocals + 8);
        try {
            Frame<BasicValue>[] frames = new Analyzer<>(new BasicVerifier()).analyze(owner, method);
            int peak = 0;
            for (Frame<BasicValue> frame : frames) {
                if (frame != null) peak = Math.max(peak, frame.getStackSize());
            }
            return new Verification(true, peak, "");
        } catch (AnalyzerException | RuntimeException e) {
            String error = e.getClass().getSimpleName() + ":" + e.getMessage();
            if (e instanceof AnalyzerException analyzer && analyzer.node != null) {
                error += "@insn=" + method.instructions.indexOf(analyzer.node);
            }
            return new Verification(false, original, error);
        } finally {
            method.maxStack = original;
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
