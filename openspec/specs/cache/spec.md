# Transformation Cache Specification

## Requirements

Cached derived artifacts MUST be disposable and reproducible.

A cache identity MUST include, directly or through a deterministic aggregate key:
- cryptographic hash of source artifact bytes;
- LMS version/build identity;
- selected profile and profile/rule-set version;
- target Minecraft/Forge version;
- transformation configuration affecting output;
- relevant mod-specific compatibility module versions.

A cache hit MUST NOT be accepted when compatibility-relevant inputs differ.

Cache corruption MUST be recoverable by discarding and rebuilding derived output.

The original legacy JAR MUST never be replaced by a cache artifact.

Per-transformer raw hierarchy and name-pair subtype evidence MAY be cached when
its managed binary graph is fixed. Rebinding MUST invalidate positive and negative
evidence. Cache entries MUST retain only bytes/names/results, not loaded source
classes. Decisions using a currently mutated ClassNode MUST start from its current
superclass/interfaces and MUST not use a name-only cached whole-node decision.
