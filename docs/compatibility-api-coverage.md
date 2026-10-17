# API-family coverage and verification queue

Evidence review: 2026-10-17 (bounded runtime104 addition; earlier inventory retained). Source: Forge 1.19.2. Target: Minecraft26.3 /
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
- F07: [empty handler interface](../src/main/java/com/fuckingdeveloper/lms/profile/forge1192/runtime/fluid/LegacyIFluidHandler.java), [tank facade](../src/main/java/com/fuckingdeveloper/lms/profile/forge1192/runtime/fluid/LegacyFluidTank.java), [native fluid-operation boundary](../src/main/java/com/fuckingdeveloper/lms/profile/forge1192/runtime/capability/Forge1192BlockCapabilityBridge.java).
- F08: [equipment acceptance](../openspec/changes/source-armor-equipment/tasks.md), [gameplay batch](../openspec/changes/gameplay-batch-acceptance/tasks.md).
- F09/F10: [world overlay](../openspec/changes/source-world-render-extraction/tasks.md), [source consumer thread](../openspec/changes/source-network-consumer-thread/design.md), [search context](../openspec/changes/client-search-tooltip-context/design.md).

## Runtime104 bounded terrain and preview acceptance (2026-10-17)

This fresh test does not revalidate every historical matrix row. Source/target
versions remain pinned above. `compatibilityTest nativeCompatibilitySmoke
compileLaunchJava` and the selected 4323-class audit passed. The audit now reports
1146 grouped findings / 5748 sites; these remain an inventory, not bug counts.

- Height queries: eleven native-backed source query overloads and factory preserve
  source exclusive maxima over native inclusive bounds. Negative/unaligned ranges,
  endpoints, section indices and integer overflow have native regression coverage.
  Custom source height implementations/super callbacks remain an explicit gap.
- Chunk/light snapshots: three constructors, thirteen getters and callable forms
  use actual target snapshots/codecs under user-approved native lighting. Legacy
  trustEdges reads, raw codecs and custom packet subclasses remain unsupported.
- Gameplay: MCP used the original TNT x5 Dynamite on one stone block at
  (-157,245,28), high above terrain. Original collision/terrain processing changed
  it to air; native sky light returned 15 and no height/packet exception followed.
  This does not establish arbitrary explosive variants or full chunk-edit APIs.
- Preview: the original TNT workbench opened/closed/reopened with 13 slots. User
  accepted block icons, large preview and selection. No missing-item-model warning.
- Cleanup: tagged test item removed; inventory/equipment summaries matched baseline;
  vitals, selected slot, position and camera restored. Prior owned workbench at
  (-197,79,12) removed and air confirmed. No legacy JAR was modified.

Next causal GUI blocker: runtime104 human clicks logged removed
SoundManager.play(SoundInstance):void (source m_120367_) twice without terminating
the client. Preview acceptance is not a claim of an error-free complete GUI.
Evidence: [height checks](../openspec/changes/source-height-query-family/tasks.md),
[packet checks](../openspec/changes/source-chunk-light-packets/tasks.md), local logs
`/tmp/lms-chunk-light-check.log`, `/tmp/lms-height-light-audit.log` and
`/tmp/lms-container-families-runtime-104.log`. Overall level6 plus observed level7
subpaths; no complete-mod/remote-client/persistence claim.

## Runtime106–108: entity allocation and native explosions (2026-10-17)

- Runtime106 reported no click sound; native master volume was 0.0. The removed
  SoundManager member exception did not recur. Audible output remains UNVERIFIED.
- Source entity data definition hooks now use the real native construction
  builder. Independent named/SRG parent/child fixtures verify local indices,
  inherited fuse, dirty get/set and duplicate/unsupported read boundaries.
- Native explosion execution preserves source NONE/BREAK/DESTROY identities and
  explicit no-break/no-decay/decay intent. The actual ServerLevel path supplies
  its real result, NeoForge events and packets; no second/dummy explosion.
- Runtime107's saved projectile reached another missing raw EntityType allocation
  call. Raw allocation and all four source position-snap overloads are now covered
  together. Native checks preserve factory operands/results/null/failure and
  source override/super dispatch, old position/rotation and unchanged motion.
- Runtime108 loaded the same world and completed the saved projectile. MCP then
  threw tagged original Weak and Mini TNT Dynamite from an isolated sky platform.
  Mini TNT changed glass at (-157,245,28) to air and left no projectile; the log
  remained free of errors through those throws. Weak TNT did not break stone:
  its source dynamite scale is 0.25, so this is not a failed blast assertion.
