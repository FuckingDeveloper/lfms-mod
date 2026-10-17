# Contract

- Select the exact pinned source EnchantedBookItem.getEnchantments(ItemStack)
  declaration and Enchantment.getDescriptionId() across direct and callable uses.
  Do not project the removed book class to Item or admit construction/subclasses.
- Preserve the source StoredEnchantments list identity, including nested entry
  mutation. An absent list returns a detached empty list. A present list remains
  connected to the receiver's existing mutable-tag state, not a writable snapshot.
- Native STORED_ENCHANTMENTS is mirrored into that source tag on acquisition.
  Source edits MUST be reconciled before native component reads, snapshots,
  equality, copying and mutation. Later native writes replace the previous
  source mirror on the next acquisition. Detached copies must not share tags.
  Synchronization is receiver-owned, guarded against recursion, and absent for
  stacks that have never acquired this source view. Unsupported malformed entries
  and missing authoritative registry context MUST fail explicitly.
- ACTIVE may return a stable key-backed enchantment read view. Each value read
  resolves the current authoritative registration/world/reload context through
  the existing dynamic registry resolver. No world registry or instance is cached.
  This extends the prior static-only read-view boundary solely for enchantments;
  other moved registries and mutations remain rejected.
- Description IDs retain the source registry-key-derived translation identity;
  arbitrary native description text is not substituted for a translation key.
  Unregistered values and unavailable context fail explicitly.

# Evidence

Pinned Forge43.4.0 named patched EnchantedBookItem bytecode returns the actual
StoredEnchantments ListTag, or a new empty list if absent. Enchantment description
uses Util.makeDescriptionId with its registry name. Target26.3.0.58-beta sources
remove EnchantedBookItem, store enchantments in immutable ItemEnchantments, and
expose Enchantment as a data-driven record. Existing source mutable-tag ownership
and dynamic authoritative registry consumption contracts are reused.

# Verification

Independent transformed-bytecode checks plus native FML checks cover list and
nested mutation, native replacement, absent-list detachment, copy ownership,
missing context, stable keyed views and source translation identities. A fresh
explicit-world client must no longer log the original book-reader failure.
Linkage/read acceptance does not imply full enchanting or JEI functionality.

# Verified implementation checkpoint

`./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava
compatibilityAudit --args='legacy-mods /tmp/lms-enchanted-book-audit.tsv'` passes.
Log: /tmp/lms-enchanted-book-final-check.log. Independent native tests exercise
pending nested edits before copying/comparing, source root assignment/removal,
native replacement, list identity and source bytecode/handle linkage. Audit:
3829 classes, 1139 grouped gaps / 6738 sites; the same two unrelated dormant
transformation failures remain. This inventory is not functional coverage.
Stored levels outside the native representable range fail explicitly.
Runtime consumer acceptance remains pending.
