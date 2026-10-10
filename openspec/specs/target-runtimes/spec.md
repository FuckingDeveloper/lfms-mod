# Target Runtime and Upgrade Specification

## Purpose

LMS MUST be maintainable when Minecraft and NeoForge advance beyond the initial Minecraft 26.3 target.

A future target such as Minecraft 27.3 is a supported evolution path, not a new LMS project.

## Target generations

A **target generation** is a tested tuple containing at least:

- Minecraft version;
- NeoForge/FML version;
- Java runtime/toolchain version;
- mapping/symbol data used by LMS;
- target adapter implementation version;
- compatibility test results.

The initial tuple is Minecraft 26.3 / NeoForge 26.3.x / Java 25.

LMS MUST NOT claim compatibility with an untested target merely because it compiles or falls inside a broad metadata version range.

## Build artifacts

LMS SHOULD produce a target-specific artifact for each supported target generation unless binary compatibility across those generations is proven and continuously tested.

Conceptually:

```text
lms-<version>-neoforge-26.3.jar
lms-<version>-neoforge-27.3.jar
```

The project MAY later use a multi-module build or source sets to share core/profile code while isolating target adapters.

A single universal target JAR is not a requirement.

## Upgrade procedure

When adopting a new target generation, maintainers MUST:

1. pin the new Minecraft, NeoForge/FML and Java versions;
2. compile LMS core and target adapter against the new runtime;
3. regenerate/load target mappings and symbol evidence;
4. compare target API/ABI used by LMS against the previous supported target;
5. classify every changed dependency as unchanged, structurally changed, semantically changed, removed, or unknown;
6. update only the affected target adapters/core integration;
7. run focused compatibility fixtures;
8. run representative legacy integration mods;
9. record the achieved compatibility level;
10. publish only after required gates pass.

Compilation alone is never a sufficient upgrade test.

## Target API comparison

LMS SHOULD provide tooling that can compare supported target generations.

The comparison SHOULD cover:

- classes and inheritance;
- methods and descriptors;
- fields and descriptors;
- constructors;
- relevant annotations/interfaces;
- registry keys and lifecycle APIs;
- events used by LMS;
- capability APIs;
- networking/payload APIs;
- rendering integration points.

Structural equality MUST NOT be interpreted automatically as semantic equality.

The comparator MAY produce categories such as:

- VERIFIED_UNCHANGED
- STRUCTURAL_CHANGE
- SEMANTIC_REVIEW_REQUIRED
- REMOVED
- NEW
- UNKNOWN

## Target-adapter isolation

When an operation changes from target 26.3 to target 27.3, maintainers SHOULD be able to update the target implementation without modifying every legacy source profile.

For example:

```text
Forge1192 registration --+
Forge1165 registration ---+--> REGISTER_OBJECT --> Target263RegistryAdapter
Forge1122 registration ---+                       Target273RegistryAdapter
```

If adding a target generation requires widespread edits inside all source profiles, the architecture SHOULD be reviewed for missing semantic boundaries.

## Regression fixtures

LMS SHOULD maintain small synthetic legacy fixtures compiled against supported source generations.

At minimum the fixture strategy SHOULD eventually cover:

- discovery/profile selection;
- block/item registration;
- cross-registry staging;
- event subscription and cancellation;
- capabilities (item/fluid/energy);
- networking;
- block entities;
- recipes/tags;
- creative tabs;
- client setup/rendering;
- world creation/load.

Fixtures MUST remain compiled for their legacy source runtime. They MUST NOT be recompiled merely to make a new target adapter pass.

## Integration matrix

Each target release SHOULD record a matrix similar to:

| Source profile | Link | Lifecycle | Registration | Menu | World | Functional |
| --- | --- | --- | --- | --- | --- | --- |
| Forge 1.19.2 | PASS | PASS | PASS | PASS | PASS | PARTIAL |
| Forge 1.16.5 | PASS | PASS | PASS | PASS | PARTIAL | UNVERIFIED |
| Forge 1.12.2 | PASS | PARTIAL | UNVERIFIED | UNVERIFIED | UNVERIFIED | UNVERIFIED |

The values above are illustrative only and MUST NOT be copied as actual project status.

## Representative integration mods

Large real mods MAY be used as external integration targets.

They complement, but MUST NOT replace, focused fixtures because:

- they can fail in multiple unrelated subsystems;
- proprietary/non-redistributable artifacts cannot be repository fixtures;
- one mod does not exercise every semantic variant;
- failures are harder to localize.

## Compatibility preservation

When upgrading a target, maintainers MUST verify that an adapter change does not silently reduce compatibility for previously supported source profiles.

Target-specific fixes MUST NOT be implemented as source-mod-specific patches.

## Release policy

A target generation SHOULD be released only when:

- LMS itself builds reproducibly;
- required focused fixtures pass;
- the compatibility matrix is recorded;
- unsupported semantic boundaries are documented;
- no known failure is hidden by a silent no-op;
- target metadata declares only versions actually tested.

## Future target example

For a hypothetical Minecraft 27.3 upgrade:

```text
LMS Core ----------------------- shared
Forge1192 Source Profile ------- shared
Forge1165 Source Profile ------- shared
Semantic operations ----------- shared
Target 26.3 Adapter ------------ retained for 26.3 release
Target 27.3 Adapter ------------ added/updated
Target 27.3 tests -------------- new execution matrix
```

The goal is to make target upgrades an explicit maintenance workflow rather than repeating legacy-source reverse engineering from zero.
