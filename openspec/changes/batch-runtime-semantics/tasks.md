# Runtime semantics batch — 2026-10-14

Observed overall milestone remains 6 (world load). No new interactive client or
world was run by the agent. The user's latest crash reached the source damage
hook after the migrated fall-distance getter; it failed on ItemStack.m_41622_.

## Implemented and verified

- [x] Whole-artifact offline transformed linkage inventory: all 2798 classes,
  exact grouped method/field/type/descriptor references and callable constants.
  Optional integrations/unreached classes are inventory, not causal runtime failures.
- [x] Raw managed/hierarchy byte caches and subtype indexes; positive/negative
  rebinding, mutated-node decisions, atomic rebind/transform lifetime and counters.
- [x] Checked shared ASM traversal for source sound, neighbor notification and
  block capability call families, including receiver/capture thunks and source-super
  handles. Exact source identity/inheritance/invocation kind remain mandatory.
- [x] Eager source block attachment event at native base construction, receiver-owned
  ordered dispatcher, genuine absence, original optional identity, nullable side,
  duplicate rejection, invalidation/revival and ForgeCaps serialization.
- [x] Explicit registered-event owner context switching/restoration; strict network
  scope entry remains separate and unchanged.
- [x] Source neighbor callback delivery with authoritative source positions, capture
  on queued entries, native multi-update origins and instant dispatch. Callback
  origins cannot silently become origins of unrelated nested native notifications.
- [x] Source durability/raw hurt family and source item hook callback migration.
  Entity notification/consumption/statistic/reset order is source-owned. Positive
  enchanted damage with a supplied source random remains explicit unsupported behavior.
- [x] Mutable stack-tag ownership: detach on source access, live identity, explicit
  supplied-tag identity, native copy/resource snapshots, and comparison change detection.
- [x] Native config extension key/function projection, lazy suppliers and same-container
  identity, real native mod-list factory construction, named client reload listeners,
  and pinned source network sentinel values/identity/mutability.
- [x] Physical client render mixin moved to the client-only configuration list.
- [x] Dependency fixture lifecycle assessment: 28 requirements, 6 remaining gaps
  (previously 14); gate stays blocked rather than claiming full initialization.
- [x] `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava`.
  The native smoke probe runs the actual physical-client FML/mixin loader without
  a window, world, server main or original legacy entrypoint initialization.
  It verifies gather before subclass fields, lazy resolution afterwards, NBT save/load,
  removal/unload, native mutable-tag copies and ItemResource snapshots, transformed
  queued/instant classes, retained origin and the actual injected operation scope.
- [x] `git diff --check`.

## Pending human acceptance and explicit gaps

- [x] User confirms jumping no longer crashes; broader fall variants are unverified.
- [ ] Place/remove mechanisms; open generator, change slots, close/reopen.
- [ ] Tin cables with first insulation: visual connections and updates after removal.
- [ ] Charged chainsaw in survival: charge delta and independent copied-stack state.
- [x] User confirms canned food consumption and can return.
- [ ] Hunger recovery after consumption.

CarbonConfig remains blocked on input event/key registration semantics, managed
container iteration and source reflection mapping. GLFW and SDL integer key/scancode
meaning must not be equated by their common descriptors. Source config-screen content
also has independent rendering/input ABI gaps outside constructor-reachable planning.

Native client capabilities, source item/entity attachments, arbitrary generic-event
APIs, native energy direction flags/fluid operations, source enchantment/random
migration, direct native source-neighbor calls with unknown origin, level notification
overrides and source worn armor rendering remain unsupported/unverified boundaries.
The original source machine-specific NBT callbacks are a separate persistence gap;
attachment ForgeCaps save/load is not proof of all mechanism persistence.

The exploratory dedicated-server `--initSettings` run reached settings initialization,
then the standard Server wrapper exited 1 because that flag deliberately starts no
server thread. A subsequent dedicated-server probe exposed pre-existing client types
in the mixed common EventBus bridge. The passing native smoke is explicitly physical
client; dedicated-server functionality is not claimed.

Original artifacts and the pre-existing 8-file tracked diff (345 additions/11 deletions)
plus the user's untracked container-screen bridge must remain unchanged/excluded.

The 09:42 user run reached source Slot construction on both sides and failed on
an incorrectly migrated source-owned field. Cable field updates reached native
model-data refresh but failed at the removed LevelRenderer dirty-range method.
Follow-up batch: `openspec/changes/restore-menu-and-model-updates/tasks.md`.
