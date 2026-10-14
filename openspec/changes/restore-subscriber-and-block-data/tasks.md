# Restore source block data lifecycle — 2026-10-14

Highest observed overall milestone remains 6 (world load). Generator/wire gameplay
acceptance is pending; headless native probes establish bounded lifecycle semantics.

- [x] Inspect exact source Forge 1.19.2 BlockEntity/IForgeBlockEntity and pinned
  target BlockEntity, ValueInput/ValueOutput and NeoForge extension implementations.
- [x] Adapt native load/save/initial update/handle operations to source callbacks,
  preserving most-derived dispatch, explicit super order and inherited base defaults.
- [x] Keep full NBT snapshots at native input boundaries, direct source tag identity,
  actual output root/serialization ops/reporter and native registry context.
- [x] Preserve ForgeData ownership/identity and once-only ForgeCaps persistence;
  preserve native attachment handling and opaque source NeoForgeData keys.
- [x] Preserve source handle default load-only behavior without target component replacement.
- [x] Index source data hierarchy evidence per transformer binding and runtime
  callbacks via ClassValue; restore scoped context after callback failures/nesting.
- [x] Reject unsupported native subclass semantics and source-super callable references.
- [x] Verify immutable synthetic source JARs with real physical-client FML/mixins,
  actual native storage/update APIs, source parent/child and partial override hierarchies.
- [x] `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava compatibilityAudit`.
- [x] `git diff --check`.
- [ ] User checks generator open/slots/close/reopen.
- [ ] User checks fresh insulated tin cable placement/removal and world re-entry.

Verification logs: `/tmp/lms-block-data-final.log` and
`/tmp/lms-compatibility-audit.tsv`. The audit inventories unsupported linkage; it
does not claim all reported gaps have been closed: 2798 classes, 1543 grouped
gaps, including the existing PackConstructor data-flow rejection from the
pre-existing resource-pack work. No source block-data transformation rejection
was observed. Changed vanilla subclass data
contracts and source-super callable references remain explicit boundaries.
No original integration JAR was patched. Existing user changes are excluded from
these commits. CarbonConfig, worn armor, full GUI rendering/input and energy
functionality remain tracked independently.
