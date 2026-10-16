# API-family coverage and verification queue

Evidence review: 2026-10-16. Source: Forge 1.19.2. Target: Minecraft26.3 /
NeoForge26.3.0.58-beta / Java25. This is an implementation/testing inventory,
not a new compatibility contract or a promise to implement every source API.

Historical accepted baseline: runtime82, world-load level6 plus individually
observed functional level7 paths. No level8 or complete-mod claim. The bridge
was unreachable during this audit and no dev client was running; historical
gameplay results below are not fresh runtime acceptance of an audited build.

## Interpretation and evidence rules

Track three separate columns: linkage, lifecycle and functional behavior.
PASS applies only to a named scenario. PARTIAL means a bounded subset passes;
UNVERIFIED means no adequate observation; BLOCKED identifies a missing prerequisite.

Classify causes separately:

- LMS_DEFECT: a proven source contract is lost or changed by the adapter.
- LMS_GAP: a required source operation has no executable adapter yet.
- SOURCE_BEHAVIOR: original behavior established by source evidence/control.
- EXCLUDED: explicitly retired by the user or outside the selected integration set.
- UNKNOWN: insufficient evidence to attribute the symptom.

Every open item needs exact API identities, source/target evidence, affected
consumer examples, an independent test and one bounded gameplay assertion.
An unchecked historical checkbox, an absent optional dependency, a deliberate
fail-fast or a static callsite count alone is not a new confirmed runtime bug.
Use a separate immutable source-runtime control when code inspection cannot
resolve an attribution. Do not launch it through the user's ordinary launcher.

## Current family matrix

| ID / family | Linkage and lifecycle evidence | Functional evidence | Remaining boundary / next assertion |
| --- | --- | --- | --- |
| F01 Discovery, registration, events | Three original entrypoints initialized in runtime82; staging/subscriber tests | Actual source content used; separate dedicated run6 | PARTIAL: late/optional API paths and independent event variants; do not infer whole-JAR linkage |
| F02 Recipes, tags, initial data | Common-tag, loot, recipe and initial-data tests | runtime49 initial tags/tree persistence; runtime59 gold and runtime60/73 copper processing | PARTIAL: recipe variants need consumers; no manual reload as a substitute for cold entry |
| F03 Block state, callbacks, persistence | Source load/save/unload, neighbor-origin and analog callback tests | runtime73 active fuel cold persistence; fresh cables; runtime78 output0/7/15 | PARTIAL: additional direct native neighbor producers need their own source-origin proof |
| F04 Menus, input, configuration | Pointer/list/edit-box/layer/container tests | CarbonConfig boolean/enum/numeric saves accepted runtime62/63; hover correction runtime65; machine menus/Shift and comparator source persistence | PARTIAL: arbitrary widgets/custom text/render paths; use a representative input case, not every IC2 parameter |
| F05 Source item handlers and vanilla automation | Actual mixins, simulation, counts, callback timing, helper and NBT tests | runtime82 hopper import/export/rejection, source access rules, dropper one-item/rejection | PARTIAL: player-give helper, source entity providers, native transactional export; live minecart path UNVERIFIED |
| F06 Source/native energy | Actual native handler transaction/rollback/invalidation tests | Source EU network and chainsaw independent-copy charge consumption/persistence accepted | PARTIAL: native direction flags and nested transactions explicitly unsupported; native-provider gameplay BLOCKED by absent independent provider/consumer fixture. EU is not Forge Energy |
| F07 Source fluids and containers | Identity/early-fluid tests; handler/tank facades are structural placeholders | Early bucket/registration queries do not prove tank transfer | LMS_GAP: FluidAction, handler/tank methods, item-container helpers; selected first implementation batch |
| F08 Item use, damage, equipment | Item/food/repair/armor/combat tests | Consumption/container return, textures, attributes, repair/damage and default armor dispenser accepted | PARTIAL: custom dispenser/dye/armor-model/enchantment callbacks need actual consumers; do not reopen accepted texture/repair paths as unknown |
| F09 Rendering/resources | Item/GUI/world extraction and resource tests | Iron Chests GUI/hand/frame/shelf/dropped views accepted; source overlay accepted runtime72 | PARTIAL: textured/lit/sky/OIT/custom-font variants and unused renderer/pack branches; static failures are not fresh visual regressions |
| F10 Networking and logical side | Plain channel/network-thread/enqueue and search-worker tests | Source menu settings, fresh model updates and search paths accepted | PARTIAL: login/builder variants, independent remote-client session and arbitrary worker safety |
| F11 World generation | Source feature/placement/tag tests | Natural ores and rubber trees in previously uncreated regions; first-load tree retention/re-entry runtime49 | PARTIAL: current fixtures under the approved native-generation policy, not every biome/dimension/population |
| F12 Startup and physical server | Phase measurements, source ancestry cache, distribution tests | Dedicated run6 loads three entrypoints, content use, save/restart | PARTIAL: no quantified general speedup; measure critical-path wall time before further optimization |

