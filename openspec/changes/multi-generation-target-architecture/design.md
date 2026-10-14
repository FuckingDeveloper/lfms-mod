# Design: Multi-Generation Source and Target Architecture

## Direction

The desired dependency direction is:

```text
legacy artifact
  -> discovery
  -> source profile
  -> semantic compatibility service/operation
  -> target adapter
  -> Minecraft/NeoForge
```

The active implementation may migrate toward this incrementally.

## Incremental migration

Do not pause Forge 1.19.2 integration for a wholesale rewrite.

When touching an existing compatibility family:

1. identify whether code is source-specific, target-specific or semantic;
2. remove reference-mod identity conditions where a generic rule is provable;
3. introduce a shared semantic boundary when it reduces source/target coupling;
4. preserve existing verified behavior;
5. add focused regression evidence where practical.

## Source profile families

A profile family shares behavior across close legacy versions while retaining explicit mapping/ABI deltas.

The canonical next family after Forge 1.19.2 is Forge 1.16.5/1.16.x. Forge 1.12.2 and 1.7.10 are progressively larger architectural tests.

## Target adapters

Target adapters isolate modern runtime mechanics that are expected to change again:

- registration/lifecycle;
- events;
- capabilities;
- networking;
- rendering;
- constructors/invariants;
- mappings/symbol resolution where target-dependent.

## Semantic layer

An explicit object-model IR is optional initially. Typed bridge APIs may serve as the semantic layer.

The test for a useful boundary is whether adding target 27.3 can change the target implementation without editing every source profile.

## Verification

Use small compiled legacy fixtures for deterministic subsystem tests and external real mods for broad integration.

A future target upgrade must execute the same legacy fixtures unchanged.

## Release shape

Prefer target-specific LMS artifacts with shared source code over one speculative universal JAR.
