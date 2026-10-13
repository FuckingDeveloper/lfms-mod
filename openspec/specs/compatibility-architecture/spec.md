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

If such whole-callback deferral moves source setup past the target's initial
client-resource reload, and source code already contributed a client reload
listener whose state is populated by that setup, LMS MUST request a subsequent
ordinary target client reload after all deferred callbacks for that data-load
dispatch complete. LMS MUST use the target reload machinery so listener
ordering, preparation barriers, application threading, and failure handling
remain authoritative; it MUST NOT invoke an arbitrary listener callback
directly or special-case the state that one integration mod happens to build.

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

When the target changes source `CompoundTag` reads to optional-valued results,
LMS MUST preserve the source generation's type-specific default values and
numeric-tag matching rules. Source key enumeration MUST expose the target
compound's actual key-set view rather than a detached or filtered collection.

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
retains only an extracted render state, a target adapter SHOULD capture the
exact source-entity/render-state pair at the target state-extraction boundary
when that boundary exposes both values. If that exact association is
unavailable, LMS MAY recover the source entity from the target client level
only when target-authoritative type, interpolation time, position, and
extracted entity-state properties identify exactly one entity. Missing or
ambiguous matches MUST fail explicitly; an arbitrary nearby entity MUST NOT be
substituted.

When a source player-interaction event expresses its block/item-use decision
with Forge's three-valued `Event.Result`, a target adapter MAY translate that
decision to a target three-state type only after proving the exact event
owner, accessor/mutator member identity, and value semantics. Source
`ALLOW`, `DENY`, and `DEFAULT` MUST retain their force-allow, force-deny, and
defer-to-normal-processing meanings respectively. LMS MUST rewrite both the
method descriptor and the corresponding enum constants as one compatibility
family; it MUST NOT generalize this conversion from an enum descriptor alone.

When a source Mixin interface exposes an accessor or invoker for exactly one
source target class, LMS MAY replace the interface cast and invocation with a
target-runtime adapter only when the Mixin annotation, accessor/invoker
annotation, member descriptor, mappings, and actual target implementation
prove the same member identity and semantics. Any required target access
widening MUST name that verified member explicitly. LMS MUST NOT apply this
migration from the interface method descriptor alone, guess a target from the
Mixin interface name, or silently skip an accessor/invoker whose target
equivalence is unproven.

When a source Mixin accessor names client sound-engine fields that retain the
same identity, type, and purpose in the target, LMS MAY replace casts and
accessor calls with direct target field reads after verifying both source
annotations and target fields. The adapter MUST preserve access to the target
sound manager, channel access, and sound-buffer library; it MUST NOT skip
source audio playback when these fields are unavailable.

Forge 1.19.2 Blaze3D `Channel` SRG calls MAY be renamed to target methods
only when the exact source owner, name, and descriptor resolve to a public
target method with the same channel-control semantics. The source profile
MUST cover the verified channel-control family rather than only the first
failing call. Unknown or changed channel operations MUST remain unresolved;
an audio executor failure MUST NOT be hidden as successful playback.

For legacy block-state occlusion queries, LMS MAY adapt source
`isSolidRender(BlockGetter, BlockPos)` to the target's cached state-only
predicate when source and target
both use a position-independent occlusion shape. The full-cube predicate and
the `canOcclude` decision MUST be preserved. A position-dependent shape or
unmigrated legacy block occlusion override MUST fail explicitly until its
source world/position semantics can be preserved; LMS MUST NOT silently
discard those inputs for such a block. A direct source `getOcclusionShape`
query MUST NOT be treated as equivalent to the target cache solely because
the returned types match: the source query can return a shape even when
`canOcclude` is false, while the target cache stores an empty shape then.

When a source public field survives with the same identity, type, ownership,
and lifecycle but becomes non-public in the target, the target adapter MAY
explicitly restore the source visibility for that exact verified member.
Final-field invariants MUST remain intact. A direct field read MUST NOT become
a virtual accessor invocation if an override could select a different value;
source object identity and state lifetime MUST be preserved.

When a source level sound operation excludes a nullable player from hearing a
sound and the target widens that exclusion parameter to any entity, LMS MAY
adapt the exact mapped level member to the widened target descriptor. The
remaining position, sound, category, volume, and pitch operands MUST be
preserved unchanged; descriptor similarity on an unrelated owner is not
sufficient evidence for this migration.

When a source interaction-result enum becomes a sealed target result family,
LMS MAY adapt exact source constant field identities to their corresponding
target concrete result types. The source result meaning (success with swing,
consume without swing, pass, or fail) MUST be preserved. A source constant
whose target semantics are not proven MUST remain unresolved rather than being
collapsed into a superficially similar result.