Evidence anchors:

- F01/F04/F09: [container families](../openspec/changes/source-container-and-native-container-families/design.md), [configuration acceptance](../openspec/changes/source-config-ui-controls/design.md), [deferred item views](../openspec/changes/deferred-custom-item-contexts/tasks.md).
- F02/F11: [initial data acceptance](../openspec/changes/initial-data-load-transaction/tasks.md), [worldgen observations](../openspec/changes/worldgen-feature-family/tasks.md), [common tags](../openspec/changes/common-tag-identity-projection/design.md).
- F03: [runtime73](../openspec/changes/source-world-render-extraction/runtime73.md), [analog output](../openspec/changes/source-analog-output-callback/tasks.md), [neighbor origins](../openspec/changes/native-directed-neighbor-origin/tasks.md).
- F05: [runtime82 acceptance and limits](../openspec/changes/source-vanilla-item-transfer/tasks.md).
- F06: [native energy contract and limits](../openspec/changes/native-energy-operations/design.md).
- F07: [empty handler interface](../src/main/java/com/fuckingdeveloper/lms/runtime/fluid/LegacyIFluidHandler.java), [tank facade](../src/main/java/com/fuckingdeveloper/lms/runtime/fluid/LegacyFluidTank.java), [native fluid-operation boundary](../src/main/java/com/fuckingdeveloper/lms/runtime/Forge1192BlockCapabilityBridge.java).
- F08: [equipment acceptance](../openspec/changes/source-armor-equipment/tasks.md), [gameplay batch](../openspec/changes/gameplay-batch-acceptance/tasks.md).
- F09/F10: [world overlay](../openspec/changes/source-world-render-extraction/tasks.md), [source consumer thread](../openspec/changes/source-network-consumer-thread/design.md), [search context](../openspec/changes/client-search-tooltip-context/design.md).

## Symptoms not requiring another mechanics-specific fix

| Report | Current disposition |
| --- | --- |
| Historical saved cables | EXCLUDED: explicitly retired. Fresh-cable acceptance remains a different scenario |
| Transparent chest cold preview | EXCLUDED by user from fixture-specific repair. Source load/display-dirty code supports the source-behavior hypothesis; no paired original-runtime control or missing general callback has been established. Do not upgrade this to a proven universal source bug |
| Generator smoke/flame | SOURCE_BEHAVIOR: this source generator declares no particle callback. The original iron furnace is the positive accepted particle/audio control |
| Comparator panel selected direction resets | SOURCE_BEHAVIOR: ephemeral source GUI selection. Persistent per-side output rules were separately verified |
| Fractional trackpad scroll does not move source slider | SOURCE_BEHAVIOR: actual original screen narrows delta to int; native discrete scrolling accepted runtime75. Preserve fractional API consumers |
| Creative-search null-player crash | Historical attribution corrected: LMS logical-side gap, fixed by source CLIENT scope. Do not retain it as an open source-mod defect |
| Enum/numeric editors and hover stripe | Concrete failures fixed and accepted; other widget variants remain UNVERIFIED, not automatically broken |

## Structural audit: what the numbers mean

Executed `./gradlew compatibilityAudit` using the existing diagnostic tool.
The first top-level graph contained2886 classes and reported1176 grouped
findings/8440 observations. It omitted the actual managed Primitive Collections
dependency. A second audit used symlinks in a temporary input directory for the
three unchanged source JARs plus that dependency:3412 classes,1086 grouped
findings/7253 observations. All90 embedded-library groups/1187 observations
disappeared. No dependency JAR or legacy input was modified.

The dependency-complete inventory partitions by exact symbol-owner prefix as:

| Diagnostic bucket | Grouped findings | Observations | Interpretation |
| --- | ---: | ---: | --- |
| Absent optional-integration owners | 354 | 4147 | JEI/The One Probe/Curios/etc.; outside installed integration set, not354 confirmed LMS bugs |
| Source fluid API owners | 39 | 611 | Concrete broad missing-contract candidate, corroborated by empty LMS interfaces/tank facade |
| Other symbols needing triage | 691 | 2493 | Requires exact target ABI, source reachability and runtime relevance review |
| Transformation failures | 2 | 2 | Source custom-renderer base and resource-pack constructor branches; relevance UNVERIFIED |

Observations count repeated type/member references, not unique executed calls,
independent defects or a compatibility percentage. This tool does not initialize
legacy classes, run callbacks, apply every FML runtime injection, establish
reachability or validate behavior. It also cannot detect every linked method
whose body deliberately throws UnsupportedOperationException. Code boundaries
and runtime evidence must therefore accompany its inventory.

Raw generated evidence remains outside Git:
`/tmp/lms-api-family-audit-1.log`, `/tmp/lms-compatibility-audit.tsv`,
`/tmp/lms-api-family-audit-2.log`, `/tmp/lms-api-family-audit-with-deps.tsv`.
Rerun the second form using an input directory containing the actual managed
dependency closure, not just the three top-level fixtures:

```bash
./gradlew compatibilityAudit --args='<artifact-directory> /tmp/lms-api-family-audit.tsv' > /tmp/lms-api-family-audit.log 2>&1
```

## Execution queue and bounded acceptance

1. **F07 fluid contracts.** Read pinned source/target fluid APIs and all affected
   consumer shapes. Specify source-owned mutable storage separately from native
   transactional views. Cover action flags, both drain forms, fill/limits/filters,
   subclass callbacks and save/read without implementing particular machines.
   Check FluidStack metadata/component projection before declaring equivalence.
   Select one unchanged consumer for real container-to-tank fill/drain,
   simulation/no-loss/rejection and save/restart. Item wrappers/FluidUtil are
   separate subfamilies when their contracts require further evidence. A batch
   is not done merely because FluidAction now links.
2. **F05/F08 residual item contracts.** Determine actual consumers of player-give,
   custom dispenser and item/entity capabilities; run independent callbacks first.
   Keep arbitrary native transactional exports a separate design boundary.
3. **F04/F09 and F10 targeted expansion.** Use evidence of executed missing paths
   to prioritize GUI/render/network variants. Do not enumerate every setting or
   comparator circuit. Cross-check a shared API in another existing fixture where
   it is actually used; do not manufacture coverage through mod identity.
4. **F06 live interoperability.** Select a compatible independent native energy
   fixture before declaring gameplay coverage; existing transaction tests remain
   useful without substituting for that interaction.
5. **F12 startup.** Profile one fresh startup critical path, distinguish cumulative
   transformation CPU from wall time, preserve setup/data semantics and compare
   only equivalent launches.

For each batch, run appropriate focused checks, then a fresh Gradle client under
[the MCP workflow](runtime-debugging-mcp.md). Preserve diagnostics and stop any
live client before compiling or running native smoke. Current standard checks:

```bash
./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava > /tmp/lms-preflight.log 2>&1
./gradlew runClient -PlmsTestWorld='Новый мир' > /tmp/lms-run.log 2>&1
```

`Новый мир` is the verified existing directory under run/saves for this checkout;
revalidate it when the workspace changes. Require actual server/player/world
readiness and use normal interaction after fixture preparation. A command that
sets a block/energy value is not proof of placement or transfer callbacks.

The short regression pack selects only touched families: IC2 mechanism/item
state, Iron Chests inventory/persistence/render context, CarbonConfig one boolean/
enum/numeric workflow. Source-generation expansion and exhaustive mod mechanics
are not part of this queue. Human checks are reserved for unsupported GUI/visual
assertions, bundled into a short request with an exact log filter:

```bash
rg -n -A30 -B5 'ERROR|FATAL|Exception|Caused by:|NoSuchMethodError|NoSuchFieldError|NoClassDefFoundError|VerifyError|AbstractMethodError' /tmp/lms-run.log
```

A scenario closes only after its observable assertion passes and the previous
causal failure is absent. Record the next failure separately; stop dependent
checks at world loss. Lack of an MCP operation is BLOCKED/UNVERIFIED, not a reason
to create another automation framework or to infer a successful result.
