# Bytecode Transformer Specification

## Requirements

- Transformations MUST be selected by an active compatibility profile or core-safe rule.
- Original legacy artifacts MUST remain unchanged.
- Applied transformations MUST be diagnosable by artifact and rule.
- A failed mandatory transformation MUST fail explicitly rather than produce a silently corrupted class.
- Transformed output MAY be cached when its cache key includes all inputs that affect transformation.

## Transformation plan and execution contract

Planning MUST be separated from early launch execution. A versioned, deterministic plan MUST identify source artifact, active profile, target method, anchors, operations, required capabilities and provenance for each resolved binding.

A plan MUST distinguish READY, UNRESOLVED and rejected/unsupported operations. Only sufficiently verified READY operations MAY execute. Unresolved mandatory operations MUST NOT be silently skipped while claiming that the affected capability is supported.

An early processor MUST resolve each anchor against the actual target method, reject ambiguous occurrences unless occurrence identity is proven, and apply each logical transformation transactionally. It MUST verify resulting bytecode and roll back the complete edit if verification fails; a partial edit MUST NOT escape to classloading.

Instruction migration MUST preserve operand-stack shape, control flow, exception handlers, stack-map validity and existing target side effects. Where old local slots differ from the new descriptor, remapping MUST account for static/instance access, category-2 slots and newly allocated temporary locals. A verifier pass proves structural safety, not behavioral equivalence; semantic equivalence needs separate evidence/tests.

Cross-version expression rewrites SHOULD use bytecode dataflow/provenance instead of hardcoded opcode sequences. A semantic rewrite MUST explicitly document where the legacy check occurs relative to migrated consumers and whether moving it changes side effects.

Hook availability, owner visibility, invocation kind and linkage dependencies MUST be checked before a transformed class is considered loadable. Transform execution MUST NOT depend on mod-specific names in LMS core.
