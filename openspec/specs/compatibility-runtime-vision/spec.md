# Compatibility Runtime Vision Specification

## Status

This document defines the long-term architectural direction of LMS.

It does NOT claim that all described source loaders, target loaders, Minecraft generations, compatibility domains or semantic operations are implemented.

The current executable MVP remains Forge 1.19.2 legacy binaries on Minecraft 26.3 / NeoForge 26.3.x.

## Product direction

LMS is intended to evolve from a single compatibility bridge into a **Legacy Minecraft Mod Compatibility Runtime**.

A concise long-term definition is:

> LMS is a binary compatibility runtime for executing legacy Minecraft mods across Minecraft versions and mod-loader generations without requiring modification of the original mod artifacts.

The runtime SHOULD interpret legacy API/ABI interactions as semantic operations and adapt those operations to the active target runtime rather than permanently translating one specific historical API directly into one specific modern API.

## Compatibility dimensions

A legacy mod depends on at least two conceptually separate compatibility surfaces:

1. **Minecraft source ABI/API**
   - blocks/items/entities;
   - world/level APIs;
   - block entities/tile entities;
   - attributes;
   - components/NBT/data;
   - recipes/tags;
   - rendering;
   - world generation;
   - resource identifiers;
   - vanilla constructors and invariants.

2. **Loader/platform source ABI/API**
   - Forge/FML, Fabric or another supported loader;
   - lifecycle;
   - registries;
   - event systems;
   - capabilities/services;
   - networking;
   - loader-specific rendering/resource hooks;
   - metadata and dependency model.

These surfaces SHOULD be modeled separately when doing so allows reuse.

For example, a removed Minecraft 1.19.2 `AxeItem` contract is primarily Minecraft-generation compatibility and may be reusable for both Forge and Fabric mods compiled against that Minecraft generation.

## Conceptual pipeline

```text
Legacy mod binary
      |
      +--> Minecraft source-generation profile
      |
      +--> Loader source profile
                |
                v
        semantic compatibility layer
                |
        +-------+--------+
        |       |        |
     Registry Events  Network ...
        |       |        |
        +-------+--------+
                |
                v
        Minecraft target adapter
                +
         Loader target adapter
                |
                v
        modern Minecraft runtime
```

The implementation MAY combine layers where separation has no practical value, but dependencies SHOULD flow in this direction.

## LMS semantic model / IR

LMS MAY introduce an explicit intermediate representation (IR) for compatibility operations.

An IR is not required to be a new bytecode format or interpreter.

Initially it MAY consist of:

- typed Java interfaces;
- transformation-plan records;
- runtime bridge contracts;
- normalized event/registry/network operations;
- semantic descriptors used by source profiles and target adapters.

The important property is target independence.

Example:

```text
Forge 1.19.2:
    IForgeRegistry.register(...)

Fabric:
    Registry.register(...)

                 |
                 v

REGISTER_OBJECT {
    registry
    identifier
    value
}

                 |
                 v

Target NeoForge adapter:
    execute during valid RegisterEvent phase
```

## IR growth rule

LMS MUST NOT design a speculative giant IR before runtime evidence exists.

Semantic operations SHOULD emerge incrementally from proven compatibility work.

Preferred evolution:

```text
real legacy behavior
    -> first working adapter
    -> repeated/related use confirms semantics
    -> semantic contract extracted
    -> additional source/target adapters reuse contract
```

Do not invent hundreds of operations solely for architectural completeness.

A direct verified rewrite is acceptable until an abstraction provides demonstrated reuse or isolation value.

## Candidate semantic operation families

The following names are architectural examples, not a frozen API:

- REGISTER_OBJECT
- STAGE_REGISTRATION
- LOOKUP_REGISTRY_VALUE
- RESOLVE_IDENTIFIER
- SUBSCRIBE_EVENT
- MAP_EVENT_PHASE
- REGISTER_CAPABILITY
- QUERY_SERVICE
- REGISTER_NETWORK_MESSAGE
- SEND_NETWORK_MESSAGE
- CREATE_ITEM
- CREATE_BLOCK_ITEM
- CREATE_BLOCK_ENTITY_TYPE
- CREATE_ENTITY_TYPE
- MAP_ATTRIBUTE
- BUILD_CREATIVE_TAB_CONTENT
- PRESERVE_CREATIVE_TAB_INTENT

Operation names and payloads MUST be validated by implementation evidence before becoming stable contracts.

## Loader-neutral services

Where semantics can be normalized safely, the runtime SHOULD expose loader-neutral concepts.

Examples:

### Registry

```text
Forge registration ---+
Fabric registration --+--> REGISTER_OBJECT --> target registry adapter
```

### Events

```text
Forge PlayerTickEvent ----+
Fabric player tick callback+--> PLAYER_TICK/PRE|POST --> target event adapter
```

Only equivalent event timing and cancellation semantics may share a normalized event.

### Capabilities/services

Forge capabilities, Fabric transfer/storage APIs and future loader service models MAY map into semantic service contracts such as:

- ITEM_STORAGE
- FLUID_STORAGE
- ENERGY_STORAGE

Such normalization MUST preserve direction/side, lifetime, invalidation and mutability semantics.

Similar names are not sufficient evidence.

### Networking

Legacy networking APIs MAY normalize message registration/delivery concepts including:

- protocol/channel identity;
- message type;
- encoder;
- decoder;
- direction;
- handler;
- thread/execution policy.

