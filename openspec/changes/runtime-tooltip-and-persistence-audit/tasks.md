# Runtime66–68: search, generator hover/persistence and startup

Scope: Forge1.19.2 -> Minecraft26.3 / NeoForge26.3.0.58-beta / Java25.
Transparent chest cold preview remains excluded by explicit user decision.
Overall observed compatibility remains level6 with individually verified level7
paths. No complete-mod compatibility claim.

## Search tooltip logical side

- [x] Correct the old source-null-player-only diagnosis: original client handler
  accepts null; native asynchronous indexing can inherit a server thread group.
- [x] Scope source CLIENT at the exact native search tooltip evaluation site,
  preserving null/other arguments, result, exceptions and native FML side.
- [x] Applied FML/mixin test, controlled SERVER worker, nested/failed restoration,
  once-only actual injected operation and nullable arguments (batch checks25/27).
- [x] Runtime67/68 world entry and original content initialization without fresh
  ERROR/FATAL at the search site. Runtime65 human creative search was already
  successful; no fresh reproduction of runtime39 is claimed.
- [x] Commit/push5a6fde5.

## Generator hover

Source ChargeBarComponent requires an EU reader in hotbar. Runtime65 human
confirmed missing hover despite supplied reader and4000EU. Original IC2Screen
collects these tooltips in full render after super; only background/labels had
native entrypoints. m_96617_ is Screen.renderTooltip(FormattedCharSequence list),
not renderComponentTooltip; pinned source mappings prove the distinction.

- [x] Add full source render entry, source background timing at super, native
  contents/foreground/carried item, explicit base tooltip and formatted/component
  native tooltip operations. Independent source inheritance/special dispatch and
  immutable IC2Screen bytecode coverage pass batch checks29/31.
- [x] Runtime68 actual right-click opens original generator without crash;
  temporary reader in hotbar and4321EU supplied for human hover acceptance.
- [x] Human confirms energy text in runtime68; ordinary item tooltip still unconfirmed.
- [x] Compile/test subsequent dynamic-tooltip-override dispatch hardening (batch35); runtime68 used the earlier variant.
- [x] Runtime71 opens the original generator after hardening; coherent renderer commit75cd2b2 pushed.

## Persistent block data

Runtime65 real coal burns to4000EU. Actual region NBT contains storage4000 before
restart; runtime66 loses it. Native merge/read of4321EU works. Runtime67 one
unload reload invokes native load with saved4321, another cycle loses it;
serialized block_entities later becomes empty. A vanilla chest control also
loses its block entity, so this is not an energy-only source defect.

Pinned source ChunkMap unload order is setLoaded(false) -> source Unload event
-> final save -> level.unload. Pinned target order moves native Unload after
save. Source listeners mark chunks dirty; target dirty marks schedule eager
persistence. A late dirty mark can cause a second save after native unload has
cleared every BE. Runtime68 opt-in native-save/chunk-save traces added to verify.

- [x] Specify source unload notification before final save, native timing intact,
  isolated source-phase bus, matching-transaction duplicate suppression and
  scoped failure/nesting cleanup.
- [x] Compile/test source unload order bridge and target hooks (batch34/35).
- [x] Runtime70 two unload/reload cycles preserve2000EU; runtime71 cold restart preserves2000EU and vanilla control stone.
- [x] Active remaining-fuel persistence subsequently verified in runtime73:
  ordinary fuel ticks, unload/reload and cold restart preserve the controlled
  remaining-fuel state; see source-world-render-extraction/runtime73.md.
- [x] Runtime71 removes own test generator and emptied vanilla control chest; original air restored.

The vanilla test chest(-206,78,13) was removed in runtime67. Runtime68 probe uses
slot3 temporarily for EU reader, with the original crafting table backed up in
slot25. Subsequent swap attempts were rejected because the generator GUI was
open (native mismatching-container warnings); inspect inventory before cleanup.
No source artifacts changed. Player remains creative. Restore source inventory,
position(-201.94841037006023,78,9.240555854225244), yaw65.57759/pitch24.038132,
selection3 after hover acceptance. No damage or survival testing authorized here.

## Startup

Runtime66 JFR recorded74 seconds including world entry. Nearest LMS sampled
frames: hierarchyContains214, tag-resource listed109, descriptor migration45.
Source reactor simulations also occupy many samples; sample counts are not wall
clock attribution. Runtime65 FML start to login was about53seconds. Member
planning has a roughly9second wall interval but is not established as sole cause.

