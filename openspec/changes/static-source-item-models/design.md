# Source generated-item model definitions

The 1.19.2 client discovers inventory models directly at models/item/<id>.json.
The target 26.3 ClientItemInfoLoader requires items/<id>.json selecting an
ItemModel.Unbaked. Its minecraft:model codec selects a cuboid model by identity;
the target retains the generated/handheld geometry parents.

For admitted source artifact models with an exact vanilla generated, handheld,
handheld_rod or builtin/generated parent, LMS MAY derive the missing target
ClientItem definition selecting that same source model. Original geometry,
textures and display transforms MUST remain unchanged. Source overrides and
custom loaders MUST NOT be discarded. An explicit target items definition MUST
take precedence. Unknown parent graphs MUST remain unsupported and MUST NOT be replaced with
empty or invented models. The separately specified builtin/entity adapter retains
its own admission and rendering rules.

Generated models have five verified source texture-layer tint indices (0..4).
The derived definition MUST project each through the preserved source item-color
callback, using opaque white for absent source callbacks, as source rendering
does. The adapter MUST register its tint codec in the native client lifecycle.
Definitions MUST be derived from immutable artifact resources, participate in
normal resource reload and never alter native packs or source JARs.

This establishes generated inventory-model discovery and tint evaluation. It
does not establish source override predicates, arbitrary parent graphs,
custom geometry loaders or BlockEntityWithoutLevelRenderer migration.

## Closed static block-model inheritance (2026-10-17)

The adapter MAY also derive definitions for same-artifact parent chains ending at
verified native minecraft:block/cube or minecraft:block/cube_bottom_top templates.
Both retained templates have native cuboid geometry/texture-variable inheritance;
the source model identity, textures and display properties MUST remain unchanged.
The original source JSON is selected, never flattened or replaced. Each visited
source model MUST be free of overrides and custom loaders. Missing parents,
cycles, external unproven parents and inherited custom-render markers MUST leave
the item unsupported. Ancestor lookup MUST be indexed/memoized per artifact,
without retaining classloaders. Explicit target definitions retain precedence.
This is a bounded extension, not admission of arbitrary resource graphs.

Evidence: source 1.19.2 client and exact 26.3.0.58-beta cube and cube_bottom_top
resources are JSON-equal (including geometry/texture inheritance), native
CuboidItemModelWrapper codec, and immutable source block/item JSON. Native resource
reload remains responsible for texture resolution and model baking.

## Native parser parity and explicit cuboid leaves (2026-10-17)

Resource indexing MUST use the configured target model loader’s JSON root-reading
contract. Native GsonHelper.parse reads one strict root through its adapter,
without JsonParser’s additional end-of-document check. LMS MUST NOT reject an
input solely because its indexer is stricter than native model loading. Malformed
root objects MUST still fail; no lenient parser or source resource rewrite is used.

A same-artifact chain MAY terminate at an explicit parentless elements cuboid
model, after actual configured native CuboidModel parsing validates that geometry.
The source model and original textures/transforms MUST remain selected. Parentless
objects without elements, cycles, missing/external parents, source override
predicates and custom geometry loaders remain unsupported. This extends only the
proven native cuboid leaf family, not arbitrary resource graphs.

Runtime114 exposed the parser difference after source banner initialization
succeeded. Independent fixtures compare actual native CuboidModel reading,
immutable graph discovery and malformed-root rejection. Native resource baking
and actual fixture rendering remain separate runtime acceptance.

## Indexed verified vanilla template graphs

The native adapter MAY admit additional vanilla block templates using a generated
source/target evidence table. Each selected model and every reachable vanilla
parent MUST have equal JSON intent in the pinned source assets and configured
target resources. Only the target-only on_shelf display context, absent from the
source generation, may be excluded from comparison; that new context uses the
native placement policy already approved for shelves. Changed source fields,
missing parents, cycles, loaders and overrides MUST prevent admission.

Generation pins the immutable1.19.2 client-extra.jar by SHA256 and writes only
verified identifiers, not vanilla asset bodies. Runtime lookup MUST use a loaded
set, not rescan source/target resources per item. Artifact-local overrides MUST
be inspected before trusting a table entry. Runtime116 exposed64 missing native
item definitions; the expanded table proves cube_all/slab/stairs families too.
The changed wall_inventory template (source ambientocclusion=false, target default
true) is intentionally excluded rather than silently changing that source intent.