When a source placed-block advancement trigger is replaced by a generalized
item-used-on-location trigger, LMS MAY migrate the exact built-in trigger field
and invocation together when the target retains the same player, position, and
item observation. Widening the item operand from a source stack to a target
item-instance interface is valid only when the source stack implements that
interface; arbitrary advancement triggers MUST NOT be redirected by descriptor
similarity.

When a source block-state clone/pick operation carries hit, world, position,
and player context but the target no longer represents hit context, LMS MAY
adapt the exact source extension member to the target player-sensitive clone
operation. The world MUST satisfy the target level-reader contract, the player
and position MUST be preserved, and source behavior MUST NOT be replaced with
an arbitrary registry lookup. The removed hit value MAY be discarded only
because the target operation has no equivalent input.

When a source concrete block-state identity check moves to a target inherited
typed-instance contract, LMS MAY adapt the exact source owner, member, block
operand, and boolean result together. It MUST preserve registry object identity
and MUST NOT replace the check with class, identifier-string, or descriptor-only
comparison.

When a target version renames a built-in block identity while retaining the
same concrete gameplay role, LMS MAY map the exact source SRG/Mojang field to
the exact target declaration only with verified source and target registry
evidence. Similar field types or names alone are insufficient.

When a target class rename also changes construction or instance behavior, LMS
MUST NOT globally relocate the source class merely to resolve a verified static
member. It MAY migrate an exact source static field to the corresponding target
field while leaving unproven constructors, inheritance, and instance operations
fail-closed.

When a source chat click event stores an action and string but the target uses
action-specific value types, LMS MUST construct the exact target event type
for values representable by the target and MUST preserve source action/value
accessors. A source screen's non-numeric `CHANGE_PAGE` navigation token MAY be
preserved as a local compatibility value only for source callbacks that consume
that token themselves; LMS MUST NOT present it as a functional target book-page
event.

When a source hover event stores an action and erased value but the target uses
action-specific records, LMS MUST validate the source value against the exact
action contract and construct the corresponding target text, item, or entity
hover event. Source item-stack hover carriers MAY be represented by an immutable
target item-stack template, but source reads MUST reconstruct an equivalent
stack value rather than exposing the target carrier type.

When the target removes the source chat-format lookup by normalized display
name, LMS MUST preserve its nullable result and source normalization rule. It
MUST NOT substitute enum `valueOf`, whose case and separator semantics differ.

When a source item-stack predicate tests specifically for the explicit custom
display-name tag, LMS MUST test the target component that represents that same
explicit naming intent. It MUST NOT substitute a broader target custom-name
accessor whose result also includes unrelated item families such as written
book titles.

When source bytecode uses the removed position-independent biome precipitation
enum only to compare against `NONE`, LMS MAY replace that exact comparison with
the target biome precipitation-presence predicate. Uses that distinguish rain
from snow MUST remain fail-closed unless the source call site also provides the
position and sea-level context required by the target API.

When a source coordinate value type exposes convenience constructors and
public coordinate fields that the target replaces with exact factories and
record accessors, LMS MAY migrate those operations as one value-contract
family. The target factory and accessors MUST preserve the source coordinate
conversion and value semantics; descriptor similarity alone is insufficient.

When a source chunk dirty-state setter is replaced by target mark/save
transitions, LMS MAY migrate the exact constant-true operation to the target
dirty-mark operation. The target-required dirty-listener notification MUST be
preserved. Source calls that can clear dirty state MUST remain unresolved until
their target transition semantics are proven separately.

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

When a translated source coremod injects a call to a hook owned by a managed
legacy artifact into target-owned bytecode, the target call site MUST NOT
directly reference the managed owner. LMS MUST route the call through an
LMS-owned target-visible linkage point bound to the exact admitted runtime
session and exact hook owner, name, descriptor, and invocation kind. Linkage
MUST preserve the hook's argument, return, and exception semantics and MUST
fail explicitly when the binding is absent, ambiguous, or type-incompatible.
Descriptor coincidence alone MUST NOT select a hook or runtime session.

When a source static registry identity is exposed directly as a value but the
target exposes the same mapped identity as a registry holder, LMS MAY migrate
the field access to that holder when both the exact source SRG/Mojang identity
and one exact target holder declaration are verified. Downstream member
migrations MUST consume the holder according to the target ABI; LMS MUST NOT
unwrap or substitute a different registry entry merely to satisfy a descriptor.

