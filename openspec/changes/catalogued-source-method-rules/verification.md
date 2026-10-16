# Verification: catalogued source method rules

## Evidence and checks

Pinned source Forge1.19.2/43.4.0 development artifact SHA256:
`579b78e19ff551ae3b340a6f908ca0923633a9395b35e6ae446772a778793bb4`.
Inspected actual ParallelDispatchEvent bytecode and exact target FML12.0.9 source
for ParallelDispatchEvent, FMLCommonSetupEvent and DeferredWorkQueue. Both source
work overloads enqueue to their lifecycle queue, returning its CompletableFuture.
Target scheduling, ordering and exception aggregation remain native.

- `generateSourceHierarchy` passed with the pinned source input; 8315 method
  declarations added across the existing 1161 indexed classes. Removing the new
  M rows reproduces the original resource byte-for-byte (class/field rows intact).
- `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed
  on the final production changes (`/tmp/lms-method-catalog-check-5.log`).
- Rule tests cover eight source root/subclass headers, managed declaration
  overrides, source missing/negative caches and invalidation, duplicate rules,
  descriptor/owner/opcode rejection, wrong-opcode transformation rejection and
  admission refusal for mixed invocation shapes.
- Independent bytecode links actual native direct calls, inherited bound lambdas,
  raw handles and nested ConstantDynamic handles. Native queue tests assert no
  eager execution, order, launch-thread execution, result values, failed futures,
  native error aggregation and exact source ownership restoration. Expected work
  failure logs in the native probe are deliberate tests, not client failures.
- Registration analysis retains call opcode, traverses callable constants, and is
  checked against the same surface assessment on an immutable synthetic JAR.
- An initial smoke packaging attempt exposed a split package; fixtures now live
  in the existing check package and final native packaging succeeds.
- Baseline dependency-complete audit passed (`/tmp/lms-method-catalog-regression-audit.log`):
  3412 classes /1086 groups /7253 sites; exact 1087-row equality with the preceding
  storage-boundary audit. The final later change only adds planner provenance and
  tests; transformation rules/header evidence are the same.
- Selected-profile batch audit passed (`/tmp/lms-method-catalog-selected-audit.log`):
  4323 classes /1201 groups /5980 sites. Input: six profile-selected top-level JARs
  plus actual Primitive Collections dependency. Metadata-incomplete Fetzi remains
  excluded from Forge transformation, not silently assigned a profile.
  Four transform boundaries remain: existing IC2 renderer/pack branches and new
  direct legacy Button allocations in WickOptionsScreen / JEI DebugRecipe. Static
  presence does not prove runtime reachability or four fresh gameplay failures.

## Batch result

`./gradlew compatibilityBatchReport --args='/Users/mrh/Documents/Develop/lfms-mod/run/legacy-mods /tmp/lms-method-catalog-final-batch.tsv'`
passed (`/tmp/lms-method-catalog-final-batch.log`). Seven descriptors:
CarbonConfig, IC2, Iron Chests have no known blocker in this bounded static surface;
TNT Expansion has one remaining boundary (registerConfig), JEI18, Wick2; Fetzi
has unresolved source profile due missing Minecraft metadata. Twenty unique API
blocker groups include shared config registration. A surface with no blockers
is not initialization/gameplay acceptance or complete dependency admission.

## Scope and retained gaps

Only lifecycle work has migrated to the shared catalog. Other assessment families
still use the old list until their actual implementation/evidence is consolidated.
Config registration/specs, DisplayTest/extension semantics and EventNetworkChannel
remain blocked. Original profile-selection policy and component-bound setup
parameter-use guard are unchanged. No mod identity branches, immutable JAR edits,
MCP modifications, metadata range widening or replacement game-control framework.

Checks use the full worktree with the eight original dirty paths preserved;
only this checkpoint's deterministic transformer addition is staged from HEAD.

## Fresh runtime89 / MCP

Launched with `./gradlew runClient -PlmsTestWorld='Новый мир'` after checks and
audits. Log `/tmp/lms-container-families-runtime-89.log` confirms CarbonConfig,
IC2 and Iron Chests INITIALIZED. TNT's enqueueWork boundary is no longer a gap;
registerConfig is its remaining admission blocker. JEI/Wick remain deliberately
blocked and Fetzi remains profile-unresolved. No new-mod functionality is claimed.

MCP status/capabilities, agent brief, dimensions, player and inventory establish
actual integrated world `sp:Новый мир`, Minecraft26.3 / NeoForge and player Dev.
Generator (-206,78,12) cold-loaded storage3610 and the prior handler/comparator
state; actual interaction opened it, closed it and opened it again. Macerator
(-203,78,15) also opened. No-match bedrock transfers confirmed open menus without
moving items. No ERROR/FATAL/leading Caused-by through final runtime inspection.
Full structured inventory is unchanged, exact player position/camera/selected
slot restored. No test blocks/items created. Client remains running, menus closed.

This acceptance preserves existing level6 plus previously bounded level7 paths.
The new work family has independent native functional tests, but is not executed
by TNT while its config registration remains blocked. Current first batch priority
is the shared config family; source fluids remain a separate previously known gap.
All seven input JAR hashes were checked unchanged; generated reports/logs remain
outside Git. Original dirty files remain excluded from this checkpoint.
