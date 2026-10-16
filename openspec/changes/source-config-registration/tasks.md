# Configuration family checkpoint

- [x] Inspect actual source/target builders, values, contexts, loader files/events.
- [x] Record user acceptance of NeoForge26.3 SERVER/SYNCED policy.
- [x] Preserve source owner/type/default filename/original spec identity over native storage.
- [x] Share exact registration/builder/value rules between admission and transformation.
- [x] Include hash-pinned FML core/Java FML declarations; reject invocation-shape mismatches.
- [x] Preserve source immediate-set/cache/reset-all and save-without-reload contracts.
- [x] Isolate source config listeners; retain native listeners and source unregister.
- [x] Verify real native loader correction/save/unload and immutable TNT config construction.
- [x] Compile and execute compatibilityTest/nativeCompatibilitySmoke/compileLaunchJava.
- [x] Run batch admission inventory and selected-artifact transformed-linkage audit.
- [x] Run fresh Gradle client and MCP connection gate; record world/content checks BLOCKED by registration failures.
- [x] Review/stage only this family's changes and preserve the original eight-file dirty baseline.

No dynamic config unregistration, arbitrary custom specs, list correction,
blank-comment normalization, remote multiplayer config-sync acceptance or
whole-mod compatibility is claimed. Static reports are bounded inventories.
Runtime evidence and final checkpoint information follow after verification.

## Verification observed

- `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava`:
  BUILD SUCCESSFUL, final `/tmp/lms-config-check-final.log`. Tests execute typed
  source-shaped direct calls/callables, scalar/enum defaults, source restart-cache
  set/reload, native correction/save/unload and source/native listener isolation.
- `generateSourceHierarchy` verifies all three immutable source hashes;
  1,245 class headers. Original Forge class/field/method rows are retained.
- `compatibilityBatchReport`: TNT constructor surface has no known blockers;
  JEI requirements reduced18 ->11. Wick retains2; Fetzi profile unresolved because
  its metadata does not establish the source Minecraft generation.
- `compatibilityAudit`: six source fixtures plus the actual Carbon embedded
  Primitive Collections dependency; 4,323 classes,1,184 grouped findings. Wick's
  nested MixinExtras is not in this selected audit graph. Counts are inventories,
  not tested failures or compatibility percentages.
- Fresh `./gradlew runClient -PlmsTestWorld='Новый мир'`, runtime90:
  TNT constructor INITIALIZED and declared200 TNT types. CarbonConfig, IC2 and
  Iron Chests constructors initialize as before. Source registration then fails
  on `ThrowableItemProjectile` ancestry/constructors and `IForgeMenuType.create`.
  Deferred registration did not complete; no successful world-load milestone.
- MCP Fabric0.5.1+26.3 reached this NeoForge26.3 client with no server/player.
  World/block/inventory/gameplay checks are BLOCKED, not successful empty results.
  Exact target: NeoForge26.3.0.58-beta, FML12.0.9, Java25.0.4.1. Crash preserved;
  failed dev process stopped. No player/inventory/world fixture was changed.

Next causal work: source projectile base-class family and source menu-factory/
extra-opening-data contract. The current constructor-surface report does not
prove all deferred registry suppliers or later client renderer callbacks link.
No source input, generated run/config/log/token file or prior user edit belongs
in this checkpoint.