Old SimpleNetworkWrapper/SimpleChannel and modern payload APIs MUST still receive explicit semantic adapters.

## Fabric

Fabric support is a future architectural possibility, not current scope.

If implemented, Fabric SHOULD normally be represented as another loader source profile rather than being translated into Forge first.

Preferred:

```text
Forge source --+
Fabric source -+--> LMS semantic runtime --> target adapter
```

Avoid:

```text
Fabric -> fake Forge -> Forge compatibility -> target
```

unless evidence later demonstrates that such an intermediate representation is materially safer.

Fabric support is expected to require substantial independent work for:

- ModInitializer/ClientModInitializer lifecycle;
- Fabric registries/events;
- Fabric networking;
- Fabric rendering APIs;
- Fabric-specific dependencies;
- Mixins.

Legacy Mixins are a major semantic migration problem. LMS MUST NOT claim Fabric compatibility merely because Fabric entrypoints and registry calls link.

## Source-loader and target-loader independence

Long term, source and target loader identity MAY both be variable.

Conceptually:

```text
                    Sources
       Forge 1.12 / Forge 1.19 / Fabric 1.19
                         |
                         v
                    LMS Runtime
                         |
              +----------+----------+
              |                     |
          NeoForge target        Fabric target
```

Supporting a Fabric target is not required by the current product roadmap.

The architecture SHOULD avoid unnecessary assumptions that make such a target impossible, but current implementation quality for NeoForge MUST take priority over speculative target neutrality.

## Avoiding translator-matrix explosion

Without semantic boundaries, N source generations multiplied by M target generations create up to N*M direct translation relationships.

With useful source normalization and target adapters, the maintenance goal becomes closer to:

```text
N source adapters + M target adapters + shared semantic contracts
```

This is a design objective, not a mathematical guarantee. Some compatibility operations will remain source-target-specific when semantics cannot be normalized safely.

## Compatibility domains

A future LMS MAY isolate incompatible legacy dependency graphs into compatibility domains.

Example:

```text
Domain A
  Forge 1.12 profile
  Mod A
  Library X 1.12

Domain B
  Forge 1.19 profile
  Mod B
  Library X 1.19
```

Domains could share the modern target through LMS semantic services while retaining separate managed classloading/dependency namespaces.

Compatibility domains are research scope.

The current managed child-first classloading work is relevant foundation, but it does NOT prove that multiple legacy Minecraft generations can coexist safely in one game process.

## Multi-generation coexistence

Running mods built for different legacy Minecraft generations simultaneously is a possible long-term research goal:

```text
mod-A (1.12.2) --+
mod-B (1.16.5) --+--> LMS Runtime --> modern target
mod-C (1.19.2) --+
```

LMS MUST NOT claim this capability until interaction between domains is explicitly designed and tested.

Important unresolved areas include:

- mods calling each other's APIs;
- duplicate/incompatible library versions;
- shared registries;
- serialization and save data;
- networking;
- event ordering;
- cross-domain object identity;
- mixins/coremods targeting shared Minecraft classes.

## Binary preservation

The original legacy mod artifact remains immutable.

The compatibility runtime MAY:

- transform classes in memory;
- produce derived/cache artifacts;
- provide runtime facades;
- adapt lifecycle and services.

It MUST NOT require modification of the original JAR as the normal compatibility model.

## Relationship to Wine/Rosetta-style systems

LMS MAY be described conceptually as an application compatibility runtime:

```text
legacy Minecraft mod API/ABI
            |
            v
       LMS Runtime
            |
            v
modern Minecraft mod API/ABI
```

Comparisons with Wine, Rosetta or similar systems are architectural analogies only.

LMS does not claim equivalent implementation, completeness, isolation or security properties.

## Architectural value accumulation

A mature semantic runtime SHOULD make new compatibility work compositional:

- adding a source profile expands the legacy ecosystem that can use existing target adapters;
- adding a target adapter gives existing source profiles a path to a new runtime;
- improving a shared semantic subsystem benefits multiple source/target combinations.

This compositional effect is a core long-term reason for separating source profiles, semantic operations and target adapters.

## Current MVP boundary

The project MUST resist scope explosion.

Before expanding to Fabric or multiple historical Forge generations, the current vertical slice SHOULD demonstrate:

1. Forge 1.19.2 artifact discovery;
2. managed binary loading/transformation;
3. lifecycle and registration;
4. main-menu startup;
5. fresh world creation/load;
6. meaningful functional behavior in multiple independent Forge 1.19.2 mods;
7. absence of hidden IC2-specific behavior in generic compatibility code.

The first successful world load is a milestone, not proof of full IC2 compatibility.

## Expansion gates

### Additional Forge source generation

A second Forge generation SHOULD be attempted after the Forge 1.19.2 MVP demonstrates sufficient generality.

Forge 1.16.5 is the preferred next source-generation experiment.

### Additional target generation

A future target (for example 27.3) SHOULD validate that target adapters can evolve without reimplementing all source profiles.

### Fabric source profile

Fabric SHOULD be considered only after:

- Minecraft-generation compatibility has meaningful separation from Forge-specific compatibility;
- semantic registry/event/network boundaries have proven useful;
- at least one multi-source or multi-target architectural assumption has been validated in practice.

## Success criterion

The long-term success of the Compatibility Runtime is not that one legacy mod reaches the main menu.

Success means that additional independent legacy mods and generations require progressively less mod-specific work because they reuse verified compatibility semantics.
