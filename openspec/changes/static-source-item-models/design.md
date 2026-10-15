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
take precedence. Unknown parents and builtin/entity MUST remain unsupported by
this adapter; they MUST NOT be replaced with empty or invented models.

Generated models have five verified source texture-layer tint indices (0..4).
The derived definition MUST project each through the preserved source item-color
callback, using opaque white for absent source callbacks, as source rendering
does. The adapter MUST register its tint codec in the native client lifecycle.
Definitions MUST be derived from immutable artifact resources, participate in
normal resource reload and never alter native packs or source JARs.

This establishes generated inventory-model discovery and tint evaluation. It
does not establish source override predicates, arbitrary parent graphs,
custom geometry loaders or BlockEntityWithoutLevelRenderer migration.
