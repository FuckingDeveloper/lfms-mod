package com.fuckingdeveloper.lms.transform;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;
import org.objectweb.asm.tree.ClassNode;

/** Ordered source-identity -> projection -> target-shape passes; never sorts mutations. */
public final class TransformationPipeline {
    public enum Phase { SOURCE_SEMANTICS, NAMESPACE_PROJECTION, TARGET_SHAPE }
    public record Pass(String id,Phase phase,ToIntFunction<ClassNode> operation) {
        public Pass {
            if (id == null || id.isBlank()) throw new IllegalArgumentException("Empty pass identity");
            Objects.requireNonNull(phase); Objects.requireNonNull(operation);
        }
    }
    public record Step(String id,Phase phase) {}
    private final List<Pass> passes;

    public TransformationPipeline(List<Pass> passes) {
        this.passes = List.copyOf(passes);
        var ids = new HashSet<String>();
        Phase previous = Phase.SOURCE_SEMANTICS;
        int projections = 0;
        for (var pass : this.passes) {
            if (!ids.add(pass.id())) throw new IllegalArgumentException("Duplicate transformation pass: " + pass.id());
            if (pass.phase().ordinal() < previous.ordinal())
                throw new IllegalArgumentException("Out-of-order transformation phase: " + pass.id());
            if (pass.phase() == Phase.NAMESPACE_PROJECTION && ++projections > 1)
                throw new IllegalArgumentException("Multiple namespace projections");
            previous = pass.phase();
        }
    }

    public List<Step> steps() { return passes.stream().map(p -> new Step(p.id(),p.phase())).toList(); }

    public int apply(ClassNode node) {
        Objects.requireNonNull(node);
        int rewrites = 0;
        for (var pass : passes) {
            int count = pass.operation().applyAsInt(node);
            if (count < 0) throw new IllegalStateException("Negative rewrite count: " + pass.id());
            rewrites = Math.addExact(rewrites,count);
        }
        return rewrites;
    }
}
