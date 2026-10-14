# Paired vanilla block/item tag projection

Target 26.3 exposes shared BlockItemTags constants as BlockItemTagId pairs. Some
source ItemTags declarations no longer exist. Exact source owner/member/descriptor,
pinned source tag identifiers and actual target pair declarations determine the
migration; descriptor coincidence or field-name coincidence alone is insufficient.

Source declarations are metadata from the pinned Forge 1.19.2 named artifact.
The adapter MUST read the actual target pair's item/block projection and MUST
verify its registry and identifier against the source facts. It MUST NOT construct
an arbitrary empty TagKey or infer membership. Unknown/mismatched identities fail
explicitly. Tag object identity and reload-driven bindings remain native.

Direct GETSTATIC, getter handles and nested/indy constants MUST retain their
source getter shape. Native declaration lookups are cached separately from source
ancestry. Original inputs are immutable. Resource tag JSON migration is not implied.
