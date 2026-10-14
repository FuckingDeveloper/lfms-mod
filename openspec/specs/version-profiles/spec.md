# Version Profiles Specification

## Purpose

Version profiles describe legacy **source** generations. They are intentionally independent from the modern target-runtime adapter.

## Requirements

- Each supported legacy generation MUST be represented by an isolated source profile or a proven-compatible profile family.
- The first implemented profile MUST target Forge 1.19.2.
- Profile selection MUST be based on the normalized legacy descriptor and evidence.
- Version-specific mappings, source ABI, transformation rules and source runtime contracts MUST be owned or declared by the source profile.
- Core LMS MUST NOT contain scattered source-version conditionals when behavior belongs to a profile.
- A source profile MUST NOT assume Minecraft 26.3 is the only possible modern target.
- Neighboring versions MAY share a profile family only when differences are represented explicitly and compatibility is supported by evidence.
- An unknown/ambiguous artifact MUST NOT be forced into the nearest profile.

## Planned canonical source generations

Priority is architectural/product guidance, not a claim of implemented support.

### P0 — Forge 1.19.2

Current reference source profile.

Reasons:

- already under active implementation;
- modern-enough Forge architecture;
- exercises registries, capabilities, events, networking and modern Java bytecode;
- IC2 Classic provides a broad external integration target.

### P1 — Forge 1.16.5

Preferred canonical profile for the 1.16 generation.

A family MAY include nearby 1.16.x releases through version-specific mapping/ABI deltas. 1.16.4 SHOULD NOT become a separate full profile merely because a mod targets that patch version.

Expected reusable infrastructure:

- managed class loading;
- transformation engine;
- safe frame computation;
- semantic registry engine;
- event infrastructure;
- parts of capability/networking compatibility.

Expected source-specific work includes MCP/SRG/Mojang mapping differences and older vanilla APIs.

### P1 — Forge 1.12.2

A major classic Forge generation and an important proof that LMS is not limited to modern Forge.

This profile is expected to require substantial adapters for:

- older registry/GameRegistry patterns;
- TileEntity-era APIs;
- OreDictionary/tag migration;
- recipes;
- rendering/model APIs;
- SimpleNetworkWrapper;
- older capability and item/block contracts.

### P2 — Forge 1.20.1

A later Forge generation useful for broad coverage near the Forge/NeoForge transition.

It is expected to share more target-independent semantic operations with 1.19.2 than 1.12.2 does.

### P2 — Forge 1.7.10

A high-value but high-cost classic generation.

This profile is expected to require major compatibility work for:

- FML/GameRegistry;
- pre-modern block/item APIs;
- TileEntity;
- OreDictionary;
- old rendering/IIcon-era concepts;
- world generation;
- SimpleNetworkWrapper;
- coremods/ASM patches.

Support for legacy coremods MUST NOT be claimed until their transformation semantics are explicitly handled.

### Later/optional — Forge 1.6.4 and other generations

Older or intermediate versions MAY be added based on demand and evidence.

The project SHOULD prefer a small number of canonical profile families with explicit deltas over one duplicated profile for every Minecraft patch release.

## Profile families

A profile family MAY contain version-specific mapping and ABI variants.

Conceptually:

```text
Forge116Family
  |- 1.16.1 mappings/deltas
  |- 1.16.2 mappings/deltas
  |- 1.16.3 mappings/deltas
  |- 1.16.4 mappings/deltas
  '- 1.16.5 canonical behavior
```

Shared behavior MUST live at the family/common semantic layer.

A delta MUST be selected by evidence from the artifact descriptor, not by guessing.

## Source-to-semantic boundary

Profiles SHOULD increasingly translate source operations into target-independent semantic operations rather than directly manufacturing one target runtime call.

Direct rewrites remain acceptable where source and target identity is proven and the abstraction would add no value.

## Cross-profile reuse

Common concepts such as registration, event subscription, capability access and network message registration SHOULD converge on shared runtime services when their semantics can be normalized safely.

Profiles MUST NOT share an adapter merely because two APIs have similar names.

## Reference mods

Reference mods MAY be assigned per source generation.

Reference-mod identity MUST NOT be used as a generic transformation selector.

Any unavoidable mod-specific compatibility MUST live outside the generic source profile and be documented separately.

## Implementation status

Only profiles actually implemented and verified by tests/runtime evidence may be reported as supported.

Mentioning a generation in this specification reserves architectural scope; it does not imply compatibility.
