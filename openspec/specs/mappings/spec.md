# Mapping System Specification

## Concept

LMS SHOULD resolve legacy symbols through a canonical compatibility representation:

```text
legacy symbol/name space
        |
        v
canonical LMS symbol/semantic operation
        |
        v
target Minecraft/Forge symbol
```

## Requirements

- Mapping data MUST be version/profile scoped.
- Descriptor-aware method/field mappings MUST distinguish overloads.
- A mapping MUST be able to represent class, method, field and descriptor changes.
- Pure renames SHOULD be handled as mapping/transformation rules rather than runtime bridges.
- Semantic API changes MUST NOT be misrepresented as simple name mappings.
- Unresolved mandatory symbols MUST be diagnosable.
- Mapping data SHOULD be testable independently of a full game launch.

## Evidence-driven migration contract

A resolver MUST retain both the original invocation owner and the mapped declaring owner; they are not interchangeable. Legacy source aliases MAY be canonicalized using profile mappings, but canonicalization MUST NOT itself prove that a target method survives.

A method candidate MUST record provenance (mapping file, legacy runtime bytecode, current runtime bytecode, or explicit profile rule), descriptor and access/invocation evidence, and confidence/status. Unique descriptor-only matches are candidates, not verified semantic migrations. Ambiguous matches MUST remain unresolved.

When the current owner or method disappears, the profile MAY search a bounded, enumerated target-runtime symbol index. Candidate discovery MUST be separated from verification. Verification MUST use sufficiently discriminating evidence such as preserved named identity, mapped call-site identity, compatible dataflow, or explicit versioned semantic rules. Similar names, same return type, or approximate operation fingerprints alone MUST NOT authorize executable rewriting.

The target-runtime index SHOULD enumerate classes without loading/initializing them, include class/method/field metadata and provenance, and be reproducible for a pinned target build. Missing or partial index coverage MUST be reported, not treated as proof of absence.

Method migrations MUST validate invocation opcode, static/instance access, argument and return types, owner hierarchy, and call-site receiver semantics. Descriptor changes MUST carry explicit argument/result adaptation rules. Field-to-method or method-to-field migration is semantic adaptation, not a simple rename.

Mapping evidence and decisions SHOULD be serializable for reproducible offline tests; mapping decisions MUST NOT depend on classpath enumeration order.

Field resolution MUST honor source declarations before mapping inherited vanilla
members. An exact name/descriptor declaration in a managed class hides a matching
ancestor field, even when the name resembles an SRG symbol. The lookup order MUST
be the declaring class, its superinterfaces, then its superclass, as defined by
JVM field resolution. LMS MUST NOT redirect source-owned field reads/writes to a
native field merely because an ancestor has the same mapped identity. Declaration
indexes MUST be invalidated when the managed source graph is rebound, and a
currently transformed class MUST use its current declarations and ancestry.

For Forge 1.19.2 Slot coordinate mixin accessors, a target adapter MAY preserve
post-construction writes to the same native x/y coordinates when the exact
target, field, descriptor and source @Mutable annotation are verified. This
MUST NOT grant access to unrelated private Slot fields or infer that changed
slot background-icon or screen-rendering contracts are equivalent.
