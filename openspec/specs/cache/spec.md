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
