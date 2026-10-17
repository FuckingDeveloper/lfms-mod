# Built-in block-entity constant catalog

The complete pinned Forge1.19.2 built-in BlockEntityType constant set MAY move to
the verified native BlockEntityTypes catalog. Each projection MUST prove the
source declaration/member identity and exact native public static final field
ABI; the canonical built-in registry identifier MUST remain unchanged. This is a
field-declaration relocation, not a registry mutation or new type construction.

Direct, inherited and callable/nested-constant reads MUST share the rule. A
managed subclass's own hiding declaration MUST remain untouched. Unrelated owners
and target-only source names MUST NOT match from descriptors alone. Writes to
admitted source final constants MUST fail explicitly. Unknown or missing native
members MUST fail closed. Source field-name/target ABI lookup MUST be indexed or
cached without retaining managed classes or disposable classloaders.

Evidence: pinned source BlockEntityType declarations/registration and mappings;
actual target BlockEntityTypes and BlockEntityTypeIds declarations. Native probes
verify every source constant's actual registry identity, not only its field name.
Banner item NBT/components and source block-entity construction remain separate.

All 36 source members are accounted for: 35 survive in the native catalog. The
source BED member has neither a native field nor the source minecraft:bed
block-entity registry entry. BED reads MUST fail explicitly; no dummy type or
other native type may substitute for it. Supporting new bed storage is separate.

The profile ships its source declaration/registry-ID snapshot with pinned artifact
provenance. It MUST NOT derive source registry identities from target field names.
Tests compare the snapshot with actual source registration bytecode when the
pinned development artifact is present and always compare its complete source
member set with the configured source mappings and all surviving native values.
