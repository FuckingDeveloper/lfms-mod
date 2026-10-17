# Contract

- Proven surviving Potions fields MUST yield the exact native holder value,
  preserving raw source Potion identity. Field references, including handles,
  MUST use verified source mapping and exact descriptor evidence.
- Source EMPTY MUST be a distinct source-owned absence token, never a new native
  registry entry or a replacement native WATER value. Typed potion-item consumers
  MUST encode it as absent base potion and return the same token on absent or
  unknown source identity. Converting it to a native registered holder MUST fail.
  Source registry numeric IDs/defaulted-root parity for this removed entry remain
  outside this bounded family; do not invent native membership or numeric IDs.
- Proven source Potion.getName(prefix) MUST retain its suffix/prefix meaning.
  byName returns source EMPTY on malformed/absent names, as the source defaulted
  registry did. Native raw values and existing own callbacks remain actual values.
- Source PotionUtils typed ItemStack queries MUST read native PotionContents.
  Effects results MUST be fresh mutable lists: base effects retain their original
  instance identity while custom effects are copied, as in the source reader.
- setPotion MUST preserve custom effects/color/name and return the exact receiver.
  setCustomEffects on an empty collection MUST be a no-op. The pinned source's
  mismatched expected list type replaces existing compound custom effects for
  nonempty input; the adapter MUST retain this verified source behavior.
- Source colors MUST retain the pinned defaults, visible-effect filtering,
  amplifier weighting, float arithmetic and RGB result. Native ARGB defaults or
  rounding MUST NOT silently replace source results.
- Raw NBT effect serialization, arbitrary live root mutation and source potion
  constructors/subclasses are not admitted by this change. Existing native/source
  stack serialization boundaries MUST continue failing explicitly on unsupported
  potion contents. Such missing parity MUST NOT be claimed as supported.
- Static brewing queries and mutation remain separate. Never return empty
  recipes/containers to hide that boundary.

# Evidence and verification

Source: pinned Forge43.4.0 named Potion/Potions/PotionUtils bytecode and source
mappings. Target: actual26.3.0.58-beta Potion/Potions/PotionContents and component
code. Source EMPTY is removed; target absent base holder is explicit Optional.
Tests must cover all surviving source constant identities, empty/unknown reads,
receiver identity, custom data preservation, source color arithmetic and
immutable source direct/callable bytecode. A fresh explicit world must identify
whether recipe-manager creation advances and record the first remaining blocker.


# Implementation verification

`./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava
compatibilityAudit --args='legacy-mods /tmp/lms-potion-audit.tsv'` passes.
Log: /tmp/lms-potion-final-check.log. Native FML checks exercise every pinned
source constant against actual native holder values; source SRG direct calls,
field handles/nested constants, receiver identity, name suffixes, copied custom
effects, preservation of color/name/effects, count-zero source visibility, empty
collection no-op, source EMPTY absence, unknown IDs, RGB defaults/weighting and
raw NBT rejection. Source input is immutable. Audit: 3829 classes, 1128 grouped
gaps / 6407 sites; the same two unrelated dormant transform failures remain.
This is inventory, not functional coverage. Original runtime acceptance is pending.

Raw legacy potion keys in CUSTOM_DATA fail before typed reads can silently ignore
them. Source raw getTag/getOrCreateTag/setTag on potion-component stacks fail until
a complete source schema projection is implemented. A no-change setPotion does
not create an unnecessary native patch. Source EMPTY is an LMS-owned absence
token with source empty name/effects; it is never added to the native registry.