- Block ignition created/tracked the original primed entity, then exposed two
  remaining causal blockers: raw GameEvent fields/delivery now require holders;
  the original immediate EntityRenderer lacks native createRenderState/extraction.
  Runtime108 crashed on those boundaries. Block-TNT gameplay is not accepted.

Checks: `compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed in
`/tmp/lms-entity-allocation-position-check.log`; selected 4323-class audit reports
1134 grouped findings / 5709 sites (`/tmp/lms-entity-allocation-position-audit.tsv`).
This inventory is not a compatibility percentage. Explosion subclasses/manual
lifecycle, custom Level/EntityType overrides and entity persistence remain gaps.
Game-event follow-up: source registered constants/field handles and four level /
two entity delivery overloads pass independent native checks. Actual holder,
actor, Vec3/context identity, center defaults, native virtual dispatch and original
exceptions are preserved; custom raw callbacks fail explicitly. Checks passed in
`/tmp/lms-game-events-final-check.log` (selected audit: 1124 groups / 5693 sites).

Entity-render follow-up: the user selected corresponding native renderer contexts.
Source root EntityRenderer construction and erased/typed callbacks now extract
geometry once and submit native frame-owned model states in the same frame.
Proven native PrimedTnt and AbstractMinecart ancestry selects their real renderer
contexts. Managed parent calls, exact partial/yaw/light, source-super name intent,
white overlay, copied poses and actual model submission are covered independently.
Unknown context consumers and changed unsupported callbacks fail explicitly.
Checks `compatibilityTest nativeCompatibilitySmoke compileLaunchJava` passed in
`/tmp/lms-entity-render-check-final.log`; selected audit: 4323 classes, 1121 groups /
5685 sites (`/tmp/lms-entity-render-audit.tsv`). Inventory, not gameplay coverage.

Runtime109 loaded the explicit `Новый мир` save with MCP gates satisfied. Normal
flint-and-steel interaction produced the original `tntexpansion:primed_tnt` and
removed its block. Repeated Mini TNT ignition/explosion destroyed the prepared
glass support, removed the primed entity and produced no ERROR/FATAL/causal
exception. Optional MCP framebuffer captures at native frozen/stepped ticks
confirmed both textured TNT and its white-flash phase; normal ticks were restored.
This accepts that bounded block-TNT scenario, not every variant or renderer family.
The log remains `/tmp/lms-container-families-runtime-109.log`.

Owned runtime108/109 sky fixtures and the earlier TNT workbench were removed.
Only the tagged runtime108 test dynamite was cleared; original slot21 content
returned to hotbar4. The full exposed inventory exactly matched its baseline;
position, yaw/pitch, selected slot, health, food, saturation and XP matched too.
Sound output remains UNVERIFIED: native master volume is still the user's 0.0.
The unrelated native empty-salmon-sound warning is retained in the log. Custom
entity persistence, manual explosion lifecycle and other renderer roots remain gaps.

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

## Source class domains and plugin discovery (runtime123–125)

One Forge1.19.2 domain is prepared before managed class definition: seven source
mods and nine canonical artifacts in this integration set. Independent fixtures
verify ordinary Class.forName and ServiceLoader, API/constructor identity,
concurrent resolution, library deduplication/conflicts, circularity, immutable
annotation values and owner disposal. Runtime125's original JEI PluginCaller
executes ConfigManager callbacks for actual CarbonConfig and IC2 plugins.

The source formatting query family and read-only loader launch queries also pass
the original JEI startup sites. Full compile/compatibility/native-smoke checks and
static audit passed; audit remains unresolved inventory, not gameplay coverage.
The first remaining blocker is StartEventObserver's RecipesUpdatedEvent class
literal. The older timing-only adapter covers unused handler parameters, not the
complete event-token/registration family or recipe-map synchronization. World,
JEI recipe UI, optional integration functionality and pending chair recovery are
BLOCKED by that startup failure. Pre-existing source Mixin/render limitations
remain separately unverified; class visibility does not establish them.

## Recipe/tag notifications and native display views (runtime126–128)

- Runtime127 loads the explicit existing world without the runtime126 native-custom-
  ingredient display failure. The declared native display contract now covers
  custom implementations through the proven context-free display records;
  component-aware and nested compound native fixtures pass.
- Runtime128's original JEI observer receives LoggingIn, the source tag view, and
  RecipesReceivedEvent, then begins JEI startup. Source tag identity and approved
  post-login readiness are accepted at that bounded original-consumer gate.
- Independent compile/compatibility/native checks and static audit pass. Source
  scope/priority/unregister, connection replacement/disconnect, once-only delivery,
  constructors and actual native target mixin installation are covered.
- Actual source-owned JEI atlas GPU upload completes. Complete recipe UI/render
  acceptance remains unverified.
- MCP confirms world/player readiness, persisted baseline inventory/position and
  removal of the old owned chair/support fixture. No recovery remains pending.

First remaining causal blocker: JEI ItemStackListFactory reads the removed source
CreativeModeTab.TABS (`f_40748_`) to enumerate tabs/items. The later missing vanilla
crafting category is fallout from the original plugin's caught LinkageError.
The next family is read-only tab enumeration, special-tab identities and tab item
population against native registry/population phases; source array mutation and
old numeric-index semantics must not be silently redefined. Source Mixin execution,
remote full-recipe access and source seating gameplay remain separate gaps.

Runtime128 subsequently crashes at 21:20:47 when opening a screen triggers the
original JEI observer's startup retry. The fatal exception is
`NullPointerException: vanilla crafting category`; the first causal failure
remains the 21:04:07 missing `CreativeModeTab.f_40748_` field. The original
plugin's failed initialization leaves its vanilla category unavailable on retry.
Evidence: `/tmp/lms-container-families-runtime-128.log` and
`run/crash-reports/crash-2026-10-17_21.20.48-client.txt`. The client process has
exited. This is not a new accepted milestone or a verified fix; the proposed
enumeration contract is still pending approval.

## Creative enumeration family (implementation checkpoint)

The approved adapter admits proven enhanced-for iteration of source TABS and
projects SEARCH/HOTBAR/INVENTORY identity. Native registry order, current client
features/permissions and genuine content building determine enumeration.
Source tab-level overrides now participate in native custom-tab population;
source super calls retain item membership and item overrides. External consumers
receive copied, validated native display contents. Unsupported mutation, escaping
arrays, numeric-index dependence and array handles fail before execution.

Compilation, compatibilityTest, nativeCompatibilitySmoke, compileLaunchJava and
static audit pass (/tmp/lms-creative-final-check.log). Independent fixtures cover
iteration admission, negative array shapes, callable/nested special identities,
source-super dispatch, source ownership restoration, duplicate stacks, invalid
counts, context absence and immutable input JARs. The audit still reports the two
previous unrelated transformation failures; its 1161 groups/6781 sites are gap
inventory. Full native population and JEI acceptance await runtime129.

## Runtime129 creative population acceptance and next blocker

All seven source entrypoints initialize and the explicit existing world/player
are ready through MCP (Dev, minecraft:overworld). Native content building produces
nine nonempty source tabs: IC2 874/649/141, furniture 1967, Iron Chests 21, TNT
147/106/110/40 stacks. The original JEI consumer passes TABS, special-tab identity
and fillItemList, then fails in its registry wrapper at removed
RegistryManager.ACTIVE. Its later missing vanilla crafting category is still
secondary. The original StackHelper bytecode also obtains item registry names
through that wrapper before subtype handling. No full JEI acceptance is claimed.

The screen-opening retry crashes at 21:34:00; the client exited before attempted
TERM. Evidence: /tmp/lms-container-families-runtime-129.log and
run/crash-reports/crash-2026-10-17_21.34.00-client.txt. No fixture/player/inventory
mutation was performed for this run; no recovery remains pending. Last observed
overall milestone is world load (level 6), with bounded creative population
functional acceptance. RegistryManager/ForgeRegistry read views are the next API
family; source Mixins, complete recipe UI and other previous gaps remain separate.

A subsequent review added verified source-parent super dispatch and explicit
rejection of unresolved inherited-super declarations. Native child/parent/base
fixtures pass, including owner restoration, duplicate output and invalid stack
counts. Commands actually executed:
`./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava compatibilityAudit --args='legacy-mods /tmp/lms-creative-super-audit.tsv'`
and `./gradlew nativeCompatibilitySmoke` (additional negative fixture).
Logs: /tmp/lms-creative-super-check.log and
/tmp/lms-creative-super-negative-check.log. This narrow refinement has not had
another live client run; the audit has the same two prior unrelated failures.

## Source active registry reads and callable keys (runtime130)

The general adapter preserves a standard source ACTIVE read view, concrete/interface
storage identity, source defaults and absence, name/ID/reverse lookups, immutable
live collections and source iterator exhaustion. Tags and raw numeric wire reads
accept the same view. Mutation and historical manager snapshots are rejected;
known moved data-driven/loader registries remain explicit boundaries.

Runtime130 loads the explicit world and Dev player, initializes all seven source
entrypoints and populates all nine source tabs. JEI passes RegistryManager.ACTIVE
and reaches reverse item-name lookup. The next first observable failure is the
ResourceKey.m_135782_ method reference in RegistryWrapper; the missing vanilla
crafting category remains secondary. Client was stopped with TERM before another
screen-opening retry. No player/inventory/block mutation was performed.

The linked ResourceKey family (seven pinned methods) now handles direct calls,
method handles, unbound lambdas and nested constants; native factories, codec and
registry/cast semantics are covered. Bound interface registry lambdas preserve
capture types through the established semantic traversal. Source concrete casts
of admitted static native registry tokens normalize to the same read-view identity.
The current CarbonConfig NamedRegistry data-driven path remains unverified;
full JEI startup is not established by these checks.

Commands: `./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava
compatibilityAudit --args='legacy-mods /tmp/lms-registry-callables-audit.tsv'`.
Checks pass (/tmp/lms-registry-callables-check.log); audit has the same two prior
unrelated transform failures and is not gameplay coverage. Live acceptance of
callable key migration awaits runtime131. Prior user changes remain untouched.

## Runtime131: registry/key boundary passed, subtype and fluid blockers exposed

MCP confirms the intended world/player and unchanged test inventory/position.
The original registry wrapper passes ACTIVE and callable ResourceKey lookup.
The first chronological error is now NoClassDefFoundError EnchantedBookItem in
VanillaPlugin's subtype callback. The original JEI code catches/logs these errors
and omits those ingredient entries; LMS does not claim the resulting list complete.
A later VerifyError in FluidIngredientHelper.getTagStream is a separate registry
read boundary: source DefaultedRegistry's inherited SRG call got a concrete native
MappedRegistry owner, incompatible with the new DefaultedRegistry interface.
The missing vanilla crafting category remains secondary. Runtime131 was stopped
with TERM after evidence collection; no gameplay state was modified.

Inherited source declaration lookup and three holder-read operations are now
covered by actual native registry fixtures, including the original bound-capture
shape, optional absence and explicit missing-holder failure. Checks/audit pass:
/tmp/lms-registry-holder-check.log, /tmp/lms-registry-holder-audit.tsv. The audit's
1141 groups/6742 sites are inventory, with the same two pre-existing unrelated
transformation failures. Live acceptance of the narrow holder correction remains
pending; enchanted-book subtype data is the next separate API family.

## Runtime132: read family accepted, subtype/rendering families next

Fresh source build on the pinned 26.3 target loads the explicit existing world;
MCP status confirms client+integrated server, Dev in minecraft:overworld, unchanged
position/vitals/selected slot and baseline inventory summary. All seven source
entrypoints initialize. Original ACTIVE and callable key operations pass.
FluidIngredientHelper now passes verification/construction (VanillaPlugin:187),
then setup reaches fluid renderer construction (:188). Native fixtures prove
actual optional/throwing holder reads; live fluid-tag GUI execution is unverified.

The first chronological causal blocker remains removed EnchantedBookItem in the
original book subtype callback. The original factory catches it and skips those
entries; this is not complete ingredient functionality. A separate first fatal
vanilla-plugin setup blocker is missing com.mojang.math.Matrix4f during fluid
renderer construction. The later vanilla crafting category error is secondary.
There is no old ACTIVE/key/registry VerifyError in this run. Full JEI startup and
rendering remain BLOCKED. Overall observed milestone: world loaded (level 6).

Evidence: /tmp/lms-container-families-runtime-132.log. Latest executable checks:
`./gradlew compatibilityTest nativeCompatibilitySmoke compileLaunchJava compatibilityAudit --args='legacy-mods /tmp/lms-registry-holder-audit.tsv'`
(/tmp/lms-registry-holder-check.log), all pass. Audit retains the same two prior
unrelated transform failures; totals are inventory, not functional coverage.
The client was stopped with TERM after evidence collection, before a screen
retry; no gameplay fixture was created and no recovery is pending.

Pushed implementation: 03b8e85, ef623e9, fc54a18. Original dirty user changes are
preserved. The next separate work is stored-enchantment/book subtype data with
authoritative dynamic registry reads, followed by the immediate matrix/vertex
family used by the fluid ingredient renderer. Existing mutable/snapshot/custom
registry gaps, unsupported concrete holder/delegate/codec members and the
CarbonConfig NamedRegistry data-driven initialization path remain explicit gaps.
