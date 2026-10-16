# Resource and Data Compatibility Specification

Legacy compatibility includes non-class content where required: metadata, language resources, models, textures, recipes, tags or legacy tag-like systems, data files and other loader/game resources.

Resource transformation MUST be separated conceptually from arbitrary bytecode transformation. Original resource bytes MUST remain unchanged in the source artifact.

A profile MUST be able to declare resource/data transformations required by its legacy generation. Unsupported resource formats that affect declared functionality MUST be represented in diagnostics/capabilities.

Resource transformations MUST participate in transformation cache identity.

For Forge 1.19.2 standard standing-sign textures, the target 26.3 adapter MAY
derive the sign-edit GUI image from the source 64x32 UV layout: the 24x12 board
front at (2,2) followed by the 2x14 post front at (2,16), centered at x=11 in
the target 24x26 image. Integer-resolution scaling and alpha MUST be preserved.
The source namespace/path identity MUST select the corresponding target
`textures/gui/signs/` resource. Explicit target-shaped resources MUST take
precedence, malformed layouts MUST fail explicitly, and legacy artifacts MUST
remain unchanged. This GUI adaptation MUST NOT be claimed as proof of world
sign-model, hanging-sign, animation, or arbitrary custom UV compatibility.

For a source generation that selects global loot modifiers through an ordered
resource-stack list, LMS MUST preserve list replacement, last-occurrence order,
and exclusion of unlisted source definitions. Migrating to target per-modifier
priority MUST retain the source group order at the target default priority;
native higher/lower priorities remain authoritative and equal-priority interleaving
with native modifiers remains unspecified. The control document MUST NOT be
parsed as a modifier definition. Adaptation MUST be confined to the corresponding
reload consumer and MUST preserve native lookup of selected IDs. Missing or
undecodable selected modifiers MUST fail explicitly, without losing entries.

For an admitted source-generation standard common-tag declaration with a verified
modern counterpart, LMS MAY canonicalize its tag identity to that counterpart.
Admission MUST use the exact registry and pinned source declaration together with
an actual target declaration and verified path/member correspondence. Namespace
prefixes or descriptor coincidence alone MUST NOT select migration. Managed
source key construction/codecs, source tag definition paths and nested tag
references MUST use the same table. Unknown/custom identifiers and plain element
IDs MUST remain unchanged. Explicit native-shaped entries in a source pack MUST
win over projected entries; native resource-stack replacement/order semantics
MUST remain authoritative. Direct lookup, enumeration and namespace discovery
MUST agree, and procedural source data MUST NOT be cached across native load
phases. This exception MUST NOT be applied to native packs or arbitrary JSON
schemas. Public source key construction MUST preserve fresh object identity;
factory interning MUST retain the authoritative target key identity.
