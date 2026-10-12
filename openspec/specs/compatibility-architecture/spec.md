# Compatibility Architecture Specification

## Purpose

LMS MUST be designed as a compatibility engine across two independent version axes:

1. the **legacy source generation** for which a mod binary was built; and
2. the **modern target runtime generation** on which LMS executes it.

The implementation MUST NOT collapse those axes into a permanent Forge 1.19.2 -> Minecraft 26.3 translation.

## Normative architecture

The long-term compatibility pipeline is:

```text
Legacy artifact
    |
    v
Discovery + evidence
    |
    v
Source profile
    |
    v
Semantic compatibility operations / normalized legacy intent
    |
    v
Target adapter
    |
    v
Target Minecraft + NeoForge runtime
```

The semantic layer MAY initially be represented by typed bridge APIs and transformation plans rather than a serialized intermediate representation. The architecture MUST nevertheless preserve the boundary so that a later explicit LMS IR can be introduced without rewriting every source profile.

## Source profiles

A source profile owns facts specific to a legacy generation, including:

- metadata and version detection;
- source mappings and symbol identity;
- legacy class/member ABI;
- lifecycle shape;
- source-side event contracts;
- source-side registry contracts;
- source-side capability and networking contracts;
- transformation rules required to recover semantic intent.

A source profile MUST NOT encode assumptions that only hold for one modern target release when the operation can be represented semantically.

Example:

```text
Forge 1.19.2 RegisterEvent/IForgeRegistry operation
    -> REGISTER_OBJECT(registry-key, identifier, value)
```

is preferred over embedding a Minecraft 26.3 registration implementation throughout the Forge 1.19.2 transformer.

## Semantic compatibility operations

Where practical, repeated compatibility families SHOULD converge on target-independent semantic operations.

Candidate operation families include:

- REGISTER_OBJECT
- LOOKUP_REGISTRY_VALUE
- SUBSCRIBE_EVENT
- REGISTER_CAPABILITY
- QUERY_CAPABILITY
- REGISTER_NETWORK_MESSAGE
- SEND_NETWORK_MESSAGE
- BUILD_CREATIVE_TAB_CONTENT
- CREATE_BLOCK_ENTITY_TYPE
- CREATE_ENTITY_TYPE
- RESOLVE_RESOURCE_IDENTIFIER
- APPLY_LEGACY_ITEM_PROPERTIES

Semantic operations MUST preserve meaningful source behavior. They MUST NOT exist merely to hide linkage failures.

An operation whose semantics cannot be represented safely MUST remain an explicit compatibility gap.

## Target adapters

A target adapter owns behavior specific to a tested modern Minecraft/NeoForge generation.

For the initial implementation this is Minecraft 26.3 / NeoForge 26.3.x.

Target adapters SHOULD contain:

- current registry/lifecycle execution;
- current event registration and event-model adaptation;
- current capability implementation;
- current networking/payload implementation;
- current rendering integration;
- target constructors and invariants;
- target mappings/symbol resolution;
- target-specific classloading integration where required.

Direct target-runtime API calls SHOULD be concentrated behind target adapters or target-aware bridge implementations when doing so prevents source profiles from becoming coupled to one target generation.

When a source lifecycle callback is internally ordered but the target makes a
required invariant available only at a later phase, LMS MUST preserve the
callback as one ordered lifecycle unit. Splitting individual calls across the
phase boundary is forbidden when later calls can observe state produced by an
earlier deferred call. Whole-callback deferral is permitted only with evidence
that the source event parameter is not consumed after its original phase;
otherwise the unsupported lifecycle migration MUST fail closed.

## Generic compatibility requirement

Reference mods are integration tests, not architectural namespaces.

Production compatibility logic MUST NOT be selected by:

- mod id such as `ic2`;
- package prefix such as `ic2/`;
- exact reference-mod class name;
- CarbonConfig class/package identity;

unless the behavior is truly mod-specific and intentionally implemented outside LMS core/profile compatibility.

A generic transformation MUST instead be justified by evidence such as:

- legacy owner/member identity;
- verified mapping identity;
- descriptor and inheritance relationship;
- bytecode shape;
- target runtime ABI;
- documented or source-verified semantics.

Descriptor coincidence alone MUST NOT be treated as semantic proof.

## Mod-specific compatibility

LMS core MAY expose an extension mechanism for unavoidable mod-specific compatibility in the future.

Such compatibility MUST:

- be isolated from generic source profiles;
- be explicitly identified as mod-specific;
- never silently alter generic Forge behavior;
- have separate compatibility claims/tests.

IC2 Classic MUST NOT acquire hidden special cases in the generic Forge 1.19.2 profile.

## Fail-fast policy

LMS MUST prefer deterministic failure to false compatibility.

It MUST NOT make startup succeed by:

- swallowing arbitrary exceptions;
- returning fabricated/null values where semantics matter;
- skipping listeners or registrations;
- silently removing meaningful behavior;
- mutating frozen/global registries outside valid lifecycle phases;
- claiming a subsystem is supported when only linkage is supported.

A deliberate no-op is permitted only when the removed source operation has no target-runtime semantic effect required to preserve behavior. The reason SHOULD be documented and tested.

## Compatibility levels

Compatibility claims MUST distinguish at least:

0. LMS compiles.
1. Target runtime loads LMS.
2. Legacy classes link and construction begins.
3. Legacy mod construction/lifecycle completes.
4. Legacy registration completes.
5. Target reaches the main menu with the legacy mod loaded.
6. A world can be created/loaded.
7. Basic legacy content operates without immediate compatibility failure.
8. Major functional systems operate sufficiently for meaningful use.

Passing a lower level MUST NOT be described as passing a higher level.

## Functional completeness versus linkage

LMS MUST distinguish:

- **structural/linkage compatibility**: bytecode verifies and required types/members resolve;
- **lifecycle compatibility**: callbacks execute in valid target phases;
- **functional compatibility**: observable legacy behavior is preserved.

For example, adapting a removed creative-tab property so item construction succeeds does not by itself prove that the item appears in the appropriate modern creative tab.

## Reference-mod strategy

IC2 Classic 1.19.2 is the first broad integration target because it exercises many Forge/Minecraft subsystems.

Every compatibility family discovered through IC2 SHOULD be generalized and, where practical, covered by a focused synthetic fixture so future testing does not depend exclusively on IC2.

The original IC2 artifact MUST remain external and unmodified.
