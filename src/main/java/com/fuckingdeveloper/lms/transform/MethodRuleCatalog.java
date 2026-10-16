package com.fuckingdeveloper.lms.transform;

import java.util.*;
import org.objectweb.asm.Handle;

/** Explicit implemented method rules; resolution requires source declaration evidence. */
public final class MethodRuleCatalog {
    public record Member(String owner, String name, String descriptor) {
        public Member { Objects.requireNonNull(owner); Objects.requireNonNull(name); Objects.requireNonNull(descriptor); }
    }
    public record Rule(String id, String family, Member source, int opcode, Handle bridge, String evidence) {
        public Rule {
            Objects.requireNonNull(id); Objects.requireNonNull(family); Objects.requireNonNull(source);
            Objects.requireNonNull(bridge); Objects.requireNonNull(evidence);
            if (id.isBlank() || evidence.isBlank()) throw new IllegalArgumentException("Rule needs identity and evidence");
        }
    }
    @FunctionalInterface public interface Declaration {
        String owner(String receiver, String name, String descriptor);
    }
    private final Map<Member, Rule> rules;
    private final Set<String> signatures;
    public MethodRuleCatalog(List<Rule> entries) {
        var indexed = new LinkedHashMap<Member, Rule>(); var ids = new HashSet<String>();
        var signatures = new HashSet<String>();
        for (var rule : entries) {
            if (indexed.putIfAbsent(rule.source(), rule) != null || !ids.add(rule.id()))
                throw new IllegalArgumentException("Duplicate method rule: " + rule.id());
            signatures.add(rule.source().name() + rule.source().descriptor());
        }
        this.rules = Map.copyOf(indexed); this.signatures = Set.copyOf(signatures);
    }
    public Optional<Rule> resolve(int opcode, String owner, String name, String descriptor, Declaration declarations) {
        if (!signatures.contains(name + descriptor)) return Optional.empty();
        String declaration = declarations.owner(owner, name, descriptor);
        if (declaration == null) return Optional.empty();
        var rule = rules.get(new Member(declaration, name, descriptor));
        return rule != null && rule.opcode() == opcode ? Optional.of(rule) : Optional.empty();
    }
    public List<Rule> entries() { return rules.values().stream().sorted(Comparator.comparing(Rule::id)).toList(); }
}
