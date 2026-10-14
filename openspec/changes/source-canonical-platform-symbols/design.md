# Source canonical platform-library symbols

Canonical source method identity MAY include com.mojang platform-library owners
present in the pinned Forge 1.19.2 source mappings. Source SRG lookup MUST compare
normalized dot/slash owner spelling and the fully source-remapped descriptor,
then select exactly one named source declaration. It MUST NOT accept an inherited
or descriptor-only candidate as an exact declaration.

Comma-separated argument types MUST use actual whitespace regex tokenization,
including multi-argument methods. Owner/member indexes remain reused rather than
scanning the mapping dataset per instruction. This source information lookup
MUST NOT authorize blind target linkage, namespace migration, or changed runtime
semantics; existing target resolution/ABI validation remains independent.
