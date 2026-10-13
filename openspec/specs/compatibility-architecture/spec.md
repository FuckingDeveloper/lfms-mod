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

When a source lifecycle registers behavior that the target consumes lazily,
LMS MAY preserve the complete source intent and defer an unsupported-operation
failure until the first matching target consumption. Such deferral MUST retain
the source objects and selection scope, MUST remain explicitly diagnosed as a
functional gap, and MUST fail closed when consumed; it MUST NOT discard the
registration merely to let startup continue.

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

When a source registry exposes stable tag-manager and tag objects whose
contents are rebound across data reloads, LMS MUST preserve the stable source
object identity while resolving each operation against the target registry's
current tag binding. An unknown source tag MUST remain an empty, unbound view;
LMS MUST NOT manufacture registry entries or snapshot stale holder contents.

When a source lifecycle callback is internally ordered but the target makes a
required invariant available only at a later phase, LMS MUST preserve the
callback as one ordered lifecycle unit. Splitting individual calls across the
phase boundary is forbidden when later calls can observe state produced by an
earlier deferred call. Whole-callback deferral is permitted only with evidence
that the source event parameter is not consumed after its original phase;
otherwise the unsupported lifecycle migration MUST fail closed.

When such a callback requires a source-era value before the target binds its
global default-component state, a target adapter MAY materialize an isolated
value from the exact preserved construction intent and target component
initializers. It MUST NOT bind or mutate global holder state early, fabricate
empty defaults, or continue when the complete value cannot be materialized.

When a read-only source capability query is valid during that pre-binding
window, the target adapter MAY present the isolated value through a read-only
target capability context. The context MUST expose the exact materialized
components and count, MUST NOT derive a default resource from unbound global
holder state, and MUST reject mutation. A missing provider or genuinely empty
handler may map to the source's empty result; failure to materialize the value
MUST NOT be reported as an empty capability result.

When that same ordered callback also contributes target component initializers,
the target adapter MAY execute it after dynamic registries and tags are loaded
but before the target applies its pending component set. Any newly contributed
initializers MUST be incorporated by rebuilding that still-pending set as one
atomic lifecycle operation. LMS MUST NOT mutate already-bound holder defaults
or split the source callback around the binding boundary.

When a source generation exposes a live mutable arbitrary-NBT view of an item
stack but the target stores that payload in a data component, the target
adapter MUST preserve mutations made through the returned source view and MUST
store them in the target's serializable custom-data component. Returning a
detached copy from a source mutation API is forbidden. The adapter MUST NOT
reinterpret component-owned target data as arbitrary legacy NBT.

When source-generation saved data is registered by a runtime loader function,
constructor and global string key, while the target requires a typed saved-data
descriptor and codec, LMS MAY synthesize that target descriptor from the exact
source operations. The bridge MUST use the source loader for decoding, the
source saved-data writer for encoding, and the source constructor for absent
data. It MUST retain the source string key's global collision semantics and
MUST fail explicitly if a produced value does not expose the verified source
saved-data writer contract. Existing on-disk path migration, when source and
target storage layouts differ, remains a separate compatibility operation and
MUST NOT be claimed from codec linkage alone.

When a source client API exposes the complete recipe manager but the target
only synchronizes a reduced client recipe view, LMS MAY satisfy the source
contract from the authoritative co-resident integrated server recipe manager.
It MUST fail explicitly when no co-resident server exists and MUST NOT claim
remote-client recipe compatibility from this singleplayer bridge.

When a source render-stage enum is replaced by target event subclasses, LMS
MAY expose source stages only for target events whose ordering point is
semantically established. Target-only stages MUST NOT be invented as source
stages. If several source chunk-layer stages collapse into one target stage,
the adapter MUST identify the last completed source-equivalent stage and
document that earlier intermediate callbacks are unavailable.

When a source render event exposes the rendered entity but the target event
retains only an extracted render state, LMS MAY recover the source entity from
the target client level only when target-authoritative type, interpolation
time, position, and extracted entity-state properties identify exactly one
entity. Missing or ambiguous matches MUST fail explicitly; an arbitrary nearby
entity MUST NOT be substituted.

When adapting a source entity-valued render-layer callback to a target
render-state submission callback, LMS MAY reconstruct source animation inputs
only from the uniquely matched entity and target-authoritative render-state
fields. Immediate source geometry MUST be recorded and submitted through the
target collector; unsupported source render types or ambiguous entity matches
MUST remain explicit failures rather than silently dropping the layer.

When source code compares a removed static damage-source singleton by identity
and the target models that identity as a registry-backed damage-type key, LMS
MAY replace the exact comparison with the target damage-source key predicate.
The source SRG/Mojang field identity and the unique target key declaration MUST
both be verified; arbitrary DamageSource object equality MUST NOT be rewritten.

When a source client debug option moves from a renderer-owned toggle to a
named target debug-entry model, LMS MAY bridge the exact source toggle to the
verified target entry. The bridge MUST preserve both the state transition and
the returned post-transition value; it MUST NOT replace the operation with a
constant merely because the option is diagnostic.

When a source generation exposes one combined baked-model contract but the
target splits block-state and item rendering, LMS MUST preserve the legacy
registration lifecycle and adapt the model independently into each target
pipeline. A target mutable baking result MAY be captured before the legacy
callback and mutated at the source-equivalent later phase when the target
retains those map identities. LMS MUST NOT cast or namespace-rename the legacy
combined model into either unrelated target model interface. Unsupported
legacy rendering operations MUST remain explicit fail-closed boundaries.

When a source render-type class combines chunk-layer identity with immediate
render-pipeline state but the target separates those concepts, LMS MUST retain
source chunk layers as semantic identities until the model adapter selects the
target chunk/material representation. It MUST NOT substitute an unrelated
target render pipeline merely because both APIs use a class named RenderType.

When the source renderer pre-registers render types in a mutable fixed-buffer
map that the target no longer exposes, LMS MAY capture the exact registration
intent in an LMS-owned target adapter. It MUST NOT reflectively mutate a target
renderer's private buffers or treat captured intent as proof that the target
render pipeline supports that type. Attempted functional use MUST remain
fail-closed until a verified target rendering operation consumes the intent.

When a source renderer separates CPU geometry construction from upload into a
persistent GPU vertex buffer that the target removed, LMS MAY preserve the
completed CPU-cache lifecycle without performing the obsolete upload. A later
draw of that cache MUST fail explicitly until a verified target render-pass or
submission adapter consumes the preserved geometry; construction compatibility
MUST NOT be reported as functional rendering compatibility.

Managed legacy artifacts remain immutable inputs, but their client and server
resources MUST be exposed through the target generation's resource-pack
lifecycle when that artifact is admitted to a runtime session. The target
adapter MAY provide target-current pack metadata instead of trusting obsolete
legacy `pack.mcmeta` compatibility declarations. That metadata adaptation MUST
NOT rewrite the artifact or reinterpret resource contents; parsing and semantic
migration of individual resource families remain separate, fail-closed
compatibility operations.

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