- [x] Capture runtime-only JFR under/tmp; no trace/results committed.
- [x] Find repeated ClassReader ancestry parsing despite raw-byte caching.
- [x] Compile/test immutable parsed-parent cache with source rebinding
  and active mutated-ClassNode ancestry checks (batch34/35).
- [ ] Compare a fresh startup before claiming any speedup. Keep approved
  two-phase data loading and source callbacks; do not weaken build freshness.

Last full wrapper checks: compatibilityTest/nativeCompatibilitySmoke/
compileLaunchJava passed batch29; later nativeCompatibilitySmoke batch31 passed
original source full-render coverage. At that historical checkpoint those edits were uncompiled; batch35 below supersedes it. Existing
user dirty baseline remains untouched; no generated runtime/config/token staged.

## User acceptance and runtime68–70 follow-up

User confirms the generator energy tooltip appears.4321/4000 was deliberately
injected with native data merge for save tracing, not produced by generation;
future persistence values must stay inside capacity. Ordinary inventory tooltip
acceptance is not supplied. Batch35 passes compatibilityTest,
nativeCompatibilitySmoke, compileLaunchJava, including dynamic tooltip overrides,
source chunk-unload ordering/cleanup, hierarchy rebinding and player messaging.

Runtime68 subsequently crashes when EU reader onItemUseFirst invokes removed
Player.displayClientMessage(Component,boolean). Source Player's default is empty;
ServerPlayer delegates to virtual two-argument sendSystemMessage; LocalPlayer's
old overlay boolean is not equivalent to target ChatListener remote provenance.
The bounded source-player-message-delivery contract/bridge/tests preserve these
operations without changing the artifact or guessing message-side behavior.

Runtime69 enters the world then fails in EU reader world-overlay rendering:
GLUtils.enableHighlight -> GUI-only disableTexture adapter outside extraction.
This is an unresolved world-render lifecycle/primitive family, not the GUI energy
text or the corrected server message. Do not disable that callback or claim
EU reader functionality from its message linkage. It must remain a separate
backlog entry with actual target extraction/submission evidence required.

Because the selected test reader immediately reproduces that overlay failure,
restored the original crafting table from saved slot25 to slot3 and removed the
one temporary EU reader offline, with full player-file backup in/tmp. nbtlib
roundtrip retained all other player fields/inventory entries. No world/legacy
artifact deletion or general automation framework. Runtime70 uses the same
compiled batch35 implementation, with that temporary inventory cleanup only.

JFR runtime69 ancestry walking/parent parsing nearest-LMS frames total10 versus
runtime66's214 hierarchyContains samples. Other prominent measured work remains
source simulations and native resources; tag-resource listing still109 samples.
This is one profile comparison, not a general startup-time guarantee. No approved
source callback/data-load phase or build freshness rule was removed.

## Final runtime71 checkpoint

Actual cold native load supplies storage2000/max_fuel400; native data command
returns2000. Vanilla chest restores its stone in slot0. Original generator GUI
opens and closes after cold loading without ERROR/FATAL/Caused-by; no human
ordinary-item-tooltip acceptance is inferred. Empty test chest, stone and test
generator removed; original air at both test positions restored. Native table
back in hotbar3, slot25 empty, temporary EU reader absent. Position/camera from
the recovered runtime70 baseline restored (-203.5,75,10.5;51.74999/-28.961308),
creative health20/food20/saturation20/XP1+1/3 preserved. User-built nearby cables
and machines retained. Runtime71 remains running, no new ERROR/FATAL/Caused-by
through cleanup.

Pushed coherent checkpoints:75cd2b2 container-render lifecycle;0697fa8 parsed
source ancestry cache;d9e390e source chat/action-bar delivery;b2e6c78 source unload
before final persistence.5a6fde5 search context was pushed previously. Wrapper
compatibilityTest/nativeCompatibilitySmoke/compileLaunchJava passed batch35.
Only the original seven tracked dirty files and original untracked
Forge1192ContainerScreenBridge remain outside these commits. No test saves, logs,
configs, tokens, traces or temporary dependency installation staged.

First remaining causal blocker: source world-overlay primitives currently enter
the GUI-only adapter without an extraction scope when a bound EU reader is held.
Do not suppress it or describe the reader as fully functional. Next work requires
target world render extraction/submission evidence and the full source primitive
family. Additional open verification: active fuel, ordinary item-tooltip visual
acceptance, remaining startup costs (tag resource enumeration/source simulations/
target models). Current compatibility level remains6 plus observed level7 paths.
