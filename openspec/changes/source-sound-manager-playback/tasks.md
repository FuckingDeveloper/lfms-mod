# Source sound manager playback verification

- [x] Inspect runtime104 mouse-click failure and exact source/native manager and
  engine contracts before adapting the changed return/parameter shapes.
- [x] Implement source play/pause/debug virtual and base dispatch, preserving
  original managed callbacks and propagating failures without a dummy status.
- [x] Native source-shaped probe covers every PlayResult, original sound identity,
  once-only virtual play and callable handle, native/source override/base behavior,
  pause-all empty exclusions, actual debug values and exception identity.
- [x] Verify unchanged playDelayed/stop/isActive/queueTickingSound native ABI.
- [x] `compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed
  (`/tmp/lms-sound-manager-check.log`). Headless spies prove dispatch; audible
  playback and mouse widgets require the loaded client.
- [x] Selected transformed-linkage audit: 4323 classes, 1145 grouped findings /
  5746 sites; removed SoundManager playback member finding absent
  (`/tmp/lms-sound-manager-audit.log` and `.tsv`).
- [x] Remove client-only types from shared InstalledTargetAdapters signatures;
  resolve exact client service handles only in the client bridge. Full checks
  passed again (`/tmp/lms-sound-manager-check-2.log`); javap inspection found no
  client types in shared composition signatures. This is not a dedicated-server
  gameplay acceptance claim.
- [x] Runtime106 user exercised the GUI and reported no click sound. No removed
  SoundManager member exception appears in that run. The saved native setting was
  soundCategory_master:0.0; audible playback remains UNVERIFIED while muted.
  The same run later crashed on independent TNT entity-data/explosion boundaries.
  Runtime105 was stopped before human testing to apply the side-safe binding.

Source category-gain/ResourceManager construction/reload internals remain separate
contracts. No sound success is invented and no backend exception is suppressed.
