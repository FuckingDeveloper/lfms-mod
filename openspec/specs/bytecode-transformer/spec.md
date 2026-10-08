# Bytecode Transformer Specification

## Requirements

- Transformations MUST be selected by an active compatibility profile or core-safe rule.
- Original legacy artifacts MUST remain unchanged.
- Applied transformations MUST be diagnosable by artifact and rule.
- A failed mandatory transformation MUST fail explicitly rather than produce a silently corrupted class.
- Transformed output MAY be cached when its cache key includes all inputs that affect transformation.