When the target item constructor requires its registry identity before a
legacy block-item subclass stores that identity, LMS MUST prefer an explicit
source item-name operation proven in that constructor over the paired block's
identifier. The block identifier is authoritative only when the source proves
that the item derives its name from the block. Source `registerBlocks`
overrides MUST remain authoritative for the block-to-item association; LMS
MUST NOT eagerly install the base association for a subclass that overrides
that operation.

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

When a source texture-atlas sprite exposes its own resource identity directly
but the target moves that identity onto the sprite contents, LMS MUST recover
the identity from those exact contents. The containing atlas identity MUST NOT
be substituted merely because it remains directly accessible from the target
sprite.

When a source entity renderer selects model or texture identity from the live
entity but the target extracts immutable render state before submission, LMS
MAY preserve the exact source override by capturing its result during target
state extraction and consuming it during target submission. Such adaptation
MUST be gated by the verified source superclass, override descriptor, and
constructor shape; it MUST NOT choose a fixed target model or texture merely
to make renderer construction succeed.

When a target replaces a source normalized color vector with a packed color,
LMS MAY reconstruct the source value shape only from the exact target semantic
color member and its documented channel encoding. It MUST preserve channel
order and normalization and MUST NOT reinterpret an unrelated packed color
merely because it has the same primitive type.

When a source operation consumes a process-global enchantment value and the
target represents the same exact enchantment identity as a data-driven
resource key, LMS MUST resolve that key through the active registration,
server, or client-world registry at the consuming operation. LMS MUST fail
explicitly when no authoritative registry context exists; it MUST NOT cache or
fabricate a process-global enchantment instance.

When the target snapshots or derives its block-state dispatch before the
source-equivalent completion phase, LMS MAY execute a legacy baked-model
registration callback during the target's final mutable baking phase. This
timing migration is permitted only when the exposed source event view is
restricted to registration operations backed by that exact mutable result and
the callback's other transformed operations are valid in the target phase.
Source event operations that require the completed model manager or other
later state MUST remain fail-closed. LMS MUST NOT mutate a completed target
model manager reflectively or claim that a late mutation of a detached baking
map updates the target dispatch cache.

When a source item-model shaper binds a baked model to an item object but the
target renderer resolves models through an item data component, LMS MUST
publish the migrated model under the source binding's explicit model identity
and, when distinct, the exact target construction model identity of that item.
The item's registry identifier remains authoritative for validating the source
item identity; it MUST NOT replace the explicit source model identity merely
because older generations commonly used one identifier for both. If the target
has not yet bound global holder components during the source-equivalent
model-registration phase, LMS MUST derive its construction model identity from
the exact preserved item properties or the proven target default; it MUST NOT
force early global binding or read incomplete holder state.

When a source baked block model consumes position-specific model data and the
target exposes that data through its dynamic block-state model collection
contract, LMS MUST pass the target-provided model-data snapshot for the exact
render position into the source quad operation. LMS MUST NOT substitute empty
model data for positioned block rendering or query a live block entity from a
meshing worker to reconstruct the snapshot.

When the source render-shape enum distinguishes block-entity-only rendering
from fully invisible blocks but the target chunk compiler handles block
entities independently, LMS MAY map the exact source block-entity-only enum
constant to the target no-model shape. This mapping MUST preserve independent
block-entity discovery/submission and MUST NOT be inferred from enum ordinal or
from another removed constant with unverified rendering semantics.

When a source Forge event-factory hook survives in the target under a different
owner with the same event construction, dispatch, cancellation, arguments and
return contract, LMS MAY migrate that exact owner/member/descriptor identity.
The rewrite MUST match the source owner directly even when fail-closed namespace
projection intentionally leaves the removed owner unchanged.

When a source generation registers procedural block or item color handlers but
the target consumes packed tint values during block tessellation or item render-
state extraction, an LMS-owned legacy model adapter MAY evaluate the preserved
source handler at that target consumption point. It MUST preserve the source
quad tint index and source default-white result, convert source RGB values to
the target's opaque packed-ARGB representation, and include dynamic item tint
values in the target model identity when the target cache contract requires it.
When the target requires a finite data-defined block tint-layer list but the
source callback accepts arbitrary quad tint indices, the LMS-owned block model
adapter MUST evaluate each actual tinted quad with its exact world and position
context, multiply that color into the copied target quad, and clear the copied
quad's target tint index. It MUST NOT represent an unbounded source callback as
an arbitrary or effectively infinite target layer list.
This adapter MUST NOT be claimed as general target data-driven tint-source
registration for target-owned models.

