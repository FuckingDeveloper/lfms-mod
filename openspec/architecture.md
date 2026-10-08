# LFMS Architecture

## Pipeline

```text
Legacy Mod JAR
      |
      v
Legacy Discovery
      |
      v
LegacyModDescriptor
      |
      v
Profile Resolver
      |
      v
Version Profile
      |
      v
Bytecode Transformation
      |
      +-------------------+
      |                   |
      v                   v
Compatibility Runtime   Native Forge API
      |                   |
      +---------+---------+
                |
                v
        Minecraft 26.3
```

## Responsibilities

### Legacy Discovery
MUST inspect legacy artifacts and produce descriptive metadata. Discovery MUST NOT mutate the legacy artifact.

### Profile Resolver
MUST select a compatibility profile from discovered evidence. Ambiguous or unsupported artifacts MUST be reported explicitly.

### Version Profile
MUST isolate behavior tied to a legacy Minecraft/Forge generation. Version-specific conditions SHOULD NOT leak throughout core code.

### Bytecode Transformer
MUST perform transformations that can be safely resolved before or during class loading. Transformations MUST be attributable in diagnostics.

### Compatibility Runtime
MUST provide runtime adaptation where static transformation is insufficient, including lifecycle or semantic bridges.

### Diagnostics
MUST record detection, selected profile, applied transformations, unsupported APIs, failures and achieved compatibility level.

## Design constraints

- Core code MUST remain independent of IC2-specific behavior.
- Profiles MUST be independently evolvable.
- A compatibility shim MUST have a documented semantic reason; arbitrary per-mod patches belong in optional mod-specific compatibility modules.
- Failures SHOULD be deterministic and actionable.
