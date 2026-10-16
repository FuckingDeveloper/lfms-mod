# Verification: explicit target block-data callback layout

## Automated checks

- `./gradlew compileJava compileCompatibilityTestJava compileLaunchJava`:
  passed (`/tmp/lms-block-target-compile-1.log`).
- `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava`:
  passed on the final sequential run (`/tmp/lms-block-target-check-3.log`).
- Pure checks cover immutable layout ownership, missing operations, duplicate
  callbacks, invalid descriptors/result shapes, synthetic alternate wrapper ABI,
  source-super view descriptors and profile factory propagation. The synthetic
  layout is test evidence only, not a supported Minecraft target.
- Native checks resolve the exact callback ABI on BlockEntity, including inherited
  NeoForge extension defaults; verify bridge/view signatures and overridability;
  reject a mismatched layout before source-bootstrap creation. Existing actual
  save/load/update/default/super/capability checks pass.
- Dependency-complete `compatibilityAudit` passed with 3412 classes, 1086 grouped
  observations and 7253 sites. All 1087 TSV rows (including header) exactly match
  `/tmp/lms-pass-extraction-audit.tsv`. These are inventory groups, not 1086 bugs.
- A repeated smoke invocation failed when an overlapping audit rebuilt classes
  under its module loader (`LegacyClassTransformer$Result` missing). The final
  check ran after audit completion and passed; checks must remain sequential.

## Scope

No callback timing, source aliases/defaults, NBT/capability projection, pass order
or original JAR changed. Runtime ValueInput/ValueOutput/context implementation in
Forge1192BlockDataBridge remains target-aware. Additional source/target versions
and whole-mod compatibility are not claimed.

## Fresh runtime87

Launched with `./gradlew runClient -PlmsTestWorld='Новый мир'` after all checks;
log: `/tmp/lms-container-families-runtime-87.log`. MCP status confirmed Minecraft
26.3 / NeoForge, integrated server and one player. Capabilities, dimensions,
`agent_brief`, player and inventory verified `sp:Новый мир`. Native launch log
correlates the pinned NeoForge runtime; Java25 is the configured Gradle toolchain.

- Cold-loaded generator at (-206,78,12): source storage 3610, handler side49 /
  access3843, comparator max2/slot3 and eu_storage flags5/slot6, max_fuel400.
  Current state was retained through open/close/reopen. Storage is this run's
  observed persisted value, not an assertion that earlier player sessions left
  it at 4000.
- Actual `look_at` + `use_item` opened the source generator twice and macerator
  (-203,78,15) once. A no-match bedrock `container_transfer` confirmed each open
  menu while transferring nothing. Closed menus through MCP.
- Restored exact captured player coordinates/camera/selected slot. Full structured
  inventory before/after is identical; no fixture blocks/items created or removed.
- No `/ERROR]`, `/FATAL]` or leading `Caused by:` in the client log through final
  inspection. Native smoke intentional exception probes remain test-only evidence.
- Existing overall observed milestone remains level6 plus previously bounded
  level7 paths; this checkpoint proves refactor regression checks, not full mod
  compatibility. First deferred broad functional family remains F07 fluids.

Checks exercised the complete development worktree, including the pre-existing
uncommitted compatibility changes. Those eight original dirty paths were compared
against a captured baseline and preserved; only this checkpoint's transformer
refactor is indexed from the corresponding HEAD content. The other original
changes are not part of this commit.