When a source generation constructs custom creative tabs as process-global
objects and assigns item membership through item properties, LMS MAY preserve
the tab object, source label, source icon override, and per-item enumeration
until the target creative-tab registry and content-building phases. Target tab
keys MUST be deterministic and scoped to the active legacy mod; membership
MUST be selected by preserved tab-object identity. LMS MUST execute source item
enumeration through its verified override contract, deduplicate only stacks
that the target defines as identical, and fail explicitly when source output
cannot satisfy target tab invariants.

When source model baking is ordered after texture-stitch completion but the
target uploads the prepared atlas only after it snapshots model dispatch,
LMS MAY deliver the source texture-stitch completion against an immutable view
of the exact target atlas preparations. That view MUST preserve atlas identity,
sprite lookup, missing-sprite fallback, and the source ordering before model
registration. It MUST NOT upload GPU resources early, expose an unrelated live
atlas, or replay the source completion callback a second time after upload.
Operations outside the proven prepared-atlas view MUST remain unsupported.

When a source dynamic item override selects the baked model of another item
stack through the exact source item-renderer lookup contract, LMS MAY carry
that selected stack back to the target item-model update and append its modern
render layers through the target resolver. The target adapter MUST preserve
the selected stack plus level, owner/entity, display context, and seed. It MUST
NOT fabricate a legacy baked model for an unrelated modern model or generalize
the migration from a return descriptor alone.

When a source fluid type registers immutable client texture/tint intent through
an `initializeClient` callback that the target removed, the source profile MAY
capture that exact callback result lazily before its first model-baking use.
The captured object MUST remain associated with the originating fluid type,
and its verified source accessors MUST be used in preference to uninitialized
target model-manager state. LMS MUST NOT infer fluid textures from registry
names or substitute the target missing-fluid model for absent source intent.
For target-owned vanilla fluids whose baked model is computed but not yet
published in this phase, LMS MAY expose a texture identity only when the exact
target fluid registry identity and target model declaration prove it. This
exception MUST NOT be generalized to custom fluids by identifier convention.

When a source render-type class combines chunk-layer identity with immediate
render-pipeline state but the target separates those concepts, LMS MUST retain
source chunk layers as semantic identities until the model adapter selects the
target chunk/material representation. It MUST NOT substitute an unrelated
target render pipeline merely because both APIs use a class named RenderType.
An exact source texture-bearing entity render-type factory MAY resolve to the
target factory only when both factories retain the same named blend, culling,
depth, and texture semantics.

When a direct source model subclass owns its geometry and implements the exact
source immediate-render method, LMS MAY replace only its removed base-model
constructor contract with a source-shaped bridge. The bridge MUST preserve the
source render-type function and abstract render method; an empty target root
used solely to satisfy target base-class bookkeeping MUST NOT be treated as the
subclass's functional geometry or as proof of target render-state migration.

When the source renderer pre-registers render types in a mutable fixed-buffer
map that the target no longer exposes, LMS MAY capture the exact registration
intent in an LMS-owned target adapter. It MUST NOT reflectively mutate a target
renderer's private buffers or treat captured intent as proof that the target
render pipeline supports that type. Attempted functional use MUST remain
fail-closed until a verified target rendering operation consumes the intent.

When the target removes the source immediate block dispatcher in favor of an
extracted block render-state pipeline, LMS MAY preserve constructor-time
dispatcher dependency injection through a source-shaped facade. Any functional
model lookup or immediate tessellation through that facade MUST fail explicitly
until a verified state-extraction/submission adapter exists; successful renderer
construction alone MUST NOT be reported as functional block rendering.

When a source block-entity renderer emits immediate vertex geometry but the
target requires separate extraction and submission, LMS MAY invoke the exact
source renderer during target extraction and preserve its completed geometry
as immutable render state. Submission MUST consume that recorded geometry
through target render nodes, MUST apply the target-authoritative block-position
pose after source-local transforms, and MUST NOT retain the live block entity
for later submission. Unsupported source render types or dispatcher operations
MUST remain explicit fail-closed boundaries.

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

When a target removes the source-generation gameplay `Material` identity,
LMS MAY retain source material constants and their read-only property queries
as source-profile semantic values. Native blocks shared with the source
generation MUST obtain that identity from a version-scoped snapshot of the
actual source registry, selected by exact registry identifier. Managed legacy
blocks MUST retain the material supplied to their construction properties,
including supported properties-copy operations. LMS MUST NOT infer material
identity from modern tool tags, map colors, names, or class-name heuristics.
Unknown native blocks and custom source material construction MUST fail
explicitly until evidence-backed rules exist. Source material colors MUST
retain their palette identities and push reactions MUST use a verified target
value mapping. Supporting material reads MUST NOT be claimed as proof that
tool energy use, harvesting, drops, or other consumers work.

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
