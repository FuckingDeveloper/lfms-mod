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

## Typed list reads

Runtime133 loads the world and passes the removed-book class boundary. The next
causal book failure is the source ListTag.getCompound(int) descriptor, whose
native counterpart now returns Optional. The bounded source read family MUST
preserve the exact tag type for numeric getters, source defaults for wrong types
and invalid indices, actual compound/list/array identity, string extraction and
non-string SNBT fallback. Source getElementType rejects heterogeneous native lists
which have no source representation. Direct/handle/lambda/nested-constant reads
share the existing semantic traversal. Native collection mutations and Java List
aliases are outside this typed-read contract.

## Runtime134 acceptance

The fresh explicit-world Gradle client loads Новый мир. MCP verifies the actual
integrated server, local Dev player in overworld, matching position/vitals and
baseline inventory summary. All seven source entrypoint constructors initialize.
Original JEI book subtype enumeration produces zero “Couldn't get unique name”
errors and no removed-book/ListTag linkage errors, then advances to fluid renderer
construction at VanillaPlugin:188. The first causal ERROR is missing Matrix4f;
the later vanilla crafting category NPE is secondary. Full JEI startup/rendering
remains BLOCKED. Overall highest observed milestone remains world load (level 6).
Source read-family acceptance does not establish native in-world enchanting,
custom enchantment subclasses, book construction/subclass semantics, category
migration or source EnchantmentInstance compatibility.

Evidence: /tmp/lms-container-families-runtime-134.log. The final list-family
checks pass in /tmp/lms-enchanted-book-list-check.log using the same Gradle command
with audit report /tmp/lms-enchanted-book-list-audit.tsv: 3829 classes, 1137 grouped
gaps / 6725 sites, same two prior unrelated transform failures. No gameplay
fixture or inventory mutation was performed; no recovery is pending. The client
is left running. Implementation commits 5de5095 and 3e2dcd3 are pushed; unrelated
pre-existing worktree changes remain preserved.
